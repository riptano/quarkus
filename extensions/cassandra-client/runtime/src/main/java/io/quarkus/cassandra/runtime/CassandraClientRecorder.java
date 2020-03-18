package io.quarkus.cassandra.runtime;

import javax.enterprise.inject.Default;
import javax.enterprise.util.AnnotationLiteral;

import com.datastax.oss.driver.api.core.CqlSession;

import io.quarkus.arc.Arc;
import io.quarkus.arc.runtime.BeanContainerListener;
import io.quarkus.cassandra.config.CqlSessionConfig;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class CassandraClientRecorder {

    public BeanContainerListener addCassandraClient(
            Class<? extends AbstractCqlSessionProducer> cqlSessionProducerClass) {
        return beanContainer -> {
            beanContainer.instance(cqlSessionProducerClass);
        };
    }

    public void configureRuntimeProperties(CqlSessionConfig config) {
        AbstractCqlSessionProducer producer = Arc.container().instance(AbstractCqlSessionProducer.class).get();
        producer.setConfig(config);
    }

    @SuppressWarnings("rawtypes")
    private AnnotationLiteral defaultName() {
        return Default.Literal.INSTANCE;
    }

    public RuntimeValue<CqlSession> getClient() {
        return new RuntimeValue<>(Arc.container().instance(CqlSession.class, defaultName()).get());
    }
}
