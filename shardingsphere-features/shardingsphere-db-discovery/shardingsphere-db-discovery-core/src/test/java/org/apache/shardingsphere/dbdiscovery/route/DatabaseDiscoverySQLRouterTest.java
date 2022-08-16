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

package org.apache.shardingsphere.dbdiscovery.route;

import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryHeartBeatConfiguration;
import org.apache.shardingsphere.dbdiscovery.rule.DatabaseDiscoveryRule;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.mode.PersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.route.SQLRouterFactory;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.session.ConnectionContext;
import org.apache.shardingsphere.schedule.core.ScheduleContextFactory;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class DatabaseDiscoverySQLRouterTest {
    
    private static final String DATA_SOURCE_NAME = "ds";
    
    private static final String NONE_DB_DISCOVERY_DATA_SOURCE_NAME = "noneDatabaseDiscoveryDataSource";
    
    private static final String PRIMARY_DATA_SOURCE = "primary";
    
    private DatabaseDiscoveryRule rule;
    
    @Mock
    private SQLStatementContext<SQLStatement> sqlStatementContext;
    
    private DatabaseDiscoverySQLRouter sqlRouter;
    
    @Before
    public void setUp() {
        ScheduleContextFactory.getInstance().init("foo_id", new ModeConfiguration("Cluster", mock(PersistRepositoryConfiguration.class), false));
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceConfig = new DatabaseDiscoveryDataSourceRuleConfiguration(
                DATA_SOURCE_NAME, Collections.singletonList(PRIMARY_DATA_SOURCE), "", "CORE.FIXTURE");
        AlgorithmConfiguration algorithmConfig = new AlgorithmConfiguration("CORE.FIXTURE", new Properties());
        DatabaseDiscoveryRuleConfiguration config = new DatabaseDiscoveryRuleConfiguration(Collections.singleton(dataSourceConfig),
                Collections.singletonMap("ha_heartbeat", new DatabaseDiscoveryHeartBeatConfiguration(new Properties())),
                Collections.singletonMap("CORE.FIXTURE", algorithmConfig));
        InstanceContext instanceContext = mock(InstanceContext.class, RETURNS_DEEP_STUBS);
        when(instanceContext.getInstance().getCurrentInstanceId()).thenReturn("foo_id");
        rule = new DatabaseDiscoveryRule(DATA_SOURCE_NAME, Collections.singletonMap(PRIMARY_DATA_SOURCE, new MockedDataSource()), config, instanceContext);
        sqlRouter = (DatabaseDiscoverySQLRouter) SQLRouterFactory.getInstances(Collections.singleton(rule)).get(rule);
    }
    
    @After
    public void tearDown() {
        ScheduleContextFactory.getInstance().getScheduleStrategy().remove("foo_id");
    }
    
    @Test
    public void assertCreateRouteContextToPrimaryWithoutRouteUnits() {
        LogicSQL logicSQL = new LogicSQL(mock(SQLStatementContext.class), "", Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME, mock(DatabaseType.class), mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS),
                new ShardingSphereRuleMetaData(Collections.singleton(rule)), Collections.emptyMap());
        RouteContext actual = sqlRouter.createRouteContext(logicSQL, database, rule, new ConfigurationProperties(new Properties()), new ConnectionContext());
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(PRIMARY_DATA_SOURCE));
    }
    
    @Test
    public void assertDecorateRouteContextToPrimaryDataSource() {
        RouteContext actual = mockRouteContext();
        LogicSQL logicSQL = new LogicSQL(mock(SQLStatementContext.class), "", Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME, mock(DatabaseType.class), mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS),
                new ShardingSphereRuleMetaData(Collections.singleton(rule)), Collections.emptyMap());
        sqlRouter.decorateRouteContext(actual, logicSQL, database, rule, new ConfigurationProperties(new Properties()), new ConnectionContext());
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(NONE_DB_DISCOVERY_DATA_SOURCE_NAME));
        assertThat(routedDataSourceNames.next(), is(PRIMARY_DATA_SOURCE));
    }
    
    @Test
    public void assertCreateRouteContextToPrimaryDataSourceWithLock() {
        LogicSQL logicSQL = new LogicSQL(sqlStatementContext, "", Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME, mock(DatabaseType.class), mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS),
                new ShardingSphereRuleMetaData(Collections.singleton(rule)), Collections.emptyMap());
        RouteContext actual = sqlRouter.createRouteContext(logicSQL, database, rule, new ConfigurationProperties(new Properties()), new ConnectionContext());
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(PRIMARY_DATA_SOURCE));
    }
    
    @Test
    public void assertDecorateRouteContextToPrimaryDataSourceWithLock() {
        RouteContext actual = mockRouteContext();
        LogicSQL logicSQL = new LogicSQL(sqlStatementContext, "", Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME, mock(DatabaseType.class), mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS),
                new ShardingSphereRuleMetaData(Collections.singleton(rule)), Collections.emptyMap());
        sqlRouter.decorateRouteContext(actual, logicSQL, database, rule, new ConfigurationProperties(new Properties()), new ConnectionContext());
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(NONE_DB_DISCOVERY_DATA_SOURCE_NAME));
        assertThat(routedDataSourceNames.next(), is(PRIMARY_DATA_SOURCE));
    }
    
    @Test
    public void assertCreateRouteContextToPrimaryDataSource() {
        LogicSQL logicSQL = new LogicSQL(sqlStatementContext, "", Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME, mock(DatabaseType.class), mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS),
                new ShardingSphereRuleMetaData(Collections.singleton(rule)), Collections.emptyMap());
        RouteContext actual = sqlRouter.createRouteContext(logicSQL, database, rule, new ConfigurationProperties(new Properties()), new ConnectionContext());
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(PRIMARY_DATA_SOURCE));
    }
    
    private RouteContext mockRouteContext() {
        RouteContext result = new RouteContext();
        RouteUnit routeUnit = new RouteUnit(new RouteMapper(DATA_SOURCE_NAME, DATA_SOURCE_NAME), Collections.singletonList(new RouteMapper("table", "table_0")));
        result.getRouteUnits().add(routeUnit);
        result.getRouteUnits().add(new RouteUnit(new RouteMapper(NONE_DB_DISCOVERY_DATA_SOURCE_NAME, NONE_DB_DISCOVERY_DATA_SOURCE_NAME), Collections.emptyList()));
        return result;
    }
}
