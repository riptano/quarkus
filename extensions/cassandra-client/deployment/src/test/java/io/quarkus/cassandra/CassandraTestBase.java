package io.quarkus.cassandra;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.wait.CassandraQueryWaitStrategy;

public class CassandraTestBase {
    private static FixedHostPortGenericContainer<?> cassandraContainer;
    private static final String CASSANDRA_311 = "cassandra:3.11";
    protected static final int CASSANDRA_INTERNAL_PORT = 9042;

    @BeforeAll
    public static void startCassandraDatabase() throws IOException, InterruptedException {
        // create the container with the internal Cassandra port exposed as a mapped port.
        cassandraContainer = new FixedHostPortGenericContainer<>(CASSANDRA_311)
                .withFixedExposedPort(CASSANDRA_INTERNAL_PORT, CASSANDRA_INTERNAL_PORT);
        cassandraContainer.setWaitStrategy(new CassandraQueryWaitStrategy());

        // start the container
        cassandraContainer.start();
    }

    @AfterAll
    public static void stopCassandraDatabase() {
        if (cassandraContainer != null && cassandraContainer.isRunning()) {
            cassandraContainer.stop();
        }
    }
}
