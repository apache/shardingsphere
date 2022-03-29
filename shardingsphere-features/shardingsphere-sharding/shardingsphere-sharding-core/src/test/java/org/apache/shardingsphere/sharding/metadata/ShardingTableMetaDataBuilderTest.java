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

package org.apache.shardingsphere.sharding.metadata;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OracleDatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.loader.spi.DialectTableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.RuleBasedTableMetaDataBuilder;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.ordered.OrderedSPIRegistry;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
public class ShardingTableMetaDataBuilderTest {
    
    private static final String TABLE_NAME = "t_order";
    
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
    
    private ShardingRule shardingRule;
    
    @Before
    public void setUp() throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(dataSource.getConnection()).thenReturn(connection);
        shardingRule = createShardingRule();
        mockH2ResultSet(connection);
        mockMySQLResultSet(connection);
        mockOracleResultSet(connection);
        mockPGResultSet(connection);
        mockSQLServerResultSet(connection);
        mockDatabaseMetaData(connection);
    }
    
    private ShardingRule createShardingRule() {
        ShardingTableRuleConfiguration tableRuleConfig = new ShardingTableRuleConfiguration(TABLE_NAME, "ds.t_order_${0..1}");
        tableRuleConfig.setKeyGenerateStrategy(new KeyGenerateStrategyConfiguration("product_id", "snowflake"));
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(tableRuleConfig);
        return new ShardingRule(shardingRuleConfig, Collections.singletonList("ds"));
    }
    
    private void mockSQLServerResultSet(final Connection connection) throws SQLException {
        ResultSet resultSet = createColumnResultSet("t_order_0");
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(startsWith("SELECT obj.name AS TABLE_NAME, col.name AS COLUMN_NAME, t.name AS DATA_TYPE"))).thenReturn(preparedStatement);
        ResultSet indexResultSet = createIndexResultSet();
        PreparedStatement indexStatement = mock(PreparedStatement.class);
        when(indexStatement.executeQuery()).thenReturn(indexResultSet);
        when(connection.prepareStatement(startsWith("SELECT a.name AS INDEX_NAME, c.name AS TABLE_NAME FROM sys.indexes a"))).thenReturn(indexStatement);
    }
    
    private void mockPGResultSet(final Connection connection) throws SQLException {
        ResultSet resultSet = createColumnResultSetForPostgreSQL();
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(startsWith("SELECT table_name, column_name, ordinal_position, data_type, udt_name, column_default"))).thenReturn(preparedStatement);
        ResultSet indexResultSet = createPostgreSQLIndexResultSet();
        PreparedStatement indexStatement = mock(PreparedStatement.class);
        when(indexStatement.executeQuery()).thenReturn(indexResultSet);
        when(connection.prepareStatement(startsWith("SELECT tablename, indexname FROM pg_indexes WHERE schemaname"))).thenReturn(indexStatement);
    }
    
    private void mockOracleResultSet(final Connection connection) throws SQLException {
        ResultSet resultSet = createColumnResultSetForOracle();
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(startsWith("SELECT OWNER AS TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, DATA_TYPE"))).thenReturn(preparedStatement);
        ResultSet indexResultSet = createIndexResultSetForOracle();
        PreparedStatement indexStatement = mock(PreparedStatement.class);
        when(indexStatement.executeQuery()).thenReturn(indexResultSet);
        when(connection.prepareStatement(startsWith("SELECT OWNER AS TABLE_SCHEMA, TABLE_NAME, INDEX_NAME FROM ALL_INDEXES WHERE OWNER"))).thenReturn(indexStatement);
    }
    
    private void mockMySQLResultSet(final Connection connection) throws SQLException {
        ResultSet resultSet = createColumnResultSet("t_order_0");
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(startsWith("SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, COLUMN_KEY, EXTRA, COLLATION_NAME, ORDINAL_POSITION FROM information_schema.columns")))
                .thenReturn(preparedStatement);
        ResultSet indexResultSet = createIndexResultSet();
        PreparedStatement indexStatement = mock(PreparedStatement.class);
        when(indexStatement.executeQuery()).thenReturn(indexResultSet);
        when(connection.prepareStatement(startsWith("SELECT TABLE_NAME, INDEX_NAME FROM information_schema.statistics WHERE TABLE_SCHEMA"))).thenReturn(indexStatement);
    }
    
    private void mockH2ResultSet(final Connection connection) throws SQLException {
        ResultSet resultSet = createColumnResultSet("t_order_0");
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(startsWith("SELECT TABLE_CATALOG, TABLE_NAME"))).thenReturn(preparedStatement);
        ResultSet indexResultSet = createIndexResultSet();
        PreparedStatement indexStatement = mock(PreparedStatement.class);
        when(indexStatement.executeQuery()).thenReturn(indexResultSet);
        when(connection.prepareStatement(startsWith("SELECT TABLE_CATALOG, TABLE_NAME, INDEX_NAME, COLUMN_NAME FROM INFORMATION_SCHEMA.INDEXES"))).thenReturn(indexStatement);
    }
    
    private ResultSet createIndexResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("INDEX_NAME")).thenReturn("order_index_t_order_t_order_0");
        when(result.getString("TABLE_NAME")).thenReturn("t_order_0");
        return result;
    }
    
    private ResultSet createPostgreSQLIndexResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("indexname")).thenReturn("order_index_t_order_t_order_0");
        when(result.getString("tablename")).thenReturn("t_order_0");
        return result;
    }
    
    private ResultSet createIndexResultSetForOracle() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("INDEX_NAME")).thenReturn("ORDER_INDEX_T_ORDER_T_ORDER_0");
        when(result.getString("TABLE_NAME")).thenReturn("T_ORDER_0");
        return result;
    }
    
    private void mockDatabaseMetaData(final Connection connection) throws SQLException {
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        ResultSet dataTypeResultSet = createDataTypeResultSet();
        when(databaseMetaData.getTypeInfo()).thenReturn(dataTypeResultSet);
        ResultSet tableResultSet1 = createTableResultSet();
        ResultSet tableResultSet2 = createTableResultSet();
        ResultSet columnResultSet1 = createColumnResultSet("t_order_0");
        ResultSet columnResultSet2 = createColumnResultSet("t_order_1");
        when(databaseMetaData.getTables(any(), any(), eq("t_order_0"), eq(null))).thenReturn(tableResultSet1);
        when(databaseMetaData.getTables(any(), any(), eq("t_order_1"), eq(null))).thenReturn(tableResultSet2);
        when(databaseMetaData.getColumns(any(), any(), eq("t_order_0"), eq("%"))).thenReturn(columnResultSet1);
        when(databaseMetaData.getColumns(any(), any(), eq("t_order_1"), eq("%"))).thenReturn(columnResultSet2);
    }
    
    private ResultSet createTableResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        return result;
    }
    
    private ResultSet createColumnResultSetForPostgreSQL() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, true, false);
        when(result.getString("table_name")).thenReturn("t_order_0");
        when(result.getString("column_name")).thenReturn("id", "pwd_cipher", "pwd_plain");
        when(result.getString("udt_name")).thenReturn("INT");
        when(result.getInt("ordinal_position")).thenReturn(1, 2, 3);
        return result;
    }
    
    private ResultSet createColumnResultSetForOracle() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, true, false);
        when(result.getString("TABLE_NAME")).thenReturn("T_ORDER_0");
        when(result.getString("COLUMN_NAME")).thenReturn("ID", "PWD_CIPHER", "PWD_PLAIN");
        when(result.getString("DATA_TYPE")).thenReturn("INT");
        return result;
    }
    
    private ResultSet createColumnResultSet(final String actualTable) throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, true, false);
        when(result.getString("TABLE_NAME")).thenReturn(actualTable);
        when(result.getString("COLUMN_NAME")).thenReturn("id", "pwd_cipher", "pwd_plain");
        when(result.getString("TYPE_NAME")).thenReturn("INT");
        when(result.getString("DATA_TYPE")).thenReturn("INT");
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
    public void assertLoadTablesH2() throws SQLException {
        when(props.getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED)).thenReturn(false);
        when(databaseType.getName()).thenReturn("H2");
        Collection<String> tableNames = new LinkedList<>();
        tableNames.add(TABLE_NAME);
        Collection<ShardingSphereRule> rules = Collections.singletonList(shardingRule);
        ShardingTableMetaDataBuilder loader = (ShardingTableMetaDataBuilder) OrderedSPIRegistry.getRegisteredServices(RuleBasedTableMetaDataBuilder.class, rules).get(shardingRule);
        Map<String, TableMetaData> actual = loader.load(tableNames, shardingRule, new SchemaBuilderMaterials(databaseType, Collections.singletonMap("ds", dataSource), rules, props));
        assertResult(actual);
    }
    
    @Test
    public void assertLoadTablesMySQL() throws SQLException {
        when(props.getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED)).thenReturn(false);
        when(databaseType.getName()).thenReturn("MySQL");
        Collection<String> tableNames = new LinkedList<>();
        tableNames.add(TABLE_NAME);
        Collection<ShardingSphereRule> rules = Collections.singletonList(shardingRule);
        ShardingTableMetaDataBuilder loader = (ShardingTableMetaDataBuilder) OrderedSPIRegistry.getRegisteredServices(RuleBasedTableMetaDataBuilder.class, rules).get(shardingRule);
        Map<String, TableMetaData> actual = loader.load(tableNames, shardingRule, new SchemaBuilderMaterials(databaseType, Collections.singletonMap("ds", dataSource), rules, props));
        assertResult(actual);
    }
    
    @Test
    public void assertLoadTablesOracle() throws SQLException {
        ShardingRule shardingRule = createShardingRuleForOracle();
        Collection<ShardingSphereRule> rules = Collections.singletonList(shardingRule);
        ShardingTableMetaDataBuilder loader = (ShardingTableMetaDataBuilder) OrderedSPIRegistry.getRegisteredServices(RuleBasedTableMetaDataBuilder.class, rules).get(shardingRule);
        when(props.getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED)).thenReturn(false);
        DatabaseType databaseType = mock(OracleDatabaseType.class);
        when(databaseType.getName()).thenReturn("Oracle");
        Map<String, TableMetaData> actual = loader.load(Collections.singletonList(TABLE_NAME), shardingRule, 
                new SchemaBuilderMaterials(databaseType, Collections.singletonMap("ds", dataSource), rules, props));
        assertThat(actual.keySet().iterator().next(), is("t_order"));
        TableMetaData tableMetaData = actual.values().iterator().next();
        List<String> actualColumnNames = new ArrayList<>(tableMetaData.getColumns().keySet());
        assertThat(tableMetaData.getColumns().get(actualColumnNames.get(0)).getName(), is("ID"));
        assertThat(tableMetaData.getColumns().get(actualColumnNames.get(1)).getName(), is("PWD_CIPHER"));
        assertThat(tableMetaData.getColumns().get(actualColumnNames.get(2)).getName(), is("PWD_PLAIN"));
        assertThat(tableMetaData.getIndexes().values().iterator().next().getName(), is("ORDER_INDEX_T_ORDER_T_ORDER_0"));
    }
    
    private ShardingRule createShardingRuleForOracle() {
        ShardingTableRuleConfiguration tableRuleConfig = new ShardingTableRuleConfiguration(TABLE_NAME, "ds.T_ORDER_${0..1}");
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(tableRuleConfig);
        return new ShardingRule(shardingRuleConfig, Collections.singletonList("ds"));
    }
    
    @Test
    public void assertLoadTablesPGSQL() throws SQLException {
        when(props.getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED)).thenReturn(false);
        when(databaseType.getName()).thenReturn("PostgreSQL");
        Collection<String> tableNames = new LinkedList<>();
        tableNames.add(TABLE_NAME);
        Collection<ShardingSphereRule> rules = Collections.singletonList(shardingRule);
        ShardingTableMetaDataBuilder loader = (ShardingTableMetaDataBuilder) OrderedSPIRegistry.getRegisteredServices(RuleBasedTableMetaDataBuilder.class, rules).get(shardingRule);
        ResultSet roleTableGrantsResultSet = mockRoleTableGrantsResultSet();
        when(dataSource.getConnection().prepareStatement(startsWith("SELECT table_name FROM information_schema.role_table_grants")).executeQuery()).thenReturn(roleTableGrantsResultSet);
        Map<String, TableMetaData> actual = loader.load(tableNames, shardingRule, new SchemaBuilderMaterials(databaseType, Collections.singletonMap("ds", dataSource), rules, props));
        assertResult(actual);
    }
    
    private ResultSet mockRoleTableGrantsResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("table_name")).thenReturn("t_order_0");
        return result;
    }
    
    @Test
    public void assertLoadTablesSQLServer() throws SQLException {
        when(props.getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED)).thenReturn(false);
        when(databaseType.getName()).thenReturn("SQLServer");
        Collection<String> tableNames = new LinkedList<>();
        tableNames.add(TABLE_NAME);
        Collection<ShardingSphereRule> rules = Collections.singletonList(shardingRule);
        ShardingTableMetaDataBuilder loader = (ShardingTableMetaDataBuilder) OrderedSPIRegistry.getRegisteredServices(RuleBasedTableMetaDataBuilder.class, rules).get(shardingRule);
        Map<String, TableMetaData> actual = loader.load(tableNames, shardingRule, new SchemaBuilderMaterials(databaseType, Collections.singletonMap("ds", dataSource), rules, props));
        assertResult(actual);
    }
    
    private void assertResult(final Map<String, TableMetaData> actual) {
        TableMetaData tableMetaData = actual.values().iterator().next();
        List<String> actualColumnNames = new ArrayList<>(tableMetaData.getColumns().keySet());
        assertThat(tableMetaData.getColumns().get(actualColumnNames.get(0)).getName(), is("id"));
        assertThat(tableMetaData.getColumns().get(actualColumnNames.get(1)).getName(), is("pwd_cipher"));
        assertThat(tableMetaData.getColumns().get(actualColumnNames.get(2)).getName(), is("pwd_plain"));
        IndexMetaData indexMetaData = tableMetaData.getIndexes().values().iterator().next();
        assertThat(indexMetaData.getName(), is("order_index_t_order_t_order_0"));
    }
    
    @Test
    public void assertLoadTablesDefault() throws SQLException {
        when(props.getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED)).thenReturn(false);
        when(databaseType.getName()).thenReturn("default");
        when(databaseType.formatTableNamePattern("t_order_0")).thenReturn("t_order_0");
        Collection<String> tableNames = new LinkedList<>();
        tableNames.add(TABLE_NAME);
        Collection<ShardingSphereRule> rules = Collections.singletonList(shardingRule);
        ShardingTableMetaDataBuilder loader = (ShardingTableMetaDataBuilder) OrderedSPIRegistry.getRegisteredServices(RuleBasedTableMetaDataBuilder.class, rules).get(shardingRule);
        Map<String, TableMetaData> actual = loader.load(tableNames, shardingRule, new SchemaBuilderMaterials(databaseType, Collections.singletonMap("ds", dataSource), rules, props));
        TableMetaData tableMetaData = actual.values().iterator().next();
        List<String> actualColumnNames = new ArrayList<>(tableMetaData.getColumns().keySet());
        assertThat(tableMetaData.getColumns().get(actualColumnNames.get(0)).getName(), is("id"));
        assertThat(tableMetaData.getColumns().get(actualColumnNames.get(1)).getName(), is("pwd_cipher"));
        assertThat(tableMetaData.getColumns().get(actualColumnNames.get(2)).getName(), is("pwd_plain"));
    }
    
    @Test
    public void assertLoadTablesWithCheck() throws SQLException {
        when(props.getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED)).thenReturn(true);
        when(databaseType.formatTableNamePattern("t_order_0")).thenReturn("t_order_0");
        when(databaseType.formatTableNamePattern("t_order_1")).thenReturn("t_order_1");
        Collection<String> tableNames = new LinkedList<>();
        tableNames.add(TABLE_NAME);
        Collection<ShardingSphereRule> rules = Collections.singletonList(shardingRule);
        ShardingTableMetaDataBuilder loader = (ShardingTableMetaDataBuilder) OrderedSPIRegistry.getRegisteredServices(RuleBasedTableMetaDataBuilder.class, rules).get(shardingRule);
        Map<String, TableMetaData> actual = loader.load(tableNames, shardingRule, new SchemaBuilderMaterials(databaseType, Collections.singletonMap("ds", dataSource), rules, props));
        TableMetaData tableMetaData = actual.values().iterator().next();
        List<String> actualColumnNames = new ArrayList<>(tableMetaData.getColumns().keySet());
        assertThat(tableMetaData.getColumns().get(actualColumnNames.get(0)).getName(), is("id"));
        assertThat(tableMetaData.getColumns().get(actualColumnNames.get(1)).getName(), is("pwd_cipher"));
        assertThat(tableMetaData.getColumns().get(actualColumnNames.get(2)).getName(), is("pwd_plain"));
    }
    
    @Test
    public void assertDecorateWithKeyGenerateStrategy() {
        Collection<ShardingSphereRule> rules = Collections.singletonList(shardingRule);
        ShardingTableMetaDataBuilder builder = (ShardingTableMetaDataBuilder) OrderedSPIRegistry.getRegisteredServices(RuleBasedTableMetaDataBuilder.class, rules).get(shardingRule);
        Map<String, TableMetaData> tableMetaDataMap = new LinkedHashMap<>();
        tableMetaDataMap.put(TABLE_NAME, createTableMetaData());
        Map<String, ColumnMetaData> columns = builder.decorate(tableMetaDataMap, shardingRule, mock(SchemaBuilderMaterials.class)).get(TABLE_NAME).getColumns();
        Iterator<ColumnMetaData> iterator = columns.values().iterator();
        assertFalse(iterator.next().isGenerated());
        assertFalse(iterator.next().isGenerated());
        assertFalse(iterator.next().isGenerated());
        assertTrue(iterator.next().isGenerated());
    }
    
    private TableMetaData createTableMetaData() {
        Collection<ColumnMetaData> columns = Arrays.asList(new ColumnMetaData("id", 1, true, true, true),
                new ColumnMetaData("pwd_cipher", 2, false, false, true),
                new ColumnMetaData("pwd_plain", 2, false, false, true),
                new ColumnMetaData("product_id", 2, false, false, true));
        return new TableMetaData(TABLE_NAME, columns, Collections.emptyList(), Collections.emptyList());
    }
}
