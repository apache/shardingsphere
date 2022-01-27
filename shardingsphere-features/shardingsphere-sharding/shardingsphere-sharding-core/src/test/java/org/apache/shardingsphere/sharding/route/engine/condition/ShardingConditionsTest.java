package org.apache.shardingsphere.sharding.route.engine.condition;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ShardingConditionsTest {

    @Mock
    private ShardingConditions shardingConditions;

    @Before
    public void setUp() {
        when(shardingConditions.isAlwaysFalse()).thenReturn(false);
        when(shardingConditions.isNeedMerge()).thenReturn(true);
        when(shardingConditions.isSameShardingCondition()).thenReturn(false);
    }

    @Test
    public void assertIsAlwaysFalse(){
        assertFalse(shardingConditions.isAlwaysFalse());
    }

    @Test
    public void assertIsNeedMerge(){
        assertTrue(shardingConditions.isNeedMerge());
    }

    @Test
    public void  isSameShardingCondition(){
        assertFalse(shardingConditions.isSameShardingCondition());
    }

    @Test
    public void assertMerge(){
       createShardingConditions("t_order").merge();
    }

    protected final ShardingConditions createShardingConditions(final String tableName) {
        List<ShardingCondition> result = new ArrayList<>(1);
        ShardingConditionValue shardingConditionValue1 = new ListShardingConditionValue<>("user_id", tableName, Collections.singleton(1L));
        ShardingConditionValue shardingConditionValue2 = new ListShardingConditionValue<>("order_id", tableName, Collections.singleton(1L));
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingCondition.getValues().add(shardingConditionValue1);
        shardingCondition.getValues().add(shardingConditionValue2);
        result.add(shardingCondition);
        return new ShardingConditions(result, mock(SQLStatementContext.class), mock(ShardingRule.class));
    }
}
