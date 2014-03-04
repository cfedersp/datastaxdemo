package com.and.wellnessconnected.cassandra;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.datastax.driver.core.exceptions.NoHostAvailableException;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.and.wellnessconnected.models.ReadingAddress;
import com.and.wellnessconnected.models.StoredReading;

import javax.naming.AuthenticationException;


/**
 * Unit test for simple App. Per junit.org: Do not extend TestCase. Do not use any classes in junit.framework or junit.extensions.
 * AndEmbeddedCassandra is the external resource and it must be initialized by the junit Rule framework and is configured by test/resources/cassandra.yaml
 * AstyanaxReadingsWithUserLinkManager is our client interface to cassandra. It is injected by Spring.
 * In production, the database and the client are loosely coupled through the IP, port and keyspace name and no database initialization is performed.
 * In testing, our embedded database initializes the client interface before initializing itself.
 */
@RunWith(JUnit4.class)
public class AppTest
{
	// com.and.wellnessconnected.cassandra.AppTest > classMethod FAILED
	// means one of these static initializers failed

	protected static ApplicationContext appCtx = new ClassPathXmlApplicationContext("/test-backend.xml");
	protected static ReadingsDatabaseInitializer intializableDbManager = (ReadingsDatabaseInitializer) appCtx.getBean("cassandraReadingsManager");
	protected static AndEmbeddedCassandra cassandra = new AndEmbeddedCassandra("/cassandra.yaml", intializableDbManager);
	
	@ClassRule //instantiate a RuleChain of TestRules or a single TestRule. must be static. This rule ensures there is a connection to cassandra open before any tests are run.
	public static TestRule dbAvailableRule = new EmbeddedCassandraAvailableTestRule(cassandra);
	
	List<UserReadingTestDataGenerator> typeGenerators = new ArrayList<UserReadingTestDataGenerator>();
	
    /**
     * Create the test case
     *
     */
    public AppTest() throws NoHostAvailableException, AuthenticationException
    {
        System.out.println("testing " + intializableDbManager.getClass().getName());
        //appCtx = new ClassPathXmlApplicationContext("/test-backend.xml");
        System.out.println("appCtx null?" + (appCtx == null));
        //intializableDbManager = (ReadingsDatabaseInitializer) appCtx.getBean("cassandraReadingsManager");
        System.out.println("readingMgr null?" + (intializableDbManager == null));
        //cassandra = new AndEmbeddedCassandra("/cassandra.yaml", intializableDbManager);
        System.out.println("cassandra null?" + (cassandra == null));

        typeGenerators.add(new BCAGenerator());
		typeGenerators.add(new BPGenerator());
    }





    /**
     * Rigourous Test :-)
     */
    @Test
    public void runDBTests()
    {
    	try {

            StoredReading r = saveSingleReadingTest();
			deleteTest(r.getAddress());
			
			//save multiple readings from multiple sources in a single call
			saveMultipleReadingTestWithFewUsersUsingManyDifferentDevices();


			//TODO: update some of them
			
			//TODO: find them and make sure they have been updated..

    	} catch (Exception e) {
    		System.out.println(e.toString());
    		e.printStackTrace();
    		assertTrue(false);
    	}
    }
    
    
    //TODO: test many users with few input devices
    //TODO: test a shorter range of dates
    //TODO: separate this out into two test tests - one for user search and one for device search
    protected void saveMultipleReadingTestWithFewUsersUsingManyDifferentDevices() {
    	
    	//System.out.println("now: " + new Date().getTime() + " random: " + randomTestDate.getTime() + " yesterday: " + yesterday.getTime());
    	Map<String, ArrayList<StoredReading>> userReadingMap = new HashMap<String, ArrayList<StoredReading>>(); // all readings by UserId, regardless of how they were sourced
    	Map<String, ArrayList<StoredReading>> deviceReadingMap = new HashMap<String, ArrayList<StoredReading>>(); // all readings by deviceId, regardless of how they were sourced
    	ArrayList<StoredReading> readings = new ArrayList<StoredReading>();
    	BCAGenerator bcaGen = new BCAGenerator();
    	BPGenerator bpGen = new BPGenerator();
    	UserReadingTestDataGenerator g = bpGen;
    	Date yesterday = g.createRecentDate(-1);
    	for(int i = 0; i < 200; i++) {
    		
    		/*
    		 * if(i%5 == 0) {
    		 
    			g = bcaGen;
    		} else {
    			g = bpGen;
    		}
    		*/
    		int userCounter = i%6;
    		String userId = i%4>0?null:"user" + userCounter;
    		String deviceId = i%8>0?"ble.xyz" + i: null;
    		System.out.println("u: " + userId + ",d: " + deviceId);
    		
    		StoredReading newReading = g.generateUserReading(userId, g.selectRandomDate(yesterday, new Date()), deviceId, i);
    		readings.add(newReading);
    		
    		// record the reading by userId so queries by userid can be verified
    		if(userId != null) {
    			//testUserIds.add(userId);
                ArrayList<StoredReading> existingUserReadings = userReadingMap.get(userId);
	    		if(existingUserReadings == null) {
	    			existingUserReadings = new ArrayList<StoredReading>();
	    			userReadingMap.put(userId, existingUserReadings);
	    		}
	    		
	    		existingUserReadings.add(newReading);
	    		//System.out.println(userId + ": " + newReading.getReadingTakenTime().getTime() + ", " + existingUserReadings.size());
    		}
    		if(deviceId != null) {
    			//testDeviceIds.add(userId);
                ArrayList<StoredReading> existingDeviceReadings = deviceReadingMap.get(deviceId);
	    		if(existingDeviceReadings == null) {
	    			existingDeviceReadings = new ArrayList<StoredReading>();
	    			deviceReadingMap.put(deviceId, existingDeviceReadings);
	    		}
	    		
	    		existingDeviceReadings.add(newReading);
    		}
    		//record the reading by deviceId so queries by device id can be verified
    		
    		
    		//0 : user0, null
    		//1 : null, ble.xyz1
    		//2 : user2, ble.xyz2
    		//3 : null, ble.xyz3
    		//4 : user4, null
    		//5:  null, ble.xyz5
    		//6 : user0, ble.xyz6
    	}
    	
    	try {
    		
	    	intializableDbManager.saveReadings(userReadingMap);
    		System.out.println("verifying device id query");
    		Iterator<String> deviceIdIterator = deviceReadingMap.keySet().iterator();
	    	while(deviceIdIterator.hasNext()) {
	    		String currentDeviceId = deviceIdIterator.next();
	    		List<StoredReading> existingReadingsForDevice = deviceReadingMap.get(currentDeviceId);
	    		//System.out.println("searching for source: " + currentDeviceId);
	    		List<StoredReading> foundReadings = intializableDbManager.searchStorageReadingRange(currentDeviceId, null, "bp", yesterday, null);
	    		//System.out.println("deviceid: " + currentDeviceId + " saved: " + existingReadingsForDevice.size() + "found: " + foundReadings.size());
	    		assertTrue(existingReadingsForDevice.size() == foundReadings.size());
	    		
	    		/*// not needed since each device in the dataset only produced 1 reading
    			System.out.println("verifying device query results ordering");
	    		Date lastReadingTakenTime = yesterday;
	    		for(StoredReading r : foundReadings) {
	    			Date currentReadingDate = r.getAddress().getReadingTakenTime();
	    			assertTrue(lastReadingTakenTime.before(currentReadingDate) || lastReadingTakenTime.equals(currentReadingDate));
	    			lastReadingTakenTime = currentReadingDate;
	    			System.out.print(".");
	    		}
	    		*/
	    	}
	    	System.out.println("verifying user id query");
	    	Iterator<String> userIdIterator = userReadingMap.keySet().iterator();
	    	while(userIdIterator.hasNext()) {
	    		String currentUserId = userIdIterator.next();
	    		List<StoredReading> existingReadingsForUser = userReadingMap.get(currentUserId);
	    		if(existingReadingsForUser == null || existingReadingsForUser.isEmpty()) {
	    			continue;
	    		}

	    		List<StoredReading> foundReadings = intializableDbManager.searchStorageReadingRange(null, currentUserId, "bp", yesterday, null);
	    		if(foundReadings == null) {
	    			System.out.println("userid: " + currentUserId + " saved: " + existingReadingsForUser.size() + "found: 0");
	    		} else {
	    			System.out.println("userid: " + currentUserId + " saved: " + existingReadingsForUser.size() + "found: " + foundReadings.size());
	    		}
	    		assertTrue(foundReadings != null);
	    		assertTrue(foundReadings.size() == existingReadingsForUser.size());
	    		// make sure all results have been returned exactly as they were input
	    		for(StoredReading existingReading : existingReadingsForUser) {
	    			boolean foundReading = false;
	    			boolean detailsMatch = false;
	    			for(StoredReading currentFoundReading : foundReadings) {
	    				if(currentFoundReading.getReadingType().equals(existingReading.getReadingType()) && currentFoundReading.getAddress().getReadingTakenTime().equals(existingReading.getAddress().getReadingTakenTime())) {
	    					foundReading = true;
                            assertTrue(currentFoundReading.getReadingData().equals(existingReading.getReadingData()));

	    				}
	    			}
	    			System.out.println("reading found: " + foundReading);
	    			assertTrue(foundReading);
	    			System.out.println("verifying user query results ordering");
	    			//make sure returned readings are sorted
	    			Date lastReadingTakenTime = yesterday;
	    			for(StoredReading currentUserReading : foundReadings) {
	    				Date currentReadingDate = currentUserReading.getAddress().getReadingTakenTime();
		    			if(!(lastReadingTakenTime.before(currentReadingDate) || lastReadingTakenTime.equals(currentReadingDate))) {
		    				System.out.println("wrong order: " + lastReadingTakenTime + ", " + currentReadingDate);
		    				assertTrue(false);
		    			}
		    			lastReadingTakenTime = currentReadingDate;
		    			System.out.print(".");
	    			}
	    		}
	    	}
    	
    	
    	} catch (Exception e) {
    		System.out.println("Ex while testing multiple save: " + e.toString());
			e.printStackTrace();
    		assertTrue(false);
    	}
    }
    
    /**
     * This test creates and saves a user-attributed BCA with a taken time sometime in the past day, then searches for bcas taken in the past day, confirming that the saved reading is among the found results.
     * @return
     */
    protected StoredReading saveSingleReadingTest() {
    	Calendar cal = new GregorianCalendar();
    	cal.add(Calendar.DAY_OF_MONTH,  -1);
    	Date yesterday = cal.getTime(); 
    	
		String deviceId = "ble.wefwefnwefxe32";
		String userId = "cjf";
		UserReadingTestDataGenerator g = new BCAGenerator();
		Date randomTestDate = g.selectRandomDate(yesterday, new Date());
    	System.out.println("now: " + new Date().getTime() + " random: " + randomTestDate.getTime() + " yesterday: " + yesterday.getTime());

        StoredReading r = g.generateUserReading(userId, randomTestDate, deviceId, 1);
		System.out.println("testing save of single " + r.getReadingType());
    	//save a contract
		
		
		//save reading(s)
		
		
		try {
            Map<String, ArrayList<StoredReading>> readingsMap = new HashMap<String, ArrayList<StoredReading>>();
            ArrayList<StoredReading> newReadingsForUser = new ArrayList<StoredReading>();
			newReadingsForUser.add(r);
            readingsMap.put(deviceId, newReadingsForUser);
			
			intializableDbManager.saveReadings(readingsMap);
			//Calendar gc = new GregorianCalendar();
			//gc.add(Calendar.HOUR, -1);

    		
			List<StoredReading> foundReadings = intializableDbManager.searchStorageReadingRange(null, userId, "bca", yesterday, null);
			if(foundReadings == null || foundReadings.size() == 0) {
				System.out.println("no results found for recent readings");
			}
			assertTrue(foundReadings != null && foundReadings.size() > 0);
			System.out.println("found " + foundReadings.size() + " recent readings.");
            StoredReading resultSameAsInput = null;
			for(StoredReading currentResult : foundReadings) {
				if(currentResult.getReadingType().equals("bca") && currentResult.getAddress().getReadingTakenTime().equals(r.getAddress().getReadingTakenTime())) {
					resultSameAsInput = currentResult;
					System.out.println("found the same result!");
					break;
				}
			}
			assertTrue(resultSameAsInput != null);
		} catch (NoHostAvailableException connEx) {
			System.out.println("NoHostEx while testing save: " + connEx.toString());
			connEx.printStackTrace();
			assertTrue(false);
		} catch (Exception e) {
			System.out.println("ConnectionEx while testing save: " + e.toString());
			e.printStackTrace();
			assertTrue(false);
		}
		return r;
	}
    
    protected void updateTest() {
		
	}

	protected void deleteTest(ReadingAddress k) {
		System.out.println("testing delete.." + k.toString());
		try {
			intializableDbManager.deleteReading(k);
            StoredReading r = intializableDbManager.findStorageReadingByKey(k);
			//System.out.println("reading should have been deleted. usable: " + r.isUsable());
			
			assertTrue(r == null);
            //QueryValidationException, IllegalStateException, QueryExecutionException
		} catch (ColumnNotFoundException noRecordEx) {
			System.out.println("NotFoundException while testing delete: " + noRecordEx.toString());
            noRecordEx.printStackTrace();
			assertTrue(false);
		} catch (NoHostAvailableException connEx) {
			System.out.println("configEx while testing delete: " + connEx.toString());
			connEx.printStackTrace();
			assertTrue(false);
		} catch (Exception e) {
			System.out.println("Unexpected Ex while testing delete: " + e.toString());
			e.printStackTrace();
			assertTrue(false);
		}
	}
}
