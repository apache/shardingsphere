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
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
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
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.SQLRouter;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.route.engine.SQLRouteEngine;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.TableSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.table.CreateTableStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SingleSQLRouterTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final SingleSQLRouter singleSQLRouter = OrderedSPILoader.getServices(SQLRouter.class).stream()
            .filter(SingleSQLRouter.class::isInstance)
            .map(SingleSQLRouter.class::cast)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("SingleSQLRouter SPI service is unavailable."));
    
    @Test
    void assertCreateRouteContextWithSingleDataSource() throws SQLException {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", mock()));
        SingleRule rule = createSingleRule();
        RouteContext actual = singleSQLRouter.createRouteContext(createQueryContext(), mock(), database, rule, Collections.singletonList("foo_tbl"), new ConfigurationProperties(new Properties()));
        assertThat(actual.getRouteUnits().size(), is(1));
        RouteUnit routeUnit = actual.getRouteUnits().iterator().next();
        assertThat(routeUnit.getDataSourceMapper().getLogicName(), is("foo_ds"));
        assertThat(routeUnit.getDataSourceMapper().getActualName(), is("foo_ds"));
        assertThat(routeUnit.getTableMappers().size(), is(1));
        RouteMapper tableMapper = routeUnit.getTableMappers().iterator().next();
        assertThat(tableMapper.getLogicName(), is("foo_tbl"));
        assertThat(tableMapper.getActualName(), is("foo_tbl"));
    }
    
    @Test
    void assertCreateRouteContextWithReadwriteSplittingDataSource() throws SQLException {
        ShardingSphereDatabase database = mockReadwriteSplittingDatabase();
        SingleRule rule = createReadwriteSplittingRule();
        RouteContext actual = singleSQLRouter.createRouteContext(createQueryContext(), mock(), database, rule, Collections.singletonList("foo_tbl"), new ConfigurationProperties(new Properties()));
        assertThat(actual.getRouteUnits().size(), is(1));
        RouteUnit routeUnit = actual.getRouteUnits().iterator().next();
        assertThat(routeUnit.getDataSourceMapper().getLogicName(), is("readwrite_ds"));
        assertThat(routeUnit.getDataSourceMapper().getActualName(), is("write_ds"));
        assertThat(routeUnit.getTableMappers().size(), is(1));
        RouteMapper tableMapper = routeUnit.getTableMappers().iterator().next();
        assertThat(tableMapper.getLogicName(), is("foo_tbl"));
        assertThat(tableMapper.getActualName(), is("foo_tbl"));
    }
    
    @Test
    void assertCreateRouteContextWithMultiDataSource() throws SQLException {
        ShardingSphereDatabase database = mockDatabaseWithMultipleResources();
        SingleRule rule = new SingleRule(new SingleRuleConfiguration(), "foo_db", databaseType, createMultiDataSourceMap(), Collections.emptyList());
        RouteContext actual = singleSQLRouter.createRouteContext(createQueryContext(), mock(), database, rule, Collections.singletonList("foo_tbl"), new ConfigurationProperties(new Properties()));
        assertThat(actual.getRouteUnits().size(), is(1));
        RouteUnit routeUnit = actual.getRouteUnits().iterator().next();
        assertThat(routeUnit.getDataSourceMapper().getLogicName(), is(routeUnit.getDataSourceMapper().getActualName()));
        assertTrue(Arrays.asList("ds_0", "ds_1").contains(routeUnit.getDataSourceMapper().getActualName()));
        assertThat(routeUnit.getTableMappers().size(), is(1));
        RouteMapper tableMapper = routeUnit.getTableMappers().iterator().next();
        assertThat(tableMapper.getLogicName(), is("foo_tbl"));
        assertThat(tableMapper.getActualName(), is("foo_tbl"));
    }
    
    @Test
    void assertCreateRouteContextWithDistributedTable() {
        QueryContext queryContext = mockQueryContext(new CreateTableStatement(databaseType));
        ShardingSphereDatabase database = mockDatabaseWithDistributedTables(Collections.singleton("foo_tbl"));
        SingleRule rule = mock(SingleRule.class);
        when(rule.getQualifiedTables(queryContext.getSqlStatementContext(), database)).thenReturn(Collections.singletonList(new QualifiedTable("foo_db", "foo_tbl")));
        RouteContext actual = singleSQLRouter.createRouteContext(queryContext, mock(), database, rule, Collections.singletonList("foo_tbl"), new ConfigurationProperties(new Properties()));
        assertTrue(actual.getRouteUnits().isEmpty());
    }
    
    @Test
    void assertRouteBySQLRouteEngine() throws SQLException {
        SingleRule rule = createSingleRule();
        SQLRouteEngine sqlRouteEngine = new SQLRouteEngine(Collections.singleton(rule), new ConfigurationProperties(new Properties()));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", mock()));
        RouteContext actual = sqlRouteEngine.route(createQueryContext(), mock(), database);
        assertThat(actual.getRouteUnits().size(), is(1));
    }
    
    @Test
    void assertCreateRouteContextWithNonCreateStatement() {
        QueryContext queryContext = mockQueryContext(new SQLStatement(databaseType));
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        ShardingSphereDatabase database = mockDatabaseWithDistributedTables(Collections.emptyList());
        QualifiedTable qualifiedTable = new QualifiedTable("foo_db", "foo_tbl");
        SingleRule rule = mock(SingleRule.class);
        when(rule.getQualifiedTables(sqlStatementContext, database)).thenReturn(Collections.singletonList(qualifiedTable));
        when(rule.getSingleTables(Collections.singletonList(qualifiedTable))).thenReturn(Collections.emptyList());
        RouteContext actual = singleSQLRouter.createRouteContext(queryContext, mock(), database, rule, Collections.singletonList("foo_tbl"), new ConfigurationProperties(new Properties()));
        assertTrue(actual.getRouteUnits().isEmpty());
        verify(rule).getSingleTables(Collections.singletonList(qualifiedTable));
    }
    
    @Test
    void assertDecorateRouteContextWithSingleDataSource() {
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper("foo_ds", "foo_ds"), Collections.singletonList(new RouteMapper("foo_tbl", "foo_tbl"))));
        ShardingSphereDatabase database = mockReadwriteSplittingDatabase();
        SingleRule rule = new SingleRule(new SingleRuleConfiguration(), "foo_db", databaseType, Collections.singletonMap("foo_ds", new MockedDataSource()), Collections.emptyList());
        singleSQLRouter.decorateRouteContext(routeContext, createQueryContext(), database, rule, Collections.singletonList("foo_tbl"), new ConfigurationProperties(new Properties()));
        assertThat(routeContext.getActualDataSourceNames().size(), is(1));
        assertThat(routeContext.getActualDataSourceNames().iterator().next(), is("foo_ds"));
    }
    
    @Test
    void assertDecorateRouteContextWithReadwriteSplittingDataSource() {
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper("readwrite_ds", "readwrite_ds"), Collections.emptyList()));
        routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper("write_ds", "write_ds"), Collections.singletonList(new RouteMapper("foo_tbl", "foo_tbl"))));
        SingleRule rule = new SingleRule(new SingleRuleConfiguration(),
                "foo_db", databaseType, Collections.singletonMap("readwrite_ds", new MockedDataSource()), Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db",
                mock(DatabaseType.class), mock(ResourceMetaData.class, RETURNS_DEEP_STUBS), new RuleMetaData(Collections.singleton(rule)), Collections.emptyList());
        singleSQLRouter.decorateRouteContext(routeContext, createQueryContext(), database, rule, Collections.singletonList("foo_tbl"), new ConfigurationProperties(new Properties()));
        assertThat(routeContext.getActualDataSourceNames().size(), is(1));
        assertTrue(Arrays.asList("write_ds", "readwrite_ds").contains(routeContext.getActualDataSourceNames().iterator().next()));
    }
    
    @Test
    void assertDecorateRouteContextWithMultiDataSource() throws SQLException {
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper("ds_0", "ds_0"), Collections.emptyList()));
        routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper("ds_1", "ds_1"), Collections.emptyList()));
        ShardingSphereDatabase database = mockDatabaseWithMultipleResources();
        SingleRule rule = new SingleRule(new SingleRuleConfiguration(), "foo_db", databaseType, createMultiDataSourceMap(), Collections.emptyList());
        singleSQLRouter.decorateRouteContext(routeContext, createQueryContext(), database, rule, Collections.singletonList("foo_tbl"), new ConfigurationProperties(new Properties()));
        assertThat(routeContext.getActualDataSourceNames().size(), is(1));
        assertTrue(Arrays.asList("ds_0", "ds_1").contains(routeContext.getActualDataSourceNames().iterator().next()));
    }
    
    @Test
    void assertDecorateRouteContextWithDistributedTable() {
        QueryContext queryContext = mockQueryContext(new CreateTableStatement(databaseType));
        ShardingSphereDatabase database = mockDatabaseWithDistributedTables(Collections.singleton("foo_tbl"));
        SingleRule rule = mock(SingleRule.class);
        SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
        when(rule.getQualifiedTables(sqlStatementContext, database)).thenReturn(Collections.singletonList(new QualifiedTable("foo_db", "foo_tbl")));
        RouteContext routeContext = new RouteContext();
        routeContext.getRouteUnits().add(new RouteUnit(new RouteMapper("foo_ds", "foo_ds"), Collections.singletonList(new RouteMapper("foo_tbl", "foo_tbl"))));
        singleSQLRouter.decorateRouteContext(routeContext, queryContext, database, rule, Collections.singletonList("foo_tbl"), new ConfigurationProperties(new Properties()));
        assertThat(routeContext.getActualDataSourceNames().size(), is(1));
        assertThat(routeContext.getActualDataSourceNames().iterator().next(), is("foo_ds"));
    }
    
    private SingleRule createSingleRule() throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:mock://127.0.0.1/db");
        SingleRule result = new SingleRule(new SingleRuleConfiguration(),
                "foo_db", databaseType, Collections.singletonMap("foo_ds", new MockedDataSource(connection)), Collections.emptyList());
        result.getAttributes().getAttribute(DataNodeRuleAttribute.class).getAllDataNodes().put("foo_tbl", Collections.singleton(new DataNode("foo_ds", "foo_db", "foo_tbl")));
        return result;
    }
    
    private SingleRule createReadwriteSplittingRule() throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:mock://127.0.0.1/db");
        SingleRule result = new SingleRule(new SingleRuleConfiguration(),
                "foo_db", databaseType, Collections.singletonMap("readwrite_ds", new MockedDataSource(connection)), Collections.emptyList());
        result.getAttributes().getAttribute(DataNodeRuleAttribute.class).getAllDataNodes().put("foo_tbl", Collections.singletonList(new DataNode("write_ds", "foo_db", "foo_tbl")));
        return result;
    }
    
    private ShardingSphereDatabase mockReadwriteSplittingDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn(" db_schema");
        when(result.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("write_ds", mock(StorageUnit.class)));
        return result;
    }
    
    private Map<String, DataSource> createMultiDataSourceMap() throws SQLException {
        Map<String, DataSource> result = new HashMap<>(2, 1F);
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:mock://127.0.0.1/db");
        result.put("ds_0", new MockedDataSource(connection));
        result.put("ds_1", new MockedDataSource(connection));
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
        when(schema.containsTable("foo_tbl")).thenReturn(true);
        when(result.getSchema(any())).thenReturn(schema);
        return result;
    }
    
    private ShardingSphereDatabase mockDatabaseWithDistributedTables(final Collection<String> distributedTableNames) {
        Map<String, StorageUnit> storageUnits = new HashMap<>(2, 1F);
        storageUnits.put("ds_0", mock(StorageUnit.class));
        storageUnits.put("ds_1", mock(StorageUnit.class));
        TableMapperRuleAttribute tableMapperRuleAttribute = mock(TableMapperRuleAttribute.class);
        when(tableMapperRuleAttribute.getDistributedTableNames()).thenReturn(distributedTableNames);
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        when(result.getRuleMetaData().getAttributes(TableMapperRuleAttribute.class)).thenReturn(Collections.singletonList(tableMapperRuleAttribute));
        return result;
    }
    
    private QueryContext mockQueryContext(final SQLStatement sqlStatement) {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        TablesContext tablesContext = mock(TablesContext.class);
        when(tablesContext.getTableNames()).thenReturn(Collections.singleton("foo_tbl"));
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        when(sqlStatementContext.getTablesContext()).thenReturn(tablesContext);
        QueryContext result = mock(QueryContext.class);
        when(result.getSqlStatementContext()).thenReturn(sqlStatementContext);
        return result;
    }
    
    private QueryContext createQueryContext() {
        CreateTableStatement createTableStatement = new CreateTableStatement(databaseType);
        TableNameSegment tableNameSegment = new TableNameSegment(1, 2, new IdentifierValue("foo_tbl"));
        tableNameSegment.setTableBoundInfo(new TableSegmentBoundInfo(new IdentifierValue("foo_db"), new IdentifierValue("foo_schema")));
        createTableStatement.setTable(new SimpleTableSegment(tableNameSegment));
        createTableStatement.buildAttributes();
        ConnectionContext connectionContext = mock(ConnectionContext.class);
        when(connectionContext.getCurrentDatabaseName()).thenReturn(Optional.of("foo_db"));
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class);
        when(metaData.containsDatabase("foo_db")).thenReturn(true);
        when(metaData.getDatabase("foo_db")).thenReturn(mock(ShardingSphereDatabase.class));
        return new QueryContext(new CommonSQLStatementContext(createTableStatement), "CREATE TABLE", Collections.emptyList(), new HintValueContext(), connectionContext, metaData);
    }
}
