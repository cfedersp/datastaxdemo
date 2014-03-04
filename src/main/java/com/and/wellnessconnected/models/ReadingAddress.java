package com.and.wellnessconnected.models;

import java.io.Serializable;
import java.util.Date;

public class ReadingAddress implements Serializable {
	private static final long serialVersionUID = 1l;
	
	protected String sourceId = null;
	protected Date readingTakenTime = null;
	public ReadingAddress() {
		
	}
	public ReadingAddress(String sourceId, Date readingTakenTime) {
		this.sourceId = sourceId;
		this.readingTakenTime = readingTakenTime;
	}
	
	
	public String getSourceId() {
		return sourceId;
	}
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
	public Date getReadingTakenTime() {
		return readingTakenTime;
	}
	public void setReadingTakenTime(Date readingTakenTime) {
		this.readingTakenTime = readingTakenTime;
	}
	@Override
	public String toString() {
		return "ReadingAddress [sourceId=" + sourceId + ", readingTakenTime="
				+ readingTakenTime + "]";
	}
	
	
}
