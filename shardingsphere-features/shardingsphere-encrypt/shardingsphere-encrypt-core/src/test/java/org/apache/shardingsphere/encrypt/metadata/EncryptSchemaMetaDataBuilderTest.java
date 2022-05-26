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

import org.apache.shardingsphere.encrypt.rule.EncryptColumn;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.encrypt.spi.context.EncryptColumnDataType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.spi.RuleBasedSchemaMetaDataBuilderFactory;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
import org.apache.shardingsphere.test.mock.MockedDataSource;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class EncryptSchemaMetaDataBuilderTest {
    
    private static final String TABLE_NAME = "t_encrypt";
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DatabaseType databaseType;
    
    private DataSource dataSource;
    
    @Before
    public void setUp() throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        mockH2ResultSet(connection);
        mockMySQLResultSet(connection);
        mockOracleResultSet(connection);
        mockPGResultSet(connection);
        mockSQLServerResultSet(connection);
        mockDatabase(connection);
        dataSource = new MockedDataSource(connection);
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
        when(connection.prepareStatement(startsWith("SELECT table_name, column_name, ordinal_position, data_type, udt_name, column_default, table_schema"))).thenReturn(preparedStatement);
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
        when(connection.prepareStatement(startsWith("SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, COLUMN_KEY, EXTRA, COLLATION_NAME, ORDINAL_POSITION FROM information_schema.columns")))
                .thenReturn(preparedStatement);
    }
    
    private void mockH2ResultSet(final Connection connection) throws SQLException {
        ResultSet resultSet = createColumnResultSet();
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(startsWith("SELECT TABLE_CATALOG, TABLE_NAME"))).thenReturn(preparedStatement);
    }
    
    private void mockDatabase(final Connection connection) throws SQLException {
        DatabaseMetaData database = mock(DatabaseMetaData.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData()).thenReturn(database);
        ResultSet dataTypeResultSet = createDataTypeResultSet();
        when(database.getTypeInfo()).thenReturn(dataTypeResultSet);
        ResultSet tableResultSet = createTableResultSet();
        ResultSet columnResultSet = createColumnResultSet();
        when(database.getTables(any(), any(), any(), eq(null))).thenReturn(tableResultSet);
        when(database.getColumns(any(), any(), any(), eq("%"))).thenReturn(columnResultSet);
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
        when(result.getString("table_schema")).thenReturn("public");
        return result;
    }
    
    private ResultSet createDataTypeResultSet() throws SQLException {
        ResultSet dataTypeResultSet = mock(ResultSet.class);
        when(dataTypeResultSet.next()).thenReturn(true, true, false);
        when(dataTypeResultSet.getString("TYPE_NAME")).thenReturn("INT", "VARCHAR");
        when(dataTypeResultSet.getInt("DATA_TYPE")).thenReturn(1, 12);
        return dataTypeResultSet;
    }
    
    @Test
    public void assertLoadByTablesWithDefaultLoader() throws SQLException {
        EncryptRule encryptRule = createEncryptRule();
        Collection<ShardingSphereRule> rules = Arrays.asList(createSingleTableRule(), encryptRule);
        EncryptSchemaMetaDataBuilder loader = getEncryptMetaDataBuilder(encryptRule, rules);
        when(databaseType.formatTableNamePattern(TABLE_NAME)).thenReturn(TABLE_NAME);
        Map<String, SchemaMetaData> actual = loader.load(Collections.singleton(TABLE_NAME), encryptRule,
                new GenericSchemaBuilderMaterials(databaseType, databaseType, Collections.singletonMap("logic_db", dataSource), rules, new ConfigurationProperties(new Properties()), "logic_db"));
        TableMetaData tableMetaData = actual.get("logic_db").getTables().values().iterator().next();
        List<String> columnNames = new ArrayList<>(tableMetaData.getColumns().keySet());
        assertThat(tableMetaData.getColumns().get(columnNames.get(0)).getName(), is("id"));
        assertThat(tableMetaData.getColumns().get(columnNames.get(1)).getName(), is("pwd_cipher"));
    }
    
    @Test
    public void assertLoadByTablesH2() throws SQLException {
        EncryptRule encryptRule = createEncryptRule();
        Collection<ShardingSphereRule> rules = Arrays.asList(createSingleTableRule(), encryptRule);
        loadByH2(getEncryptMetaDataBuilder(encryptRule, rules), Collections.singleton(TABLE_NAME), rules, encryptRule);
    }
    
    private void loadByH2(final EncryptSchemaMetaDataBuilder loader, final Collection<String> tableNames, final Collection<ShardingSphereRule> rules,
                          final EncryptRule encryptRule) throws SQLException {
        when(databaseType.getType()).thenReturn("H2");
        Map<String, SchemaMetaData> actual = loader.load(tableNames, encryptRule,
                new GenericSchemaBuilderMaterials(databaseType, databaseType, Collections.singletonMap("logic_db", dataSource), rules, new ConfigurationProperties(new Properties()), "logic_db"));
        assertResult(actual, "logic_db");
    }
    
    @Test
    public void assertLoadByTablesMySQL() throws SQLException {
        EncryptRule encryptRule = createEncryptRule();
        Collection<ShardingSphereRule> rules = Arrays.asList(createSingleTableRule(), encryptRule);
        loadByMySQL(getEncryptMetaDataBuilder(encryptRule, rules), Collections.singleton(TABLE_NAME), rules, encryptRule);
    }
    
    private void loadByMySQL(final EncryptSchemaMetaDataBuilder loader, final Collection<String> tableNames, final Collection<ShardingSphereRule> rules,
                             final EncryptRule encryptRule) throws SQLException {
        when(databaseType.getType()).thenReturn("MySQL");
        Map<String, SchemaMetaData> actual = loader.load(tableNames, encryptRule,
                new GenericSchemaBuilderMaterials(databaseType, databaseType, Collections.singletonMap("logic_db", dataSource), rules, new ConfigurationProperties(new Properties()), "logic_db"));
        assertResult(actual, "logic_db");
    }
    
    @Test
    public void assertLoadByTablesOracle() throws SQLException {
        EncryptRule encryptRule = createEncryptRule();
        Collection<ShardingSphereRule> rules = Arrays.asList(createSingleTableRule(), encryptRule);
        loadByOracle(getEncryptMetaDataBuilder(encryptRule, rules), Collections.singleton(TABLE_NAME), rules, encryptRule);
    }
    
    private void loadByOracle(final EncryptSchemaMetaDataBuilder loader, final Collection<String> tableNames, final Collection<ShardingSphereRule> rules,
                              final EncryptRule encryptRule) throws SQLException {
        when(databaseType.getType()).thenReturn("Oracle");
        Map<String, SchemaMetaData> actual = loader.load(tableNames, encryptRule,
                new GenericSchemaBuilderMaterials(databaseType, databaseType, Collections.singletonMap("logic_db", dataSource), rules, new ConfigurationProperties(new Properties()), "logic_db"));
        assertResult(actual, "logic_db");
    }
    
    @Test
    public void assertLoadByTablesPGSQL() throws SQLException {
        EncryptRule encryptRule = createEncryptRule();
        Collection<ShardingSphereRule> rules = Arrays.asList(createSingleTableRule(), encryptRule);
        loadByPostgreSQL(getEncryptMetaDataBuilder(encryptRule, rules), Collections.singleton(TABLE_NAME), rules, encryptRule);
    }
    
    private void loadByPostgreSQL(final EncryptSchemaMetaDataBuilder loader, final Collection<String> tableNames, final Collection<ShardingSphereRule> rules,
                                  final EncryptRule encryptRule) throws SQLException {
        when(databaseType.getType()).thenReturn("PostgreSQL");
        ResultSet roleTableGrantsResultSet = mockRoleTableGrantsResultSet();
        when(dataSource.getConnection().prepareStatement(startsWith("SELECT table_name FROM information_schema.role_table_grants")).executeQuery()).thenReturn(roleTableGrantsResultSet);
        ResultSet schemaMetaData = mockSchemaMetaData();
        when(dataSource.getConnection().getMetaData().getSchemas()).thenReturn(schemaMetaData);
        Map<String, SchemaMetaData> actual = loader.load(tableNames, encryptRule,
                new GenericSchemaBuilderMaterials(databaseType, databaseType, Collections.singletonMap("logic_db", dataSource), rules, new ConfigurationProperties(new Properties()), "logic_db"));
        assertResult(actual, "public");
    }
    
    private ResultSet mockSchemaMetaData() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("TABLE_SCHEM")).thenReturn("public");
        return result;
    }
    
    private ResultSet mockRoleTableGrantsResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("table_name")).thenReturn(TABLE_NAME);
        return result;
    }
    
    @Test
    public void assertLoadByTablesSQLServer() throws SQLException {
        EncryptRule encryptRule = createEncryptRule();
        Collection<ShardingSphereRule> rules = Arrays.asList(createSingleTableRule(), encryptRule);
        loadBySQLServer(getEncryptMetaDataBuilder(encryptRule, rules), Collections.singleton(TABLE_NAME), rules, encryptRule);
    }
    
    private void loadBySQLServer(final EncryptSchemaMetaDataBuilder loader, final Collection<String> tableNames, final Collection<ShardingSphereRule> rules,
                                 final EncryptRule encryptRule) throws SQLException {
        when(databaseType.getType()).thenReturn("SQLServer");
        Map<String, SchemaMetaData> actual = loader.load(tableNames, encryptRule,
                new GenericSchemaBuilderMaterials(databaseType, databaseType, Collections.singletonMap("logic_db", dataSource), rules, new ConfigurationProperties(new Properties()), "logic_db"));
        assertResult(actual, "logic_db");
    }
    
    private void assertResult(final Map<String, SchemaMetaData> schemaMetaDataMap, final String schemaName) {
        TableMetaData tableMetaData = schemaMetaDataMap.get(schemaName).getTables().values().iterator().next();
        List<String> columnNames = new ArrayList<>(tableMetaData.getColumns().keySet());
        assertThat(tableMetaData.getColumns().get(columnNames.get(0)).getName(), is("id"));
        assertThat(tableMetaData.getColumns().get(columnNames.get(1)).getName(), is("pwd_cipher"));
    }
    
    @Test
    public void assertLoadByNotExistedTables() throws SQLException {
        EncryptRule encryptRule = createEncryptRule();
        Collection<ShardingSphereRule> rules = Arrays.asList(createSingleTableRule(), encryptRule);
        EncryptSchemaMetaDataBuilder loader = new EncryptSchemaMetaDataBuilder();
        Map<String, SchemaMetaData> actual = loader.load(Collections.singleton("not_existed_table"), encryptRule,
                new GenericSchemaBuilderMaterials(databaseType, databaseType, Collections.singletonMap("logic_db", dataSource), rules, new ConfigurationProperties(new Properties()), "logic_db"));
        assertTrue(actual.isEmpty());
    }
    
    @Test
    public void assertLoadByTablesAndMultiDataSources() throws SQLException {
        when(databaseType.formatTableNamePattern(TABLE_NAME)).thenReturn(TABLE_NAME);
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("logic_db", dataSource);
        dataSourceMap.put("logic_db_2", new MockedDataSource());
        EncryptRule encryptRule = createEncryptRule();
        Collection<ShardingSphereRule> rules = Arrays.asList(createSingleTableRule(), encryptRule);
        EncryptSchemaMetaDataBuilder loader = getEncryptMetaDataBuilder(encryptRule, rules);
        Map<String, SchemaMetaData> actual = loader.load(Collections.singleton(TABLE_NAME),
                encryptRule, new GenericSchemaBuilderMaterials(databaseType, databaseType, dataSourceMap, rules, new ConfigurationProperties(new Properties()), "logic_db"));
        TableMetaData tableMetaData = actual.get("logic_db").getTables().values().iterator().next();
        List<String> actualColumnNames = new ArrayList<>(tableMetaData.getColumns().keySet());
        assertThat(tableMetaData.getColumns().get(actualColumnNames.get(0)).getName(), is("id"));
        assertThat(tableMetaData.getColumns().get(actualColumnNames.get(1)).getName(), is("pwd_cipher"));
    }
    
    @Test
    public void assertLoadByNotExistedTableAndMultiDataSources() throws SQLException {
        EncryptRule encryptRule = createEncryptRule();
        Collection<ShardingSphereRule> rules = Arrays.asList(createSingleTableRule(), encryptRule);
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        dataSourceMap.put("logic_db", dataSource);
        dataSourceMap.put("logic_db_2", new MockedDataSource());
        EncryptSchemaMetaDataBuilder loader = new EncryptSchemaMetaDataBuilder();
        Map<String, SchemaMetaData> actual = loader.load(Collections.singleton("not_existed_table"),
                encryptRule, new GenericSchemaBuilderMaterials(databaseType, databaseType, dataSourceMap, rules, new ConfigurationProperties(new Properties()), "logic_db"));
        assertTrue(actual.isEmpty());
    }
    
    @Test
    public void assertDecorate() throws SQLException {
        EncryptRule rule = createEncryptRule();
        EncryptSchemaMetaDataBuilder loader = getEncryptMetaDataBuilder(rule, Collections.singleton(rule));
        Map<String, TableMetaData> tableMetaDataMap = new LinkedHashMap<>();
        tableMetaDataMap.put("t_encrypt", createTableMetaData());
        TableMetaData actual = loader.decorate(Collections.singletonMap("logic_db",
                new SchemaMetaData("logic_db", tableMetaDataMap)), rule, mock(GenericSchemaBuilderMaterials.class)).get("logic_db").getTables().get("t_encrypt");
        assertThat(actual.getColumns().size(), is(2));
        assertTrue(actual.getColumns().containsKey("id"));
        assertTrue(actual.getColumns().containsKey("pwd"));
    }
    
    @Test
    public void assertDecorateWithConfigDataType() throws SQLException {
        EncryptRule rule = createEncryptRuleWithDataTypeConfig();
        EncryptSchemaMetaDataBuilder loader = getEncryptMetaDataBuilder(rule, Collections.singleton(rule));
        Map<String, TableMetaData> tableMetaDataMap = new LinkedHashMap<>();
        tableMetaDataMap.put("t_encrypt", createTableMetaData());
        GenericSchemaBuilderMaterials materials = mock(GenericSchemaBuilderMaterials.class, RETURNS_DEEP_STUBS);
        when(materials.getDataSourceMap().values().stream().findAny()).thenReturn(Optional.of(dataSource));
        TableMetaData actual = loader.decorate(Collections.singletonMap("logic_db", new SchemaMetaData("logic_db", tableMetaDataMap)),
                rule, materials).get("logic_db").getTables().get("t_encrypt");
        assertThat(actual.getColumns().size(), is(2));
        assertTrue(actual.getColumns().containsKey("id"));
        assertTrue(actual.getColumns().containsKey("pwd"));
        assertThat(actual.getColumns().get("pwd").getDataType(), is(12));
    }
    
    private EncryptRule createEncryptRuleWithDataTypeConfig() {
        EncryptRule result = createEncryptRule();
        assertTrue(result.findEncryptTable(TABLE_NAME).isPresent());
        EncryptTable encryptTable = result.findEncryptTable(TABLE_NAME).get();
        EncryptColumn encryptColumn = mock(EncryptColumn.class);
        EncryptColumnDataType encryptColumnDataType = mock(EncryptColumnDataType.class);
        when(encryptColumnDataType.getDataType()).thenReturn(12);
        when(encryptColumn.getLogicDataType()).thenReturn(encryptColumnDataType);
        when(encryptTable.findEncryptColumn("pwd")).thenReturn(Optional.of(encryptColumn));
        return result;
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
        return new TableMetaData(TABLE_NAME, columns, Collections.emptyList(), Collections.emptyList());
    }
    
    private SingleTableRule createSingleTableRule() {
        return mock(SingleTableRule.class);
    }
    
    private EncryptSchemaMetaDataBuilder getEncryptMetaDataBuilder(final EncryptRule encryptRule, final Collection<ShardingSphereRule> rules) {
        return (EncryptSchemaMetaDataBuilder) RuleBasedSchemaMetaDataBuilderFactory.getInstances(rules).get(encryptRule);
    }
}
