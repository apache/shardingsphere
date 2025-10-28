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
        Collection<String> tableNames = Collections.singletonList("data_node_routed_table1");
        when(MetaDataLoader.load(any())).thenReturn(createSchemaMetaDataMap(tableNames, material));
        assertFalse(GenericSchemaBuilder.build(tableNames, databaseType, material).get("foo_schema").getAllTables().isEmpty());
    }
    
    @Test
    void assertLoadWithNotExistedTableName() throws SQLException {
        Collection<String> tableNames = Collections.singletonList("invalid_table");
        when(MetaDataLoader.load(any())).thenReturn(createSchemaMetaDataMap(tableNames, material));
        assertTrue(GenericSchemaBuilder.build(tableNames, databaseType, material).get("foo_schema").getAllTables().isEmpty());
    }
    
    @Test
    void assertLoadAllTables() throws SQLException {
        Collection<String> tableNames = Arrays.asList("data_node_routed_table1", "data_node_routed_table2");
        when(MetaDataLoader.load(any())).thenReturn(createSchemaMetaDataMap(tableNames, material));
        Map<String, ShardingSphereSchema> actual = GenericSchemaBuilder.build(tableNames, databaseType, material);
        assertThat(actual.size(), is(1));
        assertTables(new ShardingSphereSchema("foo_schema", actual.values().iterator().next().getAllTables(), Collections.emptyList()));
    }
    
    private Map<String, SchemaMetaData> createSchemaMetaDataMap(final Collection<String> tableNames, final GenericSchemaBuilderMaterial material) {
        if (!tableNames.isEmpty() && (tableNames.contains("data_node_routed_table1") || tableNames.contains("data_node_routed_table2"))) {
            Collection<TableMetaData> tableMetaDataList = tableNames.stream()
                    .map(each -> new TableMetaData(each, Collections.emptyList(), Collections.emptyList(), Collections.emptyList())).collect(Collectors.toList());
            return Collections.singletonMap(material.getDefaultSchemaName(), new SchemaMetaData(material.getDefaultSchemaName(), tableMetaDataList));
        }
        return Collections.emptyMap();
    }
    
    private void assertTables(final ShardingSphereSchema actual) {
        assertThat(actual.getAllTables().size(), is(2));
        assertTrue(actual.getTable("data_node_routed_table1").getAllColumns().isEmpty());
        assertTrue(actual.getTable("data_node_routed_table2").getAllColumns().isEmpty());
    }
}
