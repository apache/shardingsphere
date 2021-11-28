package org.apache.shardingsphere.sharding.route.engine.condition;

import junit.framework.TestCase;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Assert;

import java.util.Collections;

import static org.mockito.Mockito.mock;

public class ShardingConditionsTest extends TestCase {

    private final ShardingConditions shardingConditions = new ShardingConditions(Collections.emptyList(), mock(SQLStatementContext.class), mock(ShardingRule.class));

    /**
     * Tests isAlwaysFalse().
     */
    public void testIsAlwaysFalse() {
        Assert.assertEquals(shardingConditions.isAlwaysFalse(), false);
    }

    /**
     * Tests isNeedMerge().
     */
    public void testIsNeedMerge() {
        Assert.assertEquals(shardingConditions.isNeedMerge(), false);
    }

    /**
     * Tests isSameShardingCondition().
     */
    public void testIsSameShardingCondition() {
        Assert.assertEquals(shardingConditions, shardingConditions);
    }
}
