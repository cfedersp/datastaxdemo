package com.and.wellnessconnected.cassandra;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class JacksonStringSerializer<T> {
	protected Class<T> parameterizedClass = null;
	protected ObjectMapper mapper = null;
	
	public JacksonStringSerializer(Class<T> pClass, ObjectMapper mapper) {
		this.parameterizedClass = pClass;
		this.mapper = mapper;
	}
	public T fromString(String b) {
		
		T m = null;
		try {
			m = (T) mapper.readValue(b, parameterizedClass); 
		} catch (JsonParseException parseEx) {
			throw new RuntimeException(parseEx);
		} catch (JsonMappingException mappingEx) {
			throw new RuntimeException(mappingEx);
		} catch (JsonGenerationException generatingEx) {
			throw new RuntimeException(generatingEx);
		} catch (IOException ioEx) {
			throw new RuntimeException(ioEx);
		}
		return m;
	}

	public String toString(T r) {
		if (r == null) {
            return null;
	    }
		try {
			return mapper.writeValueAsString(r);
		} catch (JsonMappingException mappingEx) {
			throw new RuntimeException(mappingEx);
		} catch (JsonGenerationException generatingEx) {
			throw new RuntimeException(generatingEx);
		} catch (IOException ioEx) {
			throw new RuntimeException(ioEx);
		}
	}

	


}
