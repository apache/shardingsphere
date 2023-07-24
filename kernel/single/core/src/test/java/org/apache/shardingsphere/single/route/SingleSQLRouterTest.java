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

package org.apache.shardingsphere.single.route;

import org.apache.shardingsphere.infra.binder.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.SQLRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.util.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateTableStatement;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SingleSQLRouterTest {
    
    @Test
    void assertCreateRouteContextWithSingleDataSource() throws SQLException {
        SingleRule rule = new SingleRule(new SingleRuleConfiguration(),
                DefaultDatabase.LOGIC_NAME, Collections.singletonMap("foo_ds", new MockedDataSource(mockConnection())), Collections.emptyList());
        rule.getSingleTableDataNodes().put("t_order", Collections.singletonList(createDataNode("foo_ds")));
        ShardingSphereDatabase database = mockSingleDatabase();
        RouteContext actual = new SingleSQLRouter().createRouteContext(createQueryContext(),
                mock(ShardingSphereRuleMetaData.class), database, rule, new ConfigurationProperties(new Properties()), new ConnectionContext());
        assertThat(actual.getRouteUnits().size(), is(1));
        RouteUnit routeUnit = actual.getRouteUnits().iterator().next();
        assertThat(routeUnit.getDataSourceMapper().getLogicName(), is("foo_ds"));
        assertThat(routeUnit.getDataSourceMapper().getActualName(), is("foo_ds"));
        assertTrue(routeUnit.getTableMappers().isEmpty());
    }
    
    private ShardingSphereDatabase mockSingleDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getResourceMetaData().getDataSources()).thenReturn(Collections.singletonMap("foo_ds", new MockedDataSource()));
        return result;
    }
    
    @Test
    void assertCreateRouteContextWithReadwriteSplittingDataSource() throws SQLException {
        SingleRule rule = new SingleRule(new SingleRuleConfiguration(),
                DefaultDatabase.LOGIC_NAME, Collections.singletonMap("readwrite_ds", new MockedDataSource(mockConnection())), Collections.emptyList());
        rule.getSingleTableDataNodes().put("t_order", Collections.singletonList(createDataNode("write_ds")));
        ShardingSphereDatabase database = mockReadwriteSplittingDatabase();
        RouteContext actual = new SingleSQLRouter().createRouteContext(createQueryContext(),
                mock(ShardingSphereRuleMetaData.class), database, rule, new ConfigurationProperties(new Properties()), new ConnectionContext());
        assertThat(actual.getRouteUnits().size(), is(1));
        RouteUnit routeUnit = actual.getRouteUnits().iterator().next();
        assertThat(routeUnit.getDataSourceMapper().getLogicName(), is("readwrite_ds"));
        assertThat(routeUnit.getDataSourceMapper().getActualName(), is("write_ds"));
        assertTrue(routeUnit.getTableMappers().isEmpty());
    }
    
    private ShardingSphereDatabase mockReadwriteSplittingDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn(" db_schema");
        when(result.getResourceMetaData().getDataSources()).thenReturn(Collections.singletonMap("write_ds", new MockedDataSource()));
        return result;
    }
    
    @Test
    void assertCreateRouteContextWithMultiDataSource() throws SQLException {
        SingleRule rule = new SingleRule(new SingleRuleConfiguration(), DefaultDatabase.LOGIC_NAME, createMultiDataSourceMap(), Collections.emptyList());
        ShardingSphereDatabase database = mockDatabaseWithMultipleResources();
        RouteContext actual = new SingleSQLRouter().createRouteContext(createQueryContext(),
                mock(ShardingSphereRuleMetaData.class), database, rule, new ConfigurationProperties(new Properties()), new ConnectionContext());
        List<RouteUnit> routeUnits = new ArrayList<>(actual.getRouteUnits());
        assertThat(actual.getRouteUnits().size(), is(1));
        assertThat(routeUnits.get(0).getDataSourceMapper().getLogicName(), is(routeUnits.get(0).getDataSourceMapper().getActualName()));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(1));
        RouteMapper tableMapper = routeUnits.get(0).getTableMappers().iterator().next();
        assertThat(tableMapper.getActualName(), is("t_order"));
        assertThat(tableMapper.getLogicName(), is("t_order"));
    }
    
    private Map<String, DataSource> createMultiDataSourceMap() throws SQLException {
        Map<String, DataSource> result = new HashMap<>(2, 1F);
        Connection connection = mockConnection();
        result.put("ds_0", new MockedDataSource(connection));
        result.put("ds_1", new MockedDataSource(connection));
        return result;
    }
    
    private Connection mockConnection() throws SQLException {
        Connection result = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getURL()).thenReturn("jdbc:h2:mem:db");
        return result;
    }
    
    private DataNode createDataNode(final String dataSourceName) {
        DataNode result = new DataNode(dataSourceName, "t_order");
        result.setSchemaName(DefaultDatabase.LOGIC_NAME);
        return result;
    }
    
    private ShardingSphereDatabase mockDatabaseWithMultipleResources() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1F);
        dataSourceMap.put("ds_0", new MockedDataSource());
        dataSourceMap.put("ds_1", new MockedDataSource());
        when(result.getResourceMetaData().getDataSources()).thenReturn(dataSourceMap);
        when(result.getName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.containsTable("t_order")).thenReturn(true);
        when(result.getSchema(any())).thenReturn(schema);
        return result;
    }
    
    @Test
    void assertDecorateRouteContextWithSingleDataSource() {
        SingleRule rule = new SingleRule(new SingleRuleConfiguration(),
                DefaultDatabase.LOGIC_NAME, Collections.singletonMap("foo_ds", new MockedDataSource()), Collections.emptyList());
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper("foo_ds", "foo_ds"), Collections.singletonList(new RouteMapper("t_order", "t_order"))));
        SingleSQLRouter sqlRouter = (SingleSQLRouter) OrderedSPILoader.getServices(SQLRouter.class, Collections.singleton(rule)).get(rule);
        sqlRouter.decorateRouteContext(routeContext, createQueryContext(), mockReadwriteSplittingDatabase(), rule, new ConfigurationProperties(new Properties()), new ConnectionContext());
        Iterator<String> routedDataSourceNames = routeContext.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is("foo_ds"));
    }
    
    @Test
    void assertDecorateRouteContextWithReadwriteSplittingDataSource() {
        SingleRule rule = new SingleRule(new SingleRuleConfiguration(),
                DefaultDatabase.LOGIC_NAME, Collections.singletonMap("readwrite_ds", new MockedDataSource()), Collections.emptyList());
        RouteContext routeContext = new RouteContext();
        RouteUnit readWriteRouteUnit = new RouteUnit(new RouteMapper("readwrite_ds", "readwrite_ds"), Collections.emptyList());
        RouteUnit writeRouteUnit = new RouteUnit(new RouteMapper("write_ds", "write_ds"), Collections.singletonList(new RouteMapper("t_order", "t_order")));
        routeContext.getRouteUnits().add(readWriteRouteUnit);
        routeContext.getRouteUnits().add(writeRouteUnit);
        SingleSQLRouter sqlRouter = (SingleSQLRouter) OrderedSPILoader.getServices(SQLRouter.class, Collections.singleton(rule)).get(rule);
        ShardingSphereDatabase database = new ShardingSphereDatabase(DefaultDatabase.LOGIC_NAME, mock(DatabaseType.class), mock(ShardingSphereResourceMetaData.class, RETURNS_DEEP_STUBS),
                new ShardingSphereRuleMetaData(Collections.singleton(rule)), Collections.emptyMap());
        sqlRouter.decorateRouteContext(routeContext, createQueryContext(), database, rule, new ConfigurationProperties(new Properties()), new ConnectionContext());
        Iterator<String> routedDataSourceNames = routeContext.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is("write_ds"));
        assertThat(routedDataSourceNames.next(), is("readwrite_ds"));
    }
    
    @Test
    void assertDecorateRouteContextWithMultiDataSource() throws SQLException {
        SingleRule rule = new SingleRule(new SingleRuleConfiguration(), DefaultDatabase.LOGIC_NAME, createMultiDataSourceMap(), Collections.emptyList());
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.emptyList()));
        routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper("ds_1", "ds_1"), Collections.emptyList()));
        SingleSQLRouter sqlRouter = (SingleSQLRouter) OrderedSPILoader.getServices(SQLRouter.class, Collections.singleton(rule)).get(rule);
        sqlRouter.decorateRouteContext(routeContext, createQueryContext(), mockDatabaseWithMultipleResources(), rule, new ConfigurationProperties(new Properties()), new ConnectionContext());
        Iterator<String> routedDataSourceNames = routeContext.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is("ds_1"));
        assertThat(routedDataSourceNames.next(), is("ds_0"));
    }
    
    private QueryContext createQueryContext() {
        CreateTableStatement createTableStatement = new MySQLCreateTableStatement(false);
        createTableStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 2, new IdentifierValue("t_order"))));
        return new QueryContext(new CreateTableStatementContext(createTableStatement), "CREATE TABLE", new LinkedList<>());
    }
}
