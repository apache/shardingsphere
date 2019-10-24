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

package info.avalon566.shardingscaling.core.job;

import info.avalon566.shardingscaling.core.exception.SyncExecuteException;
import info.avalon566.shardingscaling.core.config.SyncConfiguration;
import info.avalon566.shardingscaling.core.job.schedule.Event;
import info.avalon566.shardingscaling.core.job.schedule.EventType;
import info.avalon566.shardingscaling.core.job.schedule.Reporter;
import info.avalon566.shardingscaling.core.sync.SyncExecutor;
import info.avalon566.shardingscaling.core.sync.reader.Reader;
import info.avalon566.shardingscaling.core.sync.reader.ReaderFactory;
import info.avalon566.shardingscaling.core.sync.writer.Writer;
import info.avalon566.shardingscaling.core.sync.writer.WriterFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;

/**
 * Table slice sync job.
 *
 * @author avalon566
 * @author yangyi
 */
@Slf4j
public class TableSliceSyncJob {

    private final SyncConfiguration syncConfiguration;

    private final Reporter reporter;

    public TableSliceSyncJob(final SyncConfiguration syncConfiguration, final Reporter reporter) {
        this.syncConfiguration = syncConfiguration;
        this.reporter = reporter;
    }

    /**
     * Run synchronize task.
     */
    public void run() {
        Reader reader = ReaderFactory.newInstanceJdbcReader(syncConfiguration.getReaderConfiguration());
        Writer writer = WriterFactory.newInstance(syncConfiguration.getWriterConfiguration());
        final SyncExecutor executor = new SyncExecutor(reader, Collections.<Writer>singletonList(writer));
        executor.run();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    executor.waitFinish();
                    log.info("{} table slice sync finish", syncConfiguration.getReaderConfiguration().getTableName());
                    reporter.report(new Event(EventType.FINISHED));
                } catch (Exception ex) {
                    log.error("{} table slice sync exception exit", syncConfiguration.getReaderConfiguration().getTableName(), ex);
                    reporter.report(new Event(EventType.EXCEPTION_EXIT));
                }
            }
        }).start();
    }
}
