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

package org.apache.shardingsphere.data.pipeline.opengauss.ingest;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.core.datasource.creator.PipelineDataSourceCreatorFactory;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.AbstractIncrementalDumper;
import org.apache.shardingsphere.data.pipeline.core.ingest.exception.IngestException;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.util.ThreadUtil;
import org.apache.shardingsphere.data.pipeline.opengauss.ingest.wal.OpenGaussLogicalReplication;
import org.apache.shardingsphere.data.pipeline.opengauss.ingest.wal.decode.MppdbDecodingPlugin;
import org.apache.shardingsphere.data.pipeline.opengauss.ingest.wal.decode.OpenGaussLogSequenceNumber;
import org.apache.shardingsphere.data.pipeline.opengauss.ingest.wal.decode.OpenGaussTimestampUtils;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.WalEventConverter;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.WalPosition;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.DecodingPlugin;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.AbstractWalEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.PlaceholderEvent;
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
public final class OpenGaussWalDumper extends AbstractIncrementalDumper<WalPosition> {
    
    private final WalPosition walPosition;
    
    private final DumperConfiguration dumperConfig;
    
    private final OpenGaussLogicalReplication logicalReplication = new OpenGaussLogicalReplication();
    
    private final WalEventConverter walEventConverter;
    
    private String slotName = OpenGaussLogicalReplication.SLOT_NAME_PREFIX;
    
    private final PipelineChannel channel;
    
    public OpenGaussWalDumper(final DumperConfiguration dumperConfig, final IngestPosition<WalPosition> position,
                              final PipelineChannel channel, final PipelineTableMetaDataLoader metaDataLoader) {
        super(dumperConfig, position, channel, metaDataLoader);
        walPosition = (WalPosition) position;
        if (!StandardPipelineDataSourceConfiguration.class.equals(dumperConfig.getDataSourceConfig().getClass())) {
            throw new UnsupportedOperationException("PostgreSQLWalDumper only support PipelineDataSourceConfiguration");
        }
        this.dumperConfig = dumperConfig;
        this.channel = channel;
        walEventConverter = new WalEventConverter(dumperConfig, metaDataLoader);
    }
    
    @Override
    protected void doStart() {
        dump();
    }
    
    private PgConnection getReplicationConn() throws SQLException {
        return logicalReplication
                .createPgConnection((StandardPipelineDataSourceConfiguration) dumperConfig.getDataSourceConfig())
                .unwrap(PgConnection.class);
    }
    
    private MppdbDecodingPlugin initReplication() {
        MppdbDecodingPlugin plugin = null;
        try {
            DataSource pipelineDataSource = PipelineDataSourceCreatorFactory.getInstance(
                    dumperConfig.getDataSourceConfig().getType()).createPipelineDataSource(dumperConfig.getDataSourceConfig().getDataSourceConfiguration());
            try (Connection conn = pipelineDataSource.getConnection()) {
                slotName = OpenGaussLogicalReplication.getUniqueSlotName(conn);
                OpenGaussLogicalReplication.createIfNotExists(conn);
                OpenGaussTimestampUtils utils = new OpenGaussTimestampUtils(conn.unwrap(PgConnection.class).getTimestampUtils());
                plugin = new MppdbDecodingPlugin(utils);
            }
        } catch (SQLException ex) {
            log.warn("Create replication slot failed!");
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
                pushRecord(record);
            }
        } catch (final SQLException ex) {
            if (ex.getMessage().contains("is already active")) {
                return;
            }
            throw new IngestException(ex);
        }
    }
    
    private void pushRecord(final Record record) {
        channel.pushRecord(record);
    }
    
    @Override
    protected void doStop() {
    }
}

