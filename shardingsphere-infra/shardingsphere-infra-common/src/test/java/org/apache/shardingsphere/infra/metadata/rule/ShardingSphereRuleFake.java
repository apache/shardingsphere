package org.apache.shardingsphere.infra.metadata.rule;

import org.apache.shardingsphere.infra.rule.ShardingSphereRule;

public class ShardingSphereRuleFake implements ShardingSphereRule {
    @Override
    public String getType() {
        return "type";
    }
}
