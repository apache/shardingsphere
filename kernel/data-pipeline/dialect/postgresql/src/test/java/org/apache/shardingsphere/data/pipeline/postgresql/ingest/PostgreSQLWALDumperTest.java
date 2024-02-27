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

import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.channel.memory.MemoryPipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.exception.IngestException;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.DumperCommonContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.mapper.ActualAndLogicTableNameMapper;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.mapper.TableAndSchemaNameMapper;
import org.apache.shardingsphere.infra.metadata.caseinsensitive.CaseInsensitiveIdentifier;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.StandardPipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.PostgreSQLLogicalReplication;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.WALPosition;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.PostgreSQLLogSequenceNumber;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(PostgreSQLIngestPositionManager.class)
class PostgreSQLWALDumperTest {
    
    @Mock
    private PostgreSQLLogicalReplication logicalReplication;
    
    @Mock
    private PgConnection pgConnection;
    
    @Mock
    private PGReplicationStream pgReplicationStream;
    
    private WALPosition position;
    
    private IncrementalDumperContext dumperContext;
    
    private PostgreSQLWALDumper walDumper;
    
    private MemoryPipelineChannel channel;
    
    private final PipelineDataSourceManager dataSourceManager = new PipelineDataSourceManager();
    
    @BeforeEach
    void setUp() {
        position = new WALPosition(new PostgreSQLLogSequenceNumber(LogSequenceNumber.valueOf(100L)));
        channel = new MemoryPipelineChannel(10000, records -> {
            
        });
        String jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=PostgreSQL";
        String username = "root";
        String password = "root";
        createTable(jdbcUrl, username, password);
        dumperContext = createDumperContext(jdbcUrl, username, password);
        walDumper = new PostgreSQLWALDumper(dumperContext, position, channel,
                new StandardPipelineTableMetaDataLoader(dataSourceManager.getDataSource(dumperContext.getCommonContext().getDataSourceConfig())));
    }
    
    private void createTable(final String jdbcUrl, final String username, final String password) {
        String sql = "CREATE TABLE t_order_0 (order_id INT NOT NULL, user_id INT NOT NULL, status VARCHAR(45) NULL, PRIMARY KEY (order_id))";
        try (
                Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
                Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (final SQLException ex) {
            throw new RuntimeException("Init table failed.", ex);
        }
    }
    
    private IncrementalDumperContext createDumperContext(final String jdbcUrl, final String username, final String password) {
        DumperCommonContext commonContext = new DumperCommonContext(null,
                new StandardPipelineDataSourceConfiguration(jdbcUrl, username, password),
                new ActualAndLogicTableNameMapper(Collections.singletonMap(new CaseInsensitiveIdentifier("t_order_0"), new CaseInsensitiveIdentifier("t_order"))),
                new TableAndSchemaNameMapper(Collections.emptyMap()));
        return new IncrementalDumperContext(commonContext, "0101123456", false);
    }
    
    @AfterEach
    void tearDown() {
        dataSourceManager.close();
    }
    
    @Test
    void assertStart() throws SQLException, ReflectiveOperationException {
        StandardPipelineDataSourceConfiguration dataSourceConfig = (StandardPipelineDataSourceConfiguration) dumperContext.getCommonContext().getDataSourceConfig();
        try {
            Plugins.getMemberAccessor().set(PostgreSQLWALDumper.class.getDeclaredField("logicalReplication"), walDumper, logicalReplication);
            when(logicalReplication.createConnection(dataSourceConfig)).thenReturn(pgConnection);
            when(pgConnection.unwrap(PgConnection.class)).thenReturn(pgConnection);
            when(PostgreSQLIngestPositionManager.getUniqueSlotName(eq(pgConnection), anyString())).thenReturn("0101123456");
            when(logicalReplication.createReplicationStream(pgConnection, PostgreSQLIngestPositionManager.getUniqueSlotName(pgConnection, ""), position.getLogSequenceNumber()))
                    .thenReturn(pgReplicationStream);
            ByteBuffer data = ByteBuffer.wrap("table public.t_order_0: DELETE: order_id[integer]:1".getBytes());
            when(pgReplicationStream.readPending()).thenReturn(null).thenReturn(data).thenThrow(new IngestException(""));
            when(pgReplicationStream.getLastReceiveLSN()).thenReturn(LogSequenceNumber.valueOf(101L));
            walDumper.start();
        } catch (final IngestException ignored) {
        }
        assertThat(channel.fetch(100, 0L).size(), is(1));
    }
}
