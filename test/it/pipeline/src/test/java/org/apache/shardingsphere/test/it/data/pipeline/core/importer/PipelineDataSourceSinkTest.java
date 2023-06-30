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

package org.apache.shardingsphere.test.it.data.pipeline.core.importer;

import org.apache.shardingsphere.data.pipeline.api.config.TableNameSchemaNameMapping;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.common.config.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.common.ingest.IngestDataChangeType;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.FinishedPosition;
import org.apache.shardingsphere.data.pipeline.common.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.core.importer.SingleChannelConsumerImporter;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineDataSourceSink;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineSink;
import org.apache.shardingsphere.test.it.data.pipeline.core.fixture.FixtureInventoryIncrementalJobItemContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PipelineDataSourceSinkTest {
    
    private static final String TABLE_NAME = "test_table";
    
    @Mock
    private PipelineDataSourceManager dataSourceManager;
    
    private final PipelineDataSourceConfiguration dataSourceConfig = new StandardPipelineDataSourceConfiguration(
            "jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL;USER=root;PASSWORD=root", "root", "root");
    
    @Mock
    private PipelineChannel channel;
    
    @Mock
    private PipelineDataSourceWrapper dataSource;
    
    @Mock
    private Connection connection;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    private SingleChannelConsumerImporter importer;
    
    @BeforeEach
    void setUp() throws SQLException {
        PipelineSink pipelineSink = new PipelineDataSourceSink(mockImporterConfiguration(), dataSourceManager);
        importer = new SingleChannelConsumerImporter(channel, 100, 1, TimeUnit.SECONDS, pipelineSink, new FixtureInventoryIncrementalJobItemContext());
        when(dataSourceManager.getDataSource(dataSourceConfig)).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
    }
    
    @Test
    void assertWriteInsertDataRecord() throws SQLException {
        DataRecord insertRecord = getDataRecord("INSERT");
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(channel.fetchRecords(anyInt(), anyLong(), any())).thenReturn(mockRecords(insertRecord));
        importer.run();
        verify(preparedStatement).setObject(1, 1);
        verify(preparedStatement).setObject(2, 10);
        verify(preparedStatement).setObject(3, "INSERT");
        verify(preparedStatement).addBatch();
    }
    
    @Test
    void assertDeleteDataRecord() throws SQLException {
        DataRecord deleteRecord = getDataRecord("DELETE");
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(channel.fetchRecords(anyInt(), anyLong(), any())).thenReturn(mockRecords(deleteRecord));
        when(preparedStatement.executeBatch()).thenReturn(new int[]{1});
        importer.run();
        verify(preparedStatement).setObject(1, 1);
        verify(preparedStatement).setObject(2, 10);
        verify(preparedStatement).addBatch();
    }
    
    @Test
    void assertUpdateDataRecord() throws SQLException {
        DataRecord updateRecord = getDataRecord("UPDATE");
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(channel.fetchRecords(anyInt(), anyLong(), any())).thenReturn(mockRecords(updateRecord));
        importer.run();
        verify(preparedStatement).setObject(1, 20);
        verify(preparedStatement).setObject(2, "UPDATE");
        verify(preparedStatement).setObject(3, 1);
        verify(preparedStatement).setObject(4, 10);
        verify(preparedStatement).executeUpdate();
    }
    
    @Test
    void assertUpdatePrimaryKeyDataRecord() throws SQLException {
        DataRecord updateRecord = getUpdatePrimaryKeyDataRecord();
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(channel.fetchRecords(anyInt(), anyLong(), any())).thenReturn(mockRecords(updateRecord));
        importer.run();
        InOrder inOrder = inOrder(preparedStatement);
        inOrder.verify(preparedStatement).setObject(1, 2);
        inOrder.verify(preparedStatement).setObject(2, 10);
        inOrder.verify(preparedStatement).setObject(3, "UPDATE");
        inOrder.verify(preparedStatement).setObject(4, 1);
        inOrder.verify(preparedStatement).setObject(5, 0);
        inOrder.verify(preparedStatement).executeUpdate();
    }
    
    private DataRecord getUpdatePrimaryKeyDataRecord() {
        DataRecord result = new DataRecord(IngestDataChangeType.UPDATE, TABLE_NAME, new PlaceholderPosition(), 3);
        result.addColumn(new Column("id", 1, 2, true, true));
        result.addColumn(new Column("user", 0, 10, true, false));
        result.addColumn(new Column("status", null, "UPDATE", true, false));
        return result;
    }
    
    private List<Record> mockRecords(final DataRecord dataRecord) {
        List<Record> result = new LinkedList<>();
        result.add(dataRecord);
        result.add(new FinishedRecord(new FinishedPosition()));
        return result;
    }
    
    private DataRecord getDataRecord(final String recordType) {
        Integer idOldValue = null;
        Integer userOldValue = null;
        Integer idValue = null;
        Integer userValue = null;
        String statusOldValue = null;
        String statusValue = null;
        if ("INSERT".equals(recordType)) {
            idValue = 1;
            userValue = 10;
            statusValue = recordType;
        }
        if ("UPDATE".equals(recordType)) {
            idOldValue = 1;
            idValue = idOldValue;
            userOldValue = 10;
            userValue = 20;
            statusValue = recordType;
        }
        if ("DELETE".equals(recordType)) {
            idOldValue = 1;
            userOldValue = 10;
            statusOldValue = recordType;
        }
        DataRecord result = new DataRecord(recordType, TABLE_NAME, new PlaceholderPosition(), 3);
        result.addColumn(new Column("id", idOldValue, idValue, false, true));
        result.addColumn(new Column("user", userOldValue, userValue, true, false));
        result.addColumn(new Column("status", statusOldValue, statusValue, true, false));
        return result;
    }
    
    private ImporterConfiguration mockImporterConfiguration() {
        Map<LogicTableName, Set<String>> shardingColumnsMap = Collections.singletonMap(new LogicTableName("test_table"), Collections.singleton("user"));
        return new ImporterConfiguration(dataSourceConfig, shardingColumnsMap, new TableNameSchemaNameMapping(Collections.emptyMap()), 1000, null, 3, 3);
    }
}
