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
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.fixture.rule.CommonFixtureRule;
import org.apache.shardingsphere.infra.metadata.database.schema.fixture.rule.DataNodeContainedFixtureRule;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.SchemaMetaDataLoaderEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@RunWith(MockitoJUnitRunner.class)
public final class GenericSchemaBuilderTest {
    
    @Mock
    private DatabaseType databaseType;
    
    private GenericSchemaBuilderMaterials materials;
    
    private MockedStatic<SchemaMetaDataLoaderEngine> schemaMetaDataLoaderEngine;
    
    @Before
    public void setUp() {
        Collection<ShardingSphereRule> rules = Arrays.asList(new CommonFixtureRule(), new DataNodeContainedFixtureRule());
        materials = new GenericSchemaBuilderMaterials(databaseType, databaseType, Collections.singletonMap(DefaultDatabase.LOGIC_NAME,
                mock(DataSource.class)), rules, new ConfigurationProperties(new Properties()), DefaultDatabase.LOGIC_NAME);
        schemaMetaDataLoaderEngine = mockStatic(SchemaMetaDataLoaderEngine.class);
    }
    
    @Test
    public void assertLoadWithExistedTableName() throws SQLException {
        Collection<String> tableNames = Collections.singletonList("data_node_routed_table1");
        schemaMetaDataLoaderEngine.when(() -> SchemaMetaDataLoaderEngine.load(any(), any())).thenReturn(mockSchemaMetaDataMap(tableNames, materials));
        assertFalse(GenericSchemaBuilder.build(tableNames, materials).get(DefaultDatabase.LOGIC_NAME).getTables().isEmpty());
    }
    
    @Test
    public void assertLoadWithNotExistedTableName() throws SQLException {
        Collection<String> tableNames = Collections.singletonList("invalid_table");
        schemaMetaDataLoaderEngine.when(() -> SchemaMetaDataLoaderEngine.load(any(), any())).thenReturn(mockSchemaMetaDataMap(tableNames, materials));
        assertTrue(GenericSchemaBuilder.build(tableNames, materials).get(DefaultDatabase.LOGIC_NAME).getTables().isEmpty());
    }
    
    @Test
    public void assertLoadAllTables() throws SQLException {
        Collection<String> tableNames = new DataNodeContainedFixtureRule().getTables();
        schemaMetaDataLoaderEngine.when(() -> SchemaMetaDataLoaderEngine.load(any(), any())).thenReturn(mockSchemaMetaDataMap(tableNames, materials));
        Map<String, ShardingSphereSchema> actual = GenericSchemaBuilder.build(tableNames, materials);
        assertThat(actual.size(), is(1));
        assertTables(new ShardingSphereSchema(actual.values().iterator().next().getTables()).getTables());
    }
    
    @After
    public void cleanUp() {
        schemaMetaDataLoaderEngine.close();
    }
    
    private void assertTables(final Map<String, ShardingSphereTable> actual) {
        assertThat(actual.size(), is(2));
        assertTrue(actual.get("data_node_routed_table1").getColumns().isEmpty());
        assertTrue(actual.get("data_node_routed_table2").getColumns().isEmpty());
    }
    
    private Map<String, SchemaMetaData> mockSchemaMetaDataMap(final Collection<String> tableNames, final GenericSchemaBuilderMaterials materials) {
        if (!tableNames.isEmpty() && (tableNames.contains("data_node_routed_table1") || tableNames.contains("data_node_routed_table2"))) {
            Collection<TableMetaData> tableMetaDataList = new LinkedList<>();
            for (String each : tableNames) {
                tableMetaDataList.add(new TableMetaData(each, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
            }
            return Collections.singletonMap(materials.getDefaultSchemaName(), new SchemaMetaData(materials.getDefaultSchemaName(), tableMetaDataList));
        }
        return Collections.emptyMap();
    }
}
