package io.quarkus.cassandra.config;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = CqlSessionConfig.CONFIG_NAME, phase = ConfigPhase.RUN_TIME)
public class CqlSessionConfig {
    public static final String CONFIG_NAME = "cassandra";

    /**
     * The cassandra client config.
     */
    @ConfigItem(name = ConfigItem.PARENT)
    public CqlSessionConnectionConfig cqlSessionConnectionConfig;

    // todo more low granular conf objects (i.e. ssl, cloud)

}
