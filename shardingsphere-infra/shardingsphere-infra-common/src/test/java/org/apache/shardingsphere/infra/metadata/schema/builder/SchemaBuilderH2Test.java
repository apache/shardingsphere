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
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.schema.fixture.rule.CommonFixtureRule;
import org.apache.shardingsphere.infra.metadata.schema.fixture.rule.DataNodeContainedFixtureRule;
import org.apache.shardingsphere.infra.metadata.schema.model.ColumnMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.IndexMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.junit.Before;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SchemaBuilderH2Test {

    private final String tableMetaDataSql = "SELECT TABLE_CATALOG, TABLE_NAME, COLUMN_NAME, DATA_TYPE, TYPE_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_CATALOG=? AND TABLE_SCHEMA=?";

    private final String tableMetaDataSqlWithExistedTables = tableMetaDataSql + " AND TABLE_NAME NOT IN (%s)";

    private final String indexMetaDataSql = "SELECT TABLE_CATALOG, TABLE_NAME, INDEX_NAME, COLUMN_NAME FROM INFORMATION_SCHEMA.INDEXES WHERE TABLE_CATALOG=? AND TABLE_SCHEMA=? AND TABLE_NAME IN ";

    private final String primaryKeysMetaDataSql = "SELECT TABLE_NAME, COLUMN_NAME FROM INFORMATION_SCHEMA.INDEXES WHERE TABLE_CATALOG=? AND TABLE_SCHEMA=? AND PRIMARY_KEY = TRUE";

    private final String primaryKeysMetaDataWithExistedTables = primaryKeysMetaDataSql + " AND TABLE_NAME NOT IN (%s)";

    private final String generatedInfoSql = "SELECT C.TABLE_NAME TABLE_NAME, C.COLUMN_NAME COLUMN_NAME, COALESCE(S.IS_GENERATED, FALSE) IS_GENERATED FROM INFORMATION_SCHEMA.COLUMNS C"
            + " RIGHT JOIN INFORMATION_SCHEMA.SEQUENCES S ON C.SEQUENCE_NAME=S.SEQUENCE_NAME WHERE C.TABLE_CATALOG=? AND C.TABLE_SCHEMA=?";

    private final String[] shardTableNames = {"data_node_routed_table1", "data_node_routed_table2"};

    private final String[] singleTableNames = {"single_table1"};

    private final DatabaseType databaseType = new H2DatabaseType();

    private final DataNodeContainedFixtureRule dataNodeContainedFixtureRule = new DataNodeContainedFixtureRule();

    private SchemaBuilderMaterials schemaBuilderMaterials;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataSource dataSource1;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DataSource dataSource2;

    @Mock
    private ConfigurationProperties props;

    @Before
    public void setUp() {
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1);
        dataSourceMap.put("ds_1", dataSource1);
        dataSourceMap.put("ds_2", dataSource2);

        Map<String, Collection<DataNode>> nodeMap = new LinkedHashMap<>(2);
        nodeMap.putIfAbsent("data_node_routed_table1", Arrays.asList(
                new DataNode("ds_1", "data_node_routed_table1_0"),
                new DataNode("ds_2", "data_node_routed_table1_1")));
        nodeMap.putIfAbsent("data_node_routed_table2", Arrays.asList(
                new DataNode("ds_2", "data_node_routed_table2_1"),
                new DataNode("ds_1", "data_node_routed_table2_0")));
        dataNodeContainedFixtureRule.setNodeMap(nodeMap);
        schemaBuilderMaterials = new SchemaBuilderMaterials(
                databaseType, dataSourceMap, Arrays
                .asList(new CommonFixtureRule(), dataNodeContainedFixtureRule), props);
    }

    @Test
    @SneakyThrows(SQLException.class)
    public void assertBuildOfShardingTables() {

        Connection connection1 = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(dataSource1.getConnection()).thenReturn(connection1);
        ResultSet resultSet1 = mockTypeInfoResultSet();
        when(connection1.getMetaData().getTypeInfo()).thenReturn(resultSet1);

        String sql1 = String.format(tableMetaDataSqlWithExistedTables, "'data_node_routed_table2_0'");
        resultSet1 = mockTableMetaDataResultSet1();
        when(connection1.prepareStatement(sql1).executeQuery()).thenReturn(resultSet1);
        resultSet1 = mockIndexMetaDataResultSet1();
        when(connection1.prepareStatement(contains(indexMetaDataSql)).executeQuery()).thenReturn(resultSet1);
        String primarySql1 = String.format(primaryKeysMetaDataWithExistedTables, "'data_node_routed_table2_0'");
        resultSet1 = mockPrimaryKeysMetaDataResultSet1();
        when(connection1.prepareStatement(primarySql1).executeQuery()).thenReturn(resultSet1);
        resultSet1 = mockGeneratedInfoResultSet1();
        when(connection1.prepareStatement(generatedInfoSql).executeQuery()).thenReturn(resultSet1);

        Connection connection2 = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(dataSource2.getConnection()).thenReturn(connection2);
        ResultSet resultSet2 = mockTypeInfoResultSet();
        when(connection2.getMetaData().getTypeInfo()).thenReturn(resultSet2);
        String sql2 = String.format(tableMetaDataSqlWithExistedTables, "'data_node_routed_table1_1'");
        resultSet2 = mockTableMetaDataResultSet2();
        when(connection2.prepareStatement(sql2).executeQuery()).thenReturn(resultSet2);
        resultSet2 = mockIndexMetaDataResultSet2();
        when(connection2.prepareStatement(contains(indexMetaDataSql)).executeQuery()).thenReturn(resultSet2);
        String primarySql2 = String.format(primaryKeysMetaDataWithExistedTables, "'data_node_routed_table1_1'");
        resultSet2 = mockPrimaryKeysMetaDataResultSet2();
        when(connection2.prepareStatement(primarySql2).executeQuery()).thenReturn(resultSet2);
        resultSet2 = mockGeneratedInfoResultSet2();
        when(connection2.prepareStatement(generatedInfoSql).executeQuery()).thenReturn(resultSet2);

        Map<TableMetaData, TableMetaData> tableMetaDatas = SchemaBuilder.build(schemaBuilderMaterials);
        assertThat(tableMetaDatas.keySet().size(), is(3));
        assertActualOfShardingTablesAndSingleTables(tableMetaDatas.keySet());
    }

    private ResultSet mockTypeInfoResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, false);
        when(result.getString("TYPE_NAME")).thenReturn("int", "varchar");
        when(result.getInt("DATA_TYPE")).thenReturn(4, 12);
        return result;
    }

    private ResultSet mockTableMetaDataResultSet1() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, true, true, false);
        when(result.getString("TABLE_NAME")).thenReturn(shardTableNames[0] + "_0", shardTableNames[0] + "_0", singleTableNames[0], singleTableNames[0]);
        when(result.getString("COLUMN_NAME")).thenReturn("id1", "name1", "id_s", "name_s");
        when(result.getString("TYPE_NAME")).thenReturn("int", "varchar", "int", "varchar");
        return result;
    }

    private ResultSet mockIndexMetaDataResultSet1() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, false);
        when(result.getString("INDEX_NAME")).thenReturn("id1", "id_s");
        when(result.getString("TABLE_NAME")).thenReturn(shardTableNames[0] + "_0", singleTableNames[0]);
        return result;
    }

    private ResultSet mockPrimaryKeysMetaDataResultSet1() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, false);
        when(result.getString("TABLE_NAME")).thenReturn(shardTableNames[0] + "_0", singleTableNames[0]);
        when(result.getString("COLUMN_NAME")).thenReturn("id1", "id_s");
        return result;
    }

    private ResultSet mockGeneratedInfoResultSet1() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, false);
        when(result.getString("TABLE_NAME")).thenReturn(shardTableNames[0] + "_0", singleTableNames[0]);
        when(result.getString("COLUMN_NAME")).thenReturn("id1", "id_s");
        when(result.getBoolean("IS_GENERATED")).thenReturn(false, false);
        return result;
    }

    private ResultSet mockTableMetaDataResultSet2() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, false);
        when(result.getString("TABLE_NAME")).thenReturn(shardTableNames[1] + "_1", shardTableNames[1] + "_1");
        when(result.getString("COLUMN_NAME")).thenReturn("id2", "name2");
        when(result.getString("TYPE_NAME")).thenReturn("int", "varchar");
        return result;
    }

    private ResultSet mockIndexMetaDataResultSet2() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("INDEX_NAME")).thenReturn("id2");
        when(result.getString("TABLE_NAME")).thenReturn(shardTableNames[1] + "_1");
        return result;
    }

    private ResultSet mockPrimaryKeysMetaDataResultSet2() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("TABLE_NAME")).thenReturn(shardTableNames[1] + "_1");
        when(result.getString("COLUMN_NAME")).thenReturn("id2");
        return result;
    }

    private ResultSet mockGeneratedInfoResultSet2() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("TABLE_NAME")).thenReturn(shardTableNames[1] + "_1");
        when(result.getString("COLUMN_NAME")).thenReturn("id2");
        when(result.getBoolean("IS_GENERATED")).thenReturn(false);
        return result;
    }

    private void assertActualOfShardingTablesAndSingleTables(final Collection<TableMetaData> actual) {
        Map<String, TableMetaData> tableMetaDataMap = actual.stream().collect(Collectors.toMap(TableMetaData::getName, v -> v));
        assertTrue(tableMetaDataMap.containsKey(shardTableNames[0]));
        TableMetaData tableMetaData1 = tableMetaDataMap.get(shardTableNames[0]);
        Map<String, ColumnMetaData> columnMap1 = tableMetaData1.getColumns();
        assertThat(columnMap1.size(), is(2));
        assertTrue(columnMap1.containsKey("id1"));
        assertTrue(columnMap1.get("id1").isPrimaryKey());
        assertFalse(columnMap1.get("id1").isGenerated());
        assertTrue(columnMap1.containsKey("name1"));
        assertFalse(columnMap1.get("name1").isPrimaryKey());
        assertFalse(columnMap1.get("name1").isGenerated());
        Map<String, IndexMetaData> indexMap1 = tableMetaData1.getIndexes();
        assertThat(indexMap1.size(), is(1));
        assertTrue(indexMap1.containsKey("id1"));
        assertTrue(tableMetaData1.getPrimaryKeyColumns().contains("id1"));

        assertTrue(tableMetaDataMap.containsKey(shardTableNames[1]));
        TableMetaData tableMetaData2 = tableMetaDataMap.get(shardTableNames[1]);
        Map<String, ColumnMetaData> columnMap2 = tableMetaData2.getColumns();
        assertThat(columnMap2.size(), is(2));
        assertTrue(columnMap2.containsKey("id2"));
        assertTrue(columnMap2.get("id2").isPrimaryKey());
        assertFalse(columnMap2.get("id2").isGenerated());
        assertTrue(columnMap2.containsKey("name2"));
        assertFalse(columnMap2.get("name2").isPrimaryKey());
        assertFalse(columnMap2.get("name2").isGenerated());
        Map<String, IndexMetaData> indexMap2 = tableMetaData2.getIndexes();
        assertThat(indexMap2.size(), is(1));
        assertTrue(indexMap2.containsKey("id2"));
        assertTrue(tableMetaData2.getPrimaryKeyColumns().contains("id2"));

        assertTrue(tableMetaDataMap.containsKey(singleTableNames[0]));
        TableMetaData tableMetaDataSingle = tableMetaDataMap.get(singleTableNames[0]);
        Map<String, ColumnMetaData> columnMapSingle = tableMetaDataSingle.getColumns();
        assertThat(columnMapSingle.size(), is(2));
        assertTrue(columnMapSingle.containsKey("id_s"));
        assertTrue(columnMapSingle.get("id_s").isPrimaryKey());
        assertFalse(columnMapSingle.get("id_s").isGenerated());
        assertTrue(columnMapSingle.containsKey("name_s"));
        assertFalse(columnMapSingle.get("name_s").isPrimaryKey());
        assertFalse(columnMapSingle.get("name_s").isGenerated());
        Map<String, IndexMetaData> indexMapSingle = tableMetaDataSingle.getIndexes();
        assertThat(indexMapSingle.size(), is(1));
        assertTrue(indexMapSingle.containsKey("id_s"));
        assertTrue(tableMetaDataSingle.getPrimaryKeyColumns().contains("id_s"));
    }
}
