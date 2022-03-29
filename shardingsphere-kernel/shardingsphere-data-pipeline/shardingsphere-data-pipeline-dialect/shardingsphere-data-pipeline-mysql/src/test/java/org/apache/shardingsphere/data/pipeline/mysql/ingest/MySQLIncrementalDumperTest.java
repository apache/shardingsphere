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
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.PlaceholderRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.ingest.IngestDataChangeType;
import org.apache.shardingsphere.data.pipeline.core.ingest.channel.memory.MultiplexMemoryPipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
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

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class MySQLIncrementalDumperTest {
    
    private MySQLIncrementalDumper incrementalDumper;
    
    private MultiplexMemoryPipelineChannel channel;
    
    private final PipelineDataSourceManager dataSourceManager = new PipelineDataSourceManager();
    
    @Before
    public void setUp() {
        DumperConfiguration dumperConfig = mockDumperConfiguration();
        initTableData(dumperConfig);
        channel = new MultiplexMemoryPipelineChannel();
        PipelineTableMetaDataLoader metaDataLoader = new PipelineTableMetaDataLoader(dataSourceManager.getDataSource(dumperConfig.getDataSourceConfig()));
        incrementalDumper = new MySQLIncrementalDumper(dumperConfig, new BinlogPosition("binlog-000001", 4L), channel, metaDataLoader);
    }
    
    private DumperConfiguration mockDumperConfiguration() {
        DumperConfiguration result = new DumperConfiguration();
        result.setDataSourceConfig(new StandardPipelineDataSourceConfiguration("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL", "root", "root"));
        Map<String, String> tableNameMap = new HashMap<>(1, 1);
        tableNameMap.put("t_order", "t_order");
        result.setTableNameMap(tableNameMap);
        return result;
    }
    
    @SneakyThrows(SQLException.class)
    private void initTableData(final DumperConfiguration dumperConfig) {
        DataSource dataSource = new PipelineDataSourceManager().getDataSource(dumperConfig.getDataSourceConfig());
        try (Connection connection = dataSource.getConnection();
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
    public void assertWriteRowsEvent() {
        WriteRowsEvent rowsEvent = new WriteRowsEvent();
        rowsEvent.setSchemaName("");
        rowsEvent.setTableName("t_order");
        List<Serializable[]> rows = new ArrayList<>(1);
        rows.add(new String[]{"1", "order"});
        rowsEvent.setAfterRows(rows);
        invokeHandleEvent(rowsEvent);
        List<Record> records = channel.fetchRecords(1, 0);
        assertThat(records.size(), is(1));
        assertTrue(records.get(0) instanceof DataRecord);
        assertThat(((DataRecord) records.get(0)).getType(), is(IngestDataChangeType.INSERT));
    }
    
    @Test
    public void assertUpdateRowsEvent() {
        UpdateRowsEvent rowsEvent = new UpdateRowsEvent();
        rowsEvent.setSchemaName("");
        rowsEvent.setTableName("t_order");
        List<Serializable[]> beforeRows = new ArrayList<>(1);
        beforeRows.add(new String[]{"1", "order_old"});
        List<Serializable[]> afterRows = new ArrayList<>(1);
        afterRows.add(new String[]{"1", "order_new"});
        rowsEvent.setBeforeRows(beforeRows);
        rowsEvent.setAfterRows(afterRows);
        invokeHandleEvent(rowsEvent);
        List<Record> records = channel.fetchRecords(1, 0);
        assertThat(records.size(), is(1));
        assertTrue(records.get(0) instanceof DataRecord);
        assertThat(((DataRecord) records.get(0)).getType(), is(IngestDataChangeType.UPDATE));
    }
    
    @Test
    public void assertDeleteRowsEvent() {
        DeleteRowsEvent rowsEvent = new DeleteRowsEvent();
        rowsEvent.setSchemaName("");
        rowsEvent.setTableName("t_order");
        List<Serializable[]> rows = new ArrayList<>(1);
        rows.add(new String[]{"1", "order"});
        rowsEvent.setBeforeRows(rows);
        invokeHandleEvent(rowsEvent);
        List<Record> records = channel.fetchRecords(1, 0);
        assertThat(records.size(), is(1));
        assertTrue(records.get(0) instanceof DataRecord);
        assertThat(((DataRecord) records.get(0)).getType(), is(IngestDataChangeType.DELETE));
    }
    
    @Test
    public void assertPlaceholderEvent() {
        invokeHandleEvent(new PlaceholderEvent());
        List<Record> records = channel.fetchRecords(1, 0);
        assertThat(records.size(), is(1));
        assertTrue(records.get(0) instanceof PlaceholderRecord);
    }
    
    @Test
    public void assertRowsEventFiltered() {
        WriteRowsEvent rowsEvent = new WriteRowsEvent();
        rowsEvent.setSchemaName("unknown_schema");
        invokeHandleEvent(rowsEvent);
        List<Record> records = channel.fetchRecords(1, 0);
        assertThat(records.size(), is(1));
        assertTrue(records.get(0) instanceof PlaceholderRecord);
    }
    
    @SneakyThrows({NoSuchMethodException.class, ReflectiveOperationException.class})
    private void invokeHandleEvent(final AbstractBinlogEvent event) {
        ReflectionUtil.invokeMethod(incrementalDumper, "handleEvent", new Class[]{String.class, AbstractBinlogEvent.class}, new Object[]{"", event});
    }
}
