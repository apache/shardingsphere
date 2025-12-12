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

import org.apache.groovy.util.Maps;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
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
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SingleSQLRouterTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertCreateRouteContextWithSingleDataSource() throws SQLException {
        SingleRule rule = new SingleRule(new SingleRuleConfiguration(), "foo_db", databaseType, Collections.singletonMap("foo_ds", new MockedDataSource(mockConnection())), Collections.emptyList());
        rule.getAttributes().getAttribute(DataNodeRuleAttribute.class).getAllDataNodes().put("t_order", Collections.singleton(new DataNode("foo_ds", "foo_db", "t_order")));
        ShardingSphereDatabase database = mockSingleDatabase();
        RouteContext actual = new SingleSQLRouter().createRouteContext(
                createQueryContext(), mock(RuleMetaData.class), database, rule, Collections.singletonList("t_order"), new ConfigurationProperties(new Properties()));
        assertThat(actual.getRouteUnits().size(), is(1));
        RouteUnit routeUnit = actual.getRouteUnits().iterator().next();
        assertThat(routeUnit.getDataSourceMapper().getLogicName(), is("foo_ds"));
        assertThat(routeUnit.getDataSourceMapper().getActualName(), is("foo_ds"));
        assertFalse(routeUnit.getTableMappers().isEmpty());
    }
    
    private ShardingSphereDatabase mockSingleDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", mock(StorageUnit.class)));
        return result;
    }
    
    @Test
    void assertCreateRouteContextWithReadwriteSplittingDataSource() throws SQLException {
        SingleRule rule = new SingleRule(new SingleRuleConfiguration(),
                "foo_db", databaseType, Collections.singletonMap("readwrite_ds", new MockedDataSource(mockConnection())), Collections.emptyList());
        rule.getAttributes().getAttribute(DataNodeRuleAttribute.class).getAllDataNodes().put("t_order", Collections.singletonList(new DataNode("write_ds", "foo_db", "t_order")));
        ShardingSphereDatabase database = mockReadwriteSplittingDatabase();
        RouteContext actual = new SingleSQLRouter().createRouteContext(
                createQueryContext(), mock(RuleMetaData.class), database, rule, Collections.singletonList("t_order"), new ConfigurationProperties(new Properties()));
        assertThat(actual.getRouteUnits().size(), is(1));
        RouteUnit routeUnit = actual.getRouteUnits().iterator().next();
        assertThat(routeUnit.getDataSourceMapper().getLogicName(), is("readwrite_ds"));
        assertThat(routeUnit.getDataSourceMapper().getActualName(), is("write_ds"));
        assertFalse(routeUnit.getTableMappers().isEmpty());
    }
    
    private ShardingSphereDatabase mockReadwriteSplittingDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn(" db_schema");
        when(result.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("write_ds", mock(StorageUnit.class)));
        return result;
    }
    
    @Test
    void assertCreateRouteContextWithMultiDataSource() throws SQLException {
        SingleRule rule = new SingleRule(new SingleRuleConfiguration(), "foo_db", databaseType, createMultiDataSourceMap(), Collections.emptyList());
        ShardingSphereDatabase database = mockDatabaseWithMultipleResources();
        RouteContext actual = new SingleSQLRouter().createRouteContext(
                createQueryContext(), mock(RuleMetaData.class), database, rule, Collections.singletonList("t_order"), new ConfigurationProperties(new Properties()));
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
        when(result.getMetaData().getURL()).thenReturn("jdbc:mock://127.0.0.1/db");
        return result;
    }
    
    private ShardingSphereDatabase mockDatabaseWithMultipleResources() {
        Map<String, StorageUnit> storageUnits = new HashMap<>(2, 1F);
        DataSourcePoolProperties dataSourcePoolProps0 = mock(DataSourcePoolProperties.class, RETURNS_DEEP_STUBS);
        when(dataSourcePoolProps0.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(Maps.of("url", "jdbc:mock://127.0.0.1/ds_0", "username", "test"));
        storageUnits.put("ds_0", new StorageUnit(mock(StorageNode.class), dataSourcePoolProps0, new MockedDataSource()));
        DataSourcePoolProperties dataSourcePoolProps1 = mock(DataSourcePoolProperties.class, RETURNS_DEEP_STUBS);
        when(dataSourcePoolProps1.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(Maps.of("url", "jdbc:mock://127.0.0.1/ds_1", "username", "test"));
        storageUnits.put("ds_1", new StorageUnit(mock(StorageNode.class), dataSourcePoolProps1, new MockedDataSource()));
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        when(result.getName()).thenReturn("foo_db");
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.containsTable("t_order")).thenReturn(true);
        when(result.getSchema(any())).thenReturn(schema);
        return result;
    }
    
    @Test
    void assertDecorateRouteContextWithSingleDataSource() {
        SingleRule rule = new SingleRule(new SingleRuleConfiguration(),
                "foo_db", databaseType, Collections.singletonMap("foo_ds", new MockedDataSource()), Collections.emptyList());
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper("foo_ds", "foo_ds"), Collections.singletonList(new RouteMapper("t_order", "t_order"))));
        SingleSQLRouter sqlRouter = (SingleSQLRouter) OrderedSPILoader.getServices(SQLRouter.class, Collections.singleton(rule)).get(rule);
        sqlRouter.decorateRouteContext(
                routeContext, createQueryContext(), mockReadwriteSplittingDatabase(), rule, Collections.singletonList("t_order"), new ConfigurationProperties(new Properties()));
        Iterator<String> routedDataSourceNames = routeContext.getActualDataSourceNames().iterator();
        assertThat(routedDataSourceNames.next(), is("foo_ds"));
    }
    
    @Test
    void assertDecorateRouteContextWithReadwriteSplittingDataSource() {
        SingleRule rule = new SingleRule(new SingleRuleConfiguration(),
                "foo_db", databaseType, Collections.singletonMap("readwrite_ds", new MockedDataSource()), Collections.emptyList());
        RouteContext routeContext = new RouteContext();
        RouteUnit readWriteRouteUnit = new RouteUnit(new RouteMapper("readwrite_ds", "readwrite_ds"), Collections.emptyList());
        RouteUnit writeRouteUnit = new RouteUnit(new RouteMapper("write_ds", "write_ds"), Collections.singletonList(new RouteMapper("t_order", "t_order")));
        routeContext.getRouteUnits().add(readWriteRouteUnit);
        routeContext.getRouteUnits().add(writeRouteUnit);
        SingleSQLRouter sqlRouter = (SingleSQLRouter) OrderedSPILoader.getServices(SQLRouter.class, Collections.singleton(rule)).get(rule);
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db",
                mock(DatabaseType.class), mock(ResourceMetaData.class, RETURNS_DEEP_STUBS), new RuleMetaData(Collections.singleton(rule)), Collections.emptyList());
        sqlRouter.decorateRouteContext(routeContext, createQueryContext(), database, rule, Collections.singletonList("t_order"), new ConfigurationProperties(new Properties()));
        assertThat(routeContext.getActualDataSourceNames().size(), is(1));
        assertTrue(Arrays.asList("write_ds", "readwrite_ds").contains(routeContext.getActualDataSourceNames().iterator().next()));
    }
    
    @Test
    void assertDecorateRouteContextWithMultiDataSource() throws SQLException {
        SingleRule rule = new SingleRule(new SingleRuleConfiguration(), "foo_db", databaseType, createMultiDataSourceMap(), Collections.emptyList());
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.emptyList()));
        routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper("ds_1", "ds_1"), Collections.emptyList()));
        SingleSQLRouter sqlRouter = (SingleSQLRouter) OrderedSPILoader.getServices(SQLRouter.class, Collections.singleton(rule)).get(rule);
        sqlRouter.decorateRouteContext(routeContext, createQueryContext(), mockDatabaseWithMultipleResources(), rule, Collections.singletonList("t_order"),
                new ConfigurationProperties(new Properties()));
        assertThat(routeContext.getActualDataSourceNames().size(), is(1));
        assertTrue(Arrays.asList("ds_0", "ds_1").contains(routeContext.getActualDataSourceNames().iterator().next()));
    }
    
    private QueryContext createQueryContext() {
        CreateTableStatement createTableStatement = new CreateTableStatement(databaseType);
        TableNameSegment tableNameSegment = new TableNameSegment(1, 2, new IdentifierValue("t_order"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        createTableStatement.setTable(new SimpleTableSegment(tableNameSegment));
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        when(connectionContext.getCurrentDatabaseName()).thenReturn(Optional.of("foo_db"));
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.containsDatabase("foo_db")).thenReturn(true);
        when(metaData.getDatabase("foo_db")).thenReturn(mock(ShardingSphereDatabase.class));
        return new QueryContext(new CommonSQLStatementContext(createTableStatement), "CREATE TABLE", new LinkedList<>(), new HintValueContext(), connectionContext, metaData);
    }
}
