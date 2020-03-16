package io.quarkus.cassandra;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.datastax.oss.driver.api.core.CqlSession;
import io.quarkus.test.QuarkusUnitTest;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import static org.assertj.core.api.Assertions.assertThat;

public class CassandraClientConfigTest extends CassandraTestBase {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class).addClasses(CassandraTestBase.class))
            .withConfigurationResource("application-cassandra-client.properties");

    @Inject
    CqlSession cqlSession;

    @AfterEach
    void cleanup() {
        if (cqlSession != null) {
            cqlSession.close();
        }
    }

    @Test
    public void testDataSourceViaCqlSession() {
        assertThat(cqlSession.execute("SELECT * FROM system.local")).isNotEmpty();
    }
}
