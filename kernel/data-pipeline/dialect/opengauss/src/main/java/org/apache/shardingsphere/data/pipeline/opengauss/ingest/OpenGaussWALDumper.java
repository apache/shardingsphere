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

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.executor.AbstractLifecycleExecutor;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.dumper.IncrementalDumper;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.api.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.api.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.exception.IngestException;
import org.apache.shardingsphere.data.pipeline.opengauss.ingest.wal.OpenGaussLogicalReplication;
import org.apache.shardingsphere.data.pipeline.opengauss.ingest.wal.decode.MppdbDecodingPlugin;
import org.apache.shardingsphere.data.pipeline.opengauss.ingest.wal.decode.OpenGaussLogSequenceNumber;
import org.apache.shardingsphere.data.pipeline.opengauss.ingest.wal.decode.OpenGaussTimestampUtils;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.WALEventConverter;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.WALPosition;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.DecodingPlugin;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.AbstractRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.AbstractWALEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.BeginTXEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.CommitTXEvent;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.generic.UnsupportedSQLOperationException;
import org.opengauss.jdbc.PgConnection;
import org.opengauss.replication.PGReplicationStream;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * WAL dumper of openGauss.
 */
@Slf4j
public final class OpenGaussWALDumper extends AbstractLifecycleExecutor implements IncrementalDumper {
    
    private final DumperConfiguration dumperConfig;
    
    private final AtomicReference<WALPosition> walPosition;
    
    private final PipelineChannel channel;
    
    private final WALEventConverter walEventConverter;
    
    private final OpenGaussLogicalReplication logicalReplication;
    
    private final boolean decodeWithTX;
    
    private List<AbstractRowEvent> rowEvents = new LinkedList<>();
    
    public OpenGaussWALDumper(final DumperConfiguration dumperConfig, final IngestPosition position,
                              final PipelineChannel channel, final PipelineTableMetaDataLoader metaDataLoader) {
        ShardingSpherePreconditions.checkState(StandardPipelineDataSourceConfiguration.class.equals(dumperConfig.getDataSourceConfig().getClass()),
                () -> new UnsupportedSQLOperationException("PostgreSQLWALDumper only support PipelineDataSourceConfiguration"));
        this.dumperConfig = dumperConfig;
        walPosition = new AtomicReference<>((WALPosition) position);
        this.channel = channel;
        walEventConverter = new WALEventConverter(dumperConfig, metaDataLoader);
        logicalReplication = new OpenGaussLogicalReplication();
        this.decodeWithTX = dumperConfig.isDecodeWithTX();
    }
    
    @SneakyThrows(InterruptedException.class)
    @Override
    protected void runBlocking() {
        AtomicInteger reconnectTimes = new AtomicInteger();
        while (isRunning()) {
            try {
                dump();
                break;
            } catch (final SQLException ex) {
                int times = reconnectTimes.incrementAndGet();
                log.error("Connect failed, reconnect times={}", times, ex);
                if (isRunning()) {
                    Thread.sleep(5000);
                }
                if (times >= 5) {
                    throw new IngestException(ex);
                }
            }
        }
    }
    
    @SneakyThrows(InterruptedException.class)
    private void dump() throws SQLException {
        PGReplicationStream stream = null;
        try (PgConnection connection = getReplicationConnectionUnwrap()) {
            stream = logicalReplication.createReplicationStream(connection, walPosition.get().getLogSequenceNumber(),
                    OpenGaussPositionInitializer.getUniqueSlotName(connection, dumperConfig.getJobId()));
            DecodingPlugin decodingPlugin = new MppdbDecodingPlugin(new OpenGaussTimestampUtils(connection.getTimestampUtils()), decodeWithTX);
            while (isRunning()) {
                ByteBuffer message = stream.readPending();
                if (null == message) {
                    Thread.sleep(10L);
                    continue;
                }
                AbstractWALEvent event = decodingPlugin.decode(message, new OpenGaussLogSequenceNumber(stream.getLastReceiveLSN()));
                if (decodeWithTX) {
                    processEventWithTX(event);
                } else {
                    processEventIgnoreTX(event);
                }
                walPosition.set(new WALPosition(event.getLogSequenceNumber()));
            }
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
    
    private void processEventWithTX(final AbstractWALEvent event) {
        if (event instanceof BeginTXEvent) {
            return;
        }
        if (event instanceof AbstractRowEvent) {
            rowEvents.add((AbstractRowEvent) event);
            return;
        }
        if (event instanceof CommitTXEvent) {
            Long csn = ((CommitTXEvent) event).getCsn();
            List<Record> records = new LinkedList<>();
            for (AbstractRowEvent each : rowEvents) {
                each.setCsn(csn);
                records.add(walEventConverter.convert(each));
            }
            records.add(walEventConverter.convert(event));
            channel.pushRecords(records);
            rowEvents = new LinkedList<>();
        }
    }
    
    private void processEventIgnoreTX(final AbstractWALEvent event) {
        if (event instanceof BeginTXEvent) {
            return;
        }
        channel.pushRecords(Collections.singletonList(walEventConverter.convert(event)));
    }
    
    @Override
    protected void doStop() {
    }
}
