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
import info.avalon566.shardingscaling.sync.core.SyncExecutor;
import info.avalon566.shardingscaling.sync.core.Writer;
import info.avalon566.shardingscaling.sync.mysql.MySQLBinlogReader;
import info.avalon566.shardingscaling.sync.mysql.MySQLWriter;
import lombok.var;

import java.util.ArrayList;

/**
 * Realtime data syncer.
 * @author avalon566
 */
public class RealtimeDataSyncer {

    private final SyncConfiguration syncConfiguration;

    private final MySQLBinlogReader mysqlBinlogReader;

    public RealtimeDataSyncer(final SyncConfiguration syncConfiguration) {
        this.syncConfiguration = syncConfiguration;
        mysqlBinlogReader = new MySQLBinlogReader(syncConfiguration.getReaderConfiguration());
    }

    /**
     * Do something before run,mark binlog position.
     */
    public final void preRun() {
        mysqlBinlogReader.markPosition();
    }

    /**
     * Start to sync realtime data.
     */
    public final void run() {
        var writers = new ArrayList<Writer>(syncConfiguration.getConcurrency());
        for (int i = 0; i < syncConfiguration.getConcurrency(); i++) {
            writers.add(new MySQLWriter(syncConfiguration.getWriterConfiguration()));
        }
        new SyncExecutor(mysqlBinlogReader, writers).run();
    }
}
