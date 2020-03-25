package io.quarkus.cassandra.runtime.health;

import static io.quarkus.cassandra.runtime.health.CassandraHealthCheck.HEALTH_CHECK_QUERY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponse.State;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.metadata.Metadata;
import com.datastax.oss.driver.api.core.metadata.Node;
import com.datastax.oss.driver.shaded.guava.common.collect.ImmutableMap;

import edu.umd.cs.findbugs.annotations.NonNull;

public class CassandraHealthCheckTest {

    @ParameterizedTest
    @MethodSource("statusDetails")
    public void should_report_status_up_with_data_center_details(
            String dc,
            String releaseVersion,
            String clusterName,
            String cqlVersion,
            Long numberOfNodes) {
        // given
        CqlSession session = mockCqlSessionWithResultSet(dc, releaseVersion, clusterName, cqlVersion, numberOfNodes);

        // when
        CassandraHealthCheck cassandraHealthIndicator = new CassandraHealthCheckMock(session);
        cassandraHealthIndicator.init();

        // then
        HashMap<String, Object> expected = new HashMap<>();
        expected.put("dataCenter", dc);
        expected.put("releaseVersion", releaseVersion);
        expected.put("clusterName", clusterName);
        expected.put("cqlVersion", cqlVersion);
        expected.put("numberOfNodes", numberOfNodes);
        HealthCheckResponse health = cassandraHealthIndicator.call();
        assertThat(health.getState()).isEqualTo(State.UP);
        assertThat(health.getData().get()).isEqualTo(expected);
    }

    @Test
    public void should_return_status_down_when_cql_session_throws() {
        // given
        CqlSession session = mock(CqlSession.class);
        when(session.execute(HEALTH_CHECK_QUERY)).thenThrow(new RuntimeException("problem"));

        // when
        CassandraHealthCheck cassandraHealthIndicator = new CassandraHealthCheckMock(session);
        cassandraHealthIndicator.init();

        // then
        HealthCheckResponse health = cassandraHealthIndicator.call();
        assertThat(health.getState()).isEqualTo(State.DOWN);
        assertThat(health.getData().get()).containsKeys("reason");
    }

    @Test
    public void should_return_status_down_when_cql_session_query_returns_null() {
        // given
        CqlSession session = mockCqlSessionWithOneNullResult();

        // when
        CassandraHealthCheck cassandraHealthIndicator = new CassandraHealthCheckMock(session);
        cassandraHealthIndicator.init();

        // then
        HealthCheckResponse health = cassandraHealthIndicator.call();
        assertThat(health.getState()).isEqualTo(State.DOWN);
        assertThat(health.getData().get())
                .isEqualTo(ImmutableMap.of("reason", "system.local returned null"));
    }

    @NonNull
    private CqlSession mockCqlSessionWithOneNullResult() {
        CqlSession session = mock(CqlSession.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.one()).thenReturn(null);
        when(session.execute(HEALTH_CHECK_QUERY)).thenReturn(resultSet);
        return session;
    }

    @NonNull
    private CqlSession mockCqlSessionWithResultSet(
            String dc,
            String releaseVersion,
            String clusterName,
            String cqlVersion,
            Long numberOfNodes) {
        CqlSession session = mock(CqlSession.class);
        ResultSet resultSet = mock(ResultSet.class);
        Row row = mock(Row.class);
        when(row.getString("data_center")).thenReturn(dc);
        when(row.getString("release_version")).thenReturn(releaseVersion);
        when(row.getString("cluster_name")).thenReturn(clusterName);
        when(row.getString("cql_version")).thenReturn(cqlVersion);
        when(resultSet.one()).thenReturn(row);
        when(session.execute(HEALTH_CHECK_QUERY)).thenReturn(resultSet);
        Metadata metadata = mock(Metadata.class);
        when(session.getMetadata()).thenReturn(metadata);
        Map<UUID, Node> nodes = new LinkedHashMap<>();
        for (int i = 0; i < numberOfNodes; i++) {
            nodes.put(UUID.randomUUID(), mock(Node.class));
        }
        when(metadata.getNodes()).thenReturn(nodes);
        return session;
    }

    private static Stream<Arguments> statusDetails() {
        return Stream.<Arguments> builder()
                .add(Arguments.arguments("dc1", "v1", "cluster_1", "v1", 1L))
                .add(Arguments.arguments(null, null, null, null, 0L))
                .build();
    }

    private static class CassandraHealthCheckMock extends CassandraHealthCheck {

        private CqlSession cqlSession;

        public CassandraHealthCheckMock(CqlSession cqlSession) {
            this.cqlSession = cqlSession;
        }

        @Override
        public CqlSession beanProvider() {
            return cqlSession;
        }
    }
}
