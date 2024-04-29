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

package org.apache.shardingsphere.broadcast.route;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.broadcast.api.config.BroadcastRuleConfiguration;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CreateTableStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.SQLRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BroadcastSqlRouterTest {
    
    @Test
    void assertCreateBroadcastRouteContextWithMultiDataSource() throws SQLException {
        BroadcastRuleConfiguration currentConfig = mock(BroadcastRuleConfiguration.class);
        when(currentConfig.getTables()).thenReturn(Collections.singleton("t_order"));
        BroadcastRule broadcastRule = new BroadcastRule(currentConfig, DefaultDatabase.LOGIC_NAME, createMultiDataSourceMap(), Collections.emptyList());
        RouteContext routeContext = new BroadcastSQLRouter().createRouteContext(createQueryContext(), mock(RuleMetaData.class), mockDatabaseWithMultipleResources(), broadcastRule,
                new ConfigurationProperties(new Properties()), new ConnectionContext());
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(2));
        assertThat(routeUnits.get(0).getDataSourceMapper().getLogicName(), is(routeUnits.get(0).getDataSourceMapper().getActualName()));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(1));
        RouteMapper tableMapper = routeUnits.get(0).getTableMappers().iterator().next();
        assertThat(tableMapper.getActualName(), is("t_order"));
        assertThat(tableMapper.getLogicName(), is("t_order"));
    }
    
    @Test
    void assertCreateBroadcastRouteContextWithSingleDataSource() throws SQLException {
        BroadcastRuleConfiguration currentConfig = mock(BroadcastRuleConfiguration.class);
        when(currentConfig.getTables()).thenReturn(Collections.singleton("t_order"));
        BroadcastRule broadcastRule = new BroadcastRule(currentConfig, DefaultDatabase.LOGIC_NAME, Collections.singletonMap("tmp_ds", new MockedDataSource(mockConnection())), Collections.emptyList());
        broadcastRule.getAttributes().getAttribute(DataNodeRuleAttribute.class).getAllDataNodes().put("t_order", Collections.singletonList(createDataNode("tmp_ds")));
        ShardingSphereDatabase database = mockSingleDatabase();
        RouteContext routeContext = new BroadcastSQLRouter().createRouteContext(
                createQueryContext(), mock(RuleMetaData.class), database, broadcastRule, new ConfigurationProperties(new Properties()), new ConnectionContext());
        assertThat(routeContext.getRouteUnits().size(), is(1));
        RouteUnit routeUnit = routeContext.getRouteUnits().iterator().next();
        assertThat(routeUnit.getDataSourceMapper().getLogicName(), is("tmp_ds"));
        assertThat(routeUnit.getDataSourceMapper().getActualName(), is("tmp_ds"));
        assertFalse(routeUnit.getTableMappers().isEmpty());
    }
    
    @Test
    void assertDecorateBroadcastRouteContextWithSingleDataSource() {
        BroadcastRuleConfiguration currentConfig = mock(BroadcastRuleConfiguration.class);
        when(currentConfig.getTables()).thenReturn(Collections.singleton("t_order"));
        BroadcastRule broadcastRule = new BroadcastRule(currentConfig, DefaultDatabase.LOGIC_NAME, Collections.singletonMap("foo_ds", new MockedDataSource()), Collections.emptyList());
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper("foo_ds", "foo_ds"), Lists.newArrayList()));
        BroadcastSQLRouter sqlRouter = (BroadcastSQLRouter) OrderedSPILoader.getServices(SQLRouter.class, Collections.singleton(broadcastRule)).get(broadcastRule);
        sqlRouter.decorateRouteContext(routeContext, createQueryContext(), mockSingleDatabase(), broadcastRule, new ConfigurationProperties(new Properties()), new ConnectionContext());
        Iterator<String> routedDataSourceNames = routeContext.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is("foo_ds"));
    }
    
    private Connection mockConnection() throws SQLException {
        Connection result = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(result.getMetaData().getURL()).thenReturn("jdbc:h2:mem:db");
        return result;
    }
    
    private ShardingSphereDatabase mockSingleDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", mock(StorageUnit.class)));
        when(result.getResourceMetaData().getAllInstanceDataSourceNames()).thenReturn(Collections.singletonList("foo_ds"));
        return result;
    }
    
    private QueryContext createQueryContext() {
        CreateTableStatement createTableStatement = new MySQLCreateTableStatement(false);
        createTableStatement.setTable(new SimpleTableSegment(new TableNameSegment(1, 2, new IdentifierValue("t_order"))));
        return new QueryContext(new CreateTableStatementContext(createTableStatement), "CREATE TABLE", new LinkedList<>(), new HintValueContext());
    }
    
    private Map<String, DataSource> createMultiDataSourceMap() throws SQLException {
        Map<String, DataSource> result = new HashMap<>(2, 1F);
        Connection connection = mockConnection();
        result.put("ds_0", new MockedDataSource(connection));
        result.put("ds_1", new MockedDataSource(connection));
        return result;
    }
    
    private DataNode createDataNode(final String dataSourceName) {
        DataNode result = new DataNode(dataSourceName, "t_order");
        result.setSchemaName(DefaultDatabase.LOGIC_NAME);
        return result;
    }
    
    private ShardingSphereDatabase mockDatabaseWithMultipleResources() {
        Map<String, StorageUnit> storageUnits = new HashMap<>(2, 1F);
        DataSourcePoolProperties dataSourcePoolProps0 = mock(DataSourcePoolProperties.class, RETURNS_DEEP_STUBS);
        when(dataSourcePoolProps0.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(Collections.singletonMap("url", "jdbc:mock://127.0.0.1/ds_0"));
        storageUnits.put("ds_0", new StorageUnit(mock(StorageNode.class), dataSourcePoolProps0, new MockedDataSource()));
        DataSourcePoolProperties dataSourcePoolProps1 = mock(DataSourcePoolProperties.class, RETURNS_DEEP_STUBS);
        when(dataSourcePoolProps1.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(Collections.singletonMap("url", "jdbc:mock://127.0.0.1/ds_1"));
        storageUnits.put("ds_1", new StorageUnit(mock(StorageNode.class), dataSourcePoolProps1, new MockedDataSource()));
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        when(result.getName()).thenReturn(DefaultDatabase.LOGIC_NAME);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.containsTable("t_order")).thenReturn(true);
        when(result.getSchema(any())).thenReturn(schema);
        return result;
    }
}
