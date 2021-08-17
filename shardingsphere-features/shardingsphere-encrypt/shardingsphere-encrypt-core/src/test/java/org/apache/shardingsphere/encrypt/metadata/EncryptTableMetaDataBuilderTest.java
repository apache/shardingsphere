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

package org.apache.shardingsphere.encrypt.metadata;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.DialectTableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.RuleBasedTableMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.single.SingleTableRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.LinkedList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class EncryptTableMetaDataBuilderTest {
    
    private static final String TABLE_NAME = "t_encrypt";
    
    static {
        ShardingSphereServiceLoader.register(RuleBasedTableMetaDataBuilder.class);
        ShardingSphereServiceLoader.register(DialectTableMetaDataLoader.class);
    }
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DatabaseType databaseType;
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private ConfigurationProperties props;
    
    @Before
    public void setUp() throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(dataSource.getConnection()).thenReturn(connection);
        mockH2ResultSet(connection);
        mockMySQLResultSet(connection);
        mockOracleResultSet(connection);
        mockPGResultSet(connection);
        mockSQLServerResultSet(connection);
        mockDatabaseMetaData(connection);
    }
    
    private void mockSQLServerResultSet(final Connection connection) throws SQLException {
        ResultSet resultSet = createColumnResultSet();
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(startsWith("SELECT obj.name AS TABLE_NAME, col.name AS COLUMN_NAME, t.name AS DATA_TYPE"))).thenReturn(preparedStatement);
    }
    
    private void mockPGResultSet(final Connection connection) throws SQLException {
        ResultSet resultSet = createColumnResultSet();
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(startsWith("SELECT table_name, column_name, ordinal_position, data_type, udt_name, column_default"))).thenReturn(preparedStatement);
    }
    
    private void mockOracleResultSet(final Connection connection) throws SQLException {
        ResultSet resultSet = createColumnResultSet();
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(startsWith("SELECT OWNER AS TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, DATA_TYPE"))).thenReturn(preparedStatement);
    }
    
    private void mockMySQLResultSet(final Connection connection) throws SQLException {
        ResultSet resultSet = createColumnResultSet();
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(startsWith("SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, COLUMN_KEY, EXTRA, COLLATION_NAME FROM information_schema.columns"))).thenReturn(preparedStatement);
    }
    
    private void mockH2ResultSet(final Connection connection) throws SQLException {
        ResultSet resultSet = createColumnResultSet();
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(startsWith("SELECT TABLE_CATALOG, TABLE_NAME"))).thenReturn(preparedStatement);
    }
    
    private void mockDatabaseMetaData(final Connection connection) throws SQLException {
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        ResultSet dataTypeResultSet = createDataTypeResultSet();
        when(databaseMetaData.getTypeInfo()).thenReturn(dataTypeResultSet);
        ResultSet tableResultSet = createTableResultSet();
        ResultSet columnResultSet = createColumnResultSet();
        when(databaseMetaData.getTables(any(), any(), any(), eq(null))).thenReturn(tableResultSet);
        when(databaseMetaData.getColumns(any(), any(), any(), eq("%"))).thenReturn(columnResultSet);
    }
    
    private ResultSet createTableResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        return result;
    }
    
    private ResultSet createColumnResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, true, false);
        when(result.getString("TABLE_NAME")).thenReturn(TABLE_NAME);
        when(result.getString("table_name")).thenReturn(TABLE_NAME);
        when(result.getString("COLUMN_NAME")).thenReturn("id", "pwd_cipher", "pwd_plain");
        when(result.getString("column_name")).thenReturn("id", "pwd_cipher", "pwd_plain");
        when(result.getString("TYPE_NAME")).thenReturn("INT");
        when(result.getString("DATA_TYPE")).thenReturn("INT");
        when(result.getString("udt_name")).thenReturn("INT");
        when(result.getInt("ordinal_position")).thenReturn(1, 2, 3);
        return result;
    }
    
    private ResultSet createDataTypeResultSet() throws SQLException {
        ResultSet dataTypeResultSet = mock(ResultSet.class);
        when(dataTypeResultSet.next()).thenReturn(true, false);
        when(dataTypeResultSet.getString("TYPE_NAME")).thenReturn("INT");
        when(dataTypeResultSet.getInt("DATA_TYPE")).thenReturn(1);
        return dataTypeResultSet;
    }
    
    @Test
    public void assertLoadByExistedTable() throws SQLException {
        EncryptRule encryptRule = createEncryptRule();
        Collection<ShardingSphereRule> rules = Arrays.asList(createSingleTableRule(), encryptRule);
        EncryptTableMetaDataBuilder loader = (EncryptTableMetaDataBuilder) OrderedSPIRegistry.getRegisteredServices(RuleBasedTableMetaDataBuilder.class, rules).get(encryptRule);
        when(databaseType.formatTableNamePattern(TABLE_NAME)).thenReturn(TABLE_NAME);
        Optional<TableMetaData> actual = loader.load(TABLE_NAME, databaseType, Collections.singletonMap("logic_db", dataSource), new DataNodes(rules), encryptRule, props);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getColumnMetaData(0).getName(), is("id"));
        assertThat(actual.get().getColumnMetaData(1).getName(), is("pwd_cipher"));
        assertThat(actual.get().getColumnMetaData(2).getName(), is("pwd_plain"));
    }
    
    @Test
    public void assertLoadByExistedTables() throws SQLException {
        EncryptRule encryptRule = createEncryptRule();
        Collection<ShardingSphereRule> rules = Arrays.asList(createSingleTableRule(), encryptRule);
        EncryptTableMetaDataBuilder loader = (EncryptTableMetaDataBuilder) OrderedSPIRegistry.getRegisteredServices(RuleBasedTableMetaDataBuilder.class, rules).get(encryptRule);
        when(databaseType.formatTableNamePattern(TABLE_NAME)).thenReturn(TABLE_NAME);
        Collection<String> tableNames = new LinkedList<>();
        tableNames.add(TABLE_NAME);
        Map<String, TableMetaData> actual = loader.load(tableNames, encryptRule, new SchemaBuilderMaterials(databaseType, Collections.singletonMap("logic_db", dataSource), rules, props),
                new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2, Runtime.getRuntime().availableProcessors() * 2,
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ShardingSphere-SchemaBuilder-%d").build()));
        TableMetaData tableMetaData = actual.values().iterator().next();
        assertThat(tableMetaData.getColumnMetaData(0).getName(), is("id"));
        assertThat(tableMetaData.getColumnMetaData(1).getName(), is("pwd_cipher"));
        assertThat(tableMetaData.getColumnMetaData(2).getName(), is("pwd_plain"));
    }
    
    @Test
    public void assertLoadByExistedTablesH2() throws SQLException {
        EncryptRule encryptRule = createEncryptRule();
        Collection<ShardingSphereRule> rules = Arrays.asList(createSingleTableRule(), encryptRule);
        EncryptTableMetaDataBuilder loader = (EncryptTableMetaDataBuilder) OrderedSPIRegistry.getRegisteredServices(RuleBasedTableMetaDataBuilder.class, rules).get(encryptRule);
        Collection<String> tableNames = new LinkedList<>();
        tableNames.add(TABLE_NAME);
        ExecutorService executorService = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2, Runtime.getRuntime().availableProcessors() * 2,
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ShardingSphere-SchemaBuilder-%d").build());
        loadByH2(loader, tableNames, rules, encryptRule, executorService);
    }
    
    private void loadByH2(final EncryptTableMetaDataBuilder loader, final Collection<String> tableNames, final Collection<ShardingSphereRule> rules, final EncryptRule encryptRule,
                          final ExecutorService executorService) throws SQLException {
        when(databaseType.getName()).thenReturn("H2");
        Map<String, TableMetaData> actual = loader.load(tableNames, encryptRule, new SchemaBuilderMaterials(databaseType,
                Collections.singletonMap("logic_db", dataSource), rules, props), executorService);
        assertResult(actual);
    }
    
    @Test
    public void assertLoadByExistedTablesMySQL() throws SQLException {
        EncryptRule encryptRule = createEncryptRule();
        Collection<ShardingSphereRule> rules = Arrays.asList(createSingleTableRule(), encryptRule);
        EncryptTableMetaDataBuilder loader = (EncryptTableMetaDataBuilder) OrderedSPIRegistry.getRegisteredServices(RuleBasedTableMetaDataBuilder.class, rules).get(encryptRule);
        Collection<String> tableNames = new LinkedList<>();
        tableNames.add(TABLE_NAME);
        ExecutorService executorService = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2, Runtime.getRuntime().availableProcessors() * 2,
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ShardingSphere-SchemaBuilder-%d").build());
        loadByMySQL(loader, tableNames, rules, encryptRule, executorService);
    }
    
    private void loadByMySQL(final EncryptTableMetaDataBuilder loader, final Collection<String> tableNames, final Collection<ShardingSphereRule> rules, final EncryptRule encryptRule,
                             final ExecutorService executorService) throws SQLException {
        when(databaseType.getName()).thenReturn("MySQL");
        Map<String, TableMetaData> actual = loader.load(tableNames, encryptRule, new SchemaBuilderMaterials(databaseType,
                Collections.singletonMap("logic_db", dataSource), rules, props), executorService);
        assertResult(actual);
    }
    
    @Test
    public void assertLoadByExistedTablesOracle() throws SQLException {
        EncryptRule encryptRule = createEncryptRule();
        Collection<ShardingSphereRule> rules = Arrays.asList(createSingleTableRule(), encryptRule);
        EncryptTableMetaDataBuilder loader = (EncryptTableMetaDataBuilder) OrderedSPIRegistry.getRegisteredServices(RuleBasedTableMetaDataBuilder.class, rules).get(encryptRule);
        Collection<String> tableNames = new LinkedList<>();
        tableNames.add(TABLE_NAME);
        ExecutorService executorService = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2, Runtime.getRuntime().availableProcessors() * 2,
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ShardingSphere-SchemaBuilder-%d").build());
        loadByOracle(loader, tableNames, rules, encryptRule, executorService);
    }
    
    private void loadByOracle(final EncryptTableMetaDataBuilder loader, final Collection<String> tableNames, final Collection<ShardingSphereRule> rules, final EncryptRule encryptRule,
                              final ExecutorService executorService) throws SQLException {
        when(databaseType.getName()).thenReturn("Oracle");
        Map<String, TableMetaData> actual = loader.load(tableNames, encryptRule, new SchemaBuilderMaterials(databaseType,
                Collections.singletonMap("logic_db", dataSource), rules, props), executorService);
        assertResult(actual);
    }
    
    @Test
    public void assertLoadByExistedTablesPGSQL() throws SQLException {
        EncryptRule encryptRule = createEncryptRule();
        Collection<ShardingSphereRule> rules = Arrays.asList(createSingleTableRule(), encryptRule);
        EncryptTableMetaDataBuilder loader = (EncryptTableMetaDataBuilder) OrderedSPIRegistry.getRegisteredServices(RuleBasedTableMetaDataBuilder.class, rules).get(encryptRule);
        Collection<String> tableNames = new LinkedList<>();
        tableNames.add(TABLE_NAME);
        ExecutorService executorService = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2, Runtime.getRuntime().availableProcessors() * 2,
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ShardingSphere-SchemaBuilder-%d").build());
        loadByPostgreSQL(loader, tableNames, rules, encryptRule, executorService);
    }
    
    private void loadByPostgreSQL(final EncryptTableMetaDataBuilder loader, final Collection<String> tableNames, final Collection<ShardingSphereRule> rules, final EncryptRule encryptRule,
                                  final ExecutorService executorService) throws SQLException {
        when(databaseType.getName()).thenReturn("PostgreSQL");
        Map<String, TableMetaData> actual = loader.load(tableNames, encryptRule, new SchemaBuilderMaterials(databaseType,
                Collections.singletonMap("logic_db", dataSource), rules, props), executorService);
        assertResult(actual);
    }
    
    @Test
    public void assertLoadByExistedTablesSQLServer() throws SQLException {
        EncryptRule encryptRule = createEncryptRule();
        Collection<ShardingSphereRule> rules = Arrays.asList(createSingleTableRule(), encryptRule);
        EncryptTableMetaDataBuilder loader = (EncryptTableMetaDataBuilder) OrderedSPIRegistry.getRegisteredServices(RuleBasedTableMetaDataBuilder.class, rules).get(encryptRule);
        Collection<String> tableNames = new LinkedList<>();
        tableNames.add(TABLE_NAME);
        ExecutorService executorService = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2, Runtime.getRuntime().availableProcessors() * 2,
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ShardingSphere-SchemaBuilder-%d").build());
        loadBySQLServer(loader, tableNames, rules, encryptRule, executorService);
    }
    
    private void loadBySQLServer(final EncryptTableMetaDataBuilder loader, final Collection<String> tableNames, final Collection<ShardingSphereRule> rules, final EncryptRule encryptRule,
                                 final ExecutorService executorService) throws SQLException {
        when(databaseType.getName()).thenReturn("SQLServer");
        Map<String, TableMetaData> actual = loader.load(tableNames, encryptRule, new SchemaBuilderMaterials(databaseType,
                Collections.singletonMap("logic_db", dataSource), rules, props), executorService);
        assertResult(actual);
    }
    
    private void assertResult(final Map<String, TableMetaData> actual) {
        TableMetaData tableMetaData = actual.values().iterator().next();
        assertThat(tableMetaData.getColumnMetaData(0).getName(), is("id"));
        assertThat(tableMetaData.getColumnMetaData(1).getName(), is("pwd_cipher"));
        assertThat(tableMetaData.getColumnMetaData(2).getName(), is("pwd_plain"));
    }
    
    @Test
    public void assertLoadByNotExistedTable() throws SQLException {
        EncryptRule encryptRule = createEncryptRule();
        Collection<ShardingSphereRule> rules = Arrays.asList(createSingleTableRule(), encryptRule);
        EncryptTableMetaDataBuilder loader = new EncryptTableMetaDataBuilder();
        Optional<TableMetaData> actual = loader.load(
                "not_existed_table", databaseType, Collections.singletonMap("logic_db", dataSource), new DataNodes(rules), encryptRule, props);
        assertFalse(actual.isPresent());
    }
    
    @Test
    public void assertLoadByExistedTableAndMultiDataSources() throws SQLException {
        when(databaseType.formatTableNamePattern(TABLE_NAME)).thenReturn(TABLE_NAME);
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("logic_db", dataSource);
        dataSourceMap.put("logic_db_2", mock(DataSource.class));
        EncryptRule encryptRule = createEncryptRule();
        Collection<ShardingSphereRule> rules = Arrays.asList(createSingleTableRule(), encryptRule);
        EncryptTableMetaDataBuilder loader = (EncryptTableMetaDataBuilder) OrderedSPIRegistry.getRegisteredServices(RuleBasedTableMetaDataBuilder.class, rules).get(encryptRule);
        Optional<TableMetaData> actual = loader.load(TABLE_NAME, databaseType, dataSourceMap, new DataNodes(rules), encryptRule, props);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getColumnMetaData(0).getName(), is("id"));
        assertThat(actual.get().getColumnMetaData(1).getName(), is("pwd_cipher"));
        assertThat(actual.get().getColumnMetaData(2).getName(), is("pwd_plain"));
    }
    
    @Test
    public void assertLoadByNotExistedTableAndMultiDataSources() throws SQLException {
        EncryptRule encryptRule = createEncryptRule();
        Collection<ShardingSphereRule> rules = Arrays.asList(createSingleTableRule(), encryptRule);
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("logic_db", dataSource);
        dataSourceMap.put("logic_db_2", mock(DataSource.class));
        EncryptTableMetaDataBuilder loader = new EncryptTableMetaDataBuilder();
        Optional<TableMetaData> actual = loader.load("not_existed_table", databaseType, dataSourceMap, new DataNodes(rules), encryptRule, props);
        assertFalse(actual.isPresent());
    }
    
    @Test
    public void assertDecorate() {
        EncryptRule rule = createEncryptRule();
        EncryptTableMetaDataBuilder loader = (EncryptTableMetaDataBuilder) OrderedSPIRegistry.getRegisteredServices(RuleBasedTableMetaDataBuilder.class, Collections.singletonList(rule)).get(rule);
        TableMetaData actual = loader.decorate("t_encrypt", createTableMetaData(), rule);
        assertThat(actual.getColumns().size(), is(2));
        assertTrue(actual.getColumns().containsKey("id"));
        assertTrue(actual.getColumns().containsKey("pwd"));
    }
    
    private EncryptRule createEncryptRule() {
        EncryptRule result = mock(EncryptRule.class);
        EncryptTable encryptTable = mock(EncryptTable.class);
        when(result.findEncryptTable(TABLE_NAME)).thenReturn(Optional.of(encryptTable));
        when(encryptTable.getAssistedQueryColumns()).thenReturn(Collections.emptyList());
        when(encryptTable.getPlainColumns()).thenReturn(Collections.singleton("pwd_plain"));
        when(encryptTable.isCipherColumn("pwd_cipher")).thenReturn(true);
        when(encryptTable.getLogicColumn("pwd_cipher")).thenReturn("pwd");
        return result;
    }
    
    private TableMetaData createTableMetaData() {
        Collection<ColumnMetaData> columns = Arrays.asList(new ColumnMetaData("id", 1, true, true, true),
                new ColumnMetaData("pwd_cipher", 2, false, false, true),
                new ColumnMetaData("pwd_plain", 2, false, false, true));
        return new TableMetaData(TABLE_NAME, columns, Collections.emptyList());
    }
    
    private SingleTableRule createSingleTableRule() {
        SingleTableRule result = mock(SingleTableRule.class);
        when(result.getAllDataNodes()).thenReturn(Collections.singletonMap(TABLE_NAME, Collections.singletonList(new DataNode("logic_db", TABLE_NAME))));
        return result;
    }
}
