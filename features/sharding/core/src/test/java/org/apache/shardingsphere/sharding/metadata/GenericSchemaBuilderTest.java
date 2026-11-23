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

import org.apache.shardingsphere.database.connector.core.exception.RuleAndStorageMetaDataMismatchedException;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoader;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.IndexMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.util.IndexMetaDataUtils;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContextFactory;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(MetaDataLoader.class)
class GenericSchemaBuilderTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"foo_tbl", "FOO_TBL", "Foo_Tbl"})
    void assertRefreshRejectsDifferentStructuresAcrossSchemas(final String tableName) throws SQLException {
        GenericSchemaBuilderMaterial material = createMaterial();
        when(MetaDataLoader.load(any())).thenReturn(createSchemas(Types.BIGINT));
        RuleAndStorageMetaDataMismatchedException actual = assertThrows(RuleAndStorageMetaDataMismatchedException.class,
                () -> GenericSchemaBuilder.buildForRefresh(Collections.singleton(tableName), databaseType, material));
        assertThat(actual.getMessage(), containsString("Can not get uniformed table structure for logic table 'foo_tbl'"));
        assertFalse(material.getProps().<Boolean>getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED));
    }
    
    private GenericSchemaBuilderMaterial createMaterial() {
        DataSource dataSource = new MockedDataSource();
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.setTables(Collections.singleton(new ShardingTableRuleConfiguration("foo_tbl", "foo_ds.public.foo_tbl_0,foo_ds.archive.foo_tbl_1")));
        ShardingRule rule = new ShardingRule(ruleConfig, Collections.singletonMap("foo_ds", dataSource), mock(ComputeNodeInstanceContext.class), Collections.emptyList());
        StorageUnit storageUnit = mock(StorageUnit.class);
        when(storageUnit.getStorageType()).thenReturn(databaseType);
        when(storageUnit.getDataSource()).thenReturn(dataSource);
        Properties props = PropertiesBuilder.build(new Property(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED.getKey(), Boolean.FALSE.toString()));
        return new GenericSchemaBuilderMaterial(Collections.singletonMap("foo_ds", storageUnit), Collections.singleton(rule), new ConfigurationProperties(props),
                "public", DatabaseIdentifierContextFactory.createDefault());
    }
    
    private Map<String, SchemaMetaData> createSchemas(final int secondColumnType) {
        Map<String, SchemaMetaData> result = new LinkedHashMap<>();
        result.put("public", new SchemaMetaData("public", Collections.singleton(createTable("foo_tbl_0", Types.INTEGER))));
        result.put("archive", new SchemaMetaData("archive", Collections.singleton(createTable("foo_tbl_1", secondColumnType))));
        return result;
    }
    
    private TableMetaData createTable(final String tableName, final int columnType) {
        ColumnMetaData column = new ColumnMetaData("id", columnType, false, false, false, true, false, true);
        IndexMetaData index = new IndexMetaData(IndexMetaDataUtils.getActualIndexName("foo_idx", tableName, databaseType), Collections.singletonList("id"));
        TableMetaData result = new TableMetaData(tableName, Collections.singleton(column), Collections.singleton(index), Collections.emptyList());
        result.setStorageUnitName("foo_ds");
        return result;
    }
    
    @Test
    void assertRefreshRejectsMissingPhysicalTableAcrossSchemas() throws SQLException {
        Map<String, SchemaMetaData> schemas = createSchemas(Types.INTEGER);
        schemas.remove("archive");
        when(MetaDataLoader.load(any())).thenReturn(schemas);
        RuleAndStorageMetaDataMismatchedException actual = assertThrows(RuleAndStorageMetaDataMismatchedException.class,
                () -> GenericSchemaBuilder.buildForRefresh(Collections.singleton("foo_tbl"), databaseType, createMaterial()));
        assertThat(actual.getMessage(), is("Rule and storage meta data mismatched, reason is: Missing physical table metadata for 'foo_tbl': foo_ds.archive.foo_tbl_1."));
    }
    
    @Test
    void assertBuildForRefreshAfterPublicTableRemoval() throws SQLException {
        when(MetaDataLoader.load(any())).thenReturn(Collections.singletonMap("archive", createSingleSchema("archive", Types.INTEGER)));
        Map<String, ShardingSphereSchema> actual = GenericSchemaBuilder.buildForRefresh(Collections.singleton("foo_tbl"), databaseType, createSingleMaterial());
        assertThat(actual.keySet(), is(Collections.singleton("archive")));
        assertThat(actual.get("archive").getTable("foo_tbl").getColumn("id").getDataType(), is(Types.INTEGER));
    }
    
    private SchemaMetaData createSingleSchema(final String schemaName, final int columnType) {
        ColumnMetaData column = new ColumnMetaData("id", columnType, false, false, false, true, false, true);
        TableMetaData table = new TableMetaData("foo_tbl", Collections.singleton(column), Collections.emptyList(), Collections.emptyList());
        table.setStorageUnitName("foo_ds");
        return new SchemaMetaData(schemaName, Collections.singleton(table));
    }
    
    private GenericSchemaBuilderMaterial createSingleMaterial() {
        MockedDataSource dataSource = new MockedDataSource();
        SingleRule rule = new SingleRule(new SingleRuleConfiguration(), "foo_db", databaseType, Collections.singletonMap("foo_ds", dataSource), Collections.emptyList())
                .copyAndPut("foo_ds", "public", "foo_tbl").copyAndPut("foo_ds", "archive", "foo_tbl");
        StorageUnit storageUnit = mock(StorageUnit.class);
        when(storageUnit.getStorageType()).thenReturn(databaseType);
        when(storageUnit.getDataSource()).thenReturn(dataSource);
        return new GenericSchemaBuilderMaterial(Collections.singletonMap("foo_ds", storageUnit), Collections.singleton(rule), new ConfigurationProperties(new Properties()),
                "public", DatabaseIdentifierContextFactory.createDefault());
    }
    
    @Test
    void assertBuildForRefreshAfterArchiveTableRemoval() throws SQLException {
        when(MetaDataLoader.load(any())).thenReturn(Collections.singletonMap("public", createSingleSchema("public", Types.INTEGER)));
        Map<String, ShardingSphereSchema> actual = GenericSchemaBuilder.buildForRefresh(Collections.singleton("foo_tbl"), databaseType, createSingleMaterial());
        assertThat(actual.keySet(), is(Collections.singleton("public")));
        assertThat(actual.get("public").getTable("foo_tbl").getColumn("id").getDataType(), is(Types.INTEGER));
    }
    
    @Test
    void assertBuildForRefreshWithIndependentTableStructures() throws SQLException {
        Map<String, SchemaMetaData> schemas = new LinkedHashMap<>(2, 1F);
        schemas.put("public", createSingleSchema("public", Types.INTEGER));
        schemas.put("archive", createSingleSchema("archive", Types.VARCHAR));
        when(MetaDataLoader.load(any())).thenReturn(schemas);
        Map<String, ShardingSphereSchema> actual = GenericSchemaBuilder.buildForRefresh(Collections.singleton("foo_tbl"), databaseType, createSingleMaterial());
        assertThat(actual.get("public").getTable("foo_tbl").getColumn("id").getDataType(), is(Types.INTEGER));
        assertThat(actual.get("archive").getTable("foo_tbl").getColumn("id").getDataType(), is(Types.VARCHAR));
    }
    
    @Test
    void assertRefreshKeepsSchemasAndPhysicalIndexNamesAfterValidation() throws SQLException {
        Map<String, SchemaMetaData> schemas = createSchemas(Types.INTEGER);
        String expectedPhysicalMetaData = schemas.toString();
        when(MetaDataLoader.load(any())).thenReturn(schemas);
        Map<String, ShardingSphereSchema> actual = GenericSchemaBuilder.buildForRefresh(Collections.singleton("foo_tbl"), databaseType, createMaterial());
        assertThat(actual.size(), is(2));
        assertThat(actual.get("public").getTable("foo_tbl").getIndex("foo_idx").getName(), is("foo_idx"));
        assertThat(actual.get("archive").getTable("foo_tbl").getIndex("foo_idx").getName(), is("foo_idx"));
        assertThat(schemas.toString(), is(expectedPhysicalMetaData));
    }
}
