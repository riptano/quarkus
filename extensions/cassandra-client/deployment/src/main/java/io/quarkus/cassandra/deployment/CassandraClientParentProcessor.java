package io.quarkus.cassandra.deployment;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

class CassandraClientParentProcessor {

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FeatureBuildItem.CASSANDRA_CLIENT);
    }
}
