package com.and.projectdouble.backend.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.and.wcapi.backend.models.Measurement;
import com.and.wcapi.backend.models.UserReading;

public class BPGenerator extends UserReadingTestDataGenerator {
	public void generateUserReading(UserReading r, String userId, Date readingTakenTime, String deviceId, int counter) {
    	r.setUserId(userId);
		r.setDeviceId(deviceId);
		r.setReadingType("bp");
		r.setReadingTakenTime(readingTakenTime);
		
		Map<String, Object> metadata = new HashMap<String, Object>();
		metadata.put("lastChangedby", "self");
		r.setMetadata(metadata);
		
		List<Measurement> measurements = new ArrayList<Measurement>();
		Measurement pulse = new Measurement();
		pulse.setMeasurementType("pulse");
		pulse.setValue("89");
		pulse.setUnits("bpm");
		measurements.add(pulse);
		
		Measurement systolic = new Measurement();
		systolic.setMeasurementType("systolic");
		systolic.setValue("120");
		systolic.setUnits("mmHg");
		measurements.add(systolic);
		
		Measurement diastolic = new Measurement();
		diastolic.setMeasurementType("diastolic");
		diastolic.setValue("110");
		diastolic.setUnits("mmHg");
		measurements.add(diastolic);
		
		r.setMeasurements(measurements);
    }
}
