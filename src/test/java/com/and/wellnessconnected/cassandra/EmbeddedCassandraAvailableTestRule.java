package com.and.wellnessconnected.cassandra;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class EmbeddedCassandraAvailableTestRule implements TestRule {

	protected AndEmbeddedCassandra cassandra = null;
	
	public EmbeddedCassandraAvailableTestRule(AndEmbeddedCassandra cassandra) {
	
		this.cassandra = cassandra;
		System.out.println("initialized rule. cassandra null?: " + (cassandra == null));
	}
	public Statement apply(Statement statement, Description description) {
		System.out.println("applying rule.");
		return cassandra.apply(statement, description);
	}

}
