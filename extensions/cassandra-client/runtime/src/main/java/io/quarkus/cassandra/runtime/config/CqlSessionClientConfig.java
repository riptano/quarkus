package io.quarkus.cassandra.runtime.config;

import java.util.Map;

import com.datastax.oss.driver.api.core.config.ProgrammaticDriverConfigLoaderBuilder;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;

@ConfigGroup
public class CqlSessionClientConfig
{
    /**
     * Map of settings that will be passed directly to {@link ProgrammaticDriverConfigLoaderBuilder}
     */
    @ConfigItem
    public Map<String, String> datastaxJavaDriver;
}
