package io.quarkus.cassandra;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.GenericContainer;

public class CassandraTestBase {
    private static GenericContainer<?> cassandraContainer;
    private static final String CASSANDRA_311 = "cassandra:3.11";
    protected static final String CASSANDRA_INTERNAL_PORT = "9042";
    protected static final String DATACENTER = "datacenter1";

    @BeforeAll
    public static void startCassandraDatabase() throws IOException {
        // create the container with the internal Cassandra port exposed as a mapped port.
        cassandraContainer = new GenericContainer<>(CASSANDRA_311)
                .withExposedPorts(Integer.parseInt(CASSANDRA_INTERNAL_PORT));
        // add a shutdown hook to remove the container on shutdown
        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> {
                                    try {
                                        cassandraContainer.stop();
                                    } catch (Exception e) {
                                        // do nothing, we may have already shutdown
                                    }
                                }));
        // start the container
        cassandraContainer.start();
    }

    @AfterAll
    public static void stopCassandraDatabase() {
        // nothing - it will be stopped in the shutdownHook
    }

}
