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

package org.apache.shardingsphere.scaling.core.fixture;

import java.util.List;

import org.apache.shardingsphere.scaling.core.config.ImporterConfiguration;
import org.apache.shardingsphere.scaling.core.execute.executor.channel.Channel;
import org.apache.shardingsphere.scaling.core.execute.executor.importer.Importer;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.execute.executor.record.FinishedRecord;
import org.apache.shardingsphere.scaling.core.execute.executor.record.Record;

public final class FixtureNopImporter implements Importer {
    
    private boolean running;
    
    private Channel channel;
    
    public FixtureNopImporter(final ImporterConfiguration importerConfiguration, final DataSourceManager dataSourceManager) {
    }
    
    @Override
    public void setChannel(final Channel channel) {
        this.channel = channel;
    }
    
    @Override
    public void write() {
        while (running) {
            List<Record> records = channel.fetchRecords(100, 1);
            if (FinishedRecord.class.equals(records.get(records.size() - 1).getClass())) {
                channel.ack();
                break;
            }
            channel.ack();
        }
    }
    
    @Override
    public void start() {
        running = true;
    }
    
    @Override
    public void stop() {
        running = false;
    }
    
    @Override
    public void run() {
        start();
        write();
    }
}
