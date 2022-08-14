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

import org.apache.shardingsphere.data.pipeline.api.config.ImporterConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.TableNameSchemaNameMapping;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.PlaceholderPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Column;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.FinishedRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.core.fixture.FixturePipelineJobProgressListener;
import org.apache.shardingsphere.data.pipeline.core.record.RecordUtil;
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
public final class DefaultImporterTest {
    
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
    
    private DefaultImporter jdbcImporter;
    
    @Before
    public void setUp() throws SQLException {
        jdbcImporter = new DefaultImporter(mockImporterConfiguration(), dataSourceManager, channel, new FixturePipelineJobProgressListener());
        when(dataSourceManager.getDataSource(dataSourceConfig)).thenReturn(dataSource);
        when(dataSource.getConnection()).thenReturn(connection);
    }
    
    @Test
    public void assertWriteInsertDataRecord() throws SQLException {
        DataRecord insertRecord = getDataRecord("INSERT");
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
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
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(channel.fetchRecords(anyInt(), anyInt())).thenReturn(mockRecords(deleteRecord));
        jdbcImporter.run();
        verify(preparedStatement).setObject(1, 1);
        verify(preparedStatement).setObject(2, 10);
        verify(preparedStatement).addBatch();
    }
    
    @Test
    public void assertUpdateDataRecord() throws SQLException {
        DataRecord updateRecord = getDataRecord("UPDATE");
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(channel.fetchRecords(anyInt(), anyInt())).thenReturn(mockRecords(updateRecord));
        jdbcImporter.run();
        verify(preparedStatement).setObject(1, 10);
        verify(preparedStatement).setObject(2, "UPDATE");
        verify(preparedStatement).setObject(3, 1);
        verify(preparedStatement).setObject(4, 10);
        verify(preparedStatement).executeUpdate();
    }
    
    @Test
    public void assertUpdatePrimaryKeyDataRecord() throws SQLException {
        DataRecord updateRecord = getUpdatePrimaryKeyDataRecord();
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(channel.fetchRecords(anyInt(), anyInt())).thenReturn(mockRecords(updateRecord));
        jdbcImporter.run();
        InOrder inOrder = inOrder(preparedStatement);
        inOrder.verify(preparedStatement).setObject(1, 2);
        inOrder.verify(preparedStatement).setObject(2, 10);
        inOrder.verify(preparedStatement).setObject(3, "UPDATE");
        inOrder.verify(preparedStatement).setObject(4, 1);
        inOrder.verify(preparedStatement).setObject(5, 10);
        inOrder.verify(preparedStatement).executeUpdate();
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
        Map<LogicTableName, Set<String>> shardingColumnsMap = Collections.singletonMap(new LogicTableName("test_table"), Collections.singleton("user"));
        return new ImporterConfiguration(dataSourceConfig, shardingColumnsMap, new TableNameSchemaNameMapping(Collections.emptyMap()), 1000, 3, 3);
    }
}
