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

package org.apache.shardingsphere.shardingscaling.core.synctask;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.shardingscaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.shardingscaling.core.controller.ReportCallback;
import org.apache.shardingsphere.shardingscaling.core.controller.SyncProgress;
import org.apache.shardingsphere.shardingscaling.core.execute.engine.ExecuteUtil;
import org.apache.shardingsphere.shardingscaling.core.execute.engine.SyncTaskExecuteCallback;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.channel.AckCallback;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.channel.RealtimeSyncChannel;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.log.LogManager;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.log.LogManagerFactory;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.reader.Reader;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.reader.ReaderFactory;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.record.Record;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.writer.Writer;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.writer.WriterFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Realtime data execute task.
 *
 * @author avalon566
 */
@Slf4j
public final class RealtimeDataSyncTask implements SyncTask {

    private final SyncConfiguration syncConfiguration;

    private LogManager logManager;

    private Reader reader;
    
    public RealtimeDataSyncTask(final SyncConfiguration syncConfiguration) {
        this.syncConfiguration = syncConfiguration;
    }

    @Override
    public void prepare() {
        this.logManager = LogManagerFactory.newInstanceLogManager(syncConfiguration.getReaderConfiguration());
        logManager.getCurrentPosition();
    }

    @Override
    public void start(final ReportCallback callback) {
        reader = ReaderFactory.newInstanceLogReader(syncConfiguration.getReaderConfiguration(), logManager.getCurrentPosition());
        List<Writer> writers = instanceWriters();
        RealtimeSyncChannel channel = instanceChannel(writers.size());
        ExecuteUtil.execute(channel, reader, writers, new SyncTaskExecuteCallback(this.getClass().getSimpleName(), syncConfiguration, callback));
    }
    
    private List<Writer> instanceWriters() {
        List<Writer> result = new ArrayList<>(syncConfiguration.getConcurrency());
        for (int i = 0; i < syncConfiguration.getConcurrency(); i++) {
            result.add(WriterFactory.newInstance(syncConfiguration.getWriterConfiguration()));
        }
        return result;
    }
    
    private RealtimeSyncChannel instanceChannel(final int channelSize) {
        return new RealtimeSyncChannel(channelSize, Collections.<AckCallback>singletonList(new AckCallback() {
            @Override
            public void onAck(final List<Record> records) {
                logManager.updateCurrentPosition(records.get(records.size() - 1).getLogPosition());
            }
        }));
    }

    @Override
    public void stop() {
        if (null != reader) {
            reader.stop();
        }
    }

    @Override
    public SyncProgress getProgress() {
        return new SyncProgress() {
        };
    }
}
