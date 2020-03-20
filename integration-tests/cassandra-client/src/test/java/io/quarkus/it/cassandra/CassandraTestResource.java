package io.quarkus.it.cassandra;

import java.util.Collections;
import java.util.Map;

import org.jboss.logging.Logger;
import org.testcontainers.containers.GenericContainer;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class CassandraTestResource implements QuarkusTestResourceLifecycleManager {
    private static final Logger LOGGER = Logger.getLogger(CassandraTestResource.class);
    private static GenericContainer<?> cassandraContainer;
    private static final String CASSANDRA_311 = "cassandra:3.11";
    protected static final String CASSANDRA_INTERNAL_PORT = "9042";

    @Override
    public Map<String, String> start() {
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
        LOGGER.infof("Starting Cassandra %s on port %s", CASSANDRA_311, CASSANDRA_INTERNAL_PORT);
        cassandraContainer.start();
        return Collections.emptyMap();
    }

    @Override
    public void stop() {
        // nothing to do, it will be stopped in the shutdown hook
    }
}
