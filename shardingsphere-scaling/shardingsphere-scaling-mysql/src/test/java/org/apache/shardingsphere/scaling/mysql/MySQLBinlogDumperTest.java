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

package org.apache.shardingsphere.scaling.mysql;

import lombok.SneakyThrows;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.shardingsphere.scaling.core.config.DumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.JDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.constant.ScalingConstant;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.execute.executor.channel.MemoryChannel;
import org.apache.shardingsphere.scaling.core.execute.executor.record.DataRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.PlaceholderRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.Record;
import org.apache.shardingsphere.scaling.core.metadata.JdbcUri;
import org.apache.shardingsphere.scaling.mysql.binlog.BinlogPosition;
import org.apache.shardingsphere.scaling.mysql.binlog.event.AbstractBinlogEvent;
import org.apache.shardingsphere.scaling.mysql.binlog.event.DeleteRowsEvent;
import org.apache.shardingsphere.scaling.mysql.binlog.event.PlaceholderEvent;
import org.apache.shardingsphere.scaling.mysql.binlog.event.UpdateRowsEvent;
import org.apache.shardingsphere.scaling.mysql.binlog.event.WriteRowsEvent;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class MySQLBinlogDumperTest {
    
    private static final String URL = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL";
    
    private MySQLBinlogDumper mySQLBinlogDumper;
    
    private MemoryChannel channel;
    
    @Before
    public void setUp() {
        ScalingContext.getInstance().init(new ServerConfiguration());
        DumperConfiguration dumperConfiguration = mockDumperConfiguration();
        initTableData(dumperConfiguration);
        channel = new MemoryChannel(records -> {
        });
        mySQLBinlogDumper = new MySQLBinlogDumper(dumperConfiguration, new BinlogPosition("binlog-000001", 4L));
        mySQLBinlogDumper.setChannel(channel);
    }
    
    private DumperConfiguration mockDumperConfiguration() {
        DumperConfiguration result = new DumperConfiguration();
        result.setDataSourceConfiguration(new JDBCDataSourceConfiguration(URL, "root", "root"));
        Map<String, String> tableNameMap = new HashedMap<>(1);
        tableNameMap.put("t_order", "t_order");
        result.setTableNameMap(tableNameMap);
        return result;
    }
    
    @SneakyThrows(SQLException.class)
    private void initTableData(final DumperConfiguration dumperConfig) {
        DataSource dataSource = new DataSourceManager().getDataSource(dumperConfig.getDataSourceConfiguration());
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (id INT PRIMARY KEY, user_id VARCHAR(12))");
            statement.execute("INSERT INTO t_order (id, user_id) VALUES (1, 'xxx'), (999, 'yyy')");
        }
    }
    
    @Test
    public void assertWriteRowsEvent() {
        WriteRowsEvent rowsEvent = new WriteRowsEvent();
        rowsEvent.setSchemaName("");
        rowsEvent.setTableName("t_order");
        List<Serializable[]> rows = new ArrayList<>(1);
        rows.add(new String[]{"1", "order"});
        rowsEvent.setAfterRows(rows);
        invokeHandleEvent(new JdbcUri(URL), rowsEvent);
        List<Record> records = channel.fetchRecords(1, 0);
        assertThat(records.size(), is(1));
        assertTrue(records.get(0) instanceof DataRecord);
        assertThat(((DataRecord) records.get(0)).getType(), is(ScalingConstant.INSERT));
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
        invokeHandleEvent(new JdbcUri(URL), rowsEvent);
        List<Record> records = channel.fetchRecords(1, 0);
        assertThat(records.size(), is(1));
        assertTrue(records.get(0) instanceof DataRecord);
        assertThat(((DataRecord) records.get(0)).getType(), is(ScalingConstant.UPDATE));
    }
    
    @Test
    public void assertDeleteRowsEvent() {
        DeleteRowsEvent rowsEvent = new DeleteRowsEvent();
        rowsEvent.setSchemaName("");
        rowsEvent.setTableName("t_order");
        List<Serializable[]> rows = new ArrayList<>(1);
        rows.add(new String[]{"1", "order"});
        rowsEvent.setBeforeRows(rows);
        invokeHandleEvent(new JdbcUri(URL), rowsEvent);
        List<Record> records = channel.fetchRecords(1, 0);
        assertThat(records.size(), is(1));
        assertTrue(records.get(0) instanceof DataRecord);
        assertThat(((DataRecord) records.get(0)).getType(), is(ScalingConstant.DELETE));
    }
    
    @Test
    public void assertPlaceholderEvent() {
        invokeHandleEvent(new JdbcUri("jdbc:mysql://127.0.0.1:3306/test_db"), new PlaceholderEvent());
        List<Record> records = channel.fetchRecords(1, 0);
        assertThat(records.size(), is(1));
        assertTrue(records.get(0) instanceof PlaceholderRecord);
    }
    
    @Test
    public void assertRowsEventFiltered() {
        WriteRowsEvent rowsEvent = new WriteRowsEvent();
        rowsEvent.setSchemaName("unknown_schema");
        invokeHandleEvent(new JdbcUri(URL), rowsEvent);
        List<Record> records = channel.fetchRecords(1, 0);
        assertThat(records.size(), is(1));
        assertTrue(records.get(0) instanceof PlaceholderRecord);
    }
    
    @SneakyThrows({NoSuchMethodException.class, ReflectiveOperationException.class})
    private void invokeHandleEvent(final JdbcUri uri, final AbstractBinlogEvent event) {
        Method handleEvent = MySQLBinlogDumper.class.getDeclaredMethod("handleEvent", JdbcUri.class, AbstractBinlogEvent.class);
        handleEvent.setAccessible(true);
        handleEvent.invoke(mySQLBinlogDumper, uri, event);
    }
}
