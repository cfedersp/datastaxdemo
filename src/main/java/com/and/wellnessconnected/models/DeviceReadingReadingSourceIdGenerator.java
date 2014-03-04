package com.and.wellnessconnected.models;

import java.util.Date;

import com.and.wcapi.backend.models.UserReading;

public class DeviceReadingReadingSourceIdGenerator {
	public DeviceReadingReadingSourceIdGenerator() {
	
	}
	
	public String createSourceId(UserReading input) {
		return input.getDeviceId(); 
	}
	public boolean isManualReading(UserReading input) {
		return false;
	}
	public boolean containsEncryptableMeasurements(UserReading input) {
		return input.getReadingType().equals("bp");
	}
	public Date createColumnKey(UserReading input) {
		return input.getReadingTakenTime();
	}
	
}
