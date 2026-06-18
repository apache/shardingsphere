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

package org.apache.shardingsphere.data.pipeline.core.importer.sink.type;

import org.apache.shardingsphere.data.pipeline.api.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.constant.PipelineSQLOperationType;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.importer.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.core.importer.SingleChannelConsumerImporter;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineSink;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.mapper.TableAndSchemaNameMapper;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.finished.IngestFinishedPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.placeholder.IngestPlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.NormalColumn;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Record;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;
import org.apache.shardingsphere.test.it.data.pipeline.core.fixture.algorithm.FixtureTransmissionJobItemContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PipelineDataSourceSinkTest {
    
    private static final String TABLE_NAME = "test_table";
    
    private PipelineDataSourceConfiguration dataSourceConfig;
    
    @Mock
    private PipelineChannel channel;
    
    @Mock
    private Connection connection;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    private SingleChannelConsumerImporter importer;
    
    @BeforeEach
    void setUp() throws SQLException {
        Map<String, Object> poolProps = new HashMap<>(3, 1F);
        poolProps.put("url", "jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL;USER=root;PASSWORD=root");
        poolProps.put("username", "root");
        poolProps.put("password", "root");
        dataSourceConfig = new StandardPipelineDataSourceConfiguration(poolProps);
        PipelineSink pipelineSink = new PipelineDataSourceSink(mockImporterConfiguration(), mockPipelineDataSourceManager());
        importer = new SingleChannelConsumerImporter(channel, 100, 1000L, pipelineSink, new FixtureTransmissionJobItemContext());
    }
    
    private ImporterConfiguration mockImporterConfiguration() {
        Map<ShardingSphereIdentifier, Collection<String>> tableAndRequiredColumnsMap = Collections.singletonMap(new ShardingSphereIdentifier("test_table"), Collections.singleton("user"));
        return new ImporterConfiguration(dataSourceConfig, tableAndRequiredColumnsMap, new TableAndSchemaNameMapper(Collections.emptyMap()), 1000, null, 3, 3);
    }
    
    private PipelineDataSourceManager mockPipelineDataSourceManager() throws SQLException {
        PipelineDataSourceManager result = mock(PipelineDataSourceManager.class, RETURNS_DEEP_STUBS);
        when(result.getDataSource(dataSourceConfig).getConnection()).thenReturn(connection);
        return result;
    }
    
    @Test
    void assertWriteInsertDataRecord() throws SQLException {
        DataRecord insertRecord = getDataRecord(PipelineSQLOperationType.INSERT);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(channel.fetch(anyInt(), anyLong())).thenReturn(mockRecords(insertRecord));
        importer.run();
        verify(preparedStatement).setObject(1, 1);
        verify(preparedStatement).setObject(2, 10);
        verify(preparedStatement).setObject(3, PipelineSQLOperationType.INSERT);
        verify(preparedStatement).addBatch();
    }
    
    @Test
    void assertDeleteDataRecord() throws SQLException {
        DataRecord deleteRecord = getDataRecord(PipelineSQLOperationType.DELETE);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(channel.fetch(anyInt(), anyLong())).thenReturn(mockRecords(deleteRecord));
        when(preparedStatement.executeBatch()).thenReturn(new int[]{1});
        importer.run();
        verify(preparedStatement).setObject(1, 1);
        verify(preparedStatement).setObject(2, 10);
        verify(preparedStatement).addBatch();
    }
    
    @Test
    void assertUpdateDataRecord() throws SQLException {
        DataRecord updateRecord = getDataRecord(PipelineSQLOperationType.UPDATE);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(channel.fetch(anyInt(), anyLong())).thenReturn(mockRecords(updateRecord));
        importer.run();
        verify(preparedStatement).setObject(1, 20);
        verify(preparedStatement).setObject(2, PipelineSQLOperationType.UPDATE);
        verify(preparedStatement).setObject(3, 1);
        verify(preparedStatement).setObject(4, 10);
        verify(preparedStatement).executeUpdate();
    }
    
    @Test
    void assertUpdatePrimaryKeyDataRecord() throws SQLException {
        DataRecord updateRecord = getUpdatePrimaryKeyDataRecord();
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(channel.fetch(anyInt(), anyLong())).thenReturn(mockRecords(updateRecord));
        importer.run();
        InOrder inOrder = inOrder(preparedStatement);
        inOrder.verify(preparedStatement).setObject(1, 2);
        inOrder.verify(preparedStatement).setObject(2, 10);
        inOrder.verify(preparedStatement).setObject(3, PipelineSQLOperationType.UPDATE);
        inOrder.verify(preparedStatement).setObject(4, 1);
        inOrder.verify(preparedStatement).setObject(5, 0);
        inOrder.verify(preparedStatement).executeUpdate();
    }
    
    private DataRecord getUpdatePrimaryKeyDataRecord() {
        DataRecord result = new DataRecord(PipelineSQLOperationType.UPDATE, TABLE_NAME, new IngestPlaceholderPosition(), 3);
        result.addColumn(new NormalColumn("id", 1, 2, true, true));
        result.addColumn(new NormalColumn("user", 0, 10, true, false));
        result.addColumn(new NormalColumn("status", null, PipelineSQLOperationType.UPDATE, true, false));
        return result;
    }
    
    private List<Record> mockRecords(final DataRecord dataRecord) {
        List<Record> result = new LinkedList<>();
        result.add(dataRecord);
        result.add(new FinishedRecord(new IngestFinishedPosition()));
        return result;
    }
    
    private DataRecord getDataRecord(final PipelineSQLOperationType type) {
        Integer idOldValue = null;
        Integer userOldValue = null;
        Integer idValue = null;
        Integer userValue = null;
        PipelineSQLOperationType statusOldValue = null;
        PipelineSQLOperationType statusValue = null;
        if (PipelineSQLOperationType.INSERT == type) {
            idValue = 1;
            userValue = 10;
            statusValue = type;
        }
        if (PipelineSQLOperationType.UPDATE == type) {
            idOldValue = 1;
            idValue = idOldValue;
            userOldValue = 10;
            userValue = 20;
            statusValue = type;
        }
        if (PipelineSQLOperationType.DELETE == type) {
            idOldValue = 1;
            userOldValue = 10;
            statusOldValue = type;
        }
        DataRecord result = new DataRecord(type, TABLE_NAME, new IngestPlaceholderPosition(), 3);
        result.addColumn(new NormalColumn("id", idOldValue, idValue, false, true));
        result.addColumn(new NormalColumn("user", userOldValue, userValue, true, false));
        result.addColumn(new NormalColumn("status", statusOldValue, statusValue, true, false));
        return result;
    }
}
