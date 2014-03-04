package com.and.wellnessconnected.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.Session;

public class SimpleClient {
    private Cluster cluster;
    private Session session;


public static void main(String[] args) {
   SimpleClient client = new SimpleClient();
   client.connect("127.0.0.1", 9052);
   client.shutdownCluster();
}

public void connect(String node, int port) {
   cluster = Cluster.builder()
         .addContactPoint(node)
         .withPort(port)
         // .withSSL() // Uncomment if using client to node encryption
         .build();
   Metadata metadata = cluster.getMetadata();
   System.out.printf("Connected to cluster: %s\n", 
         metadata.getClusterName());
   for ( Host host : metadata.getAllHosts() ) {
      System.out.printf("Datacenter: %s; Host: %s; Rack: %s\n",
         host.getDatacenter(), host.getAddress(), host.getRack());
   }
    System.out.println("cluster null? " + (cluster == null));
    session = cluster.connect();
}

    public Session getSession() {
        return this.session;
    }


    public void shutdownCluster() {
        cluster.shutdown();
    }

}
