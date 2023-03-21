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

import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.api.config.TableNameSchemaNameMapping;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.PlaceholderRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.api.metadata.ActualTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.ColumnName;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.core.datasource.DefaultPipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.ingest.IngestDataChangeType;
import org.apache.shardingsphere.data.pipeline.core.ingest.channel.EmptyAckCallback;
import org.apache.shardingsphere.data.pipeline.core.ingest.channel.memory.MultiplexMemoryPipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.StandardPipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.BinlogPosition;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.AbstractBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.DeleteRowsEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.PlaceholderEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.UpdateRowsEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.WriteRowsEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
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
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public final class MySQLIncrementalDumperTest {
    
    private final PipelineDataSourceManager dataSourceManager = new DefaultPipelineDataSourceManager();
    
    private DumperConfiguration dumperConfig;
    
    private MySQLIncrementalDumper incrementalDumper;
    
    private MultiplexMemoryPipelineChannel channel;
    
    private PipelineTableMetaData pipelineTableMetaData;
    
    @BeforeEach
    public void setUp() {
        dumperConfig = mockDumperConfiguration();
        initTableData(dumperConfig);
        dumperConfig.setDataSourceConfig(new StandardPipelineDataSourceConfiguration("jdbc:mysql://127.0.0.1:3306/ds_0", "root", "root"));
        channel = new MultiplexMemoryPipelineChannel(1, 10000, new EmptyAckCallback());
        PipelineTableMetaDataLoader metaDataLoader = new StandardPipelineTableMetaDataLoader(dataSourceManager.getDataSource(dumperConfig.getDataSourceConfig()));
        incrementalDumper = new MySQLIncrementalDumper(dumperConfig, new BinlogPosition("binlog-000001", 4L), channel, metaDataLoader);
        pipelineTableMetaData = new PipelineTableMetaData("t_order", mockOrderColumnsMetaDataMap(), Collections.emptyList());
    }
    
    private DumperConfiguration mockDumperConfiguration() {
        DumperConfiguration result = new DumperConfiguration();
        result.setDataSourceConfig(new StandardPipelineDataSourceConfiguration("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL", "root", "root"));
        result.setTableNameMap(Collections.singletonMap(new ActualTableName("t_order"), new LogicTableName("t_order")));
        result.setTableNameSchemaNameMapping(new TableNameSchemaNameMapping(Collections.emptyMap()));
        return result;
    }
    
    @SneakyThrows(SQLException.class)
    private void initTableData(final DumperConfiguration dumperConfig) {
        DataSource dataSource = new DefaultPipelineDataSourceManager().getDataSource(dumperConfig.getDataSourceConfig());
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id INT, status VARCHAR(12))");
            statement.execute("INSERT INTO t_order (order_id, user_id, status) VALUES (101, 1, 'OK'), (102, 1, 'OK')");
        }
    }
    
    private static Map<String, PipelineColumnMetaData> mockOrderColumnsMetaDataMap() {
        return mockOrderColumnsMetaDataList().stream().collect(Collectors.toMap(PipelineColumnMetaData::getName, Function.identity()));
    }
    
    private static List<PipelineColumnMetaData> mockOrderColumnsMetaDataList() {
        List<PipelineColumnMetaData> result = new LinkedList<>();
        result.add(new PipelineColumnMetaData(1, "order_id", Types.INTEGER, "INT", false, true, true));
        result.add(new PipelineColumnMetaData(1, "user_id", Types.INTEGER, "INT", false, false, false));
        result.add(new PipelineColumnMetaData(1, "status", Types.VARCHAR, "VARCHAR", false, false, false));
        return result;
    }
    
    @AfterEach
    public void tearDown() {
        dataSourceManager.close();
    }
    
    @Test
    public void assertIsColumnUnneeded() {
        assertFalse(incrementalDumper.isColumnUnneeded(null, "order_id"));
        assertFalse(incrementalDumper.isColumnUnneeded(Stream.of("order_id", "user_id").map(ColumnName::new).collect(Collectors.toSet()), "order_id"));
        assertTrue(incrementalDumper.isColumnUnneeded(Stream.of("order_id", "user_id").map(ColumnName::new).collect(Collectors.toSet()), "status"));
    }
    
    @Test
    public void assertWriteRowsEventWithoutCustomColumns() throws ReflectiveOperationException {
        assertWriteRowsEvent0(null, 3);
    }
    
    @Test
    public void assertWriteRowsEventWithCustomColumns() throws ReflectiveOperationException {
        assertWriteRowsEvent0(mockTargetTableColumnsMap(), 1);
    }
    
    private void assertWriteRowsEvent0(final Map<LogicTableName, Set<ColumnName>> targetTableColumnsMap, final int expectedColumnCount) throws ReflectiveOperationException {
        dumperConfig.setTargetTableColumnsMap(targetTableColumnsMap);
        WriteRowsEvent rowsEvent = new WriteRowsEvent();
        rowsEvent.setDatabaseName("");
        rowsEvent.setTableName("t_order");
        rowsEvent.setAfterRows(Collections.singletonList(new Serializable[]{101, 1, "OK"}));
        Method method = MySQLIncrementalDumper.class.getDeclaredMethod("handleWriteRowsEvent", WriteRowsEvent.class, PipelineTableMetaData.class);
        Plugins.getMemberAccessor().invoke(method, incrementalDumper, rowsEvent, pipelineTableMetaData);
        List<Record> actual = channel.fetchRecords(1, 0);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), instanceOf(DataRecord.class));
        assertThat(((DataRecord) actual.get(0)).getType(), is(IngestDataChangeType.INSERT));
        assertThat(((DataRecord) actual.get(0)).getColumnCount(), is(expectedColumnCount));
    }
    
    private Map<LogicTableName, Set<ColumnName>> mockTargetTableColumnsMap() {
        return Collections.singletonMap(new LogicTableName("t_order"), Collections.singleton(new ColumnName("order_id")));
    }
    
    @Test
    public void assertUpdateRowsEventWithoutCustomColumns() throws ReflectiveOperationException {
        assertUpdateRowsEvent0(null, 3);
    }
    
    @Test
    public void assertUpdateRowsEventWithCustomColumns() throws ReflectiveOperationException {
        assertUpdateRowsEvent0(mockTargetTableColumnsMap(), 1);
    }
    
    private void assertUpdateRowsEvent0(final Map<LogicTableName, Set<ColumnName>> targetTableColumnsMap, final int expectedColumnCount) throws ReflectiveOperationException {
        dumperConfig.setTargetTableColumnsMap(targetTableColumnsMap);
        UpdateRowsEvent rowsEvent = new UpdateRowsEvent();
        rowsEvent.setDatabaseName("");
        rowsEvent.setTableName("t_order");
        rowsEvent.setBeforeRows(Collections.singletonList(new Serializable[]{101, 1, "OK"}));
        rowsEvent.setAfterRows(Collections.singletonList(new Serializable[]{101, 1, "OK2"}));
        Method method = MySQLIncrementalDumper.class.getDeclaredMethod("handleUpdateRowsEvent", UpdateRowsEvent.class, PipelineTableMetaData.class);
        Plugins.getMemberAccessor().invoke(method, incrementalDumper, rowsEvent, pipelineTableMetaData);
        List<Record> actual = channel.fetchRecords(1, 0);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), instanceOf(DataRecord.class));
        assertThat(((DataRecord) actual.get(0)).getType(), is(IngestDataChangeType.UPDATE));
        assertThat(((DataRecord) actual.get(0)).getColumnCount(), is(expectedColumnCount));
    }
    
    @Test
    public void assertDeleteRowsEventWithoutCustomColumns() throws ReflectiveOperationException {
        assertDeleteRowsEvent0(null, 3);
    }
    
    @Test
    public void assertDeleteRowsEventWithCustomColumns() throws ReflectiveOperationException {
        assertDeleteRowsEvent0(mockTargetTableColumnsMap(), 1);
    }
    
    private void assertDeleteRowsEvent0(final Map<LogicTableName, Set<ColumnName>> targetTableColumnsMap, final int expectedColumnCount) throws ReflectiveOperationException {
        dumperConfig.setTargetTableColumnsMap(targetTableColumnsMap);
        DeleteRowsEvent rowsEvent = new DeleteRowsEvent();
        rowsEvent.setDatabaseName("");
        rowsEvent.setTableName("t_order");
        rowsEvent.setBeforeRows(Collections.singletonList(new Serializable[]{101, 1, "OK"}));
        Method method = MySQLIncrementalDumper.class.getDeclaredMethod("handleDeleteRowsEvent", DeleteRowsEvent.class, PipelineTableMetaData.class);
        Plugins.getMemberAccessor().invoke(method, incrementalDumper, rowsEvent, pipelineTableMetaData);
        List<Record> actual = channel.fetchRecords(1, 0);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), instanceOf(DataRecord.class));
        assertThat(((DataRecord) actual.get(0)).getType(), is(IngestDataChangeType.DELETE));
        assertThat(((DataRecord) actual.get(0)).getColumnCount(), is(expectedColumnCount));
    }
    
    @Test
    public void assertPlaceholderEvent() throws ReflectiveOperationException {
        Plugins.getMemberAccessor().invoke(MySQLIncrementalDumper.class.getDeclaredMethod("handleEvent", AbstractBinlogEvent.class), incrementalDumper, new PlaceholderEvent());
        List<Record> actual = channel.fetchRecords(1, 0);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), instanceOf(PlaceholderRecord.class));
    }
    
    @Test
    public void assertRowsEventFiltered() throws ReflectiveOperationException {
        WriteRowsEvent rowsEvent = new WriteRowsEvent();
        rowsEvent.setDatabaseName("unknown_database");
        Plugins.getMemberAccessor().invoke(MySQLIncrementalDumper.class.getDeclaredMethod("handleEvent", AbstractBinlogEvent.class), incrementalDumper, rowsEvent);
        List<Record> actual = channel.fetchRecords(1, 0);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), instanceOf(PlaceholderRecord.class));
    }
}
