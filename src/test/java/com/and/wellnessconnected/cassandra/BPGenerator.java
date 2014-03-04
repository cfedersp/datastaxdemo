package com.and.wellnessconnected.cassandra;

import com.and.wellnessconnected.models.ReadingAddress;
import com.and.wellnessconnected.models.StoredReading;

import java.util.Date;

public class BPGenerator extends UserReadingTestDataGenerator {
	public void generateUserReading(StoredReading r, String userId, Date readingTakenTime, String deviceId, int counter) {
        r.setUserId(userId);
        r.setAddress(new ReadingAddress(deviceId, readingTakenTime));
        r.setReadingType("bca");

        r.setReadingData("dodeedodeedo");
    }
}
