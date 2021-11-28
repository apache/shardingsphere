package org.apache.shardingsphere.sharding.route.engine.condition;

import junit.framework.TestCase;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Assert;

import java.util.Collections;

import static org.mockito.Mockito.mock;

public class ShardingConditionsTest extends TestCase {

    ShardingConditions shardingConditions = new ShardingConditions(Collections.emptyList(), mock(SQLStatementContext.class), mock(ShardingRule.class));

    public void testIsAlwaysFalse() {
        Assert.assertEquals(shardingConditions.isAlwaysFalse(), false);
    }

    public void testMerge() {
    }

    public void testIsNeedMerge() {
        Assert.assertEquals(shardingConditions.isNeedMerge(), false);
    }

    public void testIsSameShardingCondition() {
        Assert.assertEquals(shardingConditions, shardingConditions);
    }
}