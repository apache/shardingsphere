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

package org.apache.shardingsphere.data.pipeline.mysql.ingest;

import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.context.DumperCommonContext;
import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.context.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.context.mapper.ActualAndLogicTableNameMapper;
import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.context.mapper.TableAndSchemaNameMapper;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.api.metadata.ActualTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.common.datasource.DefaultPipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.common.ingest.IngestDataChangeType;
import org.apache.shardingsphere.data.pipeline.common.ingest.channel.EmptyAckCallback;
import org.apache.shardingsphere.data.pipeline.common.ingest.channel.memory.SimpleMemoryPipelineChannel;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.BinlogPosition;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.AbstractBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.DeleteRowsEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.PlaceholderEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.UpdateRowsEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.WriteRowsEvent;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDriver;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class MySQLIncrementalDumperTest {
    
    private MySQLIncrementalDumper incrementalDumper;
    
    private PipelineTableMetaData pipelineTableMetaData;
    
    @BeforeAll
    static void init() throws ClassNotFoundException {
        Class.forName(MockedDriver.class.getName());
    }
    
    @BeforeEach
    void setUp() throws SQLException {
        IncrementalDumperContext dumperContext = createDumperContext();
        initTableData(dumperContext);
        PipelineTableMetaDataLoader metaDataLoader = mock(PipelineTableMetaDataLoader.class);
        SimpleMemoryPipelineChannel channel = new SimpleMemoryPipelineChannel(10000, new EmptyAckCallback());
        incrementalDumper = new MySQLIncrementalDumper(dumperContext, new BinlogPosition("binlog-000001", 4L, 0L), channel, metaDataLoader);
        pipelineTableMetaData = new PipelineTableMetaData("t_order", mockOrderColumnsMetaDataMap(), Collections.emptyList());
        when(metaDataLoader.getTableMetaData(any(), any())).thenReturn(pipelineTableMetaData);
    }
    
    private IncrementalDumperContext createDumperContext() {
        DumperCommonContext commonContext = new DumperCommonContext();
        commonContext.setDataSourceConfig(new StandardPipelineDataSourceConfiguration("jdbc:mock://127.0.0.1:3306/test", "root", "root"));
        commonContext.setTableNameMapper(new ActualAndLogicTableNameMapper(Collections.singletonMap(new ActualTableName("t_order"), new LogicTableName("t_order"))));
        commonContext.setTableAndSchemaNameMapper(new TableAndSchemaNameMapper(Collections.emptyMap()));
        return new IncrementalDumperContext(commonContext);
    }
    
    private void initTableData(final IncrementalDumperContext dumperContext) throws SQLException {
        try (
                PipelineDataSourceManager dataSourceManager = new DefaultPipelineDataSourceManager();
                PipelineDataSourceWrapper dataSource = dataSourceManager.getDataSource(dumperContext.getCommonContext().getDataSourceConfig());
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id INT, status VARCHAR(12))");
            statement.execute("INSERT INTO t_order (order_id, user_id, status) VALUES (101, 1, 'OK'), (102, 1, 'OK')");
        }
    }
    
    private Map<String, PipelineColumnMetaData> mockOrderColumnsMetaDataMap() {
        return mockOrderColumnsMetaDataList().stream().collect(Collectors.toMap(PipelineColumnMetaData::getName, Function.identity()));
    }
    
    private List<PipelineColumnMetaData> mockOrderColumnsMetaDataList() {
        List<PipelineColumnMetaData> result = new LinkedList<>();
        result.add(new PipelineColumnMetaData(1, "order_id", Types.INTEGER, "INT", false, true, true));
        result.add(new PipelineColumnMetaData(1, "user_id", Types.INTEGER, "INT", false, false, false));
        result.add(new PipelineColumnMetaData(1, "status", Types.VARCHAR, "VARCHAR", false, false, false));
        return result;
    }
    
    @Test
    void assertWriteRowsEvent() throws ReflectiveOperationException {
        List<Record> actual = getRecordsByWriteRowsEvent(createWriteRowsEvent());
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), instanceOf(DataRecord.class));
        assertThat(((DataRecord) actual.get(0)).getType(), is(IngestDataChangeType.INSERT));
        assertThat(((DataRecord) actual.get(0)).getColumnCount(), is(3));
    }
    
    private WriteRowsEvent createWriteRowsEvent() {
        WriteRowsEvent result = new WriteRowsEvent();
        result.setDatabaseName("");
        result.setTableName("t_order");
        result.setAfterRows(Collections.singletonList(new Serializable[]{101, 1, "OK"}));
        return result;
    }
    
    private List<Record> getRecordsByWriteRowsEvent(final WriteRowsEvent rowsEvent) throws ReflectiveOperationException {
        Method method = MySQLIncrementalDumper.class.getDeclaredMethod("handleWriteRowsEvent", WriteRowsEvent.class, PipelineTableMetaData.class);
        return (List<Record>) Plugins.getMemberAccessor().invoke(method, incrementalDumper, rowsEvent, pipelineTableMetaData);
    }
    
    @Test
    void assertUpdateRowsEvent() throws ReflectiveOperationException {
        List<Record> actual = getRecordsByUpdateRowsEvent(createUpdateRowsEvent());
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), instanceOf(DataRecord.class));
        assertThat(((DataRecord) actual.get(0)).getType(), is(IngestDataChangeType.UPDATE));
        assertThat(((DataRecord) actual.get(0)).getColumnCount(), is(3));
    }
    
    private UpdateRowsEvent createUpdateRowsEvent() {
        UpdateRowsEvent result = new UpdateRowsEvent();
        result.setDatabaseName("test");
        result.setTableName("t_order");
        result.setBeforeRows(Collections.singletonList(new Serializable[]{101, 1, "OK"}));
        result.setAfterRows(Collections.singletonList(new Serializable[]{101, 1, "OK2"}));
        return result;
    }
    
    private List<Record> getRecordsByUpdateRowsEvent(final UpdateRowsEvent rowsEvent) throws ReflectiveOperationException {
        Method method = MySQLIncrementalDumper.class.getDeclaredMethod("handleUpdateRowsEvent", UpdateRowsEvent.class, PipelineTableMetaData.class);
        return (List<Record>) Plugins.getMemberAccessor().invoke(method, incrementalDumper, rowsEvent, pipelineTableMetaData);
    }
    
    @Test
    void assertDeleteRowsEvent() throws ReflectiveOperationException {
        List<Record> actual = getRecordsByDeleteRowsEvent(createDeleteRowsEvent());
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), instanceOf(DataRecord.class));
        assertThat(((DataRecord) actual.get(0)).getType(), is(IngestDataChangeType.DELETE));
        assertThat(((DataRecord) actual.get(0)).getColumnCount(), is(3));
    }
    
    private DeleteRowsEvent createDeleteRowsEvent() {
        DeleteRowsEvent result = new DeleteRowsEvent();
        result.setDatabaseName("");
        result.setTableName("t_order");
        result.setBeforeRows(Collections.singletonList(new Serializable[]{101, 1, "OK"}));
        return result;
    }
    
    private List<Record> getRecordsByDeleteRowsEvent(final DeleteRowsEvent rowsEvent) throws ReflectiveOperationException {
        Method method = MySQLIncrementalDumper.class.getDeclaredMethod("handleDeleteRowsEvent", DeleteRowsEvent.class, PipelineTableMetaData.class);
        return (List<Record>) Plugins.getMemberAccessor().invoke(method, incrementalDumper, rowsEvent, pipelineTableMetaData);
    }
    
    @Test
    void assertPlaceholderEvent() throws ReflectiveOperationException {
        List<Record> actual = (List<Record>) Plugins.getMemberAccessor().invoke(MySQLIncrementalDumper.class.getDeclaredMethod("handleEvent", AbstractBinlogEvent.class),
                incrementalDumper, new PlaceholderEvent());
        assertThat(actual.size(), is(1));
    }
    
    @Test
    void assertRowsEventFiltered() throws ReflectiveOperationException {
        List<Record> actual = (List<Record>) Plugins.getMemberAccessor().invoke(MySQLIncrementalDumper.class.getDeclaredMethod("handleEvent", AbstractBinlogEvent.class),
                incrementalDumper, getFilteredWriteRowsEvent());
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), instanceOf(DataRecord.class));
    }
    
    private WriteRowsEvent getFilteredWriteRowsEvent() {
        WriteRowsEvent result = new WriteRowsEvent();
        result.setDatabaseName("test");
        result.setTableName("t_order");
        result.setAfterRows(Collections.singletonList(new Serializable[]{1}));
        return result;
    }
}
