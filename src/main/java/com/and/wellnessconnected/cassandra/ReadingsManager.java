package com.and.wellnessconnected.cassandra;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.exceptions.QueryExecutionException;
import com.datastax.driver.core.exceptions.QueryValidationException;

import com.datastax.driver.core.Row;

import com.and.wellnessconnected.models.DeviceReadingReadingSourceIdGenerator;
import com.and.wellnessconnected.models.ReadingAddress;
import com.and.wellnessconnected.models.StoredReading;
import com.datastax.driver.core.Session;


/**
 * This class implements a cassandra CQL3 interface to a readings table with a compound primary key of sourceId.readingTakenTime
 * @author charliefederspiel
 *
 */
public class ReadingsManager implements NoSQLBackend {

	
	protected DeviceReadingReadingSourceIdGenerator sourceIdGenerator = new DeviceReadingReadingSourceIdGenerator();

	protected SimpleClient client = null;

    protected String keySpaceName = null;
    protected String tableName = null;

	
	public DeviceReadingReadingSourceIdGenerator getDeviceReadingSourceIdGenerator() {
		return sourceIdGenerator;
	}

	
	public ReadingsManager(String keySpaceName, String readingsTableName) {
        this.keySpaceName = keySpaceName;
        this.tableName = readingsTableName;
	}

	public void init(SimpleClient client) {
		this.client = client;

	}
    public String getKeySpaceName() {
        return keySpaceName;
    }
    public String getTableName() {
        return tableName;
    }
    public String getTableNameInContext() {
        return keySpaceName + "." + tableName;
    }

	public void deleteReading(ReadingAddress k) throws NoHostAvailableException, QueryExecutionException, QueryValidationException, IllegalStateException, ColumnNotFoundException {
		StoredReading r = findStorageReadingByKey(k);
		//mark the reading such that the finder and search methods wont return it and the compaction method will find it later.
		r.setUsable(false);
        String deleteAndFlagReadingCQL = "\"BEGIN BATCH UPDATE READING r SET r.ENABLED = false WHERE r.SOURCE_ID = ? and r.READING_TAKEN_TIME = ?; DELETE FROM READING r WHERE r.sourceId = ? and r.readingTakenTime = ?; END BATCH;";

        Session cassandraSession = client.getSession();
        BoundStatement preparedStatement = new BoundStatement(cassandraSession.prepare(deleteAndFlagReadingCQL.toString()));
        int i =0;
        preparedStatement.setString(++i, k.getSourceId()).setDate(++i, k.getReadingTakenTime()).setString(++i, k.getSourceId()).setDate(++i, k.getReadingTakenTime());
        cassandraSession.execute(preparedStatement);
		
	}

	protected ArrayList<StoredReading> mapToList(Map<String, ArrayList<StoredReading>> inputSources) {
		
		ArrayList<StoredReading> readingsForSourceId = new ArrayList<StoredReading>();
		
		Iterator<String> keyIterator = inputSources.keySet().iterator();
		
		while(keyIterator.hasNext()) {
			String currentSourceId = keyIterator.next();
			List<StoredReading> readingsToStore = inputSources.get(currentSourceId);
			for(StoredReading r : readingsToStore) {
			
				readingsForSourceId.add(r);
			}
		}
		return readingsForSourceId;
	}
	protected void saveReadings(Map<String, ArrayList<StoredReading>> inputSources) throws NoHostAvailableException, QueryExecutionException, QueryValidationException, IllegalStateException {
		ArrayList<StoredReading> deviceReadings = mapToList(inputSources);
		
		StringBuilder saveReadingsBatchStatement = new StringBuilder();
		if(inputSources.size() > 1) {
			saveReadingsBatchStatement.append("BEGIN BATCH ");
		}
		for(StoredReading r : deviceReadings) {
			if(r.getUserId() == null) {
				saveReadingsBatchStatement.append("INSERT INTO " + getTableNameInContext() + " (SOURCE_ID, READING_TAKEN_TIME, READING_TYPE, READING_DATA, ENABLED) values (?, ?, ?, ?, true);");
			} else {
				saveReadingsBatchStatement.append("INSERT INTO " + getTableNameInContext() + " (SOURCE_ID, READING_TAKEN_TIME, READING_TYPE, USER_ID, READING_DATA, ENABLED) values (?, ?, ?, ?, ?, true);");
			}
		}
		if(inputSources.size() > 1) {
			saveReadingsBatchStatement.append(" APPLY BATCH; ");
		}
        Session cassandraSession = client.getSession();
		System.out.println("=======" + saveReadingsBatchStatement.toString() + "=======");
        BoundStatement preparedStatement = new BoundStatement(cassandraSession.prepare(saveReadingsBatchStatement.toString()));
        System.out.println("prepared insert statement");

        int i = 0;
		for(StoredReading r : deviceReadings) {
            ++i;

			preparedStatement.setString(i, r.getAddress().getSourceId());
            System.out.println("bound source id string: " + r.getAddress().getSourceId());
            preparedStatement.setDate(i, r.getAddress().getReadingTakenTime());
            System.out.println("bound readingTakenTime date: " + r.getAddress().getReadingTakenTime());

            preparedStatement.setString(i, r.getReadingType());
            System.out.println("bound string: " + r.getReadingType());
			if(r.getUserId() != null) {
				preparedStatement.setString(i, r.getUserId());
                System.out.println("bound string: " + r.getUserId());
			}
			preparedStatement.setString(i, r.getReadingData());
		}
        cassandraSession.execute(preparedStatement);

	}
	
	
	public StoredReading findStorageReadingByKey(ReadingAddress k) throws NoHostAvailableException, QueryExecutionException, QueryValidationException, IllegalStateException, ColumnNotFoundException {
		return findStorageReadingByKey(k.getSourceId(), k.getReadingTakenTime());
	}
	public StoredReading findStorageReadingByKey(String sourceId, Date readingTakenTime ) throws NoHostAvailableException, QueryExecutionException, QueryValidationException, IllegalStateException, ColumnNotFoundException {

        String findStorageReadingByKeyCQL = "SELECT SOURCE_ID, READING_TAKEN_TIME, READING_TYPE, USER_ID, READING_DATA from " + getTableNameInContext() + " where SOURCE_ID = ? and READING_TAKEN_TIME = ?";
        Session cassandraSession = client.getSession();
        BoundStatement preparedStatement = new BoundStatement(cassandraSession.prepare(findStorageReadingByKeyCQL));

        int i = 0;
        preparedStatement.setString(++i, sourceId);
        preparedStatement.setDate(++i, readingTakenTime);
		
		ResultSet foundReadingResult = cassandraSession.execute(preparedStatement);
		
		return loadStoredReadingRowResult(foundReadingResult.one(), sourceId);
		
	}

	protected BoundStatement prepareStorageReadingRangeQuery(String sourceId, String userId, String readingType, final Date startTime, final Date endTime) {
		StringBuilder queryBuffer = new StringBuilder("SELECT SOURCE_ID, READING_TAKEN_TIME, READING_TYPE, USER_ID, READING_DATA from " + getTableNameInContext() + " where ENABLED = ? ");
		
		if(sourceId != null) {
			queryBuffer.append(" and SOURCE_ID = ? ");
		}
		if(readingType != null) {
			queryBuffer.append(" and READING_TYPE = ? ");
		}
		if(userId != null) {
			queryBuffer.append(" and USER_ID = ? ");
		}
		
		if(startTime != null) {
			queryBuffer.append(" and READING_TAKEN_TIME >= ? ");
		}
		if(endTime == null) {
			queryBuffer.append(" and READING_TAKEN_TIME < ? ");
		}
		if(sourceId == null) {
			queryBuffer.append(" allow filtering ");
		}

        Session cassandraSession = client.getSession();
        BoundStatement preparedStatement = new BoundStatement(cassandraSession.prepare(queryBuffer.toString()));
        int i = 0;
        preparedStatement.setBool(++i, true);
		
		if(sourceId != null) {
            preparedStatement.setString(++i, sourceId);
		}
		if(readingType != null) {
            preparedStatement.setString(++i, readingType);
		}
		if(userId != null) {
            preparedStatement.setString(++i, userId);
		}
		if(startTime != null) {
            preparedStatement.setDate(++i, startTime);
		}
		Date bindEndTime = endTime;
		if(bindEndTime == null) {
			bindEndTime = new Date();
		}
        preparedStatement.setDate(++i, bindEndTime);
		
		return preparedStatement;
	}

	
	/**
	 * This method decrypts the measurements document for each reading. TODO: CONVERT TO USE NEW LOAD METHOD!!
	 * @param sourceId
	 * @param readingType
	 * @param startTime
	 * @param endTime
	 * @return
	 * @throws NoHostAvailableException, QueryExecutionException, QueryValidationException, IllegalStateException
	 */
	public List<StoredReading> searchStorageReadingRange(String sourceId, String userId, String readingType, Date startTime, Date endTime) throws NoHostAvailableException, QueryExecutionException, QueryValidationException, IllegalStateException, ColumnNotFoundException {
		List<StoredReading> foundReadings = new ArrayList<StoredReading>();
        Session cassandraSession = client.getSession();
        BoundStatement preparedStatement = prepareStorageReadingRangeQuery(sourceId, userId, readingType, startTime, endTime);
		
		ResultSet readingOperationResult = cassandraSession.execute(preparedStatement);
		
		loadStoredReadingQueryResults(readingOperationResult, foundReadings, sourceId);
		
		//if currentStoredReading has encryptedMeasurements, get them from the function-scope Map or query them and decrypt, and add the specifed measurements to the reading.
		return foundReadings;
		
	}
	public List<StoredReading> findAllStorageReadings(String sourceId) throws NoHostAvailableException, QueryExecutionException, QueryValidationException, IllegalStateException, ColumnNotFoundException {
		List<StoredReading> foundReadings = new ArrayList<StoredReading>();
		String allStorageReadingsQuery = "SELECT SOURCE_ID, READING_TAKEN_TIME, READING_TYPE, USER_ID, UNATTRIBUTED_READING from " + getTableNameInContext() + " where SOURCE_ID = ?";
		
		Session cassandraSession = client.getSession();
        BoundStatement preparedStatement = new BoundStatement(cassandraSession.prepare(allStorageReadingsQuery));
        preparedStatement.setString(1, sourceId);
        ResultSet readingOperationResult = cassandraSession.execute(preparedStatement);
		
		
		loadStoredReadingQueryResults(readingOperationResult, foundReadings, sourceId);
		
		//if currentStoredReading has encryptedMeasurements, get them from the function-scope Map or query them and decrypt, and add the specifed measurements to the reading.
		return foundReadings;
		
	}
	/**
	 * Counts will include deleted readings until items marked for deletion are deleted and compacted!!
	 * We could get around this by using a counter column
	 * @param deviceId
	 * @return
	 * @throws NoHostAvailableException, QueryExecutionException, QueryValidationException, IllegalStateException
	 */
	public int countAllStorageReadingsByDeviceId(String deviceId) throws NoHostAvailableException, QueryExecutionException, QueryValidationException, IllegalStateException, ColumnNotFoundException {
		StringBuilder queryBuffer = new StringBuilder("SELECT count(*) readingsCount from " + getTableNameInContext() + " where SOURCE_ID = ? and ENABLED = true");

        Session cassandraSession = client.getSession();
        BoundStatement preparedStatement = new BoundStatement(cassandraSession.prepare(queryBuffer.toString()));

        preparedStatement.setString(1, deviceId);
		ResultSet readingOperationResult = cassandraSession.execute(preparedStatement);
		Row foundRow = readingOperationResult.one();
        if(foundRow == null) {
            throw new ColumnNotFoundException("readingCountNotFound");
        }
        return foundRow.getInt("readingsCount");

	}

	public int countAllStorageReadingsByDeviceId(String deviceId, String readingType) throws NoHostAvailableException, QueryExecutionException, QueryValidationException, IllegalStateException, ColumnNotFoundException {
		StringBuilder queryBuffer = new StringBuilder("SELECT count(*) readingsCount from " + getTableNameInContext() + " where SOURCE_ID = ? and READING_TYPE = ? and ENABLED = true");

        Session cassandraSession = client.getSession();
        BoundStatement preparedStatement = new BoundStatement(cassandraSession.prepare(queryBuffer.toString()));


        preparedStatement.setString(1, deviceId);
        preparedStatement.setString(2, readingType);
        ResultSet readingOperationResult = cassandraSession.execute(preparedStatement);
        Row foundRow = readingOperationResult.one();
        if(foundRow == null) {
            throw new ColumnNotFoundException("readingCountNotFound");
        }
        return foundRow.getInt("readingsCount");
	}

	public int countStorageReadingsInRange(String deviceId, String readingType, Date startTime, Date endTime) throws NoHostAvailableException, QueryExecutionException, QueryValidationException, IllegalStateException, ColumnNotFoundException {
		StringBuilder queryBuffer = new StringBuilder("SELECT count(*) readingsCount from " + getTableNameInContext() + " where SOURCE_ID = ? and READING_TYPE = ? and READING_TAKEN_TIME >= ? and READING_TAKEN_TIME < ? and ENABLED = true");

        Session cassandraSession = client.getSession();
        BoundStatement preparedStatement = new BoundStatement(cassandraSession.prepare(queryBuffer.toString()));
        int i = 0;
        preparedStatement.setString(++i, deviceId);
        preparedStatement.setString(++i, readingType);
        preparedStatement.setDate(++i, startTime);
        preparedStatement.setDate(++i, endTime);
        ResultSet readingOperationResult = cassandraSession.execute(preparedStatement);
        Row foundRow = readingOperationResult.one();
        if(foundRow == null) {
            throw new ColumnNotFoundException("readingCountNotFound");
        }
        return foundRow.getInt("readingsCount");
	}
	
	protected void loadStoredReadingQueryResults(ResultSet queryResult, List<StoredReading> foundReadings, String currentSourceId) throws NoHostAvailableException, QueryExecutionException, QueryValidationException, IllegalStateException, ColumnNotFoundException {
		Iterator<Row> foundRowsIterator = queryResult.iterator();
		
		//System.out.println("found " + readingColumns.size() + " readings in range");
        while(foundRowsIterator.hasNext()) {
		    Row currentRow = foundRowsIterator.next();
			foundReadings.add(loadStoredReadingRowResult(currentRow, currentSourceId));
		}
	}

	protected StoredReading loadStoredReadingRowResult(Row currentRow, String currentSourceId) throws NoHostAvailableException, QueryExecutionException, QueryValidationException, IllegalStateException, ColumnNotFoundException {
		//Row currentRow can be null in case search returns no results..
        if(currentRow == null) {
            throw new ColumnNotFoundException("readingNotFound");
        }
		String currentRowSourceId = currentRow.getString("SOURCE_ID");
		
		ReadingAddress ra = new ReadingAddress(currentRowSourceId, currentRow.getDate("READING_TAKEN_TIME"));
        String serializedReadingData = currentRow.getString("UNATTRIBUTED_READING");

		
		StoredReading foundReading = new StoredReading(ra, currentRow.getString("READING_TYPE"), currentRow.getString("USER_ID"), currentRow.getBool("IS_USABLE"), serializedReadingData);
		
		return foundReading;
	}

}
