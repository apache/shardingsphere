/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.parser.context.condition;

import io.shardingsphere.api.algorithm.sharding.ListShardingValue;
import io.shardingsphere.api.algorithm.sharding.RangeShardingValue;
import io.shardingsphere.api.algorithm.sharding.ShardingValue;
import io.shardingsphere.core.parsing.parser.expression.SQLExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLNumberExpression;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
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
