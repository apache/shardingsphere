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

package org.apache.shardingsphere.sharding.route.engine.validator;

import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteStageContext;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.sharding.route.engine.ShardingRouteDecorator;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sharding.strategy.ShardingStrategy;
import org.apache.shardingsphere.sql.parser.binder.metadata.schema.SchemaMetaData;
import org.apache.shardingsphere.sql.parser.binder.segment.table.TablesContext;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.type.TableAvailable;
import org.apache.shardingsphere.sql.parser.binder.type.WhereAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

public final class ShardingRouteDecoratorTest {
    
    @Test
    public void assertDecorateCorrectly() {
        RouteContext routeContextDecorated = new ShardingRouteDecorator().decorate(makeAndPrepareRouteContext(makeAndPrepareSQLStatementContext()),
                makeAndPrepareShardingSphereMetaData(), makeAndPrepareShardingRule(), mock(ConfigurationProperties.class, Answers.RETURNS_DEEP_STUBS));
        assertThat(routeContextDecorated.getRouteResult().getRouteUnits().size(), is(1));
        Map<Class<? extends ShardingSphereRule>, RouteStageContext> routeStageContexts = routeContextDecorated.getRouteStageContexts();
        assertThat(routeStageContexts.size(), is(1));
        assertTrue(routeStageContexts.containsKey(ShardingRule.class));
    }
    
    private SQLStatementContext makeAndPrepareSQLStatementContext() {
        SQLStatementContext result = mock(SQLStatementContext.class, withSettings().extraInterfaces(TableAvailable.class, WhereAvailable.class));
        TablesContext tablesContext = mock(TablesContext.class);
        when(result.getTablesContext()).thenReturn(tablesContext);
        when(result.getSqlStatement()).thenReturn(mock(SelectStatement.class));
        when(((TableAvailable) result).getAllTables()).thenReturn(Lists.newArrayList(mock(SimpleTableSegment.class)));
        WhereSegment whereSegment = mock(WhereSegment.class, Answers.RETURNS_DEEP_STUBS);
        BetweenExpression betweenExpression = mock(BetweenExpression.class);
        ColumnSegment columnSegment = mock(ColumnSegment.class, Answers.RETURNS_DEEP_STUBS);
        when(columnSegment.getIdentifier().getValue()).thenReturn("columnName");
        when(betweenExpression.getLeft()).thenReturn(columnSegment);
        LiteralExpressionSegment betweenExpr = mock(LiteralExpressionSegment.class);
        when(betweenExpr.getLiterals()).thenReturn(mock(Comparable.class));
        when(betweenExpression.getBetweenExpr()).thenReturn(betweenExpr);
        when(betweenExpression.getAndExpr()).thenReturn(betweenExpr);
        when(whereSegment.getExpr()).thenReturn(betweenExpression);
        doReturn(Optional.of(whereSegment)).when((WhereAvailable) result).getWhere();
        when(tablesContext.getTableNames()).thenReturn(Lists.newArrayList("tableName"));
        doReturn(Optional.of("tableName")).when(tablesContext).findTableName(Mockito.any(ColumnSegment.class), Mockito.any(SchemaMetaData.class));
        return result;
    }
    
    private ShardingRule makeAndPrepareShardingRule() {
        ShardingRule shardingRule = mock(ShardingRule.class, Answers.RETURNS_DEEP_STUBS);
        doReturn(true).when(shardingRule).isShardingColumn(anyString(), anyString());
        prepareTableRules(shardingRule);
        ShardingStrategy shardingStrategy = mock(ShardingStrategy.class, Answers.RETURNS_DEEP_STUBS);
        when(shardingStrategy.getShardingColumns().contains(anyString())).thenReturn(true);
        doReturn(shardingStrategy).when(shardingRule).getDatabaseShardingStrategy(any(TableRule.class));
        return shardingRule;
    }
    
    private ShardingSphereMetaData makeAndPrepareShardingSphereMetaData() {
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, Answers.RETURNS_DEEP_STUBS);
        when(result.getRuleSchemaMetaData().getUnconfiguredSchemaMetaDataMap()).thenReturn(makeUnconfiguredSchemaMetaDataMap());
        return result;
    }
    
    @SneakyThrows
    private RouteContext makeAndPrepareRouteContext(final SQLStatementContext sqlStatementContext) {
        RouteContext routeContext = mock(RouteContext.class, Answers.RETURNS_DEEP_STUBS);
        Field field = routeContext.getClass().getDeclaredField("routeStageContexts");
        field.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(routeContext, new HashMap<>());
        when(routeContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        return routeContext;
    }
    
    private Map<String, Collection<String>> makeUnconfiguredSchemaMetaDataMap() {
        Map<String, Collection<String>> result = new HashMap<>();
        result.put("ignoredKey", Lists.newArrayList("tableName"));
        return result;
    }
    
    @SneakyThrows
    private void prepareTableRules(final ShardingRule shardingRule) {
        Field tableRules = shardingRule.getClass().getDeclaredField("tableRules");
        tableRules.setAccessible(true);
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(tableRules, tableRules.getModifiers() & ~Modifier.FINAL);
        tableRules.set(shardingRule, Lists.newArrayList(makeTableRule()));
    }
    
    private TableRule makeTableRule() {
        TableRule result = mock(TableRule.class);
        when(result.getLogicTable()).thenReturn("tableName");
        return result;
    }
    
    @SneakyThrows
    private <S, T> void assertFieldOfInstance(final S classInstance, final String fieldName, final Matcher<T> matcher) {
        Field field = classInstance.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        T value = (T) field.get(classInstance);
        assertThat(value, matcher);
    }
}
