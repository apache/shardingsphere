package org.apache.shardingsphere.singletable.fixtrue;

import org.apache.shardingsphere.infra.rule.identifier.scope.SchemaRule;

public final class FixtureRule implements SchemaRule {
    @Override
    public String getType() {
        return "FIXTURE";
    }
}
