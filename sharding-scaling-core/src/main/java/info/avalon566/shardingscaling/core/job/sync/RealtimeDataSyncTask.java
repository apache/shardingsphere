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

package info.avalon566.shardingscaling.core.job.sync;

import info.avalon566.shardingscaling.core.config.SyncConfiguration;
import info.avalon566.shardingscaling.core.exception.SyncExecuteException;
import info.avalon566.shardingscaling.core.controller.SyncTaskProgress;
import info.avalon566.shardingscaling.core.job.sync.executor.Event;
import info.avalon566.shardingscaling.core.job.sync.executor.EventType;
import info.avalon566.shardingscaling.core.job.sync.executor.Reporter;
import info.avalon566.shardingscaling.core.execute.SyncExecutor;
import info.avalon566.shardingscaling.core.execute.channel.AckCallback;
import info.avalon566.shardingscaling.core.execute.channel.RealtimeSyncChannel;
import info.avalon566.shardingscaling.core.execute.reader.LogPosition;
import info.avalon566.shardingscaling.core.execute.reader.LogReader;
import info.avalon566.shardingscaling.core.execute.reader.ReaderFactory;
import info.avalon566.shardingscaling.core.execute.record.Record;
import info.avalon566.shardingscaling.core.execute.writer.Writer;
import info.avalon566.shardingscaling.core.execute.writer.WriterFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Realtime data execute task.
 *
 * @author avalon566
 */
@Slf4j
public class RealtimeDataSyncTask implements SyncTask {

    private final SyncConfiguration syncConfiguration;

    private final LogReader logReader;

    private final Reporter reporter;

    private RealtimeSyncChannel channel;

    private LogPosition currentLogPosition;

    public RealtimeDataSyncTask(final SyncConfiguration syncConfiguration, final Reporter reporter) {
        this.syncConfiguration = syncConfiguration;
        this.reporter = reporter;
        logReader = ReaderFactory.newInstanceLogReader(syncConfiguration.getReaderConfiguration(), syncConfiguration.getPosition());
    }

    @Override
    public final void start() {
        new Thread(this).start();
    }

    @Override
    public final void stop() {
        logReader.stop();
    }

    @Override
    public final SyncTaskProgress getProgress() {
        return new SyncTaskProgress("REALTIME_DATA_SYNC", currentLogPosition);
    }

    /**
     * Do something before run,mark binlog position.
     *
     * @return log position
     */
    public final LogPosition preRun() {
        return logReader.markPosition();
    }

    /**
     * Start to execute realtime data.
     */
    @Override
    public final void run() {
        final List<Writer> writers = new ArrayList<>(syncConfiguration.getConcurrency());
        for (int i = 0; i < syncConfiguration.getConcurrency(); i++) {
            writers.add(WriterFactory.newInstance(syncConfiguration.getWriterConfiguration()));
        }
        try {
            channel = new RealtimeSyncChannel(writers.size(), Collections.singletonList((AckCallback) new AckCallback() {
                @Override
                public void onAck(final List<Record> records) {
                    Record record = records.get(records.size() - 1);
                    currentLogPosition = record.getLogPosition();
                }
            }));
            startReportRealtimeSyncPosition();
            new SyncExecutor(channel, logReader, writers).execute();
            log.info("realtime data execute finish");
            reporter.report(new Event(syncConfiguration.getTaskId(), EventType.FINISHED));
        } catch (SyncExecuteException ex) {
            log.error("realtime data execute exception exit");
            ex.logExceptions();
            reporter.report(new Event(syncConfiguration.getTaskId(), EventType.EXCEPTION_EXIT));
        }
    }

    private void startReportRealtimeSyncPosition() {
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                LogPosition lastLogPosition = null;
                while (true) {
                    try {
                        Thread.sleep(1 * 1000);
                    } catch (InterruptedException ignored) {
                        break;
                    }
                    if (null == lastLogPosition || -1 == lastLogPosition.compareTo(currentLogPosition)) {
                        lastLogPosition = currentLogPosition;
                        Event event = new Event(syncConfiguration.getTaskId(), EventType.REALTIME_SYNC_POSITION);
                        event.setPayload(lastLogPosition);
                        reporter.report(event);
                    }
                }
            }
        }).start();
    }
}
