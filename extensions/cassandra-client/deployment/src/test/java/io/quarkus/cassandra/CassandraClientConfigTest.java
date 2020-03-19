package io.quarkus.cassandra;

import static com.datastax.oss.driver.api.core.config.DefaultDriverOption.REQUEST_TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import javax.inject.Inject;

import org.assertj.core.api.AssertionsForClassTypes;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DriverExecutionProfile;

import io.quarkus.test.QuarkusUnitTest;

public class CassandraClientConfigTest extends CassandraTestBase {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class).addClasses(CassandraTestBase.class))
            .withConfigurationResource("application-cassandra-client.properties");

    @Inject
    CqlSession cqlSession;

    @Test
    public void testDataSourceViaCqlSession() {
        assertThat(cqlSession.execute("SELECT * FROM system.local")).isNotEmpty();
    }

    @Test
    public void testSettingSetFromApplicationProperties() {
        DriverExecutionProfile profile = cqlSession.getContext().getConfig().getDefaultProfile();

        AssertionsForClassTypes.assertThat(profile.getDuration(REQUEST_TIMEOUT)).isEqualTo(Duration.of(20, ChronoUnit.SECONDS));
    }
}
