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

import org.apache.shardingsphere.infra.binder.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertSelectContext;
import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.impl.InsertClauseShardingConditionEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class InsertClauseShardingConditionEngineTest {
    
    private final InsertClauseShardingConditionEngine insertClauseShardingConditionEngine = new InsertClauseShardingConditionEngine(mock(ShardingRule.class), mock(ShardingSphereSchema.class));
    
    @Mock
    private InsertStatementContext insertStatementContext;
    
    @Before
    public void setUp() {
        InsertStatement insertStatement = mockInsertStatement();
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
        return new InsertValueContext(Collections.singleton(new LiteralExpressionSegment(0, 10, "1")), Collections.emptyList(), 0);
    }
    
    @Test
    public void assertCreateShardingConditionsInsertStatement() {
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.empty());
        List<ShardingCondition> shardingConditions = insertClauseShardingConditionEngine.createShardingConditions(insertStatementContext, Collections.emptyList());
        assertThat(shardingConditions.get(0).getStartIndex(), is(0));
        assertTrue(shardingConditions.get(0).getValues().isEmpty());
    }
    
    @Test
    public void assertCreateShardingConditionsInsertStatementWithGeneratedKeyContext() {
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.of(mock(GeneratedKeyContext.class)));
        List<ShardingCondition> shardingConditions = insertClauseShardingConditionEngine.createShardingConditions(insertStatementContext, Collections.emptyList());
        assertThat(shardingConditions.get(0).getStartIndex(), is(0));
        assertTrue(shardingConditions.get(0).getValues().isEmpty());
    }
    
    @Test
    public void assertCreateShardingConditionsInsertStatementWithGeneratedKeyContextAndTableRule() {
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.of(mock(GeneratedKeyContext.class)));
        List<ShardingCondition> shardingConditions = insertClauseShardingConditionEngine.createShardingConditions(insertStatementContext, Collections.emptyList());
        assertThat(shardingConditions.get(0).getStartIndex(), is(0));
        assertTrue(shardingConditions.get(0).getValues().isEmpty());
    }
    
    @Test
    public void assertCreateShardingConditionsSelectStatement() {
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.empty());
        when(insertStatementContext.getInsertSelectContext()).thenReturn(mock(InsertSelectContext.class));
        assertTrue(insertClauseShardingConditionEngine.createShardingConditions(insertStatementContext, Collections.emptyList()).isEmpty());
    }
    
    @Test
    public void assertCreateShardingConditionsSelectStatementWithGeneratedKeyContext() {
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.of(mock(GeneratedKeyContext.class)));
        when(insertStatementContext.getInsertSelectContext()).thenReturn(mock(InsertSelectContext.class));
        assertTrue(insertClauseShardingConditionEngine.createShardingConditions(insertStatementContext, Collections.emptyList()).isEmpty());
    }
}
