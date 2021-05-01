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

package org.apache.shardingsphere.dbdiscovery.route.engine;

import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.common.rule.DatabaseDiscoveryRule;
import org.apache.shardingsphere.dbdiscovery.route.DatabaseDiscoverySQLRouter;
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.SQLRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.Iterator;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public final class DatabaseDiscoverySQLRouterTest {
    
    private static final String DATASOURCE_NAME = "ds";
    
    private static final String NONE_DB_DISCOVERY_DATASOURCE_NAME = "noneDatabaseDiscoveryDatasource";
    
    private static final String PRIMARY_DATASOURCE = "primary";
    
    private DatabaseDiscoveryRule rule;
    
    @Mock
    private SQLStatementContext<SQLStatement> sqlStatementContext;
    
    private DatabaseDiscoverySQLRouter sqlRouter;
    
    static {
        ShardingSphereServiceLoader.register(SQLRouter.class);
    }
    
    @Before
    public void setUp() {
        DatabaseDiscoveryDataSourceRuleConfiguration databaseDiscoveryDataSourceRuleConfiguration 
                = new DatabaseDiscoveryDataSourceRuleConfiguration(DATASOURCE_NAME, Collections.singletonList(PRIMARY_DATASOURCE), "discoveryTypeName");
        DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfiguration 
                = new DatabaseDiscoveryRuleConfiguration(Collections.singleton(databaseDiscoveryDataSourceRuleConfiguration), Collections.emptyMap());
        rule = new DatabaseDiscoveryRule(databaseDiscoveryRuleConfiguration, mock(DatabaseType.class), Collections.singletonMap("ds", mock(DataSource.class)), "ha_db");
        sqlRouter = (DatabaseDiscoverySQLRouter) OrderedSPIRegistry.getRegisteredServices(Collections.singleton(rule), SQLRouter.class).get(rule);
    }
    
    @Test
    public void assertCreateRouteContextToPrimaryWithoutRouteUnits() {
        LogicSQL logicSQL = new LogicSQL(mock(SQLStatementContext.class), "", Collections.emptyList());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData("logic_schema",
                mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS), new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.singleton(rule)), mock(ShardingSphereSchema.class));
        RouteContext actual = sqlRouter.createRouteContext(logicSQL, metaData, rule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(PRIMARY_DATASOURCE));
    }
    
    @Test
    public void assertDecorateRouteContextToPrimaryDataSource() {
        RouteContext actual = mockRouteContext();
        LogicSQL logicSQL = new LogicSQL(mock(SQLStatementContext.class), "", Collections.emptyList());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData("logic_schema",
                mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS), new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.singleton(rule)), mock(ShardingSphereSchema.class));
        sqlRouter.decorateRouteContext(actual, logicSQL, metaData, rule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(NONE_DB_DISCOVERY_DATASOURCE_NAME));
        assertThat(routedDataSourceNames.next(), is(PRIMARY_DATASOURCE));
    }
    
    @Test
    public void assertCreateRouteContextToPrimaryDataSourceWithLock() {
        LogicSQL logicSQL = new LogicSQL(sqlStatementContext, "", Collections.emptyList());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData("logic_schema",
                mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS), new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.singleton(rule)), mock(ShardingSphereSchema.class));
        RouteContext actual = sqlRouter.createRouteContext(logicSQL, metaData, rule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(PRIMARY_DATASOURCE));
    }
    
    @Test
    public void assertDecorateRouteContextToPrimaryDataSourceWithLock() {
        RouteContext actual = mockRouteContext();
        LogicSQL logicSQL = new LogicSQL(sqlStatementContext, "", Collections.emptyList());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData("logic_schema",
                mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS), new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.singleton(rule)), mock(ShardingSphereSchema.class));
        sqlRouter.decorateRouteContext(actual, logicSQL, metaData, rule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(NONE_DB_DISCOVERY_DATASOURCE_NAME));
        assertThat(routedDataSourceNames.next(), is(PRIMARY_DATASOURCE));
    }
    
    @Test
    public void assertCreateRouteContextToPrimaryDataSource() {
        LogicSQL logicSQL = new LogicSQL(sqlStatementContext, "", Collections.emptyList());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData("logic_schema",
                mock(ShardingSphereResource.class, RETURNS_DEEP_STUBS), new ShardingSphereRuleMetaData(Collections.emptyList(), Collections.singleton(rule)), mock(ShardingSphereSchema.class));
        RouteContext actual = sqlRouter.createRouteContext(logicSQL, metaData, rule, new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = actual.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is(PRIMARY_DATASOURCE));
    }
    
    private RouteContext mockRouteContext() {
        RouteContext result = new RouteContext();
        RouteUnit routeUnit = new RouteUnit(new RouteMapper(DATASOURCE_NAME, DATASOURCE_NAME), Collections.singletonList(new RouteMapper("table", "table_0")));
        result.getRouteUnits().add(routeUnit);
        result.getRouteUnits().add(new RouteUnit(new RouteMapper(NONE_DB_DISCOVERY_DATASOURCE_NAME, NONE_DB_DISCOVERY_DATASOURCE_NAME), Collections.emptyList()));
        return result;
    }
}
