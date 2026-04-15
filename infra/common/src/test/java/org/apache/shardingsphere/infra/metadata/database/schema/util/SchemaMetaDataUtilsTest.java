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

package org.apache.shardingsphere.infra.metadata.database.schema.util;

import org.apache.shardingsphere.database.connector.core.GlobalDataSourceRegistry;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderMaterial;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.kernel.metadata.datanode.UnsupportedActualDataNodeStructureException;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContextFactory;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SchemaMetaDataUtilsTest {
    
    private static final DatabaseType FIXTURE_DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private static final DatabaseType MYSQL_DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private static final DatabaseType ORACLE_DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "Oracle");
    
    @AfterEach
    void clearCachedDatabaseTables() {
        GlobalDataSourceRegistry.getInstance().getCachedDatabaseTables().clear();
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getMetaDataLoaderMaterialsArguments")
    void assertGetMetaDataLoaderMaterials(final String name, final Map<String, StorageUnit> storageUnits, final Collection<DataNode> dataNodes,
                                          final ConfigurationProperties props, final String defaultSchemaName, final List<String> expectedStorageUnitNames,
                                          final List<List<String>> expectedActualTableNames, final List<String> expectedDefaultSchemaNames) {
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(storageUnits, Collections.singleton(mockDataNodeRule(dataNodes)), props, defaultSchemaName,
                DatabaseIdentifierContextFactory.createDefault());
        List<MetaDataLoaderMaterial> actual = new ArrayList<>(SchemaMetaDataUtils.getMetaDataLoaderMaterials(Collections.singleton("foo_tbl"), material));
        assertThat(actual.size(), is(expectedStorageUnitNames.size()));
        for (int i = 0; i < actual.size(); i++) {
            assertThat(actual.get(i).getStorageUnitName(), is(expectedStorageUnitNames.get(i)));
            assertThat(new ArrayList<>(actual.get(i).getActualTableNames()), is(expectedActualTableNames.get(i)));
            assertThat(actual.get(i).getDefaultSchemaName(), is(expectedDefaultSchemaNames.get(i)));
        }
    }
    
    @Test
    void assertGetMetaDataLoaderMaterialsWithSupportedThreeTierStructure() {
        DataSource dataSource = mock(DataSource.class);
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(2, 1F);
        storageUnits.put("ds", mockStorageUnit(MYSQL_DATABASE_TYPE, dataSource));
        storageUnits.put("ds.foo_db", mockStorageUnit(MYSQL_DATABASE_TYPE, mock(DataSource.class)));
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(storageUnits,
                Collections.singleton(mockDataNodeRule(Collections.singleton(new DataNode("ds.foo_db", "foo_db", "foo_tbl")))),
                createProperties(Boolean.FALSE, null), "foo_db", DatabaseIdentifierContextFactory.createDefault());
        List<MetaDataLoaderMaterial> actual = new ArrayList<>(SchemaMetaDataUtils.getMetaDataLoaderMaterials(Collections.singleton("foo_tbl"), material));
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getStorageUnitName(), is("ds.foo_db"));
        assertThat(actual.get(0).getDataSource(), is(dataSource));
        assertThat(new ArrayList<>(actual.get(0).getActualTableNames()), is(Collections.singletonList("foo_tbl")));
        assertThat(actual.get(0).getDefaultSchemaName(), is("foo_db"));
        assertThat(GlobalDataSourceRegistry.getInstance().getCachedDatabaseTables().get("foo_tbl"), is("foo_db"));
    }
    
    @Test
    void assertGetMetaDataLoaderMaterialsWithUnsupportedThreeTierStructure() {
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(2, 1F);
        storageUnits.put("ds", mockStorageUnit(FIXTURE_DATABASE_TYPE, mock(DataSource.class)));
        storageUnits.put("ds.foo_db", mockStorageUnit(FIXTURE_DATABASE_TYPE, mock(DataSource.class)));
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(storageUnits,
                Collections.singleton(mockDataNodeRule(Collections.singleton(new DataNode("ds.foo_db", "foo_db", "foo_tbl")))),
                createProperties(Boolean.FALSE, null), "foo_db", DatabaseIdentifierContextFactory.createDefault());
        UnsupportedActualDataNodeStructureException actual = assertThrows(UnsupportedActualDataNodeStructureException.class,
                () -> SchemaMetaDataUtils.getMetaDataLoaderMaterials(Collections.singleton("foo_tbl"), material));
        assertThat(actual.getMessage(), is("Can not support 3-tier structure for actual data node 'ds.foo_db.foo_tbl' with JDBC 'jdbc:mock'."));
    }
    
    @Test
    void assertGetMetaDataLoaderMaterialsWithBatchSplit() {
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(1, 1F);
        storageUnits.put("ds_0", mockStorageUnit(FIXTURE_DATABASE_TYPE, mock(DataSource.class)));
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(storageUnits, Collections.singleton(
                mockDataNodeRule(Arrays.asList(new DataNode("ds_0.foo_tbl_0"), new DataNode("ds_0.foo_tbl_1"), new DataNode("ds_0.foo_tbl_2")))),
                createProperties(Boolean.TRUE, 2), "foo_db", DatabaseIdentifierContextFactory.createDefault());
        List<MetaDataLoaderMaterial> actual = new ArrayList<>(SchemaMetaDataUtils.getMetaDataLoaderMaterials(Collections.singleton("foo_tbl"), material));
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getStorageUnitName(), is("ds_0"));
        assertThat(new ArrayList<>(actual.get(0).getActualTableNames()), is(Arrays.asList("foo_tbl_0", "foo_tbl_1")));
        assertThat(new ArrayList<>(actual.get(1).getActualTableNames()), is(Collections.singletonList("foo_tbl_2")));
    }
    
    @Test
    void assertGetMetaDataLoaderMaterialsNormalizeActualTableNamesByTableScope() {
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(1, 1F);
        storageUnits.put("ds_0", mockStorageUnit(ORACLE_DATABASE_TYPE, mock(DataSource.class)));
        ConfigurationProperties props = new ConfigurationProperties(new Properties());
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(storageUnits,
                Collections.singleton(mockDataNodeRule(Collections.singleton(new DataNode("ds_0.t_user")))), props, "foo_db",
                DatabaseIdentifierContextFactory.create(MYSQL_DATABASE_TYPE, new ResourceMetaData(Collections.emptyMap(), storageUnits), props));
        List<MetaDataLoaderMaterial> actual = new ArrayList<>(SchemaMetaDataUtils.getMetaDataLoaderMaterials(Collections.singleton("foo_tbl"), material));
        assertThat(actual.size(), is(1));
        assertThat(new ArrayList<>(actual.get(0).getActualTableNames()), is(Collections.singletonList("T_USER")));
    }
    
    private ShardingSphereRule mockDataNodeRule(final Collection<DataNode> dataNodes) {
        ShardingSphereRule result = mock(ShardingSphereRule.class);
        DataNodeRuleAttribute ruleAttribute = mock(DataNodeRuleAttribute.class);
        when(ruleAttribute.getDataNodesByTableName("foo_tbl")).thenReturn(dataNodes);
        when(result.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute));
        return result;
    }
    
    private static Stream<Arguments> getMetaDataLoaderMaterialsArguments() {
        return Stream.of(
                Arguments.of("loads all actual tables when metadata check is enabled",
                        mockStorageUnits("ds.with.dot", "ds", "ds_0", "ds_1"),
                        Arrays.asList(new DataNode("ds_0.foo_tbl_0"), new DataNode("ds_1.foo_tbl_1")), createProperties(Boolean.TRUE, null), "foo_db",
                        Arrays.asList("ds_0", "ds_1"),
                        Arrays.asList(Collections.singletonList("foo_tbl_0"), Collections.singletonList("foo_tbl_1")),
                        Arrays.asList("foo_db", "foo_db")),
                Arguments.of("loads one actual table when metadata check is disabled",
                        mockStorageUnits("ds.with.dot", "ds", "ds_0", "ds_1"),
                        Arrays.asList(new DataNode("ds_0.foo_tbl_0"), new DataNode("ds_1.foo_tbl_1")), new ConfigurationProperties(new Properties()), "foo_db",
                        Collections.singletonList("ds_0"), Collections.singletonList(Collections.singletonList("foo_tbl_0")), Collections.singletonList("foo_db")),
                Arguments.of("falls back to first storage unit when schema differs",
                        mockStorageUnits("ds.with.dot", "ds", "ds_0", "ds_1"),
                        Collections.singletonList(new DataNode("ds_0", "different_schema", "foo_tbl")), new ConfigurationProperties(new Properties()), "default_schema",
                        Collections.singletonList("ds.with.dot"), Collections.singletonList(Collections.singletonList("foo_tbl")), Collections.singletonList("default_schema")),
                Arguments.of("falls back to first storage unit when data source is missing",
                        mockStorageUnits("ds.with.dot", "ds", "ds_0", "ds_1"),
                        Collections.singletonList(new DataNode("missing_ds.foo_tbl_0")), new ConfigurationProperties(new Properties()), "foo_db",
                        Collections.singletonList("ds.with.dot"), Collections.singletonList(Collections.singletonList("foo_tbl")), Collections.singletonList("foo_db")),
                Arguments.of("falls back to first storage unit when data nodes are empty",
                        mockStorageUnits("ds.with.dot", "ds", "ds_0", "ds_1"),
                        Collections.emptyList(), createProperties(Boolean.TRUE, null), "foo_db",
                        Collections.singletonList("ds.with.dot"), Collections.singletonList(Collections.singletonList("foo_tbl")), Collections.singletonList("foo_db")),
                Arguments.of("returns empty when storage units are empty and metadata check is enabled",
                        Collections.emptyMap(), Collections.emptyList(), createProperties(Boolean.TRUE, null), "foo_db",
                        Collections.emptyList(), Collections.emptyList(), Collections.emptyList()),
                Arguments.of("returns empty when storage units are empty and metadata check is disabled",
                        Collections.emptyMap(), Collections.emptyList(), createProperties(Boolean.FALSE, null), "foo_db",
                        Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
    }
    
    private static ConfigurationProperties createProperties(final Boolean checkMetaDataEnabled, final Integer loadTableMetadataBatchSize) {
        return new ConfigurationProperties(PropertiesBuilder.build(
                new Property(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED.getKey(), null == checkMetaDataEnabled ? null : checkMetaDataEnabled.toString()),
                new Property(ConfigurationPropertyKey.LOAD_TABLE_METADATA_BATCH_SIZE.getKey(), null == loadTableMetadataBatchSize ? null : loadTableMetadataBatchSize.toString())));
    }
    
    private static Map<String, StorageUnit> mockStorageUnits(final String... dataSourceNames) {
        return Arrays.stream(dataSourceNames).collect(Collectors.toMap(each -> each, each -> mockStorageUnit(FIXTURE_DATABASE_TYPE, mock(DataSource.class))));
    }
    
    private static StorageUnit mockStorageUnit(final DatabaseType storageType, final DataSource dataSource) {
        StorageUnit result = mock(StorageUnit.class);
        when(result.getStorageType()).thenReturn(storageType);
        when(result.getDataSource()).thenReturn(dataSource);
        return result;
    }
}
