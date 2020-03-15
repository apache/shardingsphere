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

package org.apache.shardingsphere.shardingscaling.postgresql;

import lombok.Setter;

import org.apache.shardingsphere.shardingscaling.core.config.JDBCDataSourceConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.RdbmsConfiguration;
import org.apache.shardingsphere.shardingscaling.core.exception.SyncTaskExecuteException;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.AbstractSyncExecutor;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.channel.Channel;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.position.LogPosition;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.reader.LogReader;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.record.Record;
import org.apache.shardingsphere.shardingscaling.postgresql.wal.LogicalReplication;
import org.apache.shardingsphere.shardingscaling.postgresql.wal.WalEventConverter;
import org.apache.shardingsphere.shardingscaling.postgresql.wal.decode.DecodingPlugin;
import org.apache.shardingsphere.shardingscaling.postgresql.wal.decode.TestDecodingPlugin;
import org.apache.shardingsphere.shardingscaling.postgresql.wal.event.AbstractWalEvent;
import org.postgresql.PGConnection;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.replication.PGReplicationStream;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * PostgreSQL WAL reader.
 */
public final class PostgreSQLWalReader extends AbstractSyncExecutor implements LogReader {
    
    private final WalPosition walPosition;
    
    private final RdbmsConfiguration rdbmsConfiguration;
    
    private DecodingPlugin decodingPlugin;
    
    private final LogicalReplication logicalReplication = new LogicalReplication();
    
    private final WalEventConverter walEventConverter;
    
    @Setter
    private Channel channel;
    
    public PostgreSQLWalReader(final RdbmsConfiguration rdbmsConfiguration, final LogPosition logPosition) {
        walPosition = (WalPosition) logPosition;
        if (!JDBCDataSourceConfiguration.class.equals(rdbmsConfiguration.getDataSourceConfiguration().getClass())) {
            throw new UnsupportedOperationException("PostgreSQLWalReader only support JDBCDataSourceConfiguration");
        }
        this.rdbmsConfiguration = rdbmsConfiguration;
        walEventConverter = new WalEventConverter(rdbmsConfiguration);
    }
    
    @Override
    public void run() {
        start();
        read(channel);
    }
    
    @Override
    public void read(final Channel channel) {
        try {
            PGConnection pgConnection = logicalReplication.createPgConnection((JDBCDataSourceConfiguration) rdbmsConfiguration.getDataSourceConfiguration());
            decodingPlugin = new TestDecodingPlugin(((Connection) pgConnection).unwrap(PgConnection.class).getTimestampUtils());
            PGReplicationStream stream = logicalReplication.createReplicationStream(pgConnection,
                    PostgreSQLLogPositionManager.SLOT_NAME, walPosition.getLogSequenceNumber());
            while (isRunning()) {
                ByteBuffer msg = stream.readPending();
                if (msg == null) {
                    try {
                        Thread.sleep(10L);
                        continue;
                    } catch (InterruptedException ignored) {
                    
                    }
                }
                AbstractWalEvent event = decodingPlugin.decode(msg, stream.getLastReceiveLSN());
                pushRecord(channel, walEventConverter.convert(event));
            }
        } catch (SQLException ex) {
            throw new SyncTaskExecuteException(ex);
        }
    }
    
    private void pushRecord(final Channel channel, final Record record) {
        try {
            channel.pushRecord(record);
        } catch (InterruptedException ignored) {
        
        }
    }
}

