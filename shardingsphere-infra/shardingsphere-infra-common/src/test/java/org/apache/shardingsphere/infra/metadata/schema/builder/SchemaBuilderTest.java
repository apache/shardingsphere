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
import org.apache.shardingsphere.infra.rule.single.SingleTableRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SchemaBuilderTest {
    
    private static final String TABLE_NAME = "TABLE_NAME";
    
    private final String[] singleTableNames = {"single_table1", "single_table2"};
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DatabaseType databaseType;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataSource dataSource;
    
    @Mock
    private ConfigurationProperties props;
    
    @Test
    public void assertBuildOfAllShardingTables() throws SQLException {
        Map<TableMetaData, TableMetaData> actual = SchemaBuilder.build(new SchemaBuilderMaterials(
                databaseType, Collections.singletonMap("logic_db", dataSource), Arrays.asList(new CommonFixtureRule(), new DataNodeContainedFixtureRule()), props));
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
    public void assertBuildOfSingleTables() {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(dataSource.getConnection()).thenReturn(connection);
        SingleTableRule singleTableRule = mockSingleTableRuleLoad(connection);
        mockSQLLoad(connection);
        Map<TableMetaData, TableMetaData> tableMetaData = SchemaBuilder.build(new SchemaBuilderMaterials(
                databaseType, Collections.singletonMap("logic_db", dataSource), Collections.singletonList(singleTableRule), props));
        assertThat(tableMetaData.keySet().size(), is(2));
        assertActualOfSingleTables(tableMetaData.keySet());
    }
    
    @SneakyThrows(SQLException.class)
    private void mockSQLLoad(final Connection connection) {
        when(databaseType.formatTableNamePattern("single_table1")).thenReturn("single_table1");
        when(databaseType.getQuoteCharacter().wrap("single_table1")).thenReturn("single_table1");
        when(databaseType.formatTableNamePattern("single_table2")).thenReturn("single_table2");
        when(databaseType.getQuoteCharacter().wrap("single_table2")).thenReturn("single_table2");
        ResultSet resultSet1 = mock(ResultSet.class);
        when(connection.getMetaData().getTables(any(), any(), eq("single_table1"), eq(null))).thenReturn(resultSet1);
        when(resultSet1.next()).thenReturn(true);
        ResultSet resultSet2 = mock(ResultSet.class);
        when(connection.getMetaData().getTables(any(), any(), eq("single_table2"), eq(null))).thenReturn(resultSet2);
        when(resultSet2.next()).thenReturn(true);
        ResultSet column = mock(ResultSet.class);
        when(column.next()).thenReturn(true, true, true, false);
        when(column.getString("TABLE_NAME")).thenReturn("single_table1");
        when(column.getString("COLUMN_NAME")).thenReturn("id", "name", "doc");
        when(column.getInt("DATA_TYPE")).thenReturn(4, 12, -1);
        when(column.getString("TYPE_NAME")).thenReturn("int", "varchar", "json");
        when(connection.getMetaData().getColumns(any(), any(), eq("single_table1"), eq("%"))).thenReturn(column);
        ResultSet column2 = mock(ResultSet.class);
        when(column2.next()).thenReturn(true, true, true, false);
        when(column2.getString("TABLE_NAME")).thenReturn("single_table2");
        when(column2.getString("COLUMN_NAME")).thenReturn("id", "name", "doc");
        when(column2.getInt("DATA_TYPE")).thenReturn(4, 12, -1);
        when(column2.getString("TYPE_NAME")).thenReturn("int", "varchar", "json");
        when(connection.getMetaData().getColumns(any(), any(), eq("single_table2"), eq("%"))).thenReturn(column2);
    }
    
    @SneakyThrows(SQLException.class)
    private SingleTableRule mockSingleTableRuleLoad(final Connection connection) {
        ResultSet resultSet = mock(ResultSet.class);
        when(connection.getMetaData().getTables(any(), any(), eq(null), any())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, true, true, true, true, false);
        when(resultSet.getString(TABLE_NAME)).thenReturn(singleTableNames[0], singleTableNames[1]);
        return new SingleTableRule(databaseType, Collections.singletonMap("logic_db", dataSource), Collections.emptyList(), new ConfigurationProperties(new Properties()));
    }
    
    private void assertActualOfSingleTables(final Collection<TableMetaData> actual) {
        Map<String, TableMetaData> tableMetaDataMap = actual.stream().collect(Collectors.toMap(TableMetaData::getName, v -> v));
        assertTrue(tableMetaDataMap.containsKey(singleTableNames[0]));
        assertFalse(tableMetaDataMap.get(singleTableNames[0]).getColumns().isEmpty());
        assertTrue(tableMetaDataMap.containsKey(singleTableNames[1]));
        assertFalse(tableMetaDataMap.get(singleTableNames[1]).getColumns().isEmpty());
    }
}
