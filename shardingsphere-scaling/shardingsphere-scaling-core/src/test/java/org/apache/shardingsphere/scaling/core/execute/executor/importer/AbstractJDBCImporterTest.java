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

package org.apache.shardingsphere.scaling.core.execute.executor.importer;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.shardingsphere.scaling.core.config.ImporterConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.execute.executor.channel.Channel;
import org.apache.shardingsphere.scaling.core.execute.executor.record.Column;
import org.apache.shardingsphere.scaling.core.execute.executor.record.DataRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.FinishedRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.Record;
import org.apache.shardingsphere.scaling.core.execute.executor.record.RecordUtil;
import org.apache.shardingsphere.scaling.core.job.position.PlaceholderPosition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AbstractJDBCImporterTest {
    
    private static final String TABLE_NAME = "test_table";
    
    private static final String INSERT_SQL = "INSERT INTO test_table (id,user,status) VALUES(?,?,?)";
    
    private static final String DELETE_SQL = "DELETE FROM test_table WHERE id = ? and user = ?";
    
    private static final String UPDATE_SQL = "UPDATE test_table SET user = ?,status = ? WHERE id = ? and user = ?";
    
    @Mock
    private DataSourceManager dataSourceManager;
    
    @Mock
    private AbstractSQLBuilder sqlBuilder;
    
    @Mock
    private ScalingDataSourceConfiguration dataSourceConfig;
    
    @Mock
    private Channel channel;
    
    @Mock
    private DataSource dataSource;
    
    @Mock
    private Connection connection;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    private AbstractJDBCImporter jdbcImporter;
    
    @Before
    public void setUp() throws SQLException {
        jdbcImporter = new AbstractJDBCImporter(getImporterConfiguration(), dataSourceManager) {
            
            @Override
            protected AbstractSQLBuilder createSQLBuilder(final Map<String, Set<String>> shardingColumnsMap) {
                return sqlBuilder;
            }
        };
        jdbcImporter.setChannel(channel);
        when(dataSourceManager.getDataSource(dataSourceConfig)).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
    }
    
    @Test
    public void assertWriteInsertDataRecord() throws SQLException {
        DataRecord insertRecord = getDataRecord("INSERT");
        when(sqlBuilder.buildInsertSQL(insertRecord)).thenReturn(INSERT_SQL);
        when(connection.prepareStatement(INSERT_SQL)).thenReturn(preparedStatement);
        when(channel.fetchRecords(anyInt(), anyInt())).thenReturn(mockRecords(insertRecord));
        jdbcImporter.run();
        verify(preparedStatement).setObject(1, 1);
        verify(preparedStatement).setObject(2, 10);
        verify(preparedStatement).setObject(3, "INSERT");
        verify(preparedStatement).addBatch();
    }
    
    @Test
    public void assertDeleteDataRecord() throws SQLException {
        DataRecord deleteRecord = getDataRecord("DELETE");
        when(sqlBuilder.buildDeleteSQL(deleteRecord, mockConditionColumns(deleteRecord))).thenReturn(DELETE_SQL);
        when(connection.prepareStatement(DELETE_SQL)).thenReturn(preparedStatement);
        when(channel.fetchRecords(anyInt(), anyInt())).thenReturn(mockRecords(deleteRecord));
        jdbcImporter.run();
        verify(preparedStatement).setObject(1, 1);
        verify(preparedStatement).setObject(2, 10);
        verify(preparedStatement).addBatch();
    }
    
    @Test
    public void assertUpdateDataRecord() throws SQLException {
        DataRecord updateRecord = getDataRecord("UPDATE");
        when(sqlBuilder.buildUpdateSQL(updateRecord, mockConditionColumns(updateRecord))).thenReturn(UPDATE_SQL);
        when(connection.prepareStatement(UPDATE_SQL)).thenReturn(preparedStatement);
        when(channel.fetchRecords(anyInt(), anyInt())).thenReturn(mockRecords(updateRecord));
        jdbcImporter.run();
        verify(preparedStatement).setObject(1, 10);
        verify(preparedStatement).setObject(2, "UPDATE");
        verify(preparedStatement).setObject(3, 1);
        verify(preparedStatement).setObject(4, 10);
        verify(preparedStatement).execute();
    }
    
    @Test
    public void assertUpdatePrimaryKeyDataRecord() throws SQLException {
        DataRecord updateRecord = getUpdatePrimaryKeyDataRecord();
        when(sqlBuilder.buildUpdateSQL(updateRecord, mockConditionColumns(updateRecord))).thenReturn(UPDATE_SQL);
        when(connection.prepareStatement(UPDATE_SQL)).thenReturn(preparedStatement);
        when(channel.fetchRecords(anyInt(), anyInt())).thenReturn(mockRecords(updateRecord));
        jdbcImporter.run();
        InOrder inOrder = inOrder(preparedStatement);
        inOrder.verify(preparedStatement).setObject(1, 2);
        inOrder.verify(preparedStatement).setObject(2, 10);
        inOrder.verify(preparedStatement).setObject(3, "UPDATE");
        inOrder.verify(preparedStatement).setObject(4, 1);
        inOrder.verify(preparedStatement).setObject(5, 10);
        inOrder.verify(preparedStatement).execute();
    }
    
    private DataRecord getUpdatePrimaryKeyDataRecord() {
        DataRecord result = new DataRecord(new PlaceholderPosition(), 3);
        result.setTableName(TABLE_NAME);
        result.setType("UPDATE");
        result.addColumn(new Column("id", 1, 2, true, true));
        result.addColumn(new Column("user", 10, true, false));
        result.addColumn(new Column("status", "UPDATE", true, false));
        return result;
    }
    
    private Collection<Column> mockConditionColumns(final DataRecord dataRecord) {
        return RecordUtil.extractConditionColumns(dataRecord, Sets.newHashSet("user"));
    }
    
    private List<Record> mockRecords(final DataRecord dataRecord) {
        List<Record> result = new LinkedList<>();
        result.add(dataRecord);
        result.add(new FinishedRecord(new PlaceholderPosition()));
        return result;
    }
    
    private DataRecord getDataRecord(final String recordType) {
        DataRecord result = new DataRecord(new PlaceholderPosition(), 3);
        result.setTableName(TABLE_NAME);
        result.setType(recordType);
        result.addColumn(new Column("id", 1, false, true));
        result.addColumn(new Column("user", 10, true, false));
        result.addColumn(new Column("status", recordType, true, false));
        return result;
    }
    
    private ImporterConfiguration getImporterConfiguration() {
        ImporterConfiguration result = new ImporterConfiguration();
        result.setDataSourceConfiguration(dataSourceConfig);
        Map<String, Set<String>> shardingColumnsMap = Maps.newHashMap();
        shardingColumnsMap.put("test_table", Sets.newHashSet("user"));
        result.setShardingColumnsMap(shardingColumnsMap);
        return result;
    }
}
