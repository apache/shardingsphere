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

package org.apache.shardingsphere.data.pipeline.core.importer;

import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.Channel;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.core.datasource.DataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.datasource.DataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.core.record.RecordUtil;
import org.apache.shardingsphere.data.pipeline.spi.sqlbuilder.PipelineSQLBuilder;
import org.apache.shardingsphere.data.pipeline.core.datasource.config.JDBCDataSourceConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class AbstractImporterTest {
    
    private static final String TABLE_NAME = "test_table";
    
    private static final String INSERT_SQL = "INSERT INTO test_table (id,user,status) VALUES(?,?,?)";
    
    private static final String DELETE_SQL = "DELETE FROM test_table WHERE id = ? and user = ?";
    
    private static final String UPDATE_SQL = "UPDATE test_table SET user = ?,status = ? WHERE id = ? and user = ?";
    
    @Mock
    private DataSourceManager dataSourceManager;
    
    @Mock
    private PipelineSQLBuilder pipelineSqlBuilder;
    
    @Mock
    private JDBCDataSourceConfiguration dataSourceConfig;
    
    @Mock
    private Channel channel;
    
    @Mock
    private DataSourceWrapper dataSource;
    
    @Mock
    private Connection connection;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    private AbstractImporter jdbcImporter;
    
    @Before
    public void setUp() throws SQLException {
        jdbcImporter = new AbstractImporter(mockImporterConfiguration(), dataSourceManager) {
            
            @Override
            protected PipelineSQLBuilder createSQLBuilder(final Map<String, Set<String>> shardingColumnsMap) {
                return pipelineSqlBuilder;
            }
        };
        jdbcImporter.setChannel(channel);
        when(dataSourceManager.getDataSource(dataSourceConfig)).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
    }
    
    @Test
    public void assertWriteInsertDataRecord() throws SQLException {
        DataRecord insertRecord = getDataRecord("INSERT");
        when(pipelineSqlBuilder.buildInsertSQL(insertRecord)).thenReturn(INSERT_SQL);
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
        when(pipelineSqlBuilder.buildDeleteSQL(deleteRecord, mockConditionColumns(deleteRecord))).thenReturn(DELETE_SQL);
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
        when(pipelineSqlBuilder.buildUpdateSQL(updateRecord, mockConditionColumns(updateRecord))).thenReturn(UPDATE_SQL);
        when(connection.prepareStatement(UPDATE_SQL)).thenReturn(preparedStatement);
        when(channel.fetchRecords(anyInt(), anyInt())).thenReturn(mockRecords(updateRecord));
        when(pipelineSqlBuilder.extractUpdatedColumns(any(), any())).thenReturn(RecordUtil.extractUpdatedColumns(updateRecord));
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
        when(pipelineSqlBuilder.buildUpdateSQL(updateRecord, mockConditionColumns(updateRecord))).thenReturn(UPDATE_SQL);
        when(connection.prepareStatement(UPDATE_SQL)).thenReturn(preparedStatement);
        when(channel.fetchRecords(anyInt(), anyInt())).thenReturn(mockRecords(updateRecord));
        when(pipelineSqlBuilder.extractUpdatedColumns(any(), any())).thenReturn(RecordUtil.extractUpdatedColumns(updateRecord));
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
        return RecordUtil.extractConditionColumns(dataRecord, Collections.singleton("user"));
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
    
    private ImporterConfiguration mockImporterConfiguration() {
        ImporterConfiguration result = new ImporterConfiguration();
        result.setDataSourceConfig(dataSourceConfig);
        result.setShardingColumnsMap(Collections.singletonMap("test_table", Collections.singleton("user")));
        return result;
    }
}
