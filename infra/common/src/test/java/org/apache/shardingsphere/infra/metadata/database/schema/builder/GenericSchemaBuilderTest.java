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

package org.apache.shardingsphere.infra.metadata.database.schema.builder;

import ch.qos.logback.classic.Level;
import org.apache.shardingsphere.database.connector.core.exception.RuleAndStorageMetaDataMismatchedException;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoader;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderMaterial;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContextFactory;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.infra.framework.extension.log.LogCaptureAssertion;
import org.apache.shardingsphere.test.infra.framework.extension.log.LogCaptureExtension;
import org.apache.shardingsphere.test.infra.framework.extension.log.LogCaptureSettings;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({AutoMockExtension.class, LogCaptureExtension.class})
@LogCaptureSettings("org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder")
@MockitoSettings(strictness = Strictness.LENIENT)
@StaticMockSettings(MetaDataLoader.class)
class GenericSchemaBuilderTest {
    
    private static final DatabaseType H2_DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "H2");
    
    private static final DatabaseType MYSQL_DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private static final DatabaseType POSTGRESQL_DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private static final DatabaseType OPEN_GAUSS_DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "openGauss");
    
    private static final DatabaseType ORACLE_DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "Oracle");
    
    private static final DatabaseType SQL_SERVER_DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "SQLServer");
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private GenericSchemaBuilderMaterial material;
    
    @BeforeEach
    void setUp() {
        material = createMaterial(databaseType, "foo_schema");
    }
    
    @Test
    void assertLoadWithExistedTableName() throws SQLException {
        Collection<String> tableNames = Collections.singleton("foo_tbl");
        when(MetaDataLoader.load(any())).thenReturn(createSchemaMetaDataMap(tableNames, material));
        assertFalse(GenericSchemaBuilder.build(tableNames, databaseType, material).get("foo_schema").getAllTables().isEmpty());
    }
    
    @Test
    void assertLoadWithNotExistedTableName() throws SQLException {
        Collection<String> tableNames = Collections.singleton("invalid_table");
        when(MetaDataLoader.load(any())).thenReturn(createSchemaMetaDataMap(tableNames, material));
        assertTrue(GenericSchemaBuilder.build(tableNames, databaseType, material).get("foo_schema").getAllTables().isEmpty());
    }
    
    @Test
    void assertLoadAllTables() throws SQLException {
        Collection<String> tableNames = Arrays.asList("foo_tbl", "bar_tbl");
        when(MetaDataLoader.load(any())).thenReturn(createSchemaMetaDataMap(tableNames, material));
        Map<String, ShardingSphereSchema> actual = GenericSchemaBuilder.build(tableNames, databaseType, material);
        assertThat(actual.size(), is(1));
        assertTables(new ShardingSphereSchema("foo_schema", databaseType, actual.values().iterator().next().getAllTables(), Collections.emptyList()));
    }
    
    @Test
    void assertBuildWithEmptyTableNames() throws SQLException {
        when(MetaDataLoader.load(any())).thenReturn(Collections.emptyMap());
        Map<String, ShardingSphereSchema> actual = GenericSchemaBuilder.build(Collections.emptyList(), databaseType, material);
        assertThat(actual.size(), is(1));
        assertTrue(actual.get("foo_schema").getAllTables().isEmpty());
    }
    
    @Test
    void assertBuildWithGetAllTableNamesFromRules() throws SQLException {
        TableMapperRuleAttribute tableMapperRuleAttribute = mock(TableMapperRuleAttribute.class);
        when(tableMapperRuleAttribute.getLogicTableNames()).thenReturn(Arrays.asList("foo_tbl", "bar_tbl"));
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getAttributes()).thenReturn(new RuleAttributes(tableMapperRuleAttribute));
        when(MetaDataLoader.load(any())).thenReturn(createSchemaMetaDataMap(Arrays.asList("foo_tbl", "bar_tbl"), material));
        GenericSchemaBuilderMaterial newMaterial = new GenericSchemaBuilderMaterial(
                Collections.singletonMap("foo_schema", material.getStorageUnits().get("foo_schema")), Collections.singleton(rule), new ConfigurationProperties(new Properties()), "foo_schema",
                DatabaseIdentifierContextFactory.createDefault());
        Map<String, ShardingSphereSchema> actual = GenericSchemaBuilder.build(databaseType, newMaterial);
        assertThat(actual.size(), is(1));
        assertTables(new ShardingSphereSchema("foo_schema", databaseType, actual.values().iterator().next().getAllTables(), Collections.emptyList()));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("schemaAvailableDatabaseTypes")
    void assertBuildKeepsPhysicalSchemasWithDifferentSchemaAvailableDatabaseTypes(final String name, final DatabaseType protocolType,
                                                                                  final DatabaseType storageType) throws SQLException {
        Map<String, SchemaMetaData> schemaMetaDataMap = new LinkedHashMap<>(2, 1F);
        schemaMetaDataMap.put("public", createSchemaMetaData("public", "foo_tbl"));
        schemaMetaDataMap.put("foo_schema", createSchemaMetaData("foo_schema", "bar_tbl"));
        when(MetaDataLoader.load(any())).thenReturn(schemaMetaDataMap);
        Map<String, ShardingSphereSchema> actual = GenericSchemaBuilder.build(Arrays.asList("foo_tbl", "bar_tbl"), protocolType, createMaterial(storageType, "public"));
        assertThat(actual.size(), is(2));
        assertSchemaTable(actual, "public", "foo_tbl");
        assertSchemaTable(actual, "foo_schema", "bar_tbl");
    }
    
    @Test
    void assertBuildTranslatesSchemaWhenProtocolSchemaUnavailable() throws SQLException {
        when(MetaDataLoader.load(any())).thenReturn(Collections.singletonMap("public", createSchemaMetaData("public", "foo_tbl")));
        Map<String, ShardingSphereSchema> actual =
                GenericSchemaBuilder.build(Collections.singleton("foo_tbl"), MYSQL_DATABASE_TYPE, createMaterial(POSTGRESQL_DATABASE_TYPE, "FOO_DB"));
        assertThat(actual.size(), is(1));
        assertSchemaTable(actual, "FOO_DB", "foo_tbl");
    }
    
    @Test
    void assertBuildTranslatesSchemaWhenStorageSchemaUnavailable() throws SQLException {
        mockLoadedSchemaWithMaterialDefault("foo_tbl");
        Map<String, ShardingSphereSchema> actual =
                GenericSchemaBuilder.build(Collections.singleton("foo_tbl"), POSTGRESQL_DATABASE_TYPE, createMaterial(ORACLE_DATABASE_TYPE, "public"));
        assertThat(actual.size(), is(1));
        assertSchemaTable(actual, "public", "foo_tbl");
    }
    
    @Test
    void assertBuildTranslatesSchemaWithStorageDataSourcePolicy() throws SQLException {
        mockLoadedSchemaWithMaterialDefault("foo_tbl");
        Map<String, ShardingSphereSchema> actual = GenericSchemaBuilder.build(Collections.singleton("foo_tbl"), POSTGRESQL_DATABASE_TYPE,
                createMaterial(MYSQL_DATABASE_TYPE, "public", mockMySQLDataSource(1)));
        assertThat(actual.size(), is(1));
        assertSchemaTable(actual, "public", "foo_tbl");
    }
    
    @Test
    void assertBuildTranslatesSchemaWithHiddenStorageSchema() throws SQLException {
        mockLoadedSchemaWithMaterialDefault("foo_tbl");
        Map<String, ShardingSphereSchema> actual = GenericSchemaBuilder.build(
                Collections.singleton("foo_tbl"), POSTGRESQL_DATABASE_TYPE, createMaterial(H2_DATABASE_TYPE, "public"));
        assertThat(actual.size(), is(1));
        assertSchemaTable(actual, "public", "foo_tbl");
    }
    
    private static Stream<Arguments> schemaAvailableDatabaseTypes() {
        return Stream.of(
                Arguments.of("PostgreSQL protocol with openGauss storage", POSTGRESQL_DATABASE_TYPE, OPEN_GAUSS_DATABASE_TYPE),
                Arguments.of("openGauss protocol with PostgreSQL storage", OPEN_GAUSS_DATABASE_TYPE, POSTGRESQL_DATABASE_TYPE));
    }
    
    private GenericSchemaBuilderMaterial createMaterial(final DatabaseType storageType, final String defaultSchemaName) {
        return createMaterial(storageType, defaultSchemaName, new MockedDataSource());
    }
    
    private GenericSchemaBuilderMaterial createMaterial(final DatabaseType storageType, final String defaultSchemaName, final DataSource dataSource) {
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getAttributes()).thenReturn(new RuleAttributes(mock(TableMapperRuleAttribute.class)));
        StorageUnit storageUnit = mock(StorageUnit.class);
        when(storageUnit.getStorageType()).thenReturn(storageType);
        when(storageUnit.getDataSource()).thenReturn(dataSource);
        return new GenericSchemaBuilderMaterial(Collections.singletonMap("foo_schema", storageUnit), Collections.singleton(rule), new ConfigurationProperties(new Properties()),
                defaultSchemaName, DatabaseIdentifierContextFactory.createDefault());
    }
    
    private static DataSource mockMySQLDataSource(final int lowerCaseTableNames) throws SQLException {
        DataSource result = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(result.getConnection()).thenReturn(connection);
        when(connection.prepareStatement("SELECT @@lower_case_table_names")).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(lowerCaseTableNames);
        return result;
    }
    
    private void mockLoadedSchemaWithMaterialDefault(final String tableName) throws SQLException {
        when(MetaDataLoader.load(any())).thenAnswer(invocation -> {
            Collection<MetaDataLoaderMaterial> loaderMaterials = invocation.getArgument(0);
            String defaultSchemaName = loaderMaterials.iterator().next().getDefaultSchemaName();
            return Collections.singletonMap(defaultSchemaName, createSchemaMetaData(defaultSchemaName, tableName));
        });
    }
    
    private SchemaMetaData createSchemaMetaData(final String schemaName, final String tableName) {
        TableMetaData tableMetaData = new TableMetaData(tableName, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        return new SchemaMetaData(schemaName, Collections.singleton(tableMetaData));
    }
    
    private void assertSchemaTable(final Map<String, ShardingSphereSchema> actual, final String expectedSchemaName, final String expectedTableName) {
        ShardingSphereSchema actualSchema = actual.get(expectedSchemaName);
        assertThat(actualSchema.getName(), is(expectedSchemaName));
        assertThat(actualSchema.getAllTables().size(), is(1));
        assertThat(actualSchema.getAllTables().iterator().next().getName(), is(expectedTableName));
    }
    
    private Map<String, SchemaMetaData> createSchemaMetaDataMap(final Collection<String> tableNames, final GenericSchemaBuilderMaterial material) {
        if (!tableNames.isEmpty() && (tableNames.contains("foo_tbl") || tableNames.contains("bar_tbl"))) {
            Collection<TableMetaData> tableMetaDataList = tableNames.stream()
                    .map(each -> new TableMetaData(each, Collections.emptyList(), Collections.emptyList(), Collections.emptyList())).collect(Collectors.toList());
            return Collections.singletonMap(material.getDefaultSchemaName(), new SchemaMetaData(material.getDefaultSchemaName(), tableMetaDataList));
        }
        return Collections.emptyMap();
    }
    
    private void assertTables(final ShardingSphereSchema actual) {
        assertThat(actual.getAllTables().size(), is(2));
        assertTrue(actual.getTable("foo_tbl").getAllColumns().isEmpty());
        assertTrue(actual.getTable("bar_tbl").getAllColumns().isEmpty());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("inventoryCheckArgs")
    void assertBuildWithInventoryCheck(final String name, final DatabaseType storageType, final String defaultSchemaName, final Collection<DataNode> inventory, final boolean checkAll,
                                       final Map<String, SchemaMetaData> loadedSchemas, final Collection<String> expectedMissing, final LogCaptureAssertion logCaptureAssertion) throws SQLException {
        GenericSchemaBuilderMaterial inventoryMaterial = createInventoryMaterial(storageType, defaultSchemaName, inventory, checkAll);
        when(MetaDataLoader.load(any())).thenReturn(loadedSchemas);
        GenericSchemaBuilder.buildWithInventoryCheck(storageType, inventoryMaterial);
        logCaptureAssertion.assertLogCount(expectedMissing.isEmpty() ? 0 : 1);
        if (!expectedMissing.isEmpty()) {
            logCaptureAssertion.assertLogContent(0, Level.WARN, "The following tables are missing from loaded metadata: " + expectedMissing, true);
        }
    }
    
    @Test
    void assertBuildForRefreshLoadsEveryStorageUnit() throws SQLException {
        DataNode firstTable = new DataNode("foo_ds", "foo_db", "foo_tbl");
        DataNode secondTable = new DataNode("bar_ds", "foo_db", "foo_tbl");
        GenericSchemaBuilderMaterial refreshMaterial = createInventoryMaterial(MYSQL_DATABASE_TYPE, "foo_db", Arrays.asList(firstTable, secondTable), false);
        when(MetaDataLoader.load(any())).thenAnswer(invocation -> {
            Collection<MetaDataLoaderMaterial> actualMaterials = invocation.getArgument(0);
            assertThat(actualMaterials.stream().map(MetaDataLoaderMaterial::getStorageUnitName).collect(Collectors.toList()), is(Arrays.asList("foo_ds", "bar_ds")));
            return Collections.singletonMap("foo_db", createInventorySchema("foo_db", firstTable, secondTable));
        });
        Map<String, ShardingSphereSchema> actual = GenericSchemaBuilder.buildForRefresh(MYSQL_DATABASE_TYPE, refreshMaterial);
        assertSchemaTable(actual, "foo_db", "foo_tbl");
        assertFalse(refreshMaterial.getProps().<Boolean>getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED));
    }
    
    @Test
    void assertBuildForRefreshWithMissingPhysicalTable() throws SQLException {
        DataNode firstTable = new DataNode("foo_ds", "foo_db", "foo_tbl");
        DataNode secondTable = new DataNode("bar_ds", "foo_db", "foo_tbl");
        GenericSchemaBuilderMaterial refreshMaterial = createInventoryMaterial(MYSQL_DATABASE_TYPE, "foo_db", Arrays.asList(firstTable, secondTable), false);
        when(refreshMaterial.getRules().iterator().next().getAttributes().getAttribute(DataNodeRuleAttribute.class).isDataNodeTableNameLoadedFromStorage()).thenReturn(false);
        when(MetaDataLoader.load(any())).thenReturn(Collections.singletonMap("foo_db", createInventorySchema("foo_db", firstTable)));
        RuleAndStorageMetaDataMismatchedException actual = assertThrows(RuleAndStorageMetaDataMismatchedException.class,
                () -> GenericSchemaBuilder.buildForRefresh(Collections.singleton("foo_tbl"), MYSQL_DATABASE_TYPE, refreshMaterial));
        assertThat(actual.getMessage(), is("Rule and storage meta data mismatched, reason is: Missing physical table metadata for 'foo_tbl': bar_ds.foo_db.foo_tbl."));
    }
    
    @Test
    void assertBuildForRefreshWhenEveryPhysicalTableWasDropped() throws SQLException {
        GenericSchemaBuilderMaterial refreshMaterial = createInventoryMaterial(MYSQL_DATABASE_TYPE, "foo_db",
                Arrays.asList(new DataNode("foo_ds", "foo_db", "foo_tbl"), new DataNode("bar_ds", "foo_db", "foo_tbl")), false);
        when(MetaDataLoader.load(any())).thenReturn(Collections.emptyMap());
        assertTrue(GenericSchemaBuilder.buildForRefresh(Collections.singleton("foo_tbl"), MYSQL_DATABASE_TYPE, refreshMaterial).values().stream().allMatch(each -> each.getAllTables().isEmpty()));
    }
    
    @Test
    void assertBuildForRefreshWithMissingPhysicalSchema() throws SQLException {
        DataNode firstTable = new DataNode("foo_ds", "public", "foo_tbl");
        DataNode secondTable = new DataNode("foo_ds", "archive", "foo_tbl");
        GenericSchemaBuilderMaterial refreshMaterial = createInventoryMaterial(POSTGRESQL_DATABASE_TYPE, "public", Arrays.asList(firstTable, secondTable), false);
        when(refreshMaterial.getRules().iterator().next().getAttributes().getAttribute(DataNodeRuleAttribute.class).isDataNodeTableNameLoadedFromStorage()).thenReturn(false);
        when(MetaDataLoader.load(any())).thenReturn(Collections.singletonMap("public", createInventorySchema("public", firstTable)));
        RuleAndStorageMetaDataMismatchedException actual = assertThrows(RuleAndStorageMetaDataMismatchedException.class,
                () -> GenericSchemaBuilder.buildForRefresh(Collections.singleton("foo_tbl"), POSTGRESQL_DATABASE_TYPE, refreshMaterial));
        assertThat(actual.getMessage(), is("Rule and storage meta data mismatched, reason is: Missing physical table metadata for 'foo_tbl': foo_ds.archive.foo_tbl."));
    }
    
    @Test
    void assertBuildForRefreshPreservesCaseDistinctPhysicalTables() throws SQLException {
        DataNode firstTable = new DataNode("foo_ds", "public", "foo_tbl");
        DataNode secondTable = new DataNode("foo_ds", "public", "FOO_TBL");
        GenericSchemaBuilderMaterial refreshMaterial = createInventoryMaterial(POSTGRESQL_DATABASE_TYPE, "public", Arrays.asList(firstTable, secondTable), false);
        DataNodeRuleAttribute dataNodes = refreshMaterial.getRules().iterator().next().getAttributes().getAttribute(DataNodeRuleAttribute.class);
        when(dataNodes.getDataNodesByTableName("foo_tbl")).thenReturn(Arrays.asList(firstTable, secondTable));
        when(dataNodes.isDataNodeTableNameLoadedFromStorage()).thenReturn(false);
        when(MetaDataLoader.load(any())).thenReturn(Collections.singletonMap("public", createInventorySchema("public", firstTable)));
        RuleAndStorageMetaDataMismatchedException actual = assertThrows(RuleAndStorageMetaDataMismatchedException.class,
                () -> GenericSchemaBuilder.buildForRefresh(Collections.singleton("foo_tbl"), POSTGRESQL_DATABASE_TYPE, refreshMaterial));
        assertThat(actual.getMessage(), is("Rule and storage meta data mismatched, reason is: Missing physical table metadata for 'foo_tbl': foo_ds.public.FOO_TBL."));
    }
    
    @Test
    void assertBuildForRefreshWithNormalizedPhysicalNamesAndWildcardSchema() throws SQLException {
        DataNode firstTable = new DataNode("foo_ds", "*", "FOO_TBL_0");
        DataNode secondTable = new DataNode("bar_ds", "*", "FOO_TBL_1");
        GenericSchemaBuilderMaterial inventoryMaterial = createInventoryMaterial(POSTGRESQL_DATABASE_TYPE, "public", Arrays.asList(firstTable, secondTable), false);
        DataNodeRuleAttribute dataNodes = inventoryMaterial.getRules().iterator().next().getAttributes().getAttribute(DataNodeRuleAttribute.class);
        when(dataNodes.getDataNodesByTableName("foo_tbl")).thenReturn(Arrays.asList(firstTable, secondTable));
        when(dataNodes.isDataNodeTableNameLoadedFromStorage()).thenReturn(false);
        GenericSchemaBuilderMaterial refreshMaterial = new GenericSchemaBuilderMaterial(inventoryMaterial.getStorageUnits(), inventoryMaterial.getRules(), inventoryMaterial.getProps(), "public",
                DatabaseIdentifierContextFactory.create(MYSQL_DATABASE_TYPE, new ResourceMetaData(Collections.emptyMap(), inventoryMaterial.getStorageUnits()), inventoryMaterial.getProps()));
        when(MetaDataLoader.load(any())).thenReturn(Collections.singletonMap("archive", createInventorySchema("archive", new DataNode("foo_ds", "archive", "foo_tbl_0"))));
        RuleAndStorageMetaDataMismatchedException actual = assertThrows(RuleAndStorageMetaDataMismatchedException.class,
                () -> GenericSchemaBuilder.buildForRefresh(Collections.singleton("foo_tbl"), MYSQL_DATABASE_TYPE, refreshMaterial));
        assertThat(actual.getMessage(), is("Rule and storage meta data mismatched, reason is: Missing physical table metadata for 'foo_tbl': bar_ds.archive.FOO_TBL_1."));
    }
    
    @Test
    void assertBuildForRefreshKeepsIndependentSingleTablesInDifferentSchemas() throws SQLException {
        GenericSchemaBuilderMaterial refreshMaterial = createInventoryMaterial(POSTGRESQL_DATABASE_TYPE, "public",
                Arrays.asList(new DataNode("foo_ds", "public", "foo_tbl"), new DataNode("foo_ds", "archive", "foo_tbl")), false);
        TableMetaData publicTable = new TableMetaData("foo_tbl",
                Collections.singleton(new ColumnMetaData("id", Types.INTEGER, false, false, false, true, false, true)), Collections.emptyList(), Collections.emptyList());
        publicTable.setStorageUnitName("foo_ds");
        TableMetaData archiveTable = new TableMetaData("foo_tbl",
                Collections.singleton(new ColumnMetaData("id", Types.VARCHAR, false, false, false, true, false, true)), Collections.emptyList(), Collections.emptyList());
        archiveTable.setStorageUnitName("foo_ds");
        Map<String, SchemaMetaData> schemas = new LinkedHashMap<>();
        schemas.put("public", new SchemaMetaData("public", Collections.singleton(publicTable)));
        schemas.put("archive", new SchemaMetaData("archive", Collections.singleton(archiveTable)));
        when(MetaDataLoader.load(any())).thenReturn(schemas);
        Map<String, ShardingSphereSchema> actual = GenericSchemaBuilder.buildForRefresh(Collections.singleton("foo_tbl"), POSTGRESQL_DATABASE_TYPE, refreshMaterial);
        assertThat(actual.get("public").getTable("foo_tbl").getColumn("id").getDataType(), is(Types.INTEGER));
        assertThat(actual.get("archive").getTable("foo_tbl").getColumn("id").getDataType(), is(Types.VARCHAR));
    }
    
    @Test
    void assertBuildForRefreshRejectsMissingH2TableWithNativeNames() throws SQLException {
        DataNode firstTable = new DataNode("foo_ds", "foo_db", "foo_tbl_0");
        DataNode secondTable = new DataNode("foo_ds", "foo_db", "foo_tbl_1");
        GenericSchemaBuilderMaterial refreshMaterial = createInventoryMaterial(H2_DATABASE_TYPE, "foo_db", Arrays.asList(firstTable, secondTable), false);
        DataNodeRuleAttribute dataNodes = refreshMaterial.getRules().iterator().next().getAttributes().getAttribute(DataNodeRuleAttribute.class);
        when(dataNodes.getDataNodesByTableName("foo_tbl")).thenReturn(Arrays.asList(firstTable, secondTable));
        when(dataNodes.isDataNodeTableNameLoadedFromStorage()).thenReturn(false);
        when(MetaDataLoader.load(any())).thenReturn(Collections.singletonMap("foo_db", createInventorySchema("foo_db", new DataNode("foo_ds", "foo_db", "FOO_TBL_0"))));
        RuleAndStorageMetaDataMismatchedException actual = assertThrows(RuleAndStorageMetaDataMismatchedException.class,
                () -> GenericSchemaBuilder.buildForRefresh(Collections.singleton("foo_tbl"), H2_DATABASE_TYPE, refreshMaterial));
        assertThat(actual.getMessage(), is("Rule and storage meta data mismatched, reason is: Missing physical table metadata for 'foo_tbl': foo_ds.foo_db.foo_tbl_1."));
    }
    
    @Test
    void assertBuildForRefreshAcceptsCompleteH2TablesWithNativeNames() throws SQLException {
        DataNode firstTable = new DataNode("foo_ds", "foo_db", "foo_tbl_0");
        DataNode secondTable = new DataNode("foo_ds", "foo_db", "foo_tbl_1");
        GenericSchemaBuilderMaterial refreshMaterial = createInventoryMaterial(H2_DATABASE_TYPE, "foo_db", Arrays.asList(firstTable, secondTable), false);
        DataNodeRuleAttribute dataNodes = refreshMaterial.getRules().iterator().next().getAttributes().getAttribute(DataNodeRuleAttribute.class);
        when(dataNodes.getDataNodesByTableName("foo_tbl")).thenReturn(Arrays.asList(firstTable, secondTable));
        when(dataNodes.isDataNodeTableNameLoadedFromStorage()).thenReturn(false);
        when(MetaDataLoader.load(any())).thenReturn(Collections.singletonMap("foo_db", createInventorySchema("foo_db",
                new DataNode("foo_ds", "foo_db", "FOO_TBL_0"), new DataNode("foo_ds", "foo_db", "FOO_TBL_1"))));
        Map<String, ShardingSphereSchema> actual = GenericSchemaBuilder.buildForRefresh(Collections.singleton("foo_tbl"), H2_DATABASE_TYPE, refreshMaterial);
        assertThat(actual.get("foo_db").getAllTables().size(), is(2));
        assertTrue(actual.get("foo_db").containsTable("FOO_TBL_0"));
        assertTrue(actual.get("foo_db").containsTable("FOO_TBL_1"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("caseCollisionArgs")
    void assertBuildForRefreshWithCaseCollision(final String name, final DataNode firstTable, final DataNode secondTable) throws SQLException {
        GenericSchemaBuilderMaterial refreshMaterial = createInventoryMaterial(POSTGRESQL_DATABASE_TYPE, "public", Arrays.asList(firstTable, secondTable), false);
        DataNodeRuleAttribute dataNodes = refreshMaterial.getRules().iterator().next().getAttributes().getAttribute(DataNodeRuleAttribute.class);
        when(dataNodes.getDataNodesByTableName("foo_tbl")).thenReturn(Arrays.asList(firstTable, secondTable));
        when(dataNodes.isDataNodeTableNameLoadedFromStorage()).thenReturn(false);
        when(MetaDataLoader.load(any())).thenReturn(Collections.singletonMap("public", createInventorySchema("public", new DataNode("foo_ds", "public", "foo_tbl"))));
        assertThrows(RuleAndStorageMetaDataMismatchedException.class, () -> GenericSchemaBuilder.buildForRefresh(Collections.singleton("foo_tbl"), POSTGRESQL_DATABASE_TYPE, refreshMaterial));
    }
    
    private static Stream<Arguments> caseCollisionArgs() {
        return Stream.of(
                Arguments.of("upper and lower table names", new DataNode("foo_ds", "public", "FOO_TBL"), new DataNode("foo_ds", "public", "foo_tbl")),
                Arguments.of("mixed and upper table names", new DataNode("foo_ds", "public", "Foo_Tbl"), new DataNode("foo_ds", "public", "FOO_TBL")),
                Arguments.of("upper and lower schema names", new DataNode("foo_ds", "PUBLIC", "foo_tbl"), new DataNode("foo_ds", "public", "foo_tbl")),
                Arguments.of("mixed and upper schema names", new DataNode("foo_ds", "Public", "foo_tbl"), new DataNode("foo_ds", "PUBLIC", "foo_tbl")));
    }
    
    @Test
    void assertBuildForRefreshRejectsMissingTableWithNativeSchemaName() throws SQLException {
        DataNode firstTable = new DataNode("foo_ds", "PUBLIC", "foo_tbl_0");
        DataNode secondTable = new DataNode("foo_ds", "PUBLIC", "foo_tbl_1");
        GenericSchemaBuilderMaterial refreshMaterial = createInventoryMaterial(POSTGRESQL_DATABASE_TYPE, "public", Arrays.asList(firstTable, secondTable), false);
        DataNodeRuleAttribute dataNodes = refreshMaterial.getRules().iterator().next().getAttributes().getAttribute(DataNodeRuleAttribute.class);
        when(dataNodes.getDataNodesByTableName("foo_tbl")).thenReturn(Arrays.asList(firstTable, secondTable));
        when(dataNodes.isDataNodeTableNameLoadedFromStorage()).thenReturn(false);
        when(MetaDataLoader.load(any())).thenReturn(Collections.singletonMap("public", createInventorySchema("public", new DataNode("foo_ds", "public", "foo_tbl_0"))));
        RuleAndStorageMetaDataMismatchedException actual = assertThrows(RuleAndStorageMetaDataMismatchedException.class,
                () -> GenericSchemaBuilder.buildForRefresh(Collections.singleton("foo_tbl"), POSTGRESQL_DATABASE_TYPE, refreshMaterial));
        assertThat(actual.getMessage(), is("Rule and storage meta data mismatched, reason is: Missing physical table metadata for 'foo_tbl': foo_ds.PUBLIC.foo_tbl_1."));
    }
    
    @Test
    void assertBuildForRefreshAcceptsCompleteTablesWithNativeSchemaName() throws SQLException {
        DataNode firstTable = new DataNode("foo_ds", "PUBLIC", "foo_tbl_0");
        DataNode secondTable = new DataNode("foo_ds", "PUBLIC", "foo_tbl_1");
        GenericSchemaBuilderMaterial refreshMaterial = createInventoryMaterial(POSTGRESQL_DATABASE_TYPE, "public", Arrays.asList(firstTable, secondTable), false);
        DataNodeRuleAttribute dataNodes = refreshMaterial.getRules().iterator().next().getAttributes().getAttribute(DataNodeRuleAttribute.class);
        when(dataNodes.getDataNodesByTableName("foo_tbl")).thenReturn(Arrays.asList(firstTable, secondTable));
        when(dataNodes.isDataNodeTableNameLoadedFromStorage()).thenReturn(false);
        when(MetaDataLoader.load(any())).thenReturn(Collections.singletonMap("public", createInventorySchema("public",
                new DataNode("foo_ds", "public", "foo_tbl_0"), new DataNode("foo_ds", "public", "foo_tbl_1"))));
        Map<String, ShardingSphereSchema> actual = GenericSchemaBuilder.buildForRefresh(Collections.singleton("foo_tbl"), POSTGRESQL_DATABASE_TYPE, refreshMaterial);
        assertThat(actual.get("public").getAllTables().size(), is(2));
        assertTrue(actual.get("public").containsTable("foo_tbl_0"));
        assertTrue(actual.get("public").containsTable("foo_tbl_1"));
    }
    
    @Test
    void assertBuildForRefreshKeepsExactCaseDistinctSchemas() throws SQLException {
        DataNode firstTable = new DataNode("foo_ds", "Public", "foo_tbl");
        DataNode secondTable = new DataNode("foo_ds", "public", "foo_tbl");
        GenericSchemaBuilderMaterial refreshMaterial = createInventoryMaterial(POSTGRESQL_DATABASE_TYPE, "public", Arrays.asList(firstTable, secondTable), false);
        when(refreshMaterial.getRules().iterator().next().getAttributes().getAttribute(DataNodeRuleAttribute.class).isDataNodeTableNameLoadedFromStorage()).thenReturn(false);
        Map<String, SchemaMetaData> schemas = new LinkedHashMap<>();
        schemas.put("Public", createInventorySchema("Public", firstTable));
        schemas.put("public", createInventorySchema("public", secondTable));
        when(MetaDataLoader.load(any())).thenReturn(schemas);
        Map<String, ShardingSphereSchema> actual = GenericSchemaBuilder.buildForRefresh(Collections.singleton("foo_tbl"), POSTGRESQL_DATABASE_TYPE, refreshMaterial);
        assertThat(actual.size(), is(2));
        assertTrue(actual.get("Public").containsTable("foo_tbl"));
        assertTrue(actual.get("public").containsTable("foo_tbl"));
    }
    
    @Test
    void assertBuildForRefreshAfterWildcardSchemaRemoval() throws SQLException {
        DataNode table = new DataNode("foo_db", POSTGRESQL_DATABASE_TYPE, "foo_ds.foo_tbl");
        GenericSchemaBuilderMaterial refreshMaterial = createInventoryMaterial(POSTGRESQL_DATABASE_TYPE, "public", Collections.singleton(table), false);
        when(refreshMaterial.getRules().iterator().next().getAttributes().getAttribute(DataNodeRuleAttribute.class).isDataNodeTableNameLoadedFromStorage()).thenReturn(false);
        Map<String, SchemaMetaData> schemas = new LinkedHashMap<>(2, 1F);
        schemas.put("public", createInventorySchema("public", new DataNode("foo_ds", "public", "foo_tbl")));
        schemas.put("archive", createInventorySchema("archive", new DataNode("foo_ds", "archive", "foo_tbl")));
        when(MetaDataLoader.load(any())).thenReturn(schemas);
        Map<String, ShardingSphereSchema> actualSchemas = GenericSchemaBuilder.buildForRefresh(Collections.singleton("foo_tbl"), POSTGRESQL_DATABASE_TYPE, refreshMaterial);
        assertThat(actualSchemas.size(), is(2));
        assertThat(actualSchemas.get("public").getAllTables().size(), is(1));
        assertTrue(actualSchemas.get("public").containsTable("foo_tbl"));
        assertThat(actualSchemas.get("archive").getAllTables().size(), is(1));
        assertTrue(actualSchemas.get("archive").containsTable("foo_tbl"));
        Collection<ShardingSphereSchema> revisionSchemas = new ArrayList<>(actualSchemas.values());
        revisionSchemas.add(new ShardingSphereSchema("unrelated", POSTGRESQL_DATABASE_TYPE));
        GenericSchemaBuilderMaterial revisionMaterial = new GenericSchemaBuilderMaterial(refreshMaterial.getStorageUnits(), refreshMaterial.getRules(), refreshMaterial.getProps(),
                refreshMaterial.getDefaultSchemaName(), refreshMaterial.getIdentifierContext(), revisionSchemas);
        when(MetaDataLoader.load(any())).thenReturn(Collections.singletonMap("public", createInventorySchema("public", new DataNode("foo_ds", "public", "foo_tbl"))));
        RuleAndStorageMetaDataMismatchedException actual = assertThrows(RuleAndStorageMetaDataMismatchedException.class,
                () -> GenericSchemaBuilder.buildForRefresh(Collections.singleton("foo_tbl"), POSTGRESQL_DATABASE_TYPE, revisionMaterial));
        assertThat(actual.getMessage(), is("Rule and storage meta data mismatched, reason is: Missing physical table metadata for 'foo_tbl': foo_ds.archive.foo_tbl."));
        assertFalse(refreshMaterial.getProps().<Boolean>getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED));
    }
    
    @Test
    void assertBuildForRefreshDoesNotConnectToUnrelatedStorageUnit() throws SQLException {
        DataNode table = new DataNode("foo_ds", "foo_db", "foo_tbl");
        GenericSchemaBuilderMaterial refreshMaterial = createInventoryMaterial(MYSQL_DATABASE_TYPE, "foo_db", Collections.singleton(table), false);
        when(refreshMaterial.getRules().iterator().next().getAttributes().getAttribute(DataNodeRuleAttribute.class).isDataNodeTableNameLoadedFromStorage()).thenReturn(false);
        DataSource unrelatedDataSource = mock(DataSource.class);
        when(unrelatedDataSource.getConnection()).thenThrow(new AssertionError("unrelated storage unit must not be connected"));
        StorageUnit unrelatedStorageUnit = mock(StorageUnit.class);
        when(unrelatedStorageUnit.getStorageType()).thenReturn(MYSQL_DATABASE_TYPE);
        when(unrelatedStorageUnit.getDataSource()).thenReturn(unrelatedDataSource);
        refreshMaterial.getStorageUnits().put("unrelated_ds", unrelatedStorageUnit);
        when(MetaDataLoader.load(any())).thenReturn(Collections.singletonMap("foo_db", createInventorySchema("foo_db", table)));
        Map<String, ShardingSphereSchema> actual = GenericSchemaBuilder.buildForRefresh(Collections.singleton("foo_tbl"), MYSQL_DATABASE_TYPE, refreshMaterial);
        assertSchemaTable(actual, "foo_db", "foo_tbl");
        verify(unrelatedDataSource, never()).getConnection();
    }
    
    private GenericSchemaBuilderMaterial createInventoryMaterial(final DatabaseType storageType, final String defaultSchemaName, final Collection<DataNode> inventory,
                                                                 final boolean checkAll) throws SQLException {
        DataNodeRuleAttribute dataNodeRuleAttribute = mock(DataNodeRuleAttribute.class);
        when(dataNodeRuleAttribute.getDataNodesByTableName(anyString())).thenAnswer(invocation -> {
            String tableName = invocation.getArgument(0);
            return inventory.stream().filter(each -> each.getTableName().equals(tableName)).collect(Collectors.toList());
        });
        when(dataNodeRuleAttribute.isDataNodeTableNameLoadedFromStorage()).thenReturn(true);
        TableMapperRuleAttribute tableMapperRuleAttribute = mock(TableMapperRuleAttribute.class);
        when(tableMapperRuleAttribute.getLogicTableNames()).thenReturn(inventory.stream().map(DataNode::getTableName).distinct().collect(Collectors.toList()));
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getAttributes()).thenReturn(new RuleAttributes(dataNodeRuleAttribute, tableMapperRuleAttribute));
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>();
        for (DataNode each : inventory) {
            if (!storageUnits.containsKey(each.getDataSourceName())) {
                StorageUnit storageUnit = mock(StorageUnit.class);
                DataSource dataSource = MYSQL_DATABASE_TYPE.equals(storageType) ? mockMySQLDataSource(0) : new MockedDataSource();
                when(storageUnit.getStorageType()).thenReturn(storageType);
                when(storageUnit.getDataSource()).thenReturn(dataSource);
                storageUnits.put(each.getDataSourceName(), storageUnit);
            }
        }
        ConfigurationProperties props = new ConfigurationProperties(PropertiesBuilder.build(
                new Property(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED.getKey(), Boolean.toString(checkAll)),
                new Property(ConfigurationPropertyKey.LOAD_TABLE_METADATA_BATCH_SIZE.getKey(), "1")));
        return new GenericSchemaBuilderMaterial(storageUnits, Collections.singleton(rule), props, defaultSchemaName, DatabaseIdentifierContextFactory.createDefault());
    }
    
    private static Stream<Arguments> inventoryCheckArgs() {
        DataNode mysqlTable = new DataNode("foo_ds", "foo_db", "foo_tbl");
        DataNode publicTable = new DataNode("foo_ds", "public", "foo_tbl");
        DataNode archiveTable = new DataNode("foo_ds", "archive", "foo_tbl");
        DataNode h2Table = new DataNode("foo_ds", "FOO_DB", "FOO_TBL");
        return Stream.of(
                Arguments.of("missing MySQL single table", MYSQL_DATABASE_TYPE, "foo_db", Collections.singleton(mysqlTable), false, Collections.emptyMap(),
                        Collections.singleton("foo_ds.foo_db.foo_tbl")),
                Arguments.of("loaded MySQL single table", MYSQL_DATABASE_TYPE, "foo_db", Collections.singleton(mysqlTable), false,
                        Collections.singletonMap("foo_db", createInventorySchema("foo_db", mysqlTable)), Collections.emptyList()),
                Arguments.of("all requested batches loaded", MYSQL_DATABASE_TYPE, "foo_db", Arrays.asList(mysqlTable, new DataNode("foo_ds", "foo_db", "bar_tbl")), true,
                        Collections.singletonMap("foo_db", createInventorySchema("foo_db", mysqlTable, new DataNode("foo_ds", "foo_db", "bar_tbl"))), Collections.emptyList()),
                Arguments.of("same table missing in another storage unit", MYSQL_DATABASE_TYPE, "foo_db", Arrays.asList(mysqlTable, new DataNode("bar_ds", "foo_db", "foo_tbl")), true,
                        Collections.singletonMap("foo_db", createInventorySchema("foo_db", mysqlTable)), Collections.singleton("bar_ds.foo_db.foo_tbl")),
                Arguments.of("unrequested storage unit excluded", MYSQL_DATABASE_TYPE, "foo_db", Arrays.asList(mysqlTable, new DataNode("bar_ds", "foo_db", "foo_tbl")), false,
                        Collections.singletonMap("foo_db", createInventorySchema("foo_db", mysqlTable)), Collections.emptyList()),
                Arguments.of("nondefault schema loaded with representative selection", POSTGRESQL_DATABASE_TYPE, "public", Collections.singleton(archiveTable), false,
                        Collections.singletonMap("archive", createInventorySchema("archive", archiveTable)), Collections.emptyList()),
                Arguments.of("nondefault schema loaded with all-node selection", POSTGRESQL_DATABASE_TYPE, "public", Collections.singleton(archiveTable), true,
                        Collections.singletonMap("archive", createInventorySchema("archive", archiveTable)), Collections.emptyList()),
                Arguments.of("missing nondefault schema", POSTGRESQL_DATABASE_TYPE, "public", Arrays.asList(publicTable, archiveTable), true,
                        Collections.singletonMap("public", createInventorySchema("public", publicTable)), Collections.singleton("foo_ds.archive.foo_tbl")),
                Arguments.of("missing nondefault schema with representative selection", POSTGRESQL_DATABASE_TYPE, "public", Arrays.asList(publicTable, archiveTable), false,
                        Collections.singletonMap("public", createInventorySchema("public", publicTable)), Collections.singleton("foo_ds.archive.foo_tbl")),
                Arguments.of("case-distinct physical tables", POSTGRESQL_DATABASE_TYPE, "public", Arrays.asList(publicTable, new DataNode("foo_ds", "public", "FOO_TBL")), true,
                        Collections.singletonMap("public", createInventorySchema("public", publicTable)), Collections.singleton("foo_ds.public.FOO_TBL")),
                Arguments.of("dotted names retain separate identities", POSTGRESQL_DATABASE_TYPE, "public",
                        Arrays.asList(new DataNode("foo_ds", "public.bar", "foo_tbl"), new DataNode("foo_ds", "public", "bar.foo_tbl")), true,
                        Collections.singletonMap("public", createInventorySchema("public", new DataNode("foo_ds", "public", "bar.foo_tbl"))), Collections.singleton("foo_ds.public.bar.foo_tbl")),
                Arguments.of("H2 schema alias mapped to loader schema", H2_DATABASE_TYPE, "foo_db", Collections.singleton(h2Table), false,
                        Collections.singletonMap("foo_db", createInventorySchema("foo_db", h2Table)), Collections.emptyList()),
                Arguments.of("missing H2 inventoried table", H2_DATABASE_TYPE, "foo_db", Collections.singleton(h2Table), false, Collections.emptyMap(), Collections.singleton("foo_ds.FOO_DB.FOO_TBL")),
                Arguments.of("Oracle schema alias mapped to loader schema", ORACLE_DATABASE_TYPE, "foo_db", Collections.singleton(h2Table), false,
                        Collections.singletonMap("foo_db", createInventorySchema("foo_db", h2Table)), Collections.emptyList()),
                Arguments.of("unproven native schema excluded", SQL_SERVER_DATABASE_TYPE, "dbo", Collections.singleton(archiveTable), true, Collections.emptyMap(), Collections.emptyList()),
                Arguments.of("empty inventory", POSTGRESQL_DATABASE_TYPE, "public", Collections.emptyList(), false, Collections.emptyMap(), Collections.emptyList()));
    }
    
    private static SchemaMetaData createInventorySchema(final String schemaName, final DataNode... dataNodes) {
        Collection<TableMetaData> tables = new ArrayList<>(dataNodes.length);
        for (DataNode each : dataNodes) {
            TableMetaData tableMetaData = new TableMetaData(each.getTableName(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
            tableMetaData.setStorageUnitName(each.getDataSourceName());
            tables.add(tableMetaData);
        }
        return new SchemaMetaData(schemaName, tables);
    }
    
    @Test
    void assertBuildWithInventoryCheckForConfiguredTables(final LogCaptureAssertion logCaptureAssertion) throws SQLException {
        GenericSchemaBuilderMaterial inventoryMaterial = createInventoryMaterial(H2_DATABASE_TYPE, "foo_db", Collections.singleton(new DataNode("foo_ds", "foo_db", "foo_tbl")), false);
        when(inventoryMaterial.getRules().iterator().next().getAttributes().getAttribute(DataNodeRuleAttribute.class).isDataNodeTableNameLoadedFromStorage()).thenReturn(false);
        when(MetaDataLoader.load(any())).thenReturn(Collections.singletonMap("foo_db", createInventorySchema("foo_db", new DataNode("foo_ds", "foo_db", "FOO_TBL"))));
        GenericSchemaBuilder.buildWithInventoryCheck(H2_DATABASE_TYPE, inventoryMaterial);
        logCaptureAssertion.assertLogCount(0);
    }
    
    @Test
    void assertBuildWithInventoryCheckForUnmatchedFallback(final LogCaptureAssertion logCaptureAssertion) throws SQLException {
        GenericSchemaBuilderMaterial inventoryMaterial = createInventoryMaterial(POSTGRESQL_DATABASE_TYPE, "public", Collections.singleton(new DataNode("foo_ds", "archive", "Foo_Tbl")), false);
        GenericSchemaBuilderMaterial actualMaterial = new GenericSchemaBuilderMaterial(inventoryMaterial.getStorageUnits(), inventoryMaterial.getRules(), inventoryMaterial.getProps(), "public",
                DatabaseIdentifierContextFactory.create(MYSQL_DATABASE_TYPE, new ResourceMetaData(Collections.emptyMap(), inventoryMaterial.getStorageUnits()), inventoryMaterial.getProps()));
        when(MetaDataLoader.load(any())).thenReturn(Collections.emptyMap());
        GenericSchemaBuilder.buildWithInventoryCheck(MYSQL_DATABASE_TYPE, actualMaterial);
        logCaptureAssertion.assertLogCount(0);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("refreshCandidateArgs")
    void assertBuildWithRefreshCandidate(final String name, final boolean fullLoad, final LogCaptureAssertion logCaptureAssertion) throws SQLException {
        GenericSchemaBuilderMaterial inventoryMaterial = createInventoryMaterial(H2_DATABASE_TYPE, "foo_db", Collections.singleton(new DataNode("foo_ds", "foo_db", "foo_tbl")), false);
        when(MetaDataLoader.load(any())).thenReturn(Collections.singletonMap("foo_db", createInventorySchema("foo_db", new DataNode("foo_ds", "foo_db", "FOO_TBL"))));
        Map<String, ShardingSphereSchema> actual = fullLoad
                ? GenericSchemaBuilder.build(H2_DATABASE_TYPE, inventoryMaterial)
                : GenericSchemaBuilder.build(Collections.singleton("foo_tbl"), H2_DATABASE_TYPE, inventoryMaterial);
        assertSchemaTable(actual, "foo_db", "FOO_TBL");
        logCaptureAssertion.assertLogCount(0);
    }
    
    private static Stream<Arguments> refreshCandidateArgs() {
        return Stream.of(Arguments.of("full rebuild with SQL candidate", true), Arguments.of("incremental DDL refresh", false));
    }
}
