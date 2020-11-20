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

package org.apache.shardingsphere.scaling.postgresql.wal;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.apache.shardingsphere.scaling.core.config.DumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.rule.StandardJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.constant.ScalingConstant;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.execute.executor.record.DataRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.PlaceholderRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.Record;
import org.apache.shardingsphere.scaling.postgresql.wal.event.AbstractRowEvent;
import org.apache.shardingsphere.scaling.postgresql.wal.event.DeleteRowEvent;
import org.apache.shardingsphere.scaling.postgresql.wal.event.PlaceholderEvent;
import org.apache.shardingsphere.scaling.postgresql.wal.event.UpdateRowEvent;
import org.apache.shardingsphere.scaling.postgresql.wal.event.WriteRowEvent;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class WalEventConverterTest {
    
    private WalEventConverter walEventConverter;
    
    @Before
    public void setUp() {
        DumperConfiguration dumperConfig = mockDumperConfiguration();
        walEventConverter = new WalEventConverter(dumperConfig);
        initTableData(dumperConfig);
    }
    
    private DumperConfiguration mockDumperConfiguration() {
        DumperConfiguration result = new DumperConfiguration();
        result.setDataSourceConfiguration(new StandardJDBCDataSourceConfiguration("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=PostgreSQL", "root", "root"));
        Map<String, String> tableNameMap = Maps.newHashMap();
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
    public void assertConvertWriteRowEvent() {
        Record record = walEventConverter.convert(mockWriteRowEvent());
        assertTrue(record instanceof DataRecord);
        assertThat(((DataRecord) record).getType(), is(ScalingConstant.INSERT));
    }
    
    @Test
    public void assertConvertUpdateRowEvent() {
        Record record = walEventConverter.convert(mockUpdateRowEvent());
        assertTrue(record instanceof DataRecord);
        assertThat(((DataRecord) record).getType(), is(ScalingConstant.UPDATE));
    }
    
    @Test
    public void assertConvertDeleteRowEvent() {
        Record record = walEventConverter.convert(mockDeleteRowEvent());
        assertTrue(record instanceof DataRecord);
        assertThat(((DataRecord) record).getType(), is(ScalingConstant.DELETE));
    }
    
    @Test
    public void assertConvertPlaceholderEvent() {
        Record record = walEventConverter.convert(new PlaceholderEvent());
        assertTrue(record instanceof PlaceholderRecord);
    }
    
    @Test
    public void assertUnknownTable() {
        Record record = walEventConverter.convert(mockUnknownTableEvent());
        assertTrue(record instanceof PlaceholderRecord);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertConvertFailure() {
        walEventConverter.convert(new AbstractRowEvent() {
        });
    }
    
    private AbstractRowEvent mockWriteRowEvent() {
        WriteRowEvent result = new WriteRowEvent();
        result.setSchemaName("");
        result.setTableName("t_order");
        result.setAfterRow(Lists.newArrayList("id", "user_id"));
        return result;
    }
    
    private AbstractRowEvent mockUpdateRowEvent() {
        UpdateRowEvent result = new UpdateRowEvent();
        result.setSchemaName("");
        result.setTableName("t_order");
        result.setAfterRow(Lists.newArrayList("id", "user_id"));
        return result;
    }
    
    private AbstractRowEvent mockDeleteRowEvent() {
        DeleteRowEvent result = new DeleteRowEvent();
        result.setSchemaName("");
        result.setTableName("t_order");
        result.setPrimaryKeys(Lists.newArrayList("id"));
        return result;
    }
    
    private AbstractRowEvent mockUnknownTableEvent() {
        WriteRowEvent result = new WriteRowEvent();
        result.setSchemaName("");
        result.setTableName("t_other");
        return result;
    }
}
