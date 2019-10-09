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

package info.avalon566.shardingscaling.job;

import info.avalon566.shardingscaling.job.config.SyncConfiguration;
import info.avalon566.shardingscaling.job.schedule.Event;
import info.avalon566.shardingscaling.job.schedule.EventType;
import info.avalon566.shardingscaling.job.schedule.Reporter;
import info.avalon566.shardingscaling.sync.core.SyncExecutor;
import info.avalon566.shardingscaling.sync.core.Writer;
import info.avalon566.shardingscaling.sync.mysql.MySQLJdbcReader;
import info.avalon566.shardingscaling.sync.mysql.MySQLWriter;
import lombok.var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Table slice sync job.
 * @author avalon566
 */
public class TableSliceSyncJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(TableSliceSyncJob.class);

    private final SyncConfiguration syncConfiguration;

    private final Reporter reporter;

    public TableSliceSyncJob(final SyncConfiguration syncConfiguration, final Reporter reporter) {
        this.syncConfiguration = syncConfiguration;
        this.reporter = reporter;
    }

    /**
     * Run.
     */
    public void run() {
        var reader = new MySQLJdbcReader(syncConfiguration.getReaderConfiguration());
        var writer = new MySQLWriter(syncConfiguration.getWriterConfiguration());
        final var executor = new SyncExecutor(reader, Arrays.<Writer>asList(writer));
        executor.run();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    executor.waitFinish();
                    LOGGER.info("{} table slice sync finish", syncConfiguration.getReaderConfiguration().getTableName());
                    reporter.report(new Event(EventType.FINISHED));
                } catch (Exception ex) {
                    LOGGER.info("{} table slice sync exception exit", syncConfiguration.getReaderConfiguration().getTableName());
                    reporter.report(new Event(EventType.FINISHED));
                }
            }
        }).start();
    }
}
