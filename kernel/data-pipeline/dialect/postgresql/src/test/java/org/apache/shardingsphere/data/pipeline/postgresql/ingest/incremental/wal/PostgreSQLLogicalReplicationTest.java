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

package org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal;

import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.decode.BaseLogSequenceNumber;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.decode.PostgreSQLLogSequenceNumber;
import org.apache.shardingsphere.infra.util.close.QuietlyCloser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.postgresql.PGConnection;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.PGReplicationConnection;
import org.postgresql.replication.PGReplicationStream;
import org.postgresql.replication.fluent.ChainedStreamBuilder;
import org.postgresql.replication.fluent.logical.ChainedLogicalStreamBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgreSQLLogicalReplicationTest {
    
    @Mock
    private PgConnection connection;
    
    @Mock
    private PGReplicationConnection pgReplicationConnection;
    
    @Mock
    private ChainedStreamBuilder chainedStreamBuilder;
    
    @Mock
    private ChainedLogicalStreamBuilder chainedLogicalStreamBuilder;
    
    private PostgreSQLLogicalReplication logicalReplication;
    
    @BeforeEach
    void setUp() {
        logicalReplication = new PostgreSQLLogicalReplication();
    }
    
    @Test
    void assertCreatePgConnectionSuccess() throws SQLException {
        Map<String, Object> poolProps = new HashMap<>(3, 1F);
        poolProps.put("url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=PostgreSQL");
        poolProps.put("username", "root");
        poolProps.put("password", "root");
        Connection connection = logicalReplication.createConnection(new StandardPipelineDataSourceConfiguration(poolProps));
        assertFalse(connection.isClosed());
    }
    
    @Test
    void assertCreateReplicationStreamSuccess() throws SQLException {
        LogSequenceNumber startPosition = LogSequenceNumber.valueOf(100L);
        when(connection.unwrap(PGConnection.class)).thenReturn(connection);
        when(connection.getReplicationAPI()).thenReturn(pgReplicationConnection);
        when(pgReplicationConnection.replicationStream()).thenReturn(chainedStreamBuilder);
        when(chainedStreamBuilder.logical()).thenReturn(chainedLogicalStreamBuilder);
        when(chainedLogicalStreamBuilder.withStartPosition(startPosition)).thenReturn(chainedLogicalStreamBuilder);
        when(chainedLogicalStreamBuilder.withSlotName("")).thenReturn(chainedLogicalStreamBuilder);
        when(chainedLogicalStreamBuilder.withSlotOption(anyString(), eq(true))).thenReturn(chainedLogicalStreamBuilder, chainedLogicalStreamBuilder);
        BaseLogSequenceNumber basePosition = new PostgreSQLLogSequenceNumber(startPosition);
        logicalReplication.createReplicationStream(connection, "", basePosition);
        PGReplicationStream stream = null;
        try {
            stream = verify(chainedLogicalStreamBuilder).start();
        } finally {
            QuietlyCloser.close(stream);
        }
    }
    
    @Test
    void assertCreateReplicationStreamFailure() throws SQLException {
        when(connection.unwrap(PGConnection.class)).thenThrow(new SQLException(""));
        assertThrows(SQLException.class, () -> logicalReplication.createReplicationStream(connection, "", new PostgreSQLLogSequenceNumber(LogSequenceNumber.valueOf(100L))));
    }
}
