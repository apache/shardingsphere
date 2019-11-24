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
import info.avalon566.shardingscaling.core.execute.executor.channel.MemoryChannel;
import info.avalon566.shardingscaling.core.execute.executor.reader.Reader;
import info.avalon566.shardingscaling.core.execute.executor.reader.ReaderFactory;
import info.avalon566.shardingscaling.core.execute.executor.writer.Writer;
import info.avalon566.shardingscaling.core.execute.executor.writer.WriterFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

/**
 * Table slice execute task.
 *
 * @author avalon566
 * @author yangyi
 */
@Slf4j
public class HistoryDataSyncTask implements SyncTask {

    private final SyncConfiguration syncConfiguration;

    public HistoryDataSyncTask(final SyncConfiguration syncConfiguration) {
        this.syncConfiguration = syncConfiguration;
    }

    @Override
    public void prepare() {

    }

    @Override
    public final void start(final ReportCallback callback) {
        final Reader reader = ReaderFactory.newInstanceJdbcReader(syncConfiguration.getReaderConfiguration());
        final Writer writer = WriterFactory.newInstance(syncConfiguration.getWriterConfiguration());
        new SyncExecutor(new MemoryChannel(), reader, Collections.singletonList(writer)).execute(new ExecuteCallback() {

            @Override
            public void onSuccess() {
                log.info("{} table slice execute finish", syncConfiguration.getReaderConfiguration().getTableName());
                callback.onProcess(new Event(syncConfiguration.getTaskId(), EventType.FINISHED));
            }

            @Override
            public void onFailure(final Throwable throwable) {
                log.error("{} table slice execute exception exit", syncConfiguration.getReaderConfiguration().getTableName());
                ((SyncExecuteException) throwable).logExceptions();
                callback.onProcess(new Event(syncConfiguration.getTaskId(), EventType.EXCEPTION_EXIT));
            }
        });
    }

    @Override
    public final void stop() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final SyncProgress getProgress() {
        return new SyncProgress() {
        };
    }
}
