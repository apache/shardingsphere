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

package org.apache.shardingsphere.ha.route.engine;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.model.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.SQLRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.ha.api.config.HARuleConfiguration;
import org.apache.shardingsphere.ha.api.config.rule.HADataSourceRuleConfiguration;
import org.apache.shardingsphere.ha.route.engine.impl.PrimaryVisitedManager;
import org.apache.shardingsphere.ha.rule.HARule;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.LockSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class HASQLRouterTest {
    
    private static final String DATASOURCE_NAME = "ds";
    
    private static final String NONE_REPLICA_QUERY_DATASOURCE_NAME = "noneHADatasource";
    
    private static final String PRIMARY_DATASOURCE = "primary";
    
    private static final String REPLICA_DATASOURCE = "query";
    
    private HARule rule;
    
    @Mock
    private SQLStatementContext<SQLStatement> sqlStatementContext;
    
    private HASQLRouter sqlRouter;
    
    static {
        ShardingSphereServiceLoader.register(SQLRouter.class);
    }
    
    @Before
    public void setUp() {
        rule = new HARule(new HARuleConfiguration(Collections.singleton(
                new HADataSourceRuleConfiguration(DATASOURCE_NAME, PRIMARY_DATASOURCE, Collections.singletonList(REPLICA_DATASOURCE), null)), Collections.emptyMap()));
        sqlRouter = (HASQLRouter) OrderedSPIRegistry.getRegisteredServices(Collections.singleton(rule), SQLRouter.class).get(rule);
    }
    
    @After
    public void tearDown() {
        PrimaryVisitedManager.clear();
    }
    
    @Test
    public void assertCreateRouteContextToPrimaryWithoutRouteUnits() {
        LogicSQL logicSQL = new LogicSQL(mock(SQLStatementContext.class), "", Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("logic_schema", Collections.emptyList(), Collections.singleton(rule), Collections.emptyMap(), mock(ShardingSphereMetaData.class));
        RouteContext actual = sqlRouter.createRouteContext(logicSQL, schema, rule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(PRIMARY_DATASOURCE));
    }
    
    @Test
    public void assertDecorateRouteContextToPrimaryDataSource() {
        RouteContext actual = mockRouteContext();
        LogicSQL logicSQL = new LogicSQL(mock(SQLStatementContext.class), "", Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("logic_schema", Collections.emptyList(), Collections.singleton(rule), Collections.emptyMap(), mock(ShardingSphereMetaData.class));
        sqlRouter.decorateRouteContext(actual, logicSQL, schema, rule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(NONE_REPLICA_QUERY_DATASOURCE_NAME));
        assertThat(routedDataSourceNames.next(), is(PRIMARY_DATASOURCE));
    }
    
    @Test
    public void assertCreateRouteContextToReplicaDataSource() {
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        when(selectStatement.getLock()).thenReturn(Optional.empty());
        LogicSQL logicSQL = new LogicSQL(sqlStatementContext, "", Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("logic_schema", Collections.emptyList(), Collections.singleton(rule), Collections.emptyMap(), mock(ShardingSphereMetaData.class));
        RouteContext actual = sqlRouter.createRouteContext(logicSQL, schema, rule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(REPLICA_DATASOURCE));
    }
    
    @Test
    public void assertDecorateRouteContextToReplicaDataSource() {
        RouteContext actual = mockRouteContext();
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        when(selectStatement.getLock()).thenReturn(Optional.empty());
        LogicSQL logicSQL = new LogicSQL(sqlStatementContext, "", Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("logic_schema", Collections.emptyList(), Collections.singleton(rule), Collections.emptyMap(), mock(ShardingSphereMetaData.class));
        sqlRouter.decorateRouteContext(actual, logicSQL, schema, rule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(NONE_REPLICA_QUERY_DATASOURCE_NAME));
        assertThat(routedDataSourceNames.next(), is(REPLICA_DATASOURCE));
    }
    
    @Test
    public void assertCreateRouteContextToPrimaryDataSourceWithLock() {
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        when(selectStatement.getLock()).thenReturn(Optional.of(mock(LockSegment.class)));
        LogicSQL logicSQL = new LogicSQL(sqlStatementContext, "", Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("logic_schema", Collections.emptyList(), Collections.singleton(rule), Collections.emptyMap(), mock(ShardingSphereMetaData.class));
        RouteContext actual = sqlRouter.createRouteContext(logicSQL, schema, rule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(PRIMARY_DATASOURCE));
    }
    
    @Test
    public void assertDecorateRouteContextToPrimaryDataSourceWithLock() {
        RouteContext actual = mockRouteContext();
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(selectStatement);
        when(selectStatement.getLock()).thenReturn(Optional.of(mock(LockSegment.class)));
        LogicSQL logicSQL = new LogicSQL(sqlStatementContext, "", Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("logic_schema", Collections.emptyList(), Collections.singleton(rule), Collections.emptyMap(), mock(ShardingSphereMetaData.class));
        sqlRouter.decorateRouteContext(actual, logicSQL, schema, rule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(NONE_REPLICA_QUERY_DATASOURCE_NAME));
        assertThat(routedDataSourceNames.next(), is(PRIMARY_DATASOURCE));
    }
    
    @Test
    public void assertCreateRouteContextToPrimaryDataSource() {
        when(sqlStatementContext.getSqlStatement()).thenReturn(mock(InsertStatement.class));
        LogicSQL logicSQL = new LogicSQL(sqlStatementContext, "", Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("logic_schema", Collections.emptyList(), Collections.singleton(rule), Collections.emptyMap(), mock(ShardingSphereMetaData.class));
        RouteContext actual = sqlRouter.createRouteContext(logicSQL, schema, rule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(PRIMARY_DATASOURCE));
    }
    
    private RouteContext mockRouteContext() {
        RouteContext result = new RouteContext();
        RouteUnit routeUnit = new RouteUnit(new RouteMapper(DATASOURCE_NAME, DATASOURCE_NAME), Collections.singletonList(new RouteMapper("table", "table_0")));
        result.getRouteUnits().add(routeUnit);
        result.getRouteUnits().add(new RouteUnit(new RouteMapper(NONE_REPLICA_QUERY_DATASOURCE_NAME, NONE_REPLICA_QUERY_DATASOURCE_NAME), Collections.emptyList()));
        return result;
    }
}
