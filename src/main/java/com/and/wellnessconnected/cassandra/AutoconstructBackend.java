package com.and.wellnessconnected.cassandra;

import com.datastax.driver.core.exceptions.NoHostAvailableException;
import com.datastax.driver.core.exceptions.QueryExecutionException;
import com.datastax.driver.core.exceptions.QueryValidationException;

public interface AutoconstructBackend extends NoSQLBackend {

	public void createKeyspace() throws NoHostAvailableException, QueryExecutionException, QueryValidationException;
	public void constructColumnFamilies() throws NoHostAvailableException, QueryExecutionException, QueryValidationException;

}
