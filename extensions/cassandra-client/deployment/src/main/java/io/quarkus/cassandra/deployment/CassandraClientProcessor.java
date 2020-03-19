package io.quarkus.cassandra.deployment;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;
import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;

import org.jboss.jandex.DotName;

import com.datastax.oss.driver.api.core.CqlSession;

import io.quarkus.arc.Unremovable;
import io.quarkus.arc.deployment.BeanContainerListenerBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanGizmoAdaptor;
import io.quarkus.cassandra.config.CqlSessionConfig;
import io.quarkus.cassandra.runtime.AbstractCqlSessionProducer;
import io.quarkus.cassandra.runtime.CassandraClientRecorder;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ConfigurationBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.recording.RecorderContext;
import io.quarkus.gizmo.ClassCreator;
import io.quarkus.gizmo.ClassOutput;
import io.quarkus.gizmo.MethodCreator;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;

class CassandraClientProcessor {
    private static final DotName UNREMOVABLE_BEAN = DotName.createSimple(AbstractCqlSessionProducer.class.getName());

    @SuppressWarnings("unchecked")
    @Record(STATIC_INIT)
    @BuildStep
    BeanContainerListenerBuildItem build(
            RecorderContext recorderContext,
            CassandraClientRecorder recorder,
            BuildProducer<FeatureBuildItem> feature,
            BuildProducer<GeneratedBeanBuildItem> generatedBean) {

        feature.produce(new FeatureBuildItem(FeatureBuildItem.CASSANDRA_CLIENT));

        String cassandraClientProducerClassName = getCassandraClientProducerClassName();
        createCassandraClientProducerBean(generatedBean, cassandraClientProducerClassName);

        return new BeanContainerListenerBuildItem(recorder.addCassandraClient(
                (Class<? extends AbstractCqlSessionProducer>) recorderContext.classProxy(cassandraClientProducerClassName)));
    }

    private String getCassandraClientProducerClassName() {
        return AbstractCqlSessionProducer.class.getPackage().getName() + "."
                + "CqlSessionProducer";
    }

    private void createCassandraClientProducerBean(
            BuildProducer<GeneratedBeanBuildItem> generatedBean,
            String cassandraClientProducerClassName) {

        ClassOutput classOutput = new GeneratedBeanGizmoAdaptor(generatedBean);

        try (ClassCreator classCreator = ClassCreator.builder().classOutput(classOutput)
                .className(cassandraClientProducerClassName)
                .superClass(AbstractCqlSessionProducer.class)
                .build()) {
            classCreator.addAnnotation(ApplicationScoped.class);

            try (MethodCreator defaultCassandraClient = classCreator.getMethodCreator("createDefaultCassandraClient",
                    CqlSession.class)) {
                defaultCassandraClient.addAnnotation(ApplicationScoped.class);
                defaultCassandraClient.addAnnotation(Produces.class);
                defaultCassandraClient.addAnnotation(Default.class);

                // make CqlSession as Unremovable bean
                defaultCassandraClient.addAnnotation(Unremovable.class);

                ResultHandle cassandraClientConfig = defaultCassandraClient.invokeVirtualMethod(
                        MethodDescriptor.ofMethod(AbstractCqlSessionProducer.class, "getCassandraClientConfig",
                                CqlSessionConfig.class),
                        defaultCassandraClient.getThis());

                defaultCassandraClient.returnValue(
                        defaultCassandraClient.invokeVirtualMethod(
                                MethodDescriptor.ofMethod(AbstractCqlSessionProducer.class, "createCassandraClient",
                                        CqlSession.class,
                                        CqlSessionConfig.class),
                                defaultCassandraClient.getThis(),
                                cassandraClientConfig));
            }

        }
    }

    @Record(RUNTIME_INIT)
    @BuildStep
    void configureRuntimePropertiesAndBuildClient(CassandraClientRecorder recorder,
            CqlSessionConfig cassandraConfig,
            ConfigurationBuildItem config) {
        recorder.configureRuntimeProperties(cassandraConfig);
    }

    @BuildStep
    @Record(value = RUNTIME_INIT, optional = true)
    CassandraClientBuildItem cassandraClient(CassandraClientRecorder recorder) {
        return new CassandraClientBuildItem(recorder.getClient());
    }
}
