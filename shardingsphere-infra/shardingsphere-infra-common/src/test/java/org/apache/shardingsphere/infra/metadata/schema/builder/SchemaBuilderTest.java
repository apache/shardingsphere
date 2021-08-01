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

package org.apache.shardingsphere.infra.metadata.schema.builder;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.fixture.rule.CommonFixtureRule;
import org.apache.shardingsphere.infra.metadata.schema.fixture.rule.DataNodeContainedFixtureRule;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SchemaBuilderTest {
    
    private static final String TEST_CATALOG = "catalog";
    
    private static final String TEST_SCHEMA = "schema";
    
    private static final String TABLE_TYPE = "TABLE";
    
    private static final String VIEW_TYPE = "VIEW";
    
    private static final String TABLE_NAME = "TABLE_NAME";
    
    private final String[] singleTableNames = {"single_table1", "single_table2"};
    
    private SchemaBuilderMaterials schemaBuilderMaterials;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DatabaseType databaseType;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataSource dataSource;
    
    @Mock
    private ConfigurationProperties props;
    
    @Before
    public void setUp() {
        schemaBuilderMaterials = new SchemaBuilderMaterials(
                databaseType, Collections.singletonMap("logic_db", dataSource), Arrays.asList(new CommonFixtureRule(), new DataNodeContainedFixtureRule()), props);
    }
    
    @Test
    public void assertBuildOfAllShardingTables() throws SQLException {
        Map<TableMetaData, TableMetaData> actual = SchemaBuilder.build(schemaBuilderMaterials);
        assertThat(actual.values().size(), is(2));
        assertThat(actual.keySet().size(), is(2));
        assertSchemaOfShardingTables(actual.keySet());
    }
    
    private void assertSchemaOfShardingTables(final Collection<TableMetaData> actual) {
        Map<String, TableMetaData> tableMetaDataMap = actual.stream().collect(Collectors.toMap(TableMetaData::getName, v -> v));
        assertTrue(tableMetaDataMap.containsKey("data_node_routed_table1"));
        assertTrue(tableMetaDataMap.get("data_node_routed_table1").getColumns().isEmpty());
        assertTrue(tableMetaDataMap.containsKey("data_node_routed_table2"));
        assertTrue(tableMetaDataMap.get("data_node_routed_table2").getColumns().isEmpty());
    }
    
    @Test
    @SneakyThrows(SQLException.class)
    public void assertBuildOfShardingTablesAndSingleTables() {
        ResultSet resultSet = mock(ResultSet.class, Answers.RETURNS_DEEP_STUBS);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class, Answers.RETURNS_DEEP_STUBS);
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(connection.getCatalog()).thenReturn(TEST_CATALOG);
        when(connection.getSchema()).thenReturn(TEST_SCHEMA);
        when(databaseType.getSchema(connection)).thenReturn(TEST_SCHEMA);
        when(databaseMetaData.getTables(connection.getCatalog(), connection.getSchema(), null, new String[]{TABLE_TYPE, VIEW_TYPE})).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, true, true, true, true, false);
        String[] mockReturnTables = {singleTableNames[1], "data_node_routed_table1_0", "data_node_routed_table1_1", "data_node_routed_table2_0", "data_node_routed_table2_1"};
        when(resultSet.getString(TABLE_NAME)).thenReturn(singleTableNames[0], mockReturnTables);
        Map<TableMetaData, TableMetaData> tableMetaData = SchemaBuilder.build(schemaBuilderMaterials);
        assertThat(tableMetaData.keySet().size(), is(4));
        assertActualOfShardingTablesAndSingleTables(tableMetaData.keySet());
    }
    
    private void assertActualOfShardingTablesAndSingleTables(final Collection<TableMetaData> actual) {
        Map<String, TableMetaData> tableMetaDataMap = actual.stream().collect(Collectors.toMap(TableMetaData::getName, v -> v));
        assertTrue(tableMetaDataMap.containsKey(singleTableNames[0]));
        assertTrue(tableMetaDataMap.get(singleTableNames[0]).getColumns().isEmpty());
        assertTrue(tableMetaDataMap.containsKey(singleTableNames[1]));
        assertTrue(tableMetaDataMap.get(singleTableNames[1]).getColumns().isEmpty());
    }
}
