package com.and.projectdouble.backend.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.and.wcapi.backend.models.Measurement;
import com.and.wcapi.backend.models.UserReading;

public class BCAGenerator extends UserReadingTestDataGenerator {
	@Override
	public void generateUserReading(UserReading r, String userId, Date readingTakenTime, String deviceId, int counter) {
    	r.setUserId(userId);
		r.setDeviceId(deviceId);
		r.setReadingType("bca");
		r.setReadingTakenTime(readingTakenTime);
		
		Map<String, Object> metadata = new HashMap<String, Object>();
		metadata.put("lastChangedby", "self");
		r.setMetadata(metadata);
		
		List<Measurement> measurements = new ArrayList<Measurement>();
		Measurement w = new Measurement();
		w.setMeasurementType("weight");
		w.setValue("228");
		w.setUnits("lb");
		measurements.add(w);
		Measurement h = new Measurement();
		h.setMeasurementType("height");
		h.setValue("200");
		h.setUnits("inches");
		measurements.add(h);
		Measurement f = new Measurement();
		f.setMeasurementType("fat");
		f.setValue("22");
		f.setUnits("%");
		measurements.add(f);
		Measurement o = new Measurement();
		o.setMeasurementType("water");
		o.setValue("22");
		o.setUnits("%");
		measurements.add(o);
		r.setMeasurements(measurements);
		
    }
}
