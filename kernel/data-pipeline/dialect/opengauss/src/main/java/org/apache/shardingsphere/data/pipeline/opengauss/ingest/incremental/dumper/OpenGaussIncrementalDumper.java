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

package org.apache.shardingsphere.data.pipeline.opengauss.ingest.incremental.dumper;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.core.exception.IngestException;
import org.apache.shardingsphere.data.pipeline.core.execute.AbstractPipelineLifecycleRunnable;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumper;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.incremental.IncrementalDumperContext;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.record.Record;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.opengauss.ingest.incremental.wal.OpenGaussLogicalReplication;
import org.apache.shardingsphere.data.pipeline.opengauss.ingest.incremental.wal.decode.MppdbDecodingPlugin;
import org.apache.shardingsphere.data.pipeline.opengauss.ingest.incremental.wal.decode.OpenGaussLogSequenceNumber;
import org.apache.shardingsphere.data.pipeline.opengauss.ingest.incremental.wal.decode.OpenGaussTimestampUtils;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.WALEventConverter;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.WALPosition;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.decode.DecodingPlugin;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.AbstractRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.AbstractWALEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.BeginTXEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.CommitTXEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.position.slot.PostgreSQLSlotNameGenerator;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.opengauss.jdbc.PgConnection;
import org.opengauss.replication.PGReplicationStream;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Incremental dumper of openGauss.
 */
@HighFrequencyInvocation
@Slf4j
public final class OpenGaussIncrementalDumper extends AbstractPipelineLifecycleRunnable implements IncrementalDumper {
    
    private static final Pattern VERSION_PATTERN = Pattern.compile("^\\(openGauss (\\d)");
    
    private static final int DEFAULT_VERSION = 2;
    
    private final IncrementalDumperContext dumperContext;
    
    private final AtomicReference<WALPosition> walPosition;
    
    private final PipelineChannel channel;
    
    private final WALEventConverter walEventConverter;
    
    private final OpenGaussLogicalReplication logicalReplication;
    
    private final boolean decodeWithTX;
    
    private List<AbstractRowEvent> rowEvents = new LinkedList<>();
    
    private final AtomicReference<Long> currentCsn = new AtomicReference<>();
    
    public OpenGaussIncrementalDumper(final IncrementalDumperContext dumperContext, final IngestPosition position,
                                      final PipelineChannel channel, final PipelineTableMetaDataLoader metaDataLoader) {
        this.dumperContext = dumperContext;
        walPosition = new AtomicReference<>((WALPosition) position);
        this.channel = channel;
        walEventConverter = new WALEventConverter(dumperContext, metaDataLoader);
        logicalReplication = new OpenGaussLogicalReplication();
        decodeWithTX = dumperContext.isDecodeWithTX();
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
                    Thread.sleep(5000L);
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
        int majorVersion = getMajorVersion();
        try (PgConnection connection = getReplicationConnectionUnwrap()) {
            stream = logicalReplication.createReplicationStream(
                    connection, walPosition.get().getLogSequenceNumber(), PostgreSQLSlotNameGenerator.getUniqueSlotName(connection, dumperContext.getJobId()), majorVersion);
            DecodingPlugin decodingPlugin = new MppdbDecodingPlugin(new OpenGaussTimestampUtils(connection.getTimestampUtils()), decodeWithTX, majorVersion >= 3);
            while (isRunning()) {
                ByteBuffer message = stream.readPending();
                if (null == message) {
                    Thread.sleep(10L);
                    continue;
                }
                AbstractWALEvent event = decodingPlugin.decode(message, new OpenGaussLogSequenceNumber(stream.getLastReceiveLSN()));
                if (decodeWithTX) {
                    processEventWithTX(event, majorVersion);
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
    
    private int getMajorVersion() throws SQLException {
        StandardPipelineDataSourceConfiguration dataSourceConfig = (StandardPipelineDataSourceConfiguration) dumperContext.getCommonContext().getDataSourceConfig();
        try (
                Connection connection = DriverManager.getConnection(dataSourceConfig.getUrl(), dataSourceConfig.getUsername(), dataSourceConfig.getPassword());
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT version()")) {
            resultSet.next();
            String versionText = resultSet.getString(1);
            return parseMajorVersion(versionText);
        }
    }
    
    private int parseMajorVersion(final String versionText) {
        Matcher matcher = VERSION_PATTERN.matcher(versionText);
        boolean isFind = matcher.find();
        log.info("openGauss major version={}, `select version()`={}", isFind ? matcher.group(1) : DEFAULT_VERSION, versionText);
        if (isFind) {
            return Integer.parseInt(matcher.group(1));
        }
        return DEFAULT_VERSION;
    }
    
    private PgConnection getReplicationConnectionUnwrap() throws SQLException {
        return logicalReplication.createConnection((StandardPipelineDataSourceConfiguration) dumperContext.getCommonContext().getDataSourceConfig()).unwrap(PgConnection.class);
    }
    
    private void processEventWithTX(final AbstractWALEvent event, final int majorVersion) {
        if (event instanceof BeginTXEvent) {
            if (majorVersion < 3) {
                return;
            }
            if (!rowEvents.isEmpty()) {
                log.warn("Commit event parse have problem, there still has uncommitted row events size={}, ", rowEvents.size());
            }
            currentCsn.set(((BeginTXEvent) event).getCsn());
            return;
        }
        if (event instanceof AbstractRowEvent) {
            AbstractRowEvent rowEvent = (AbstractRowEvent) event;
            rowEvent.setCsn(currentCsn.get());
            rowEvents.add(rowEvent);
            return;
        }
        if (event instanceof CommitTXEvent) {
            List<Record> records = new LinkedList<>();
            for (AbstractRowEvent each : rowEvents) {
                if (majorVersion < 3) {
                    each.setCsn(((CommitTXEvent) event).getCsn());
                }
                records.add(walEventConverter.convert(each));
            }
            records.add(walEventConverter.convert(event));
            channel.push(records);
            rowEvents = new LinkedList<>();
            currentCsn.set(null);
        }
    }
    
    private void processEventIgnoreTX(final AbstractWALEvent event) {
        if (event instanceof BeginTXEvent) {
            return;
        }
        channel.push(Collections.singletonList(walEventConverter.convert(event)));
    }
    
    @Override
    protected void doStop() {
    }
}
