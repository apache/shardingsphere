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

import lombok.Setter;
import org.apache.shardingsphere.scaling.core.config.DumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.StandardJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.exception.ScalingTaskExecuteException;
import org.apache.shardingsphere.scaling.core.execute.executor.AbstractShardingScalingExecutor;
import org.apache.shardingsphere.scaling.core.execute.executor.channel.Channel;
import org.apache.shardingsphere.scaling.core.execute.executor.dumper.LogDumper;
import org.apache.shardingsphere.scaling.core.execute.executor.record.Record;
import org.apache.shardingsphere.scaling.core.job.position.Position;
import org.apache.shardingsphere.scaling.core.utils.ThreadUtil;
import org.apache.shardingsphere.scaling.postgresql.wal.LogicalReplication;
import org.apache.shardingsphere.scaling.postgresql.wal.WalEventConverter;
import org.apache.shardingsphere.scaling.postgresql.wal.WalPosition;
import org.apache.shardingsphere.scaling.postgresql.wal.decode.DecodingPlugin;
import org.apache.shardingsphere.scaling.postgresql.wal.decode.TestDecodingPlugin;
import org.apache.shardingsphere.scaling.postgresql.wal.event.AbstractWalEvent;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.replication.PGReplicationStream;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * PostgreSQL WAL dumper.
 */
public final class PostgreSQLWalDumper extends AbstractShardingScalingExecutor implements LogDumper {
    
    private final WalPosition walPosition;
    
    private final DumperConfiguration dumperConfig;
    
    private final LogicalReplication logicalReplication = new LogicalReplication();
    
    private final WalEventConverter walEventConverter;
    
    @Setter
    private Channel channel;
    
    public PostgreSQLWalDumper(final DumperConfiguration dumperConfig, final Position<WalPosition> position) {
        walPosition = (WalPosition) position;
        if (!StandardJDBCDataSourceConfiguration.class.equals(dumperConfig.getDataSourceConfig().getClass())) {
            throw new UnsupportedOperationException("PostgreSQLWalDumper only support JDBCDataSourceConfiguration");
        }
        this.dumperConfig = dumperConfig;
        walEventConverter = new WalEventConverter(dumperConfig);
    }
    
    @Override
    public void start() {
        super.start();
        dump();
    }
    
    private void dump() {
        try {
            Connection pgConnection = logicalReplication.createPgConnection((StandardJDBCDataSourceConfiguration) dumperConfig.getDataSourceConfig());
            DecodingPlugin decodingPlugin = new TestDecodingPlugin(pgConnection.unwrap(PgConnection.class).getTimestampUtils());
            PGReplicationStream stream = logicalReplication.createReplicationStream(pgConnection, PostgreSQLPositionManager.SLOT_NAME, walPosition.getLogSequenceNumber());
            while (isRunning()) {
                ByteBuffer message = stream.readPending();
                if (null == message) {
                    ThreadUtil.sleep(10L);
                    continue;
                }
                AbstractWalEvent event = decodingPlugin.decode(message, stream.getLastReceiveLSN());
                pushRecord(walEventConverter.convert(event));
            }
        } catch (final SQLException ex) {
            throw new ScalingTaskExecuteException(ex);
        }
    }
    
    private void pushRecord(final Record record) {
        try {
            channel.pushRecord(record);
        } catch (final InterruptedException ignored) {
        }
    }
}

