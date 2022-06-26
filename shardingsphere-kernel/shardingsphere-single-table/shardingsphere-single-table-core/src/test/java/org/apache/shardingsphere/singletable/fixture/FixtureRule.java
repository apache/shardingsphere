package org.apache.shardingsphere.singletable.fixture;

import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

public class FixtureRule implements ShardingSphereRule {
    @Override
    public String getType() {
        return "FIXTURE";
    }
}
