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

package org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal;

import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.DataRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.PlaceholderRecord;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.ingest.IngestDataChangeType;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.AbstractRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.DeleteRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.PlaceholderEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.UpdateRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.WriteRowEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class WalEventConverterTest {
    
    private WalEventConverter walEventConverter;
    
    private final PipelineDataSourceManager dataSourceManager = new PipelineDataSourceManager();
    
    @Before
    public void setUp() {
        DumperConfiguration dumperConfig = mockDumperConfiguration();
        walEventConverter = new WalEventConverter(dumperConfig, new PipelineTableMetaDataLoader(dataSourceManager.getDataSource(dumperConfig.getDataSourceConfig())));
        initTableData(dumperConfig);
    }
    
    @After
    public void tearDown() {
        dataSourceManager.close();
    }
    
    private DumperConfiguration mockDumperConfiguration() {
        DumperConfiguration result = new DumperConfiguration();
        result.setDataSourceConfig(new StandardPipelineDataSourceConfiguration("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=PostgreSQL", "root", "root"));
        result.setTableNameMap(Collections.singletonMap("t_order", "t_order"));
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
    
    @Test
    public void assertConvertWriteRowEvent() {
        Record record = walEventConverter.convert(mockWriteRowEvent());
        assertTrue(record instanceof DataRecord);
        assertThat(((DataRecord) record).getType(), is(IngestDataChangeType.INSERT));
    }
    
    @Test
    public void assertConvertUpdateRowEvent() {
        Record record = walEventConverter.convert(mockUpdateRowEvent());
        assertTrue(record instanceof DataRecord);
        assertThat(((DataRecord) record).getType(), is(IngestDataChangeType.UPDATE));
    }
    
    @Test
    public void assertConvertDeleteRowEvent() {
        Record record = walEventConverter.convert(mockDeleteRowEvent());
        assertTrue(record instanceof DataRecord);
        assertThat(((DataRecord) record).getType(), is(IngestDataChangeType.DELETE));
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
        result.setAfterRow(Arrays.asList("id", "user_id"));
        return result;
    }
    
    private AbstractRowEvent mockUpdateRowEvent() {
        UpdateRowEvent result = new UpdateRowEvent();
        result.setSchemaName("");
        result.setTableName("t_order");
        result.setAfterRow(Arrays.asList("id", "user_id"));
        return result;
    }
    
    private AbstractRowEvent mockDeleteRowEvent() {
        DeleteRowEvent result = new DeleteRowEvent();
        result.setSchemaName("");
        result.setTableName("t_order");
        result.setPrimaryKeys(Collections.singletonList("id"));
        return result;
    }
    
    private AbstractRowEvent mockUnknownTableEvent() {
        WriteRowEvent result = new WriteRowEvent();
        result.setSchemaName("");
        result.setTableName("t_other");
        return result;
    }
}
