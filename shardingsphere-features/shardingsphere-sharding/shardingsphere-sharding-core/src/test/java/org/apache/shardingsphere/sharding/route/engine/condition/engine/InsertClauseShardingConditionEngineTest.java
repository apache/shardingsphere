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
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.impl.InsertClauseShardingConditionEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class InsertClauseShardingConditionEngineTest {

    private InsertClauseShardingConditionEngine insertClauseShardingConditionEngine;

    private InsertStatementContext insertStatementContext;

    private ShardingRule shardingRule;

    @Before
    public void setUp() {
        insertStatementContext = mock(InsertStatementContext.class);
        shardingRule = mock(ShardingRule.class);
        InsertStatement insertStatement = mock(InsertStatement.class);
        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatementContext.getColumnNames()).thenReturn(Arrays.asList(new String[]{"columnName"}));
        SimpleTableSegment simpleTableSegment = mock(SimpleTableSegment.class);
        when(insertStatement.getTable()).thenReturn(simpleTableSegment);
        TableNameSegment tableNameSegment = mock(TableNameSegment.class);
        when(simpleTableSegment.getTableName()).thenReturn(tableNameSegment);
        IdentifierValue identifierValue = mock(IdentifierValue.class);
        when(tableNameSegment.getIdentifier()).thenReturn(identifierValue);
        when(identifierValue.getValue()).thenReturn("example");
        List<ExpressionSegment> valueExpressions = new ArrayList<>();
        valueExpressions.add(new LiteralExpressionSegment(0, 10, "1"));
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Arrays.asList(new InsertValueContext[]{new InsertValueContext(valueExpressions, new ArrayList<>(), 0)}));
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.of(new GeneratedKeyContext("columnNameTwo", true)));
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        insertClauseShardingConditionEngine = new InsertClauseShardingConditionEngine(shardingRule, metaData.getSchema());
    }

    @Test
    public void assertCreateShardingConditionsInsertStatement() {
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.empty());
        List<ShardingCondition> shardingConditionList = insertClauseShardingConditionEngine.createShardingConditions(insertStatementContext, new ArrayList<>());
        Assert.assertFalse(shardingConditionList.isEmpty());
        Assert.assertThat(shardingConditionList.get(0).getStartIndex(), is(0));
        Assert.assertTrue(shardingConditionList.get(0).getValues().isEmpty());
    }

    @Test
    public void assertCreateShardingConditionsInsertStatementWithGeneratedKeyContext() {
        List<ShardingCondition> shardingConditionList = insertClauseShardingConditionEngine.createShardingConditions(insertStatementContext, new ArrayList<>());
        Assert.assertFalse(shardingConditionList.isEmpty());
        Assert.assertThat(shardingConditionList.get(0).getStartIndex(), is(0));
        Assert.assertTrue(shardingConditionList.get(0).getValues().isEmpty());
    }

    @Test
    public void assertCreateShardingConditionsInsertStatementWithGeneratedKeyContextAndTableRule() {
        when(shardingRule.findTableRule(eq("example"))).thenReturn(Optional.of(new TableRule(new ArrayList<>(), "logicTableName")));
        List<ShardingCondition> shardingConditionList = insertClauseShardingConditionEngine.createShardingConditions(insertStatementContext, new ArrayList<>());
        Assert.assertFalse(shardingConditionList.isEmpty());
        Assert.assertThat(shardingConditionList.get(0).getStartIndex(), is(0));
        Assert.assertTrue(shardingConditionList.get(0).getValues().isEmpty());
    }

    @Test
    public void assertCreateShardingConditionsSelectStatement() {
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.empty());
        InsertSelectContext insertStatement = mock(InsertSelectContext.class);
        when(insertStatementContext.getInsertSelectContext()).thenReturn(insertStatement);
        List<ShardingCondition> shardingConditionList = insertClauseShardingConditionEngine.createShardingConditions(insertStatementContext, new ArrayList<>());
        Assert.assertTrue(shardingConditionList.isEmpty());
    }

    @Test
    public void assertCreateShardingConditionsSelectStatementWithGeneratedKeyContext() {
        InsertSelectContext insertStatement = mock(InsertSelectContext.class);
        when(insertStatementContext.getInsertSelectContext()).thenReturn(insertStatement);
        List<ShardingCondition> shardingConditionList = insertClauseShardingConditionEngine.createShardingConditions(insertStatementContext, new ArrayList<>());
        Assert.assertTrue(shardingConditionList.isEmpty());
    }

}
