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
import org.apache.shardingsphere.infra.binder.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.RangeShardingConditionValue;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.timeservice.core.rule.TimestampServiceRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WhereClauseShardingConditionEngineTest {
    
    private WhereClauseShardingConditionEngine shardingConditionEngine;
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock
    private SelectStatementContext sqlStatementContext;
    
    @Mock
    private WhereSegment whereSegment;
    
    @Mock
    private TablesContext tablesContext;
    
    @BeforeEach
    void setUp() {
        shardingConditionEngine = new WhereClauseShardingConditionEngine(
                ShardingSphereDatabase.create("test_db", DatabaseTypeFactory.get("MySQL"), new ConfigurationProperties(new Properties())), shardingRule, mock(TimestampServiceRule.class));
        when(sqlStatementContext.getWhereSegments()).thenReturn(Collections.singleton(whereSegment));
        when(sqlStatementContext.getTablesContext()).thenReturn(tablesContext);
        when(sqlStatementContext.getDatabaseType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        when(tablesContext.findTableNamesByColumnSegment(anyCollection(), any())).thenReturn(Maps.of("foo_sharding_col", "table_1"));
    }
    
    @Test
    void assertCreateShardingConditionsForSelectRangeStatement() {
        int between = 1;
        int and = 100;
        ColumnSegment left = new ColumnSegment(0, 0, new IdentifierValue("foo_sharding_col"));
        ExpressionSegment betweenSegment = new LiteralExpressionSegment(0, 0, between);
        ExpressionSegment andSegment = new LiteralExpressionSegment(0, 0, and);
        BetweenExpression betweenExpression = new BetweenExpression(0, 0, left, betweenSegment, andSegment, false);
        when(whereSegment.getExpr()).thenReturn(betweenExpression);
        when(shardingRule.findShardingColumn(any(), any())).thenReturn(Optional.of("foo_sharding_col"));
        List<ShardingCondition> actual = shardingConditionEngine.createShardingConditions(sqlStatementContext, Collections.emptyList());
        assertThat(actual.get(0).getStartIndex(), is(0));
        assertTrue(actual.get(0).getValues().get(0) instanceof RangeShardingConditionValue);
    }
    
    @Test
    void assertCreateShardingConditionsForSelectInStatement() {
        ColumnSegment left = new ColumnSegment(0, 0, new IdentifierValue("foo_sharding_col"));
        ListExpression right = new ListExpression(0, 0);
        LiteralExpressionSegment literalExpressionSegment = new LiteralExpressionSegment(0, 0, 5);
        right.getItems().add(literalExpressionSegment);
        InExpression inExpression = new InExpression(0, 0, left, right, false);
        when(whereSegment.getExpr()).thenReturn(inExpression);
        when(shardingRule.findShardingColumn(any(), any())).thenReturn(Optional.of("foo_sharding_col"));
        List<ShardingCondition> actual = shardingConditionEngine.createShardingConditions(sqlStatementContext, Collections.emptyList());
        assertThat(actual.get(0).getStartIndex(), is(0));
        assertTrue(actual.get(0).getValues().get(0) instanceof ListShardingConditionValue);
    }
}
