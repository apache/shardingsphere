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
import org.apache.shardingsphere.infra.metadata.database.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.fixture.rule.CommonFixtureRule;
import org.apache.shardingsphere.infra.metadata.database.schema.fixture.rule.DataNodeContainedFixtureRule;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class SchemaMetaDataBuilderTest {
    
    @Mock
    private DatabaseType databaseType;
    
    @Test
    public void assertLoadWithExistedTableName() throws SQLException {
        GenericSchemaBuilderMaterials materials = new GenericSchemaBuilderMaterials(databaseType, databaseType, Collections.singletonMap(DefaultDatabase.LOGIC_NAME, new MockedDataSource()),
                Arrays.asList(new CommonFixtureRule(), new DataNodeContainedFixtureRule()), new ConfigurationProperties(new Properties()), DefaultDatabase.LOGIC_NAME);
        assertFalse(GenericSchemaBuilder.build(Collections.singletonList("data_node_routed_table1"), materials).get(DefaultDatabase.LOGIC_NAME).getTables().isEmpty());
    }
    
    @Test
    public void assertLoadWithNotExistedTableName() throws SQLException {
        GenericSchemaBuilderMaterials materials = new GenericSchemaBuilderMaterials(databaseType, databaseType, Collections.singletonMap(DefaultDatabase.LOGIC_NAME, new MockedDataSource()),
                Arrays.asList(new CommonFixtureRule(), new DataNodeContainedFixtureRule()), new ConfigurationProperties(new Properties()), DefaultDatabase.LOGIC_NAME);
        assertTrue(GenericSchemaBuilder.build(Collections.singletonList("invalid_table"), materials).get(DefaultDatabase.LOGIC_NAME).getTables().isEmpty());
    }
    
    @Test
    public void assertLoadAllTables() throws SQLException {
        GenericSchemaBuilderMaterials materials = new GenericSchemaBuilderMaterials(databaseType, databaseType, Collections.singletonMap(DefaultDatabase.LOGIC_NAME, new MockedDataSource()),
                Arrays.asList(new CommonFixtureRule(), new DataNodeContainedFixtureRule()), new ConfigurationProperties(new Properties()), DefaultDatabase.LOGIC_NAME);
        Map<String, ShardingSphereSchema> actual = GenericSchemaBuilder.build(new DataNodeContainedFixtureRule().getTables(), materials);
        assertThat(actual.size(), is(1));
        assertTables(new ShardingSphereSchema(actual.values().iterator().next().getTables()).getTables());
    }
    
    private void assertTables(final Map<String, TableMetaData> actual) {
        assertThat(actual.size(), is(2));
        assertTrue(actual.get("data_node_routed_table1").getColumns().isEmpty());
        assertTrue(actual.get("data_node_routed_table2").getColumns().isEmpty());
    }
}
