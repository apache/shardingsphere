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

package info.avalon566.shardingscaling.core.synctask;

import info.avalon566.shardingscaling.core.config.SyncConfiguration;
import info.avalon566.shardingscaling.core.controller.ReportCallback;
import info.avalon566.shardingscaling.core.controller.SyncProgress;
import info.avalon566.shardingscaling.core.exception.SyncExecuteException;
import info.avalon566.shardingscaling.core.execute.Event;
import info.avalon566.shardingscaling.core.execute.EventType;
import info.avalon566.shardingscaling.core.execute.engine.ExecuteCallback;
import info.avalon566.shardingscaling.core.execute.engine.SyncExecutor;
import info.avalon566.shardingscaling.core.execute.executor.channel.AckCallback;
import info.avalon566.shardingscaling.core.execute.executor.channel.RealtimeSyncChannel;
import info.avalon566.shardingscaling.core.execute.executor.log.LogManager;
import info.avalon566.shardingscaling.core.execute.executor.log.LogManagerFactory;
import info.avalon566.shardingscaling.core.execute.executor.reader.LogPosition;
import info.avalon566.shardingscaling.core.execute.executor.reader.Reader;
import info.avalon566.shardingscaling.core.execute.executor.reader.ReaderFactory;
import info.avalon566.shardingscaling.core.execute.executor.record.Record;
import info.avalon566.shardingscaling.core.execute.executor.writer.Writer;
import info.avalon566.shardingscaling.core.execute.executor.writer.WriterFactory;
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

    private LogManager logManager;

    private Reader reader;

    private LogPosition currentLogPosition;

    private RealtimeSyncChannel channel;

    public RealtimeDataSyncTask(final SyncConfiguration syncConfiguration) {
        this.syncConfiguration = syncConfiguration;
    }

    @Override
    public final void prepare() {
        this.logManager = LogManagerFactory.newInstanceLogManager(syncConfiguration.getReaderConfiguration());
        currentLogPosition = logManager.getCurrentPosition();
        this.reader = ReaderFactory.newInstanceLogReader(syncConfiguration.getReaderConfiguration(), currentLogPosition);
    }

    @Override
    public final void start(final ReportCallback callback) {
        final List<Writer> writers = new ArrayList<>(syncConfiguration.getConcurrency());
        for (int i = 0; i < syncConfiguration.getConcurrency(); i++) {
            writers.add(WriterFactory.newInstance(syncConfiguration.getWriterConfiguration()));
        }
        channel = new RealtimeSyncChannel(writers.size(), Collections.singletonList((AckCallback) new AckCallback() {
            @Override
            public void onAck(final List<Record> records) {
                Record record = records.get(records.size() - 1);
                currentLogPosition = record.getLogPosition();
            }
        }));
        new SyncExecutor(channel, reader, writers).execute(new ExecuteCallback() {

            @Override
            public void onSuccess() {
                log.info("realtime data execute finish");
                callback.onProcess(new Event(syncConfiguration.getTaskId(), EventType.FINISHED));
            }

            @Override
            public void onFailure(final Throwable throwable) {
                log.error("realtime data execute exception exit");
                ((SyncExecuteException) throwable).logExceptions();
                callback.onProcess(new Event(syncConfiguration.getTaskId(), EventType.EXCEPTION_EXIT));
            }
        });
    }

    @Override
    public final void stop() {
        reader.stop();
    }

    @Override
    public final SyncProgress getProgress() {
        return new SyncProgress() {
        };
    }
}
