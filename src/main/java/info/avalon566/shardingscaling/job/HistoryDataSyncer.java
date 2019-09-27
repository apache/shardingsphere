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
import info.avalon566.shardingscaling.job.config.SyncType;
import info.avalon566.shardingscaling.job.schedule.EventType;
import info.avalon566.shardingscaling.job.schedule.Reporter;
import info.avalon566.shardingscaling.job.schedule.standalone.InProcessScheduler;
import info.avalon566.shardingscaling.sync.jdbc.DbMetaDataUtil;
import info.avalon566.shardingscaling.sync.mysql.MySQLJdbcReader;
import lombok.extern.slf4j.Slf4j;
import lombok.var;

import java.util.ArrayList;
import java.util.List;

/**
 * @author avalon566
 */
@Slf4j
public class HistoryDataSyncer {

    private final SyncConfiguration syncConfiguration;

    public HistoryDataSyncer(SyncConfiguration syncConfiguration) {
        this.syncConfiguration = syncConfiguration;
    }

    public void run() {
        var configs = split(syncConfiguration);
        var reporter = new InProcessScheduler().schedule(configs);
        waitSlicesFinished(configs, reporter);
    }

    private List<SyncConfiguration> split(SyncConfiguration syncConfiguration) {
        List<SyncConfiguration> syncConfigurations = new ArrayList<>();
        // split by table
        for (String tableName : new DbMetaDataUtil(syncConfiguration.getReaderConfiguration()).getTableNames()) {
            var readerConfig = syncConfiguration.getReaderConfiguration().clone();
            readerConfig.setTableName(tableName);
            // split by primary key range
            for(var sliceConfig : new MySQLJdbcReader(readerConfig).split(syncConfiguration.getConcurrency())) {
                syncConfigurations.add(new SyncConfiguration(SyncType.TableSlice, syncConfiguration.getConcurrency(),
                        sliceConfig, syncConfiguration.getWriterConfiguration().clone()));
            }
        }
        return syncConfigurations;
    }

    private void waitSlicesFinished(List<SyncConfiguration> syncConfigurations, Reporter reporter) {
        var counter = 0;
        while (true) {
            var event = reporter.consumeEvent();
            if (EventType.FINISHED == event.getEventType()) {
                counter++;
            }
            if (syncConfigurations.size() == counter) {
                log.info("history data sync finish");
                break;
            }
        }
    }
}
