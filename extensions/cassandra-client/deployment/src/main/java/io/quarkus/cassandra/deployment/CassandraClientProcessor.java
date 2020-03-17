package io.quarkus.cassandra.deployment;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;
import static io.quarkus.deployment.annotations.ExecutionTime.STATIC_INIT;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;

import org.jboss.jandex.DotName;

import com.datastax.oss.driver.api.core.CqlSession;

import io.quarkus.arc.Unremovable;
import io.quarkus.arc.deployment.BeanContainerListenerBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanGizmoAdaptor;
import io.quarkus.arc.deployment.UnremovableBeanBuildItem;
import io.quarkus.cassandra.config.CqlSessionClientConfig;
import io.quarkus.cassandra.config.CqlSessionConfig;
import io.quarkus.cassandra.runtime.CassandraClientRecorder;
import io.quarkus.cassandra.runtime.CqlSessionProducer;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.annotations.Weak;
import io.quarkus.deployment.builditem.ConfigurationBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.recording.RecorderContext;
import io.quarkus.gizmo.ClassCreator;
import io.quarkus.gizmo.ClassOutput;
import io.quarkus.gizmo.MethodCreator;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;

class CassandraClientProcessor {
    private static final DotName UNREMOVABLE_BEAN = DotName.createSimple(CqlSessionProducer.class.getName());

    @BuildStep
    UnremovableBeanBuildItem markBeansAsUnremovable() {
        return new UnremovableBeanBuildItem(new UnremovableBeanBuildItem.BeanTypeExclusion(UNREMOVABLE_BEAN));
    }

    /**
     * When CassandraClientBuildItem is actually consumed by the build, then we need to make the cassandra bean unremovable
     * because it can be potentially used by the consumers
     */
    @BuildStep
    @Weak
    CassandraUnremovableBuildItem unremovable(@SuppressWarnings("unused") BuildProducer<CassandraClientBuildItem> producer) {
        return new CassandraUnremovableBuildItem();
    }

    @SuppressWarnings("unchecked")
    @Record(STATIC_INIT)
    @BuildStep
    BeanContainerListenerBuildItem build(
            RecorderContext recorderContext,
            CassandraClientRecorder recorder,
            BuildProducer<FeatureBuildItem> feature,
            Optional<CassandraUnremovableBuildItem> cassandraUnremovableBuildItem,
            BuildProducer<GeneratedBeanBuildItem> generatedBean) {

        feature.produce(new FeatureBuildItem(FeatureBuildItem.CASSANDRA_CLIENT));

        String cassandraClientProducerClassName = getCassandraClientProducerClassName();
        createCassandraClientProducerBean(generatedBean, cassandraClientProducerClassName,
                cassandraUnremovableBuildItem.isPresent());

        return new BeanContainerListenerBuildItem(recorder.addCassandraClient(
                (Class<? extends CqlSessionProducer>) recorderContext.classProxy(cassandraClientProducerClassName)));
    }

    private String getCassandraClientProducerClassName() {
        return CqlSessionProducer.class.getPackage().getName() + "."
                + "CqlSessionProducer";
    }

    private void createCassandraClientProducerBean(
            BuildProducer<GeneratedBeanBuildItem> generatedBean,
            String cassandraClientProducerClassName, boolean makeUnremovable) {

        ClassOutput classOutput = new GeneratedBeanGizmoAdaptor(generatedBean);

        try (ClassCreator classCreator = ClassCreator.builder().classOutput(classOutput)
                .className(cassandraClientProducerClassName)
                .superClass(CqlSessionProducer.class)
                .build()) {
            classCreator.addAnnotation(ApplicationScoped.class);

            try (MethodCreator defaultCassandraClient = classCreator.getMethodCreator("createCassandraClient",
                    CqlSession.class)) {
                defaultCassandraClient.addAnnotation(ApplicationScoped.class);
                defaultCassandraClient.addAnnotation(Produces.class);
                defaultCassandraClient.addAnnotation(Default.class);
                if (makeUnremovable) {
                    defaultCassandraClient.addAnnotation(Unremovable.class);
                }

                ResultHandle cassandraClientConfig = defaultCassandraClient.invokeVirtualMethod(
                        MethodDescriptor.ofMethod(CqlSessionProducer.class, "getCassandraClientConfig",
                                CqlSessionClientConfig.class),
                        defaultCassandraClient.getThis());

                defaultCassandraClient.returnValue(
                        defaultCassandraClient.invokeVirtualMethod(
                                MethodDescriptor.ofMethod(CqlSessionProducer.class, "createCassandraClient",
                                        CqlSession.class,
                                        CqlSessionConfig.class, String.class),
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
