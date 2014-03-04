package com.and.projectdouble.backend.test;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.and.wcapi.backend.models.UserReading;

public abstract class UserReadingTestDataGenerator {
	public UserReading generateUserReading(String userId, Date readingTakenTime, String deviceId, int counter) {
		UserReading r = new UserReading();
		generateUserReading(r, userId, readingTakenTime, deviceId, counter);
		return r;
	}
	public abstract void generateUserReading(UserReading outputObject, String userId, Date readingTakenTime, String deviceId, int counter);
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
