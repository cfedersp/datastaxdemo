package com.and.wellnessconnected.cassandra;

import java.io.IOException;

import org.apache.cassandra.exceptions.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.rules.ExternalResource;

public class AndEmbeddedCassandra extends ExternalResource {

	protected String configLocation = null;
	protected AutoconstructBackend backendManager = null;
	protected static int total = 0;

	
	
	public AndEmbeddedCassandra(String configLocation, AutoconstructBackend manager) {
		this.configLocation = configLocation;
		++total;
		this.backendManager = manager;
		System.out.println("constructed " + total + " AndEmbeddedCassandra with configLocation: " + configLocation + " interface null? " + (manager == null));
	}
	
	@Override
	protected void before() throws Exception {
		// TODO Auto-generated method stub
		try {
			System.out.println("starting embedded cassandra server");
			EmbeddedCassandraServerHelper.startEmbeddedCassandra(configLocation);
            SimpleClient client = new SimpleClient();
            client.connect("127.0.0.1", 9052);
            //9170 for embedded cassandra thrift testing,9160 for operations Thift client API
            //9052 for embedded CQL native transport port testing, 9042 in operations

			
			
						
			backendManager.init(client);
			backendManager.createKeyspace();
			backendManager.constructColumnFamilies();


            System.out.println("test rule initialized");
		} catch (ConfigurationException configEx) {
			System.err.println("error in test cassandra config: " + configEx.toString());
            throw configEx;
		} catch (IOException ioEx) {

			System.err.println("error in test cassandra io: " + ioEx.toString());
            throw ioEx;
		} catch (TTransportException transportEx) {

			System.err.println("error in test cassandra transport: " + transportEx.toString());
            throw transportEx;
		}
		System.out.println("done loading embedded cassandra server");
	}
	
	@Override
	protected void after() {
		EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
	}

}
