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

package org.apache.shardingsphere.sharding.route.engine.condition.engine;

import org.apache.shardingsphere.dialect.exception.data.InsertColumnsAndValuesMismatchedException;
import org.apache.shardingsphere.infra.binder.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertSelectContext;
import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.impl.InsertClauseShardingConditionEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class InsertClauseShardingConditionEngineTest {
    
    private InsertClauseShardingConditionEngine shardingConditionEngine;
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock
    private InsertStatementContext insertStatementContext;
    
    @Before
    public void setUp() {
        InsertStatement insertStatement = mockInsertStatement();
        shardingConditionEngine = new InsertClauseShardingConditionEngine(shardingRule, mock(ShardingSphereDatabase.class));
        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatementContext.getColumnNames()).thenReturn(Collections.singletonList("foo_col"));
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(createInsertValueContext()));
    }
    
    private InsertStatement mockInsertStatement() {
        InsertStatement result = mock(InsertStatement.class);
        when(result.getTable()).thenReturn(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_table"))));
        return result;
    }
    
    private InsertValueContext createInsertValueContext() {
        return new InsertValueContext(Collections.singletonList(new LiteralExpressionSegment(0, 10, "1")), Collections.emptyList(), 0);
    }
    
    private InsertValueContext createInsertValueContextAsCommonExpressionSegmentEmptyText() {
        return new InsertValueContext(Collections.singletonList(new CommonExpressionSegment(0, 10, "null")), Collections.emptyList(), 0);
    }
    
    private InsertValueContext createInsertValueContextAsCommonExpressionSegmentWithNow() {
        return new InsertValueContext(Collections.singletonList(new CommonExpressionSegment(0, 10, "now()")), Collections.emptyList(), 0);
    }
    
    @Test
    public void assertCreateShardingConditionsInsertStatement() {
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.empty());
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.emptyList());
        assertThat(shardingConditions.get(0).getStartIndex(), is(0));
        assertTrue(shardingConditions.get(0).getValues().isEmpty());
    }
    
    @Test
    public void assertCreateShardingConditionsInsertStatementWithGeneratedKeyContextUsingCommonExpressionSegmentEmpty() {
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(createInsertValueContextAsCommonExpressionSegmentEmptyText()));
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.of(mock(GeneratedKeyContext.class)));
        when(shardingRule.findShardingColumn(any(), any())).thenReturn(Optional.of("foo_sharding_col"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.emptyList());
        assertThat(shardingConditions.get(0).getStartIndex(), is(0));
        assertThat(shardingConditions.get(0).getValues().size(), is(1));
    }
    
    @Test(expected = InsertColumnsAndValuesMismatchedException.class)
    public void assertCreateShardingConditionsInsertStatementWithMismatchColumns() {
        InsertValueContext insertValueContext = new InsertValueContext(Arrays.asList(new LiteralExpressionSegment(0, 10, "1"), new LiteralExpressionSegment(0, 10, "1")), Collections.emptyList(), 0);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(shardingRule.findShardingColumn(any(), any())).thenReturn(Optional.of("foo_sharding_col"));
        when(insertStatementContext.getColumnNames()).thenReturn(Collections.singletonList("foo_col1"));
        shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.emptyList());
    }
    
    @Test
    public void assertCreateShardingConditionsInsertStatementWithGeneratedKeyContextUsingCommonExpressionSegmentNow() {
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(createInsertValueContextAsCommonExpressionSegmentWithNow()));
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.of(mock(GeneratedKeyContext.class)));
        when(shardingRule.findShardingColumn(any(), any())).thenReturn(Optional.of("foo_sharding_col"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.emptyList());
        assertThat(shardingConditions.get(0).getStartIndex(), is(0));
        assertFalse(shardingConditions.get(0).getValues().isEmpty());
    }
    
    @Test
    public void assertCreateShardingConditionsInsertStatementWithGeneratedKeyContext() {
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.of(mock(GeneratedKeyContext.class)));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.emptyList());
        assertThat(shardingConditions.get(0).getStartIndex(), is(0));
        assertTrue(shardingConditions.get(0).getValues().isEmpty());
    }
    
    @Test
    public void assertCreateShardingConditionsInsertStatementWithGeneratedKeyContextAndTableRule() {
        GeneratedKeyContext generatedKeyContext = mock(GeneratedKeyContext.class);
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.of(generatedKeyContext));
        when(generatedKeyContext.isGenerated()).thenReturn(true);
        when(generatedKeyContext.getGeneratedValues()).thenReturn(Collections.singletonList("foo_col1"));
        when(shardingRule.findTableRule(eq("foo_table"))).thenReturn(Optional.of(new TableRule(Collections.singletonList("foo_col"), "test")));
        when(shardingRule.findShardingColumn(any(), any())).thenReturn(Optional.of("foo_sharding_col"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.emptyList());
        assertThat(shardingConditions.get(0).getStartIndex(), is(0));
        assertFalse(shardingConditions.get(0).getValues().isEmpty());
    }
    
    @Test
    public void assertCreateShardingConditionsWithParameterMarkers() {
        InsertValueContext insertValueContext = new InsertValueContext(Collections.singletonList(new ParameterMarkerExpressionSegment(0, 0, 0)), Collections.singletonList(1), 0);
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(shardingRule.findShardingColumn(any(), any())).thenReturn(Optional.of("foo_sharding_col"));
        List<ShardingCondition> shardingConditions = shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.singletonList(1));
        assertThat(shardingConditions.size(), is(1));
        assertThat(shardingConditions.get(0).getValues().size(), is(1));
        assertThat(shardingConditions.get(0).getValues().get(0).getParameterMarkerIndexes(), is(Collections.singletonList(0)));
    }
    
    @Test
    public void assertCreateShardingConditionsSelectStatement() {
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.empty());
        when(insertStatementContext.getInsertSelectContext()).thenReturn(mock(InsertSelectContext.class));
        assertTrue(shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.emptyList()).isEmpty());
    }
    
    @Test
    public void assertCreateShardingConditionsSelectStatementWithGeneratedKeyContext() {
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.of(mock(GeneratedKeyContext.class)));
        when(insertStatementContext.getInsertSelectContext()).thenReturn(mock(InsertSelectContext.class));
        assertTrue(shardingConditionEngine.createShardingConditions(insertStatementContext, Collections.emptyList()).isEmpty());
    }
}
