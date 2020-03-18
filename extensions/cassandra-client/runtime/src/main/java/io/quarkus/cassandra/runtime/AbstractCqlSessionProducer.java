package io.quarkus.cassandra.runtime;

import java.util.concurrent.CompletionStage;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DriverConfig;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.config.ProgrammaticDriverConfigLoaderBuilder;
import com.datastax.oss.driver.api.core.context.DriverContext;
import com.datastax.oss.driver.internal.core.config.typesafe.DefaultDriverConfigLoader;
import com.datastax.oss.driver.internal.core.config.typesafe.DefaultProgrammaticDriverConfigLoaderBuilder;
import com.datastax.oss.driver.internal.core.util.concurrent.CompletableFutures;
import com.typesafe.config.ConfigFactory;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.quarkus.cassandra.config.CqlSessionClientConfig;
import io.quarkus.cassandra.config.CqlSessionConfig;

public abstract class AbstractCqlSessionProducer {

    private CqlSessionConfig config;

    public void setConfig(CqlSessionConfig config) {
        this.config = config;
    }

    private DriverConfigLoader createDriverConfigLoader(CqlSessionClientConfig config) {
        ProgrammaticDriverConfigLoaderBuilder builder = new DefaultProgrammaticDriverConfigLoaderBuilder(
                () -> {
                    ConfigFactory.invalidateCaches();
                    return ConfigFactory.defaultOverrides()
                            .withFallback(ConfigFactory.parseMap(config.datastaxJavaDriver,
                                    "Spring properties"))
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
        return builder.build();
    }

    CqlSessionClientConfig getCassandraClientConfig() {
        return config.cqlSessionClientConfig;
    }

    public CqlSession createCassandraClient(CqlSessionClientConfig cqlSessionClientConfig) {
        DriverConfigLoader configLoader = createDriverConfigLoader(cqlSessionClientConfig);
        CqlSessionBuilder builder = CqlSession.builder().withConfigLoader(configLoader);
        return builder.build();
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
