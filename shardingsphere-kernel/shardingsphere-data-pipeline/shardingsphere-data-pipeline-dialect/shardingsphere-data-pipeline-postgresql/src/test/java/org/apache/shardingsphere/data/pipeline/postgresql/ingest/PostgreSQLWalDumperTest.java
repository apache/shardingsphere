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

import org.apache.shardingsphere.data.pipeline.api.config.TableNameSchemaNameMapping;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.metadata.ActualTableName;
import org.apache.shardingsphere.data.pipeline.api.metadata.LogicTableName;
import org.apache.shardingsphere.data.pipeline.core.datasource.DefaultPipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.ingest.channel.memory.MultiplexMemoryPipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.ingest.exception.IngestException;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.util.ReflectionUtil;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.LogicalReplication;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.WalPosition;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.PostgreSQLLogSequenceNumber;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.PGReplicationStream;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
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
    
    private DumperConfiguration dumperConfig;
    
    private PostgreSQLWalDumper walDumper;
    
    private MultiplexMemoryPipelineChannel channel;
    
    private final PipelineDataSourceManager dataSourceManager = new DefaultPipelineDataSourceManager();
    
    @Before
    public void setUp() {
        position = new WalPosition(new PostgreSQLLogSequenceNumber(LogSequenceNumber.valueOf(100L)));
        channel = new MultiplexMemoryPipelineChannel();
        dumperConfig = mockDumperConfiguration();
        PipelineTableMetaDataLoader metaDataLoader = new PipelineTableMetaDataLoader(dataSourceManager.getDataSource(dumperConfig.getDataSourceConfig()));
        walDumper = new PostgreSQLWalDumper(dumperConfig, position, channel, metaDataLoader);
    }
    
    @After
    public void tearDown() {
        dataSourceManager.close();
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
        PipelineDataSourceConfiguration dataSourceConfig = new StandardPipelineDataSourceConfiguration(jdbcUrl, username, password);
        DumperConfiguration result = new DumperConfiguration();
        result.setJobId("0101123455F45SCALING8898");
        result.setDataSourceConfig(dataSourceConfig);
        result.setTableNameMap(Collections.singletonMap(new ActualTableName("t_order_0"), new LogicTableName("t_order")));
        result.setTableNameSchemaNameMapping(new TableNameSchemaNameMapping(Collections.emptyMap()));
        return result;
    }
    
    @Test
    public void assertStart() throws SQLException, NoSuchFieldException, IllegalAccessException {
        StandardPipelineDataSourceConfiguration dataSourceConfig = (StandardPipelineDataSourceConfiguration) dumperConfig.getDataSourceConfig();
        try {
            ReflectionUtil.setFieldValue(walDumper, "logicalReplication", logicalReplication);
            when(logicalReplication.createConnection(dataSourceConfig)).thenReturn(pgConnection);
            when(pgConnection.unwrap(PgConnection.class)).thenReturn(pgConnection);
            try (MockedStatic<PostgreSQLPositionInitializer> positionInitializer = mockStatic(PostgreSQLPositionInitializer.class)) {
                positionInitializer.when(() -> PostgreSQLPositionInitializer.getUniqueSlotName(eq(pgConnection), anyString())).thenReturn("0101123455F45SCALING8898");
                when(logicalReplication.createReplicationStream(pgConnection, PostgreSQLPositionInitializer.getUniqueSlotName(pgConnection, ""), position.getLogSequenceNumber()))
                        .thenReturn(pgReplicationStream);
                ByteBuffer data = ByteBuffer.wrap("table public.t_order_0: DELETE: order_id[integer]:1".getBytes());
                when(pgReplicationStream.readPending()).thenReturn(null).thenReturn(data).thenThrow(new SQLException(""));
                when(pgReplicationStream.getLastReceiveLSN()).thenReturn(LogSequenceNumber.valueOf(101L));
                // TODO NPE occurred here
                walDumper.start();
            }
        } catch (final IngestException ignored) {
        }
        assertThat(channel.fetchRecords(100, 0).size(), is(1));
    }
}
