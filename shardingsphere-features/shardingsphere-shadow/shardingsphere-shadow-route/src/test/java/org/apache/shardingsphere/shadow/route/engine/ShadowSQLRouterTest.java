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

package org.apache.shardingsphere.shadow.route.engine;

import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.segment.insert.values.InsertValueContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.route.SQLRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShadowSQLRouterTest {
    
    private static final String SHADOW_COLUMN = "is_shadow";
    
    private static final String ACTUAL_DATASOURCE = "ds";
    
    private static final String SHADOW_DATASOURCE = "shadow_ds";
    
    private ShadowSQLRouter sqlRouter;
    
    private ShadowRule rule;
    
    static {
        ShardingSphereServiceLoader.register(SQLRouter.class);
    }
    
    @Before
    public void setUp() {
        ShadowRuleConfiguration shadowRuleConfig = new ShadowRuleConfiguration(SHADOW_COLUMN, Collections.singletonList(ACTUAL_DATASOURCE), Collections.singletonList(SHADOW_DATASOURCE));
        rule = new ShadowRule(shadowRuleConfig);
        sqlRouter = (ShadowSQLRouter) OrderedSPIRegistry.getRegisteredServices(Collections.singleton(rule), SQLRouter.class).get(rule);
    }
    
    @Test
    public void assertCreateRouteContextToShadowDataSource() {
        LogicSQL logicSQL = new LogicSQL(mockSQLStatementContextForShadow(), "", Collections.emptyList());
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.singleton(rule));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData("logic_schema", mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS), ruleMetaData, mock(ShardingSphereSchema.class));
        RouteContext actual = sqlRouter.createRouteContext(logicSQL, metaData, rule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(SHADOW_DATASOURCE));
    }
    
    @Test
    public void assertCreateRouteContextToActualDataSource() {
        LogicSQL logicSQL = new LogicSQL(mockSQLStatementContext(), "", Collections.emptyList());
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.singleton(rule));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData("logic_schema", mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS), ruleMetaData, mock(ShardingSphereSchema.class));
        RouteContext actual = sqlRouter.createRouteContext(logicSQL, metaData, rule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(ACTUAL_DATASOURCE));
    }
    
    @Test
    public void assertCreateRouteContextForNonDMLStatement() {
        LogicSQL logicSQL = new LogicSQL(mockNonDMLSQLStatementContext(), "", Collections.emptyList());
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.singleton(rule));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData("logic_schema", mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS), ruleMetaData, mock(ShardingSphereSchema.class));
        RouteContext actual = sqlRouter.createRouteContext(logicSQL, metaData, rule, new ConfigurationProperties(new Properties()));
        assertThat(actual.getRouteUnits().size(), is(2));
        assertTrue(actual.getActualDataSourceNames().contains(SHADOW_DATASOURCE));
        assertTrue(actual.getActualDataSourceNames().contains(ACTUAL_DATASOURCE));
    }
    
    @Test
    public void assertDecorateRouteContextToShadowDataSource() {
        RouteContext actual = new RouteContext();
        actual.getRouteUnits().add(mockRouteUnit());
        LogicSQL logicSQL = new LogicSQL(mockSQLStatementContextForShadow(), "", Collections.emptyList());
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.singleton(rule));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(
                "logic_schema", mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS), ruleMetaData, mock(ShardingSphereSchema.class));
        sqlRouter.decorateRouteContext(actual, logicSQL, metaData, rule, new ConfigurationProperties(new Properties()));
        assertThat(actual.getRouteUnits().size(), is(1));
        assertTrue(actual.getActualDataSourceNames().contains(SHADOW_DATASOURCE));
    }
    
    @Test
    public void assertDecorateRouteContextToActualDataSource() {
        RouteContext actual = new RouteContext();
        actual.getRouteUnits().add(mockRouteUnit());
        LogicSQL logicSQL = new LogicSQL(mockSQLStatementContext(), "", Collections.emptyList());
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.singleton(rule));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData("logic_schema", mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS), ruleMetaData, mock(ShardingSphereSchema.class));
        sqlRouter.decorateRouteContext(actual, logicSQL, metaData, rule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(ACTUAL_DATASOURCE));
    }
    
    @Test
    public void assertDecorateRouteContextNonDMLStatement() {
        RouteContext actual = new RouteContext();
        actual.getRouteUnits().add(mockRouteUnit());
        LogicSQL logicSQL = new LogicSQL(mockNonDMLSQLStatementContext(), "", Collections.emptyList());
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.singleton(rule));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData("logic_schema", mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS), ruleMetaData, mock(ShardingSphereSchema.class));
        sqlRouter.decorateRouteContext(actual, logicSQL, metaData, rule, new ConfigurationProperties(new Properties()));
        assertThat(actual.getRouteUnits().size(), is(2));
        assertTrue(actual.getActualDataSourceNames().contains(SHADOW_DATASOURCE));
        assertTrue(actual.getActualDataSourceNames().contains(ACTUAL_DATASOURCE));
    }
    
    @Test
    public void assertDecorateRouteContextWithTableMapper() {
        RouteContext actual = new RouteContext();
        actual.getRouteUnits().add(mockRouteUnit());
        LogicSQL logicSQL = new LogicSQL(mockSQLStatementContextForShadow(), "", Collections.emptyList());
        ShardingSphereRuleMetaData ruleMetaData = new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.singleton(rule));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData("logic_schema", mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS), ruleMetaData, mock(ShardingSphereSchema.class));
        sqlRouter.decorateRouteContext(actual, logicSQL, metaData, rule, new ConfigurationProperties(new Properties()));
        assertThat(actual.getRouteUnits().size(), is(1));
        assertTrue(actual.getActualDataSourceNames().contains(SHADOW_DATASOURCE));
        Collection<RouteMapper> tableMappers = actual.getRouteUnits().iterator().next().getTableMappers();
        assertThat(tableMappers.size(), is(1));
        assertThat(tableMappers.iterator().next().getActualName(), is("table_0"));
        assertThat(tableMappers.iterator().next().getLogicName(), is("table"));
    }
    
    private InsertStatementContext mockSQLStatementContextForShadow() {
        InsertStatementContext result = mock(InsertStatementContext.class);
        when(result.getSqlStatement()).thenReturn(mock(MySQLInsertStatement.class));
        when(result.getDescendingColumnNames()).thenReturn(Collections.singletonList(SHADOW_COLUMN).iterator());
        when(result.getColumnNames()).thenReturn(Collections.singletonList(SHADOW_COLUMN));
        InsertValueContext insertValueContext = mock(InsertValueContext.class);
        when(insertValueContext.getValue(0)).thenReturn(true);
        when(result.getInsertValueContexts()).thenReturn(Collections.singletonList(insertValueContext));
        return result;
    }
    
    private InsertStatementContext mockSQLStatementContext() {
        InsertStatementContext result = mock(InsertStatementContext.class);
        when(result.getSqlStatement()).thenReturn(mock(MySQLInsertStatement.class));
        return result;
    }
    
    private CreateTableStatementContext mockNonDMLSQLStatementContext() {
        CreateTableStatementContext result = mock(CreateTableStatementContext.class);
        when(result.getSqlStatement()).thenReturn(mock(MySQLCreateTableStatement.class));
        return result;
    }
    
    private RouteUnit mockRouteUnit() {
        return new RouteUnit(new RouteMapper(ACTUAL_DATASOURCE, ACTUAL_DATASOURCE), Collections.singletonList(new RouteMapper("table", "table_0")));
    }
}
