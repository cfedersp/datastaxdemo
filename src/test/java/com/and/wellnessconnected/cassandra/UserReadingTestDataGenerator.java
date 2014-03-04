package com.and.wellnessconnected.cassandra;

import com.and.wellnessconnected.models.StoredReading;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public abstract class UserReadingTestDataGenerator {
	public StoredReading generateUserReading(String userId, Date readingTakenTime, String deviceId, int counter) {
        StoredReading r = new StoredReading();
		generateUserReading(r, userId, readingTakenTime, deviceId, counter);
		return r;
	}
	public abstract void generateUserReading(StoredReading outputObject, String userId, Date readingTakenTime, String deviceId, int counter);
	public Date createRecentDate(int offset) {
		Calendar cal = new GregorianCalendar();
    	cal.add(Calendar.DAY_OF_MONTH,  offset);
    	return cal.getTime(); 
	}
	public Date selectRandomDate(Date greaterThan, Date lessThan) {
    	double random = Math.random(); //between zero and one
    	
    	long duration = lessThan.getTime() - greaterThan.getTime();
    	double offset = duration*random;
    	System.out.println("duration: " + duration + " random: " + random);
    	return new Date((long)(greaterThan.getTime() + offset));
    	
    }
}
