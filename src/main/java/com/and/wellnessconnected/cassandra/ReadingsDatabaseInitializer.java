package com.and.wellnessconnected.cassandra;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.exceptions.QueryExecutionException;
import com.datastax.driver.core.exceptions.QueryValidationException;

public class ReadingsDatabaseInitializer extends ReadingsManager implements AutoconstructBackend {
	
	public ReadingsDatabaseInitializer(String keySpaceName, String tableName) {
		super(keySpaceName, tableName);
	}
	public String getReadingTypeIndexName() {
		return getTableName() + "_TYPE_IDX";
	}
	public String getUserIdIndexName() {
		return getTableName() + "_USERID_IDX";
	}
	public String getEnabledIndexName() {
		return getTableName() + "_ENABLED_IDX";
	}
	
	public void createKeyspace() throws NoHostAvailableException, QueryExecutionException, QueryValidationException {
		String createKeyspaceCQL = "CREATE KEYSPACE " + getKeySpaceName() + " WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};";

        Session cassandraSession = client.getSession();

        cassandraSession.execute(createKeyspaceCQL);
	}

	public void constructColumnFamilies() throws NoHostAvailableException, QueryExecutionException, QueryValidationException {
        Session cassandraSession = client.getSession();
		try {
			System.out.println("dropping Table: " + getTableNameInContext());
            String dropReadingsTableCQL = "DROP TABLE " + getTableNameInContext();

            cassandraSession.execute(dropReadingsTableCQL);
            System.out.println("deleted " + getTableNameInContext() + " table");
		} catch (InvalidQueryException badEx) {
			// dont care. dont even print it
			System.out.println("error deleting " + getTableNameInContext() + " table: " + badEx.toString());
		}
		
		System.out.println("creating table: " + getTableNameInContext());
        String createReadingsTableCQL = "CREATE TABLE " + getTableNameInContext() + " (SOURCE_ID varchar, READING_TAKEN_TIME timestamp, READING_TYPE varchar, USER_ID varchar, ENABLED boolean, READING_DATA varchar, PRIMARY KEY(SOURCE_ID, READING_TAKEN_TIME));";
		String createReadingTypeIndexCQL = "CREATE INDEX " + getReadingTypeIndexName() + " ON " + getTableNameInContext() + "(READING_TYPE);";
		String createUserIdIndexCQL = "CREATE INDEX " + getUserIdIndexName() + " ON " + getTableNameInContext() + "(USER_ID);";
		String createEnabledIndexCQL = "CREATE INDEX " + getEnabledIndexName() + " ON " + getTableNameInContext() + "(ENABLED);";


        cassandraSession.execute(createReadingsTableCQL);
        System.out.println("creating index: " + getReadingTypeIndexName());
        cassandraSession.execute(createReadingTypeIndexCQL);
        System.out.println("creating index: " + getUserIdIndexName());
        cassandraSession.execute(createUserIdIndexCQL);
        System.out.println("creating index: " + getEnabledIndexName());
        cassandraSession.execute(createEnabledIndexCQL);

}

	

}
