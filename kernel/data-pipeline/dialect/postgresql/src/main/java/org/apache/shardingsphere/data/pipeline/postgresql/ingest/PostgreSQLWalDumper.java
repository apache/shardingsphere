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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.executor.AbstractLifecycleExecutor;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.IncrementalDumper;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.ingest.exception.IngestException;
import org.apache.shardingsphere.data.pipeline.core.util.ThreadUtil;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.LogicalReplication;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.WalEventConverter;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.WalPosition;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.DecodingPlugin;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.PostgreSQLLogSequenceNumber;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.PostgreSQLTimestampUtils;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.TestDecodingPlugin;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.AbstractWalEvent;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;
import org.postgresql.jdbc.PgConnection;
import org.postgresql.replication.PGReplicationStream;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * PostgreSQL WAL dumper.
 */
@Slf4j
public final class PostgreSQLWalDumper extends AbstractLifecycleExecutor implements IncrementalDumper {
    
    private final DumperConfiguration dumperConfig;
    
    private final WalPosition walPosition;
    
    private final PipelineChannel channel;
    
    private final WalEventConverter walEventConverter;
    
    private final LogicalReplication logicalReplication;
    
    public PostgreSQLWalDumper(final DumperConfiguration dumperConfig, final IngestPosition<WalPosition> position,
                               final PipelineChannel channel, final PipelineTableMetaDataLoader metaDataLoader) {
        ShardingSpherePreconditions.checkState(StandardPipelineDataSourceConfiguration.class.equals(dumperConfig.getDataSourceConfig().getClass()),
                () -> new UnsupportedSQLOperationException("PostgreSQLWalDumper only support PipelineDataSourceConfiguration"));
        this.dumperConfig = dumperConfig;
        walPosition = (WalPosition) position;
        this.channel = channel;
        walEventConverter = new WalEventConverter(dumperConfig, metaDataLoader);
        logicalReplication = new LogicalReplication();
    }
    
    @Override
    protected void runBlocking() {
        // TODO use unified PgConnection
        try (
                Connection connection = logicalReplication.createConnection((StandardPipelineDataSourceConfiguration) dumperConfig.getDataSourceConfig());
                PGReplicationStream stream = logicalReplication.createReplicationStream(connection, PostgreSQLPositionInitializer.getUniqueSlotName(connection, dumperConfig.getJobId()),
                        walPosition.getLogSequenceNumber())) {
            PostgreSQLTimestampUtils utils = new PostgreSQLTimestampUtils(connection.unwrap(PgConnection.class).getTimestampUtils());
            DecodingPlugin decodingPlugin = new TestDecodingPlugin(utils);
            while (isRunning()) {
                ByteBuffer message = stream.readPending();
                if (null == message) {
                    ThreadUtil.sleep(10L);
                    continue;
                }
                AbstractWalEvent event = decodingPlugin.decode(message, new PostgreSQLLogSequenceNumber(stream.getLastReceiveLSN()));
                channel.pushRecord(walEventConverter.convert(event));
            }
        } catch (final SQLException ex) {
            throw new IngestException(ex);
        }
    }
    
    @Override
    protected void doStop() {
    }
}
