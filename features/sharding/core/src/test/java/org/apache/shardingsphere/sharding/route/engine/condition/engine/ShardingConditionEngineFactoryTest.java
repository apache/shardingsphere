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

import org.apache.groovy.util.Maps;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.engine.impl.DefaultShardingConditionEngine;
import org.apache.shardingsphere.sharding.route.engine.condition.value.RangeShardingConditionValue;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingConditionEngineFactoryTest {
    
    @Mock
    private QueryContext queryContext;
    
    @Mock
    private ShardingRule shardingRule;
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void assertCreateInsertClauseShardingConditionEngine() {
        ShardingSphereDatabase database = ShardingSphereDatabase.create("test_db", DatabaseTypeEngine.getDatabaseType("MySQL"));
        InsertStatementContext insertStatementContext = mock(InsertStatementContext.class);
        InsertStatement insertStatement = mock(InsertStatement.class);
        InsertValueContext insertValueContext = new InsertValueContext(Collections.singletonList(new LiteralExpressionSegment(0, 10, "1")), Collections.emptyList(), 0);
        when(insertStatement.getTable()).thenReturn(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("foo_table"))));
        when(insertStatementContext.getSqlStatement()).thenReturn(insertStatement);
        when(insertStatementContext.getColumnNames()).thenReturn(Collections.singletonList("foo_col"));
        when(insertStatementContext.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        when(insertStatementContext.getGeneratedKeyContext()).thenReturn(Optional.empty());
        ShardingConditionEngine engine = ShardingConditionEngineFactory.createShardingConditionEngine(database, shardingRule);
        assertThat(engine, instanceOf(DefaultShardingConditionEngine.class));
        List<ShardingCondition> shardingConditions = engine.createShardingConditions(insertStatementContext, Collections.emptyList());
        assertThat(shardingConditions.get(0).getStartIndex(), is(0));
        assertTrue(shardingConditions.get(0).getValues().isEmpty());
    }
    
    @Test
    public void assertCreateWhereClauseShardingConditionEngine() {
        ShardingSphereDatabase database = ShardingSphereDatabase.create("test_db", DatabaseTypeEngine.getDatabaseType("MySQL"));
        SelectStatementContext sqlStatementContext = mock(SelectStatementContext.class);
        WhereSegment whereSegment = mock(WhereSegment.class);
        TablesContext tablesContext = mock(TablesContext.class);
        int betweenStart = 1;
        int betweenEnd = 100;
        ColumnSegment left = new ColumnSegment(0, 0, new IdentifierValue("foo_sharding_col"));
        ExpressionSegment betweenSegment = new LiteralExpressionSegment(0, 0, betweenStart);
        ExpressionSegment andSegment = new LiteralExpressionSegment(0, 0, betweenEnd);
        BetweenExpression betweenExpression = new BetweenExpression(0, 0, left, betweenSegment, andSegment, false);
        when(whereSegment.getExpr()).thenReturn(betweenExpression);
        when(shardingRule.findShardingColumn(any(), any())).thenReturn(Optional.of("foo_sharding_col"));
        when(sqlStatementContext.getWhereSegments()).thenReturn(Collections.singleton(whereSegment));
        when(sqlStatementContext.getTablesContext()).thenReturn(tablesContext);
        when(tablesContext.findTableNamesByColumnSegment(anyCollection(), any())).thenReturn(Maps.of("foo_sharding_col", "table_1"));
        ShardingConditionEngine engine = ShardingConditionEngineFactory.createShardingConditionEngine(database, shardingRule);
        assertThat(engine, instanceOf(DefaultShardingConditionEngine.class));
        List<ShardingCondition> shardingConditions = engine.createShardingConditions(sqlStatementContext, Collections.emptyList());
        assertThat(shardingConditions.get(0).getStartIndex(), is(0));
        assertTrue(shardingConditions.get(0).getValues().get(0) instanceof RangeShardingConditionValue);
    }
}
