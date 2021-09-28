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

package org.apache.shardingsphere.scaling.opengauss.component;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.scaling.core.common.channel.Channel;
import org.apache.shardingsphere.scaling.core.common.constant.ScalingConstant;
import org.apache.shardingsphere.scaling.core.common.exception.ScalingTaskExecuteException;
import org.apache.shardingsphere.scaling.core.common.record.Column;
import org.apache.shardingsphere.scaling.core.common.record.DataRecord;
import org.apache.shardingsphere.scaling.core.common.record.Record;
import org.apache.shardingsphere.scaling.core.config.DumperConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.StandardJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.executor.AbstractScalingExecutor;
import org.apache.shardingsphere.scaling.core.executor.dumper.IncrementalDumper;
import org.apache.shardingsphere.scaling.core.job.position.ScalingPosition;
import org.apache.shardingsphere.scaling.core.util.ThreadUtil;
import org.apache.shardingsphere.scaling.opengauss.wal.OpenGaussLogicalReplication;
import org.apache.shardingsphere.scaling.opengauss.wal.decode.MppdbDecodingPlugin;
import org.apache.shardingsphere.scaling.opengauss.wal.decode.OpenGaussTimestampUtils;
import org.apache.shardingsphere.scaling.opengauss.wal.decode.OpenGaussLogSequenceNumber;
import org.apache.shardingsphere.scaling.postgresql.wal.WalEventConverter;
import org.apache.shardingsphere.scaling.postgresql.wal.WalPosition;
import org.apache.shardingsphere.scaling.postgresql.wal.decode.DecodingPlugin;
import org.apache.shardingsphere.scaling.postgresql.wal.event.AbstractWalEvent;
import org.apache.shardingsphere.scaling.postgresql.wal.event.PlaceholderEvent;
import org.opengauss.jdbc.PgConnection;
import org.opengauss.replication.PGReplicationStream;

import javax.sql.DataSource;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * OpenGauss WAL dumper.
 */
@Slf4j
public final class OpenGaussWalDumper extends AbstractScalingExecutor implements IncrementalDumper {

    private final WalPosition walPosition;

    private final DumperConfiguration dumperConfig;

    private final OpenGaussLogicalReplication logicalReplication = new OpenGaussLogicalReplication();

    private final WalEventConverter walEventConverter;
    
    private String slotName = OpenGaussLogicalReplication.SLOT_NAME_PREFIX;

    @Setter
    private Channel channel;

    public OpenGaussWalDumper(final DumperConfiguration dumperConfig, final ScalingPosition<WalPosition> position) {
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

    private PgConnection getReplicationConn() throws SQLException {
        return logicalReplication
                .createPgConnection((StandardJDBCDataSourceConfiguration) dumperConfig.getDataSourceConfig())
                .unwrap(PgConnection.class);
    }

    private MppdbDecodingPlugin initReplication() {
        MppdbDecodingPlugin plugin = null;
        try {
            DataSource dataSource = dumperConfig.getDataSourceConfig().toDataSource();
            try (Connection conn = dataSource.getConnection()) {
                slotName = OpenGaussLogicalReplication.getUniqueSlotName(conn);
                OpenGaussLogicalReplication.createIfNotExists(conn);
                OpenGaussTimestampUtils utils = new OpenGaussTimestampUtils(conn.unwrap(PgConnection.class).getTimestampUtils());
                plugin = new MppdbDecodingPlugin(utils);
            }
        } catch (SQLException sqlExp) {
            log.warn("create replication slot failed!");
        }
        return plugin;
    }

    private void dump() {
        DecodingPlugin decodingPlugin = initReplication();
        try (PgConnection pgConnection = getReplicationConn()) {
            PGReplicationStream stream = logicalReplication.createReplicationStream(pgConnection, walPosition.getLogSequenceNumber(), slotName);
            while (isRunning()) {
                ByteBuffer message = stream.readPending();
                if (null == message) {
                    ThreadUtil.sleep(10L);
                    continue;
                }
                AbstractWalEvent event = decodingPlugin.decode(message,
                        new OpenGaussLogSequenceNumber(stream.getLastReceiveLSN()));
                Record record = walEventConverter.convert(event);
                if (!(event instanceof PlaceholderEvent) && log.isDebugEnabled()) {
                    log.debug("dump, event={}, record={}", event, record);
                }
                updateRecordOldValue(record);
                pushRecord(record);
            }
        } catch (final SQLException ex) {
            if (ex.getMessage().contains("is already active")) {
                return;
            }
            throw new ScalingTaskExecuteException(ex);
        }
    }

    private void updateRecordOldValue(final Record record) {
        if (!(record instanceof DataRecord)) {
            return;
        }
        DataRecord dataRecord = (DataRecord) record;
        if (!ScalingConstant.UPDATE.equals(dataRecord.getType())) {
            return;
        }
        for (Column col: dataRecord.getColumns()) {
            if (col.isPrimaryKey() && col.isUpdated()) {
                col.setOldValue(col.getValue());
            }
        }
    }
    
    private void pushRecord(final Record record) {
        try {
            channel.pushRecord(record);
        } catch (final InterruptedException ignored) {
        }
    }
}

