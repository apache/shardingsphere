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

package org.apache.shardingsphere.scaling.postgresql.component;

import org.apache.shardingsphere.scaling.core.config.DumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.StandardJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.exception.ScalingTaskExecuteException;
import org.apache.shardingsphere.scaling.core.execute.executor.channel.MemoryChannel;
import org.apache.shardingsphere.scaling.core.utils.ReflectionUtil;
import org.apache.shardingsphere.scaling.postgresql.wal.LogicalReplication;
import org.apache.shardingsphere.scaling.postgresql.wal.WalPosition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.replication.LogSequenceNumber;
import org.postgresql.replication.PGReplicationStream;

import java.nio.ByteBuffer;
import java.sql.SQLException;

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
    
    private StandardJDBCDataSourceConfiguration jdbcDataSourceConfig;
    
    private MemoryChannel channel;
    
    @Before
    public void setUp() {
        ScalingContext.getInstance().init(new ServerConfiguration());
        position = new WalPosition(LogSequenceNumber.valueOf(100L));
        walDumper = new PostgreSQLWalDumper(mockDumperConfiguration(), position);
        channel = new MemoryChannel(records -> {
        });
        walDumper.setChannel(channel);
    }
    
    private DumperConfiguration mockDumperConfiguration() {
        jdbcDataSourceConfig = new StandardJDBCDataSourceConfiguration("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=PostgreSQL", "root", "root");
        DumperConfiguration result = new DumperConfiguration();
        result.setDataSourceConfig(jdbcDataSourceConfig);
        return result;
    }
    
    @Test
    public void assertStart() throws SQLException, NoSuchFieldException, IllegalAccessException {
        try {
            ReflectionUtil.setFieldValue(walDumper, "logicalReplication", logicalReplication);
            when(logicalReplication.createPgConnection(jdbcDataSourceConfig)).thenReturn(pgConnection);
            when(pgConnection.unwrap(PgConnection.class)).thenReturn(pgConnection);
            when(pgConnection.getTimestampUtils()).thenReturn(null);
            when(logicalReplication.createReplicationStream(pgConnection, PostgreSQLPositionManager.SLOT_NAME, position.getLogSequenceNumber())).thenReturn(pgReplicationStream);
            ByteBuffer data = ByteBuffer.wrap("table public.test: DELETE: data[integer]:1".getBytes());
            when(pgReplicationStream.readPending()).thenReturn(null).thenReturn(data).thenThrow(new SQLException(""));
            when(pgReplicationStream.getLastReceiveLSN()).thenReturn(LogSequenceNumber.valueOf(101L));
            walDumper.start();
        } catch (final ScalingTaskExecuteException ignored) {
        }
        assertThat(channel.fetchRecords(100, 0).size(), is(1));
    }
}
