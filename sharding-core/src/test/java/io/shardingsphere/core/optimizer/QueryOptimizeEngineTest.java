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

package io.shardingsphere.core.optimizer;

import com.google.common.collect.Range;
import io.shardingsphere.api.algorithm.sharding.ListShardingValue;
import io.shardingsphere.api.algorithm.sharding.RangeShardingValue;
import io.shardingsphere.api.algorithm.sharding.ShardingValue;
import io.shardingsphere.core.optimizer.condition.ShardingCondition;
import io.shardingsphere.core.optimizer.condition.ShardingConditions;
import io.shardingsphere.core.optimizer.query.QueryOptimizeEngine;
import io.shardingsphere.core.parsing.parser.context.condition.AndCondition;
import io.shardingsphere.core.parsing.parser.context.condition.Column;
import io.shardingsphere.core.parsing.parser.context.condition.Condition;
import io.shardingsphere.core.parsing.parser.context.condition.OrCondition;
import io.shardingsphere.core.parsing.parser.expression.SQLExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLNumberExpression;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class QueryOptimizeEngineTest {
    
    @Test
    public void assertOptimizeAlwaysFalseListConditions() {
        Condition condition1 = new Condition(new Column("column", "tbl"), Arrays.<SQLExpression>asList(new SQLNumberExpression(1), new SQLNumberExpression(2)));
        Condition condition2 = new Condition(new Column("column", "tbl"), new SQLNumberExpression(3));
        AndCondition andCondition = new AndCondition();
        andCondition.getConditions().add(condition1);
        andCondition.getConditions().add(condition2);
        OrCondition orCondition = new OrCondition();
        orCondition.getAndConditions().add(andCondition);
        ShardingConditions shardingConditions = new QueryOptimizeEngine(orCondition, Collections.emptyList()).optimize();
        assertTrue(shardingConditions.isAlwaysFalse());
    }
    
    @Test
    public void assertOptimizeAlwaysFalseRangeConditions() {
        Condition condition1 = new Condition(new Column("column", "tbl"), new SQLNumberExpression(1), new SQLNumberExpression(2));
        Condition condition2 = new Condition(new Column("column", "tbl"), new SQLNumberExpression(3), new SQLNumberExpression(4));
        AndCondition andCondition = new AndCondition();
        andCondition.getConditions().add(condition1);
        andCondition.getConditions().add(condition2);
        OrCondition orCondition = new OrCondition();
        orCondition.getAndConditions().add(andCondition);
        ShardingConditions shardingConditions = new QueryOptimizeEngine(orCondition, Collections.emptyList()).optimize();
        assertTrue(shardingConditions.isAlwaysFalse());
    }
    
    @Test
    public void assertOptimizeAlwaysFalseListConditionsAndRangeConditions() {
        Condition condition1 = new Condition(new Column("column", "tbl"), Arrays.<SQLExpression>asList(new SQLNumberExpression(1), new SQLNumberExpression(2)));
        Condition condition2 = new Condition(new Column("column", "tbl"), new SQLNumberExpression(3), new SQLNumberExpression(4));
        AndCondition andCondition = new AndCondition();
        andCondition.getConditions().add(condition1);
        andCondition.getConditions().add(condition2);
        OrCondition orCondition = new OrCondition();
        orCondition.getAndConditions().add(andCondition);
        ShardingConditions shardingConditions = new QueryOptimizeEngine(orCondition, Collections.emptyList()).optimize();
        assertTrue(shardingConditions.isAlwaysFalse());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertOptimizeListConditions() {
        Condition condition1 = new Condition(new Column("column", "tbl"), Arrays.<SQLExpression>asList(new SQLNumberExpression(1), new SQLNumberExpression(2)));
        Condition condition2 = new Condition(new Column("column", "tbl"), new SQLNumberExpression(1));
        AndCondition andCondition = new AndCondition();
        andCondition.getConditions().add(condition1);
        andCondition.getConditions().add(condition2);
        OrCondition orCondition = new OrCondition();
        orCondition.getAndConditions().add(andCondition);
        ShardingConditions shardingConditions = new QueryOptimizeEngine(orCondition, Collections.emptyList()).optimize();
        assertFalse(shardingConditions.isAlwaysFalse());
        ShardingCondition shardingCondition = shardingConditions.getShardingConditions().get(0);
        ShardingValue shardingValue = shardingCondition.getShardingValues().get(0);
        Collection<Comparable<?>> values = ((ListShardingValue<Comparable<?>>) shardingValue).getValues();
        assertThat(values.size(), is(1));
        assertTrue(values.containsAll(Collections.singleton(1)));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertOptimizeRangeConditions() {
        Condition condition1 = new Condition(new Column("column", "tbl"), new SQLNumberExpression(1), new SQLNumberExpression(2));
        Condition condition2 = new Condition(new Column("column", "tbl"), new SQLNumberExpression(1), new SQLNumberExpression(3));
        AndCondition andCondition = new AndCondition();
        andCondition.getConditions().add(condition1);
        andCondition.getConditions().add(condition2);
        OrCondition orCondition = new OrCondition();
        orCondition.getAndConditions().add(andCondition);
        ShardingConditions shardingConditions = new QueryOptimizeEngine(orCondition, Collections.emptyList()).optimize();
        assertFalse(shardingConditions.isAlwaysFalse());
        ShardingCondition shardingCondition = shardingConditions.getShardingConditions().get(0);
        ShardingValue shardingValue = shardingCondition.getShardingValues().get(0);
        Range<Comparable<?>> values = ((RangeShardingValue<Comparable<?>>) shardingValue).getValueRange();
        assertThat(values.lowerEndpoint(), CoreMatchers.<Comparable>is(1));
        assertThat(values.upperEndpoint(), CoreMatchers.<Comparable>is(2));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertOptimizeListConditionsAndRangeConditions() {
        Condition condition1 = new Condition(new Column("column", "tbl"), Arrays.<SQLExpression>asList(new SQLNumberExpression(1), new SQLNumberExpression(2)));
        Condition condition2 = new Condition(new Column("column", "tbl"), new SQLNumberExpression(1), new SQLNumberExpression(2));
        AndCondition andCondition = new AndCondition();
        andCondition.getConditions().add(condition1);
        andCondition.getConditions().add(condition2);
        OrCondition orCondition = new OrCondition();
        orCondition.getAndConditions().add(andCondition);
        ShardingConditions shardingConditions = new QueryOptimizeEngine(orCondition, Collections.emptyList()).optimize();
        assertFalse(shardingConditions.isAlwaysFalse());
        ShardingCondition shardingCondition = shardingConditions.getShardingConditions().get(0);
        ShardingValue shardingValue = shardingCondition.getShardingValues().get(0);
        Collection<Comparable<?>> values = ((ListShardingValue<Comparable<?>>) shardingValue).getValues();
        assertThat(values.size(), is(2));
        assertTrue(values.containsAll(Arrays.asList(1, 2)));
    }
}
