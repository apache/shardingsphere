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

import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoader;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderMaterial;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContextFactory;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@StaticMockSettings(MetaDataLoader.class)
class GenericSchemaBuilderTest {
    
    private static final DatabaseType H2_DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "H2");
    
    private static final DatabaseType MYSQL_DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private static final DatabaseType POSTGRESQL_DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private static final DatabaseType OPEN_GAUSS_DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "openGauss");
    
    private static final DatabaseType ORACLE_DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "Oracle");
    
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
                GenericSchemaBuilder.build(Collections.singleton("foo_tbl"), MYSQL_DATABASE_TYPE, createMaterial(POSTGRESQL_DATABASE_TYPE, "foo_db"));
        assertThat(actual.size(), is(1));
        assertSchemaTable(actual, "foo_db", "foo_tbl");
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
                createMaterial(MYSQL_DATABASE_TYPE, "Foo_DB", mockMySQLDataSource(1)));
        assertThat(actual.size(), is(1));
        assertSchemaTable(actual, "public", "foo_tbl");
    }
    
    @Test
    void assertBuildTranslatesSchemaWithHiddenStorageSchema() throws SQLException {
        mockLoadedSchemaWithMaterialDefault("foo_tbl");
        Map<String, ShardingSphereSchema> actual = GenericSchemaBuilder.build(
                Collections.singleton("foo_tbl"), POSTGRESQL_DATABASE_TYPE, createMaterial(H2_DATABASE_TYPE, "Foo_DB"));
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
}
