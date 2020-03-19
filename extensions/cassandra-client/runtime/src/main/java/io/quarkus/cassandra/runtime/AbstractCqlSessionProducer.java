package io.quarkus.cassandra.runtime;

import java.util.concurrent.CompletionStage;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfig;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.config.ProgrammaticDriverConfigLoaderBuilder;
import com.datastax.oss.driver.api.core.context.DriverContext;
import com.datastax.oss.driver.internal.core.config.typesafe.DefaultDriverConfigLoader;
import com.datastax.oss.driver.internal.core.config.typesafe.DefaultProgrammaticDriverConfigLoaderBuilder;
import com.datastax.oss.driver.internal.core.util.concurrent.CompletableFutures;
import com.typesafe.config.ConfigFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.quarkus.cassandra.config.CqlSessionConfig;
import io.quarkus.cassandra.config.CqlSessionConnectionConfig;

public abstract class AbstractCqlSessionProducer {

    private CqlSessionConfig config;

    public void setConfig(CqlSessionConfig config) {
        this.config = config;
    }

    private ProgrammaticDriverConfigLoaderBuilder createDriverConfigLoader() {
        ProgrammaticDriverConfigLoaderBuilder builder = new DefaultProgrammaticDriverConfigLoaderBuilder(
                () -> {
                    ConfigFactory.invalidateCaches();
                    return ConfigFactory.defaultOverrides()
                            // todo do we need to load both conf and json?
                            .withFallback(ConfigFactory.parseResources("application.conf"))
                            .withFallback(ConfigFactory.parseResources("application.json"))
                            .withFallback(ConfigFactory.defaultReference())
                            .resolve();
                },
                DefaultDriverConfigLoader.DEFAULT_ROOT_PATH) {
            @NonNull
            @Override
            public DriverConfigLoader build() {
                return new NonReloadableDriverConfigLoader(super.build());
            }
        };

        return builder;
    }

    CqlSessionConfig getCassandraClientConfig() {
        return config;
    }

    public CqlSession createCassandraClient(CqlSessionConfig config) {
        ProgrammaticDriverConfigLoaderBuilder configLoaderBuilder = createDriverConfigLoader();
        configureConnectionSettings(configLoaderBuilder, config.cqlSessionConnectionConfig);

        CqlSessionBuilder builder = CqlSession.builder().withConfigLoader(configLoaderBuilder.build());
        return builder.build();
    }

    private void configureConnectionSettings(ProgrammaticDriverConfigLoaderBuilder configLoaderBuilder,
            CqlSessionConnectionConfig config) {
        configLoaderBuilder.withStringList(DefaultDriverOption.CONTACT_POINTS, config.contactPoints);
        configLoaderBuilder.withString(DefaultDriverOption.LOAD_BALANCING_LOCAL_DATACENTER, config.localDataCenter);
        config.requestTimeout.ifPresent(v -> configLoaderBuilder.withDuration(
                DefaultDriverOption.REQUEST_TIMEOUT, v));
    }

    private static class NonReloadableDriverConfigLoader implements DriverConfigLoader {

        private final DriverConfigLoader delegate;

        public NonReloadableDriverConfigLoader(DriverConfigLoader delegate) {
            this.delegate = delegate;
        }

        @NonNull
        @Override
        public DriverConfig getInitialConfig() {
            return delegate.getInitialConfig();
        }

        @Override
        public void onDriverInit(@NonNull DriverContext context) {
            delegate.onDriverInit(context);
        }

        @NonNull
        @Override
        public CompletionStage<Boolean> reload() {
            return CompletableFutures.failedFuture(
                    new UnsupportedOperationException("reload not supported"));
        }

        @Override
        public boolean supportsReloading() {
            return false;
        }

        @Override
        public void close() {
            delegate.close();
        }
    }
}
