package io.quarkus.cassandra.client.parent.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class CassandraClientParentProcessor {

    private static final String FEATURE = "cassandra-client-parent";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

}
