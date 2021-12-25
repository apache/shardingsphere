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

package org.apache.shardingsphere.data.pipeline.postgresql.ingest;

import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.core.ingest.channel.MemoryChannel;
import org.apache.shardingsphere.data.pipeline.core.ingest.exception.IngestException;
import org.apache.shardingsphere.data.pipeline.core.util.ReflectionUtil;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.LogicalReplication;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.WalPosition;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.PostgreSQLLogSequenceNumber;
import org.apache.shardingsphere.data.pipeline.core.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.PGReplicationStream;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLWalDumperTest {
    
    @Mock
    private LogicalReplication logicalReplication;
    
    @Mock
    private PgConnection pgConnection;
    
    @Mock
    private PGReplicationStream pgReplicationStream;
    
    private WalPosition position;
    
    private PostgreSQLWalDumper walDumper;
    
    private StandardPipelineDataSourceConfiguration pipelineDataSourceConfig;
    
    private MemoryChannel channel;
    
    @Before
    public void setUp() {
        position = new WalPosition(new PostgreSQLLogSequenceNumber(LogSequenceNumber.valueOf(100L)));
        walDumper = new PostgreSQLWalDumper(mockDumperConfiguration(), position);
        channel = new MemoryChannel(records -> {
        });
        walDumper.setChannel(channel);
    }
    
    private DumperConfiguration mockDumperConfiguration() {
        String jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=PostgreSQL";
        String username = "root";
        String password = "root";
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            try (Statement statement = connection.createStatement()) {
                String sql = "CREATE TABLE t_order_0 (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id))";
                statement.execute(sql);
            }
        } catch (final SQLException e) {
            throw new RuntimeException("Init table failed", e);
        }
        pipelineDataSourceConfig = new StandardPipelineDataSourceConfiguration(jdbcUrl, username, password);
        DumperConfiguration result = new DumperConfiguration();
        result.setDataSourceConfig(pipelineDataSourceConfig);
        Map<String, String> tableNameMap = new HashMap<>();
        tableNameMap.put("t_order_0", "t_order");
        result.setTableNameMap(tableNameMap);
        return result;
    }
    
    @Test
    public void assertStart() throws SQLException, NoSuchFieldException, IllegalAccessException {
        try {
            ReflectionUtil.setFieldValue(walDumper, "logicalReplication", logicalReplication);
            when(logicalReplication.createConnection(pipelineDataSourceConfig)).thenReturn(pgConnection);
            when(pgConnection.unwrap(PgConnection.class)).thenReturn(pgConnection);
            when(logicalReplication.createReplicationStream(pgConnection, PostgreSQLPositionInitializer.getUniqueSlotName(pgConnection), position.getLogSequenceNumber()))
                    .thenReturn(pgReplicationStream);
            ByteBuffer data = ByteBuffer.wrap("table public.t_order_0: DELETE: order_id[integer]:1".getBytes());
            when(pgReplicationStream.readPending()).thenReturn(null).thenReturn(data).thenThrow(new SQLException(""));
            when(pgReplicationStream.getLastReceiveLSN()).thenReturn(LogSequenceNumber.valueOf(101L));
            walDumper.start();
        } catch (final IngestException ignored) {
        }
        assertThat(channel.fetchRecords(100, 0).size(), is(1));
    }
}
