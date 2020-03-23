package io.quarkus.it.cassandra;

import java.util.Collections;
import java.util.Map;

import org.jboss.logging.Logger;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.CassandraQueryWaitStrategy;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class CassandraTestResource implements QuarkusTestResourceLifecycleManager {
    private static final Logger LOGGER = Logger.getLogger(CassandraTestResource.class);
    private static GenericContainer<?> cassandraContainer;
    private static final String CASSANDRA_311 = "cassandra:3.11";
    protected static final int CASSANDRA_INTERNAL_PORT = 9042;

    @Override
    public Map<String, String> start() {
        // create the container with the internal Cassandra port exposed as a mapped port.
        cassandraContainer = new FixedHostPortGenericContainer<>(CASSANDRA_311)
                .withFixedExposedPort(CASSANDRA_INTERNAL_PORT, CASSANDRA_INTERNAL_PORT);
        cassandraContainer.setWaitStrategy(new CassandraQueryWaitStrategy());

        LOGGER.infof("Starting Cassandra %s on port %s", CASSANDRA_311, CASSANDRA_INTERNAL_PORT);
        cassandraContainer.start();
        return Collections.emptyMap();
    }

    @Override
    public void stop() {
        if (cassandraContainer != null && cassandraContainer.isRunning()) {
            cassandraContainer.stop();
        }
    }
}
