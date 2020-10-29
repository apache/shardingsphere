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

import lombok.SneakyThrows;
import org.apache.shardingsphere.scaling.core.config.JDBCScalingDataSourceConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.postgresql.PGConnection;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.PGReplicationConnection;
import org.postgresql.replication.fluent.ChainedStreamBuilder;
import org.postgresql.replication.fluent.logical.ChainedLogicalStreamBuilder;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class LogicalReplicationTest {
    
    @Mock
    private PgConnection pgConnection;
    
    @Mock
    private PGReplicationConnection pgReplicationConnection;
    
    @Mock
    private ChainedStreamBuilder chainedStreamBuilder;
    
    @Mock
    private ChainedLogicalStreamBuilder chainedLogicalStreamBuilder;
    
    private LogicalReplication logicalReplication;
    
    @Before
    public void setUp() {
        logicalReplication = new LogicalReplication();
    }
    
    @Test
    public void assertCreatePgConnectionSuccess() throws SQLException {
        Connection pgConnection = logicalReplication.createPgConnection(
                new JDBCScalingDataSourceConfiguration("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=PostgreSQL", "root", "root"));
        assertFalse(pgConnection.isClosed());
    }
    
    @Test
    public void assertCreateReplicationStreamSuccess() throws SQLException {
        LogSequenceNumber startPosition = LogSequenceNumber.valueOf(100L);
        when(pgConnection.unwrap(PGConnection.class)).thenReturn(pgConnection);
        when(pgConnection.getReplicationAPI()).thenReturn(pgReplicationConnection);
        when(pgReplicationConnection.replicationStream()).thenReturn(chainedStreamBuilder);
        when(chainedStreamBuilder.logical()).thenReturn(chainedLogicalStreamBuilder);
        when(chainedLogicalStreamBuilder.withStartPosition(startPosition)).thenReturn(chainedLogicalStreamBuilder);
        when(chainedLogicalStreamBuilder.withSlotName("")).thenReturn(chainedLogicalStreamBuilder);
        when(chainedLogicalStreamBuilder.withSlotOption(anyString(), eq(true))).thenReturn(chainedLogicalStreamBuilder, chainedLogicalStreamBuilder);
        logicalReplication.createReplicationStream(pgConnection, "", startPosition);
        verify(chainedLogicalStreamBuilder).start();
    }
    
    @Test(expected = SQLException.class)
    @SneakyThrows(SQLException.class)
    public void assertCreateReplicationStreamFailure() {
        when(pgConnection.unwrap(PGConnection.class)).thenThrow(new SQLException(""));
        logicalReplication.createReplicationStream(pgConnection, "", LogSequenceNumber.valueOf(100L));
    }
}
