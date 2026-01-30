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
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@StaticMockSettings(MetaDataLoader.class)
class GenericSchemaBuilderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private GenericSchemaBuilderMaterial material;
    
    @BeforeEach
    void setUp() {
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getAttributes()).thenReturn(new RuleAttributes(mock(TableMapperRuleAttribute.class)));
        StorageUnit storageUnit = mock(StorageUnit.class);
        when(storageUnit.getStorageType()).thenReturn(databaseType);
        when(storageUnit.getDataSource()).thenReturn(new MockedDataSource());
        material = new GenericSchemaBuilderMaterial(Collections.singletonMap("foo_schema", storageUnit), Collections.singleton(rule), new ConfigurationProperties(new Properties()), "foo_schema");
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
                Collections.singletonMap("foo_schema", material.getStorageUnits().get("foo_schema")), Collections.singleton(rule), new ConfigurationProperties(new Properties()), "foo_schema");
        Map<String, ShardingSphereSchema> actual = GenericSchemaBuilder.build(databaseType, newMaterial);
        assertThat(actual.size(), is(1));
        assertTables(new ShardingSphereSchema("foo_schema", databaseType, actual.values().iterator().next().getAllTables(), Collections.emptyList()));
    }
    
    @Test
    void assertBuildWithDifferentProtocolAndStorageTypes() throws SQLException {
        DatabaseType differentDatabaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        Collection<String> tableNames = Collections.singleton("foo_tbl");
        Map<String, SchemaMetaData> schemaMetaDataMap = createSchemaMetaDataMap(tableNames, material);
        when(MetaDataLoader.load(any())).thenReturn(schemaMetaDataMap);
        StorageUnit storageUnit = mock(StorageUnit.class);
        when(storageUnit.getStorageType()).thenReturn(differentDatabaseType);
        Map<String, StorageUnit> storageUnits = Collections.singletonMap("foo_schema", storageUnit);
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getAttributes()).thenReturn(new RuleAttributes(mock(TableMapperRuleAttribute.class)));
        GenericSchemaBuilderMaterial newMaterial = new GenericSchemaBuilderMaterial(storageUnits, Collections.singleton(rule), new ConfigurationProperties(new Properties()), "foo_schema");
        Map<String, ShardingSphereSchema> actual = GenericSchemaBuilder.build(tableNames, databaseType, newMaterial);
        assertThat(actual.size(), is(1));
        ShardingSphereSchema actualSchema = actual.values().iterator().next();
        assertTrue(actualSchema.getAllTables().isEmpty());
        assertNull(actualSchema.getTable("foo_tbl"));
        assertThat(actualSchema.getName(), is("foo_schema"));
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
