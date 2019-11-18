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
import info.avalon566.shardingscaling.core.controller.SyncTaskProgress;
import info.avalon566.shardingscaling.core.job.sync.executor.Event;
import info.avalon566.shardingscaling.core.job.sync.executor.EventType;
import info.avalon566.shardingscaling.core.job.sync.executor.Reporter;
import info.avalon566.shardingscaling.core.exception.SyncExecuteException;
import info.avalon566.shardingscaling.core.execute.SyncExecutor;
import info.avalon566.shardingscaling.core.execute.channel.MemoryChannel;
import info.avalon566.shardingscaling.core.execute.reader.NopLogPosition;
import info.avalon566.shardingscaling.core.execute.reader.Reader;
import info.avalon566.shardingscaling.core.execute.reader.ReaderFactory;
import info.avalon566.shardingscaling.core.execute.writer.Writer;
import info.avalon566.shardingscaling.core.execute.writer.WriterFactory;
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

    private final Reporter reporter;

    public HistoryDataSyncTask(final SyncConfiguration syncConfiguration, final Reporter reporter) {
        this.syncConfiguration = syncConfiguration;
        this.reporter = reporter;
    }

    @Override
    public final void start() {
        new Thread(this).start();
    }

    @Override
    public final void stop() {
        throw new UnsupportedOperationException();
    }

    @Override
    public final SyncTaskProgress getProgress() {
        return new SyncTaskProgress("HISTORY_DATA_SYNC", new NopLogPosition());
    }

    /**
     * Run synchronize task.
     */
    @Override
    public void run() {
        final Reader reader = ReaderFactory.newInstanceJdbcReader(syncConfiguration.getReaderConfiguration());
        final Writer writer = WriterFactory.newInstance(syncConfiguration.getWriterConfiguration());
        try {
            new SyncExecutor(new MemoryChannel(), reader, Collections.singletonList(writer)).execute();
            log.info("{} table slice execute finish", syncConfiguration.getReaderConfiguration().getTableName());
            reporter.report(new Event(syncConfiguration.getTaskId(), EventType.FINISHED));
        } catch (SyncExecuteException ex) {
            log.error("{} table slice execute exception exit", syncConfiguration.getReaderConfiguration().getTableName());
            ex.logExceptions();
            reporter.report(new Event(syncConfiguration.getTaskId(), EventType.EXCEPTION_EXIT));
        }
    }
}
