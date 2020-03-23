package io.quarkus.cassandra;

import static com.datastax.oss.driver.api.core.config.DefaultDriverOption.LOAD_BALANCING_LOCAL_DATACENTER;
import static com.datastax.oss.driver.api.core.config.DefaultDriverOption.REQUEST_PAGE_SIZE;
import static com.datastax.oss.driver.api.core.config.DefaultDriverOption.REQUEST_TIMEOUT;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import javax.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DriverExecutionProfile;

import io.quarkus.test.QuarkusUnitTest;
import io.quarkus.test.common.QuarkusTestResource;

@QuarkusTestResource(CassandraTestBase.class)
public class CassandraClientConfigTest {

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withConfigurationResource("application-cassandra-client.properties")
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class).addClasses(CassandraTestBase.class)
                    .addAsResource("application.json")
                    .addAsResource("application.conf"));

    @Inject
    CqlSession cqlSession;

    @Test
    public void testDataSourceViaCqlSession() {
        assertThat(cqlSession.execute("SELECT * FROM system.local")).isNotEmpty();
    }

    @Test
    public void testSettingSetFromApplicationProperties() {
        DriverExecutionProfile profile = cqlSession.getContext().getConfig().getDefaultProfile();

        assertThat(profile.getString(LOAD_BALANCING_LOCAL_DATACENTER)).isEqualTo("datacenter1");
    }

    @Test
    public void applicationConfSettingsShouldHavePriorityOverReferenceConfFromDriver() {
        DriverExecutionProfile profile = cqlSession.getContext().getConfig().getDefaultProfile();

        assertThat(profile.getDuration(REQUEST_TIMEOUT)).isEqualTo(Duration.of(20, ChronoUnit.SECONDS));
    }

    @Test
    public void applicationJsonSettingsShouldHavePriorityOverReferenceConfFromDriver() {
        DriverExecutionProfile profile = cqlSession.getContext().getConfig().getDefaultProfile();

        assertThat(profile.getInt(REQUEST_PAGE_SIZE)).isEqualTo(1000);
    }
}
