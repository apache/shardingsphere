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

import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.executor.AbstractLifecycleExecutor;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.IncrementalDumper;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.ingest.exception.IngestException;
import org.apache.shardingsphere.data.pipeline.core.util.ThreadUtil;
import org.apache.shardingsphere.data.pipeline.opengauss.ingest.wal.OpenGaussLogicalReplication;
import org.apache.shardingsphere.data.pipeline.opengauss.ingest.wal.decode.MppdbDecodingPlugin;
import org.apache.shardingsphere.data.pipeline.opengauss.ingest.wal.decode.OpenGaussLogSequenceNumber;
import org.apache.shardingsphere.data.pipeline.opengauss.ingest.wal.decode.OpenGaussTimestampUtils;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.WALEventConverter;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.WALPosition;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.DecodingPlugin;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.AbstractWALEvent;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;
import org.opengauss.jdbc.PgConnection;
import org.opengauss.replication.PGReplicationStream;

import java.nio.ByteBuffer;
import java.sql.SQLException;

/**
 * WAL dumper of openGauss.
 */
public final class OpenGaussWALDumper extends AbstractLifecycleExecutor implements IncrementalDumper {
    
    private final DumperConfiguration dumperConfig;
    
    private final WALPosition walPosition;
    
    private final PipelineChannel channel;
    
    private final WALEventConverter walEventConverter;
    
    private final OpenGaussLogicalReplication logicalReplication;
    
    public OpenGaussWALDumper(final DumperConfiguration dumperConfig, final IngestPosition<WALPosition> position,
                              final PipelineChannel channel, final PipelineTableMetaDataLoader metaDataLoader) {
        ShardingSpherePreconditions.checkState(StandardPipelineDataSourceConfiguration.class.equals(dumperConfig.getDataSourceConfig().getClass()),
                () -> new UnsupportedSQLOperationException("PostgreSQLWALDumper only support PipelineDataSourceConfiguration"));
        this.dumperConfig = dumperConfig;
        walPosition = (WALPosition) position;
        this.channel = channel;
        walEventConverter = new WALEventConverter(dumperConfig, metaDataLoader);
        logicalReplication = new OpenGaussLogicalReplication();
    }
    
    @Override
    protected void runBlocking() {
        PGReplicationStream stream = null;
        try (PgConnection connection = getReplicationConnectionUnwrap()) {
            stream = logicalReplication.createReplicationStream(connection, walPosition.getLogSequenceNumber(), OpenGaussPositionInitializer.getUniqueSlotName(connection, dumperConfig.getJobId()));
            DecodingPlugin decodingPlugin = new MppdbDecodingPlugin(new OpenGaussTimestampUtils(connection.getTimestampUtils()));
            while (isRunning()) {
                ByteBuffer message = stream.readPending();
                if (null == message) {
                    ThreadUtil.sleep(10L);
                    continue;
                }
                AbstractWALEvent event = decodingPlugin.decode(message, new OpenGaussLogSequenceNumber(stream.getLastReceiveLSN()));
                channel.pushRecord(walEventConverter.convert(event));
            }
        } catch (final SQLException ex) {
            throw new IngestException(ex);
        } finally {
            if (null != stream) {
                try {
                    stream.close();
                } catch (final SQLException ignored) {
                }
            }
        }
    }
    
    private PgConnection getReplicationConnectionUnwrap() throws SQLException {
        return logicalReplication.createConnection((StandardPipelineDataSourceConfiguration) dumperConfig.getDataSourceConfig()).unwrap(PgConnection.class);
    }
    
    @Override
    protected void doStop() {
    }
}
