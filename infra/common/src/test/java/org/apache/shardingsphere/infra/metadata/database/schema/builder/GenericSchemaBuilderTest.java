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

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.fixture.rule.TableContainedFixtureRule;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.metadata.SchemaMetaDataLoaderEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.sql.DataSource;
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
@StaticMockSettings(SchemaMetaDataLoaderEngine.class)
class GenericSchemaBuilderTest {
    
    private GenericSchemaBuilderMaterial material;
    
    @BeforeEach
    void setUp() {
        DatabaseType databaseType = mock(DatabaseType.class);
        material = new GenericSchemaBuilderMaterial(databaseType, Collections.emptyMap(), Collections.singletonMap(DefaultDatabase.LOGIC_NAME, mock(DataSource.class)),
                Collections.singleton(new TableContainedFixtureRule()), new ConfigurationProperties(new Properties()), DefaultDatabase.LOGIC_NAME);
    }
    
    @Test
    void assertLoadWithExistedTableName() throws SQLException {
        Collection<String> tableNames = Collections.singletonList("data_node_routed_table1");
        when(SchemaMetaDataLoaderEngine.load(any())).thenReturn(createSchemaMetaDataMap(tableNames, material));
        assertFalse(GenericSchemaBuilder.build(tableNames, material).get(DefaultDatabase.LOGIC_NAME).getTables().isEmpty());
    }
    
    @Test
    void assertLoadWithNotExistedTableName() throws SQLException {
        Collection<String> tableNames = Collections.singletonList("invalid_table");
        when(SchemaMetaDataLoaderEngine.load(any())).thenReturn(createSchemaMetaDataMap(tableNames, material));
        assertTrue(GenericSchemaBuilder.build(tableNames, material).get(DefaultDatabase.LOGIC_NAME).getTables().isEmpty());
    }
    
    @Test
    void assertLoadAllTables() throws SQLException {
        Collection<String> tableNames = Arrays.asList("data_node_routed_table1", "data_node_routed_table2");
        when(SchemaMetaDataLoaderEngine.load(any())).thenReturn(createSchemaMetaDataMap(tableNames, material));
        Map<String, ShardingSphereSchema> actual = GenericSchemaBuilder.build(tableNames, material);
        assertThat(actual.size(), is(1));
        assertTables(new ShardingSphereSchema(actual.values().iterator().next().getTables(), Collections.emptyMap()).getTables());
    }
    
    private Map<String, SchemaMetaData> createSchemaMetaDataMap(final Collection<String> tableNames, final GenericSchemaBuilderMaterial material) {
        if (!tableNames.isEmpty() && (tableNames.contains("data_node_routed_table1") || tableNames.contains("data_node_routed_table2"))) {
            Collection<TableMetaData> tableMetaDataList = tableNames.stream()
                    .map(each -> new TableMetaData(each, Collections.emptyList(), Collections.emptyList(), Collections.emptyList())).collect(Collectors.toList());
            return Collections.singletonMap(material.getDefaultSchemaName(), new SchemaMetaData(material.getDefaultSchemaName(), tableMetaDataList));
        }
        return Collections.emptyMap();
    }
    
    private void assertTables(final Map<String, ShardingSphereTable> actual) {
        assertThat(actual.size(), is(2));
        assertTrue(actual.get("data_node_routed_table1").getColumnValues().isEmpty());
        assertTrue(actual.get("data_node_routed_table2").getColumnValues().isEmpty());
    }
}
