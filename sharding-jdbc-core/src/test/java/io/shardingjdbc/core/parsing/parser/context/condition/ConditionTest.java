package io.shardingjdbc.core.parsing.parser.context.condition;

import io.shardingjdbc.core.api.algorithm.sharding.ShardingValue;
import io.shardingjdbc.core.api.algorithm.sharding.ListShardingValue;
import io.shardingjdbc.core.api.algorithm.sharding.RangeShardingValue;
import io.shardingjdbc.core.parsing.parser.expression.SQLExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLNumberExpression;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class ConditionTest {
    
    @Test
    public void assertGetShardingValue() {
        Condition condition = new Condition(new Column("test", "test"), new SQLNumberExpression(1));
        ShardingValue shardingValue = condition.getShardingValue(Collections.emptyList());
        assertThat((Integer) ((ListShardingValue) shardingValue).getValues().iterator().next(), is(1));
        condition = new Condition(new Column("test", "test"), Arrays.<SQLExpression>asList(new SQLNumberExpression(1), new SQLNumberExpression(2)));
        shardingValue = condition.getShardingValue(Collections.emptyList());
        Iterator<?> iterator = ((ListShardingValue) shardingValue).getValues().iterator();
        assertThat((Integer) iterator.next(), is(1));
        assertThat((Integer) iterator.next(), is(2));
        condition = new Condition(new Column("test", "test"), new SQLNumberExpression(1), new SQLNumberExpression(2));
        shardingValue = condition.getShardingValue(Collections.emptyList());
        assertThat((Integer) ((RangeShardingValue) shardingValue).getValueRange().lowerEndpoint(), is(1));
        assertThat((Integer) ((RangeShardingValue) shardingValue).getValueRange().upperEndpoint(), is(2));
    }
}
