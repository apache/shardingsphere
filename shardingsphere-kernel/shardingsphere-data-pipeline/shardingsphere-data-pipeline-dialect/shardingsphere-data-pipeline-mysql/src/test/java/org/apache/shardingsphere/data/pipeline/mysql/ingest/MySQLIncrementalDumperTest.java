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
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineColumnMetaData;
import org.apache.shardingsphere.data.pipeline.api.metadata.model.PipelineTableMetaData;
import org.apache.shardingsphere.data.pipeline.core.datasource.DefaultPipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.ingest.IngestDataChangeType;
import org.apache.shardingsphere.data.pipeline.core.ingest.channel.memory.MultiplexMemoryPipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.StandardPipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.util.ReflectionUtil;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.BinlogPosition;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.AbstractBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.DeleteRowsEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.PlaceholderEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.UpdateRowsEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.WriteRowsEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLIncrementalDumperTest {
    
    private final PipelineDataSourceManager dataSourceManager = new DefaultPipelineDataSourceManager();
    
    private MySQLIncrementalDumper incrementalDumper;
    
    private MultiplexMemoryPipelineChannel channel;
    
    @Mock
    private PipelineTableMetaData pipelineTableMetaData;
    
    @Before
    public void setUp() {
        DumperConfiguration dumperConfig = mockDumperConfiguration();
        initTableData(dumperConfig);
        dumperConfig.setDataSourceConfig(new StandardPipelineDataSourceConfiguration("jdbc:mysql://127.0.0.1:3306/ds_0", "root", "root"));
        channel = new MultiplexMemoryPipelineChannel();
        PipelineTableMetaDataLoader metaDataLoader = new StandardPipelineTableMetaDataLoader(dataSourceManager.getDataSource(dumperConfig.getDataSourceConfig()));
        incrementalDumper = new MySQLIncrementalDumper(dumperConfig, new BinlogPosition("binlog-000001", 4L), channel, metaDataLoader);
        PipelineColumnMetaData column = new PipelineColumnMetaData(1, "test", Types.INTEGER, "INTEGER", true, true, true);
        when(pipelineTableMetaData.getColumnMetaData(anyInt())).thenReturn(column);
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
            statement.execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id VARCHAR(12))");
            statement.execute("INSERT INTO t_order (order_id, user_id) VALUES (1, 'xxx'), (999, 'yyy')");
        }
    }
    
    @After
    public void tearDown() {
        dataSourceManager.close();
    }
    
    @Test
    public void assertWriteRowsEvent() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        WriteRowsEvent rowsEvent = new WriteRowsEvent();
        rowsEvent.setDatabaseName("");
        rowsEvent.setTableName("t_order");
        List<Serializable[]> rows = new ArrayList<>(1);
        rows.add(new String[]{"1", "order"});
        rowsEvent.setAfterRows(rows);
        ReflectionUtil.invokeMethod(incrementalDumper, "handleWriteRowsEvent", new Class[]{WriteRowsEvent.class, PipelineTableMetaData.class},
                new Object[]{rowsEvent, pipelineTableMetaData});
        List<Record> records = channel.fetchRecords(1, 0);
        assertThat(records.size(), is(1));
        assertThat(records.get(0), instanceOf(DataRecord.class));
        assertThat(((DataRecord) records.get(0)).getType(), is(IngestDataChangeType.INSERT));
    }
    
    @Test
    public void assertUpdateRowsEvent() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        UpdateRowsEvent rowsEvent = new UpdateRowsEvent();
        rowsEvent.setDatabaseName("");
        rowsEvent.setTableName("t_order");
        List<Serializable[]> beforeRows = new ArrayList<>(1);
        beforeRows.add(new String[]{"1", "order_old"});
        List<Serializable[]> afterRows = new ArrayList<>(1);
        afterRows.add(new String[]{"1", "order_new"});
        rowsEvent.setBeforeRows(beforeRows);
        rowsEvent.setAfterRows(afterRows);
        ReflectionUtil.invokeMethod(incrementalDumper, "handleUpdateRowsEvent", new Class[]{UpdateRowsEvent.class, PipelineTableMetaData.class},
                new Object[]{rowsEvent, pipelineTableMetaData});
        List<Record> records = channel.fetchRecords(1, 0);
        assertThat(records.size(), is(1));
        assertThat(records.get(0), instanceOf(DataRecord.class));
        assertThat(((DataRecord) records.get(0)).getType(), is(IngestDataChangeType.UPDATE));
    }
    
    @Test
    public void assertDeleteRowsEvent() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        DeleteRowsEvent rowsEvent = new DeleteRowsEvent();
        rowsEvent.setDatabaseName("");
        rowsEvent.setTableName("t_order");
        List<Serializable[]> rows = new ArrayList<>(1);
        rows.add(new String[]{"1", "order"});
        rowsEvent.setBeforeRows(rows);
        ReflectionUtil.invokeMethod(incrementalDumper, "handleDeleteRowsEvent", new Class[]{DeleteRowsEvent.class, PipelineTableMetaData.class}, new Object[]{rowsEvent, pipelineTableMetaData});
        List<Record> records = channel.fetchRecords(1, 0);
        assertThat(records.size(), is(1));
        assertThat(records.get(0), instanceOf(DataRecord.class));
        assertThat(((DataRecord) records.get(0)).getType(), is(IngestDataChangeType.DELETE));
    }
    
    @Test
    public void assertPlaceholderEvent() {
        invokeHandleEvent(new PlaceholderEvent());
        List<Record> records = channel.fetchRecords(1, 0);
        assertThat(records.size(), is(1));
        assertThat(records.get(0), instanceOf(PlaceholderRecord.class));
    }
    
    @Test
    public void assertRowsEventFiltered() {
        WriteRowsEvent rowsEvent = new WriteRowsEvent();
        rowsEvent.setDatabaseName("unknown_database");
        invokeHandleEvent(rowsEvent);
        List<Record> records = channel.fetchRecords(1, 0);
        assertThat(records.size(), is(1));
        assertThat(records.get(0), instanceOf(PlaceholderRecord.class));
    }
    
    @SneakyThrows({NoSuchMethodException.class, ReflectiveOperationException.class})
    private void invokeHandleEvent(final AbstractBinlogEvent event) {
        ReflectionUtil.invokeMethod(incrementalDumper, "handleEvent", new Class[]{AbstractBinlogEvent.class}, new Object[]{event});
    }
}
