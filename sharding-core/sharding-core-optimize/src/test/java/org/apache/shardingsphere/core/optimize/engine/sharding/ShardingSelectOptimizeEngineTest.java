/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.optimize.engine.sharding;

import com.google.common.collect.Range;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.optimize.engine.sharding.dml.ShardingSelectOptimizeEngine;
import org.apache.shardingsphere.core.optimize.statement.dml.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.statement.dml.condition.ShardingConditions;
import org.apache.shardingsphere.core.parse.sql.context.table.Table;
import org.apache.shardingsphere.core.parse.sql.segment.common.TableSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.OrPredicateSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateBetweenRightValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateInRightValue;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.strategy.route.value.ListRouteValue;
import org.apache.shardingsphere.core.strategy.route.value.RangeRouteValue;
import org.apache.shardingsphere.core.strategy.route.value.RouteValue;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingSelectOptimizeEngineTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock
    private ShardingTableMetaData shardingTableMetaData;
    
    private SelectStatement selectStatement;
    
    @Before
    public void setUp() {
        when(shardingRule.isShardingColumn("column", "tbl")).thenReturn(true);
        selectStatement = new SelectStatement();
        selectStatement.getTables().add(new Table("tbl", null));
    }
    
    @Test
    public void assertOptimizeAlwaysFalseListConditions() {
        OrPredicateSegment orPredicateSegment = new OrPredicateSegment();
        AndPredicate andPredicate = new AndPredicate();
        andPredicate.getPredicates().add(new PredicateSegment(0, 0, createColumnSegment(), 
                new PredicateInRightValue(Arrays.<ExpressionSegment>asList(new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, 2)))));
        andPredicate.getPredicates().add(new PredicateSegment(0, 0, createColumnSegment(), new PredicateCompareRightValue("=", new LiteralExpressionSegment(0, 0, 3))));
        orPredicateSegment.getAndPredicates().add(andPredicate);
        selectStatement.getSQLSegments().add(orPredicateSegment);
        ShardingConditions shardingConditions = new ShardingSelectOptimizeEngine(shardingRule, shardingTableMetaData, selectStatement, Collections.emptyList()).optimize().getShardingConditions();
        assertTrue(shardingConditions.isAlwaysFalse());
    }
    
    @Test
    public void assertOptimizeAlwaysFalseRangeConditions() {
        OrPredicateSegment orPredicateSegment = new OrPredicateSegment();
        AndPredicate andPredicate = new AndPredicate();
        andPredicate.getPredicates().add(new PredicateSegment(0, 0, createColumnSegment(),
                new PredicateInRightValue(Arrays.<ExpressionSegment>asList(new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, 2)))));
        andPredicate.getPredicates().add(new PredicateSegment(0, 0, createColumnSegment(),
                new PredicateInRightValue(Arrays.<ExpressionSegment>asList(new LiteralExpressionSegment(0, 0, 3), new LiteralExpressionSegment(0, 0, 4)))));
        orPredicateSegment.getAndPredicates().add(andPredicate);
        selectStatement.getSQLSegments().add(orPredicateSegment);
        ShardingConditions shardingConditions = new ShardingSelectOptimizeEngine(shardingRule, shardingTableMetaData, selectStatement, Collections.emptyList()).optimize().getShardingConditions();
        assertTrue(shardingConditions.isAlwaysFalse());
    }
    
    @Test
    public void assertOptimizeAlwaysFalseListConditionsAndRangeConditions() {
        OrPredicateSegment orPredicateSegment = new OrPredicateSegment();
        AndPredicate andPredicate = new AndPredicate();
        andPredicate.getPredicates().add(new PredicateSegment(0, 0, createColumnSegment(),
                new PredicateInRightValue(Arrays.<ExpressionSegment>asList(new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, 2)))));
        andPredicate.getPredicates().add(new PredicateSegment(0, 0, createColumnSegment(),
                new PredicateBetweenRightValue(new LiteralExpressionSegment(0, 0, 3), new LiteralExpressionSegment(0, 0, 4))));
        orPredicateSegment.getAndPredicates().add(andPredicate);
        selectStatement.getSQLSegments().add(orPredicateSegment);
        ShardingConditions shardingConditions = new ShardingSelectOptimizeEngine(shardingRule, shardingTableMetaData, selectStatement, Collections.emptyList()).optimize().getShardingConditions();
        assertTrue(shardingConditions.isAlwaysFalse());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertOptimizeListConditions() {
        OrPredicateSegment orPredicateSegment = new OrPredicateSegment();
        AndPredicate andPredicate = new AndPredicate();
        andPredicate.getPredicates().add(new PredicateSegment(0, 0, createColumnSegment(),
                new PredicateInRightValue(Arrays.<ExpressionSegment>asList(new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, 2)))));
        andPredicate.getPredicates().add(new PredicateSegment(0, 0, createColumnSegment(), new PredicateCompareRightValue("=", new LiteralExpressionSegment(0, 0, 1))));
        orPredicateSegment.getAndPredicates().add(andPredicate);
        selectStatement.getSQLSegments().add(orPredicateSegment);
        ShardingConditions shardingConditions = new ShardingSelectOptimizeEngine(shardingRule, shardingTableMetaData, selectStatement, Collections.emptyList()).optimize().getShardingConditions();
        assertFalse(shardingConditions.isAlwaysFalse());
        ShardingCondition shardingCondition = shardingConditions.getShardingConditions().get(0);
        RouteValue shardingValue = shardingCondition.getRouteValues().get(0);
        Collection<Comparable<?>> values = ((ListRouteValue<Comparable<?>>) shardingValue).getValues();
        assertThat(values.size(), is(1));
        assertTrue(values.containsAll(Collections.singleton(1)));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertOptimizeRangeConditions() {
        OrPredicateSegment orPredicateSegment = new OrPredicateSegment();
        AndPredicate andPredicate = new AndPredicate();
        andPredicate.getPredicates().add(new PredicateSegment(0, 0, createColumnSegment(),
                new PredicateBetweenRightValue(new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, 2))));
        andPredicate.getPredicates().add(new PredicateSegment(0, 0, createColumnSegment(),
                new PredicateBetweenRightValue(new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, 3))));
        orPredicateSegment.getAndPredicates().add(andPredicate);
        selectStatement.getSQLSegments().add(orPredicateSegment);
        ShardingConditions shardingConditions = new ShardingSelectOptimizeEngine(shardingRule, shardingTableMetaData, selectStatement, Collections.emptyList()).optimize().getShardingConditions();
        assertFalse(shardingConditions.isAlwaysFalse());
        ShardingCondition shardingCondition = shardingConditions.getShardingConditions().get(0);
        RouteValue shardingValue = shardingCondition.getRouteValues().get(0);
        Range<Comparable<?>> values = ((RangeRouteValue<Comparable<?>>) shardingValue).getValueRange();
        assertThat(values.lowerEndpoint(), CoreMatchers.<Comparable>is(1));
        assertThat(values.upperEndpoint(), CoreMatchers.<Comparable>is(2));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertOptimizeListConditionsAndRangeConditions() {
        OrPredicateSegment orPredicateSegment = new OrPredicateSegment();
        AndPredicate andPredicate = new AndPredicate();
        andPredicate.getPredicates().add(new PredicateSegment(0, 0, createColumnSegment(),
                new PredicateInRightValue(Arrays.<ExpressionSegment>asList(new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, 2)))));
        andPredicate.getPredicates().add(new PredicateSegment(0, 0, createColumnSegment(),
                new PredicateBetweenRightValue(new LiteralExpressionSegment(0, 0, 1), new LiteralExpressionSegment(0, 0, 2))));
        orPredicateSegment.getAndPredicates().add(andPredicate);
        selectStatement.getSQLSegments().add(orPredicateSegment);
        ShardingConditions shardingConditions = new ShardingSelectOptimizeEngine(shardingRule, shardingTableMetaData, selectStatement, Collections.emptyList()).optimize().getShardingConditions();
        assertFalse(shardingConditions.isAlwaysFalse());
        ShardingCondition shardingCondition = shardingConditions.getShardingConditions().get(0);
        RouteValue shardingValue = shardingCondition.getRouteValues().get(0);
        Collection<Comparable<?>> values = ((ListRouteValue<Comparable<?>>) shardingValue).getValues();
        assertThat(values.size(), is(2));
        assertTrue(values.containsAll(Arrays.asList(1, 2)));
    }
    
    private ColumnSegment createColumnSegment() {
        ColumnSegment result = new ColumnSegment(0, 0, "column");
        result.setOwner(new TableSegment(0, 0, "tbl"));
        return result;
    }
}
