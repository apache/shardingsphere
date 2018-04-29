/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.optimizer;

import io.shardingjdbc.core.api.algorithm.sharding.ListShardingValue;
import io.shardingjdbc.core.optimizer.condition.ShardingConditions;
import io.shardingjdbc.core.optimizer.insert.InsertOptimizeEngine;
import io.shardingjdbc.core.parsing.parser.context.condition.AndCondition;
import io.shardingjdbc.core.parsing.parser.context.condition.Column;
import io.shardingjdbc.core.parsing.parser.context.condition.Condition;
import io.shardingjdbc.core.parsing.parser.context.condition.OrCondition;
import io.shardingjdbc.core.parsing.parser.expression.SQLNumberExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLPlaceholderExpression;
import io.shardingjdbc.core.routing.router.sharding.GeneratedKey;
import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class InsertOptimizeEngineTest {
    
    @Test
    public void assertOptimizeWithoutConditionsAndGeneratedKey() {
        ShardingConditions shardingConditions = new InsertOptimizeEngine(new OrCondition(), Collections.emptyList(), null).optimize();
        assertFalse(shardingConditions.isAlwaysFalse());
        assertTrue(shardingConditions.getShardingConditions().isEmpty());
    }
    
    @Test
    public void assertOptimizeWithConditionsOnly() {
        Condition condition1 = new Condition(new Column("column1", "tbl"), new SQLNumberExpression(1000L));
        Condition condition2 = new Condition(new Column("column2", "tbl"), new SQLPlaceholderExpression(0));
        AndCondition andCondition = new AndCondition();
        andCondition.getConditions().add(condition1);
        andCondition.getConditions().add(condition2);
        OrCondition orCondition = new OrCondition();
        orCondition.getAndConditions().add(andCondition);
        ShardingConditions actual = new InsertOptimizeEngine(orCondition, Collections.<Object>singletonList(2000L), null).optimize();
        assertFalse(actual.isAlwaysFalse());
        assertThat(actual.getShardingConditions().size(), is(1));
        assertThat(actual.getShardingConditions().get(0).getShardingValues().size(), is(2));
        assertShardingValue((ListShardingValue) actual.getShardingConditions().get(0).getShardingValues().get(0), 1000L);
        assertShardingValue((ListShardingValue) actual.getShardingConditions().get(0).getShardingValues().get(1), 2000L);
    }
    
    @Test
    public void assertOptimizeWithGeneratedKeyOnly() {
        ShardingConditions actual = new InsertOptimizeEngine(new OrCondition(), Collections.emptyList(), new GeneratedKey(new Column("column3", "tbl"), -1, 3000L)).optimize();
        assertFalse(actual.isAlwaysFalse());
        assertThat(actual.getShardingConditions().size(), is(1));
        assertThat(actual.getShardingConditions().get(0).getShardingValues().size(), is(1));
        assertShardingValue((ListShardingValue) actual.getShardingConditions().get(0).getShardingValues().get(0), 3000L);
    }
    
    @Test
    public void assertOptimizeWithConditionsAndGeneratedKey() {
        Condition condition1 = new Condition(new Column("column1", "tbl"), new SQLNumberExpression(1000L));
        Condition condition2 = new Condition(new Column("column2", "tbl"), new SQLPlaceholderExpression(0));
        AndCondition andCondition = new AndCondition();
        andCondition.getConditions().add(condition1);
        andCondition.getConditions().add(condition2);
        OrCondition orCondition = new OrCondition();
        orCondition.getAndConditions().add(andCondition);
        ShardingConditions actual = new InsertOptimizeEngine(orCondition, Collections.<Object>singletonList(2000L), new GeneratedKey(new Column("column3", "tbl"), -1, 3000L)).optimize();
        assertFalse(actual.isAlwaysFalse());
        assertThat(actual.getShardingConditions().size(), is(1));
        assertThat(actual.getShardingConditions().get(0).getShardingValues().size(), is(3));
        assertShardingValue((ListShardingValue) actual.getShardingConditions().get(0).getShardingValues().get(0), 1000L);
        assertShardingValue((ListShardingValue) actual.getShardingConditions().get(0).getShardingValues().get(1), 2000L);
        assertShardingValue((ListShardingValue) actual.getShardingConditions().get(0).getShardingValues().get(2), 3000L);
    }
    
    private void assertShardingValue(final ListShardingValue actual, final long expected) {
        assertThat(actual.getValues().size(), is(1));
        assertThat((Long) actual.getValues().iterator().next(), is(expected));
    }
    
    //    @SuppressWarnings("unchecked")
//    @Test
//    public void assertOptimizeGeneratedKeyCondition() {
//        ShardingConditions shardingConditions = new OptimizeEngine().optimize(new OrCondition(), Collections.emptyList(), new GeneratedKey(new Column("test", "test"), 0, 1));
//        assertFalse(shardingConditions.isAlwaysFalse());
//        ShardingCondition shardingCondition = shardingConditions.getShardingConditions().get(0);
//        ShardingValue shardingValue = shardingCondition.getShardingValues().get(0);
//        Collection<Comparable<?>> values = ((ListShardingValue<Comparable<?>>) shardingValue).getValues();
//        assertThat(values.size(), is(1));
//        assertTrue(values.contains(1));
//    }
}
