package com.and.wellnessconnected.models;

public class StoredReading { // this class is not serializable. it only serves to group the pk with the reading 
	
	protected ReadingAddress address = null;
	protected String readingType = null;
	protected String userId = null;
	protected boolean usable = true;
	protected String readingData = null;
	
	public StoredReading() {
		
	}
	
	public StoredReading(ReadingAddress address) {
		this.address = address;
	}
	
	public StoredReading(ReadingAddress address, String readingType, String userId, boolean isUsable, String readingData) {
		this.address = address;
		this.readingType = readingType;
		this.userId = userId;
		this.usable = isUsable;
		this.readingData = readingData;
	}

	public ReadingAddress getAddress() {
		return address;
	}

	public void setAddress(ReadingAddress address) {
		this.address = address;
	}

	public String getReadingType() {
		return readingType;
	}

	public void setReadingType(String readingType) {
		this.readingType = readingType;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public boolean isUsable() {
		return usable;
	}

	public void setUsable(boolean usable) {
		this.usable = usable;
	}

	public String getReadingData() {
		return readingData;
	}

	public void setReadingData(String reading) {
		this.readingData = reading;
	}	

}
