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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.scaling.core.common.channel.Channel;
import org.apache.shardingsphere.scaling.core.common.exception.ScalingTaskExecuteException;
import org.apache.shardingsphere.scaling.core.common.record.Record;
import org.apache.shardingsphere.scaling.core.config.DumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.StandardJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.executor.AbstractScalingExecutor;
import org.apache.shardingsphere.scaling.core.executor.dumper.IncrementalDumper;
import org.apache.shardingsphere.scaling.core.job.position.ScalingPosition;
import org.apache.shardingsphere.scaling.core.util.ThreadUtil;
import org.apache.shardingsphere.scaling.postgresql.wal.LogicalReplication;
import org.apache.shardingsphere.scaling.postgresql.wal.WalEventConverter;
import org.apache.shardingsphere.scaling.postgresql.wal.WalPosition;
import org.apache.shardingsphere.scaling.postgresql.wal.decode.DecodingPlugin;
import org.apache.shardingsphere.scaling.postgresql.wal.decode.PostgreSQLTimestampUtils;
import org.apache.shardingsphere.scaling.postgresql.wal.decode.TestDecodingPlugin;
import org.apache.shardingsphere.scaling.postgresql.wal.event.AbstractWalEvent;
import org.apache.shardingsphere.scaling.postgresql.wal.event.PlaceholderEvent;
import org.apache.shardingsphere.scaling.postgresql.wal.decode.PostgreSQLLogSequenceNumber;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.replication.PGReplicationStream;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * PostgreSQL WAL dumper.
 */
@Slf4j
public final class PostgreSQLWalDumper extends AbstractScalingExecutor implements IncrementalDumper {
    
    private final WalPosition walPosition;
    
    private final DumperConfiguration dumperConfig;
    
    private final LogicalReplication logicalReplication = new LogicalReplication();
    
    private final WalEventConverter walEventConverter;
    
    @Setter
    private Channel channel;
    
    public PostgreSQLWalDumper(final DumperConfiguration dumperConfig, final ScalingPosition<WalPosition> position) {
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
        try (Connection pgConnection = logicalReplication.createPgConnection((StandardJDBCDataSourceConfiguration) dumperConfig.getDataSourceConfig());
             PGReplicationStream stream = logicalReplication.createReplicationStream(pgConnection, PostgreSQLPositionInitializer.SLOT_NAME, walPosition.getLogSequenceNumber())) {
            PostgreSQLTimestampUtils utils = new PostgreSQLTimestampUtils(pgConnection.unwrap(PgConnection.class).getTimestampUtils());
            DecodingPlugin decodingPlugin = new TestDecodingPlugin(utils);
            while (isRunning()) {
                ByteBuffer message = stream.readPending();
                if (null == message) {
                    ThreadUtil.sleep(10L);
                    continue;
                }
                AbstractWalEvent event = decodingPlugin.decode(message, new PostgreSQLLogSequenceNumber(stream.getLastReceiveLSN()));
                Record record = walEventConverter.convert(event);
                if (!(event instanceof PlaceholderEvent) && log.isDebugEnabled()) {
                    log.debug("dump, event={}, record={}", event, record);
                }
                pushRecord(record);
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

