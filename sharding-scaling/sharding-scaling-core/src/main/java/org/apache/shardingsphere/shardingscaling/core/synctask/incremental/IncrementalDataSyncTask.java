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

package org.apache.shardingsphere.shardingscaling.core.synctask.incremental;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.shardingscaling.core.config.ScalingContext;
import org.apache.shardingsphere.shardingscaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.shardingscaling.core.controller.SyncProgress;
import org.apache.shardingsphere.shardingscaling.core.controller.task.ReportCallback;
import org.apache.shardingsphere.shardingscaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.shardingscaling.core.execute.engine.SyncTaskExecuteCallback;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.SyncExecutorGroup;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.channel.DistributionChannel;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.position.LogPositionManager;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.position.LogPositionManagerFactory;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.dumper.Dumper;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.dumper.DumperFactory;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.record.Record;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.importer.Importer;
import org.apache.shardingsphere.shardingscaling.core.execute.executor.importer.ImporterFactory;
import org.apache.shardingsphere.shardingscaling.core.synctask.SyncTask;
import org.apache.shardingsphere.underlying.common.database.metadata.DataSourceMetaData;

import java.util.ArrayList;
import java.util.List;

/**
 * Incremental data execute task.
 */
@Slf4j
public final class IncrementalDataSyncTask implements SyncTask {
    
    private final SyncConfiguration syncConfiguration;
    
    private final DataSourceManager dataSourceManager;
    
    private final String syncTaskId;
    
    private LogPositionManager logPositionManager;
    
    private Dumper dumper;
    
    private long delayMillisecond;
    
    public IncrementalDataSyncTask(final SyncConfiguration syncConfiguration, final DataSourceManager dataSourceManager) {
        this.syncConfiguration = syncConfiguration;
        this.dataSourceManager = dataSourceManager;
        DataSourceMetaData dataSourceMetaData = syncConfiguration.getDumperConfiguration().getDataSourceConfiguration().getDataSourceMetaData();
        syncTaskId = String.format("incremental-%s", null != dataSourceMetaData.getCatalog() ? dataSourceMetaData.getCatalog() : dataSourceMetaData.getSchema());
    }
    
    @Override
    public void prepare() {
        this.logPositionManager = instanceLogPositionManager();
        logPositionManager.getCurrentPosition();
    }
    
    private LogPositionManager instanceLogPositionManager() {
        return LogPositionManagerFactory.newInstanceLogManager(
                syncConfiguration.getDumperConfiguration().getDataSourceConfiguration().getDatabaseType().getName(),
                dataSourceManager.getDataSource(syncConfiguration.getDumperConfiguration().getDataSourceConfiguration()));
    }
    
    @Override
    public void start(final ReportCallback callback) {
        syncConfiguration.getDumperConfiguration().setTableNameMap(syncConfiguration.getTableNameMap());
        SyncExecutorGroup syncExecutorGroup = new SyncExecutorGroup(new SyncTaskExecuteCallback(this.getClass().getSimpleName(), syncTaskId, callback));
        instanceSyncExecutors(syncExecutorGroup);
        ScalingContext.getInstance().getSyncTaskExecuteEngine().submitGroup(syncExecutorGroup);
    }
    
    private void instanceSyncExecutors(final SyncExecutorGroup syncExecutorGroup) {
        dumper = DumperFactory.newInstanceLogDumper(syncConfiguration.getDumperConfiguration(), logPositionManager.getCurrentPosition());
        List<Importer> importers = instanceImporters();
        DistributionChannel channel = instanceChannel(importers);
        dumper.setChannel(channel);
        for (Importer each : importers) {
            each.setChannel(channel);
        }
        syncExecutorGroup.setChannel(channel);
        syncExecutorGroup.addSyncExecutor(dumper);
        syncExecutorGroup.addAllSyncExecutor(importers);
    }
    
    private List<Importer> instanceImporters() {
        List<Importer> result = new ArrayList<>(syncConfiguration.getConcurrency());
        for (int i = 0; i < syncConfiguration.getConcurrency(); i++) {
            result.add(ImporterFactory.newInstance(syncConfiguration.getImporterConfiguration(), dataSourceManager));
        }
        return result;
    }
    
    private DistributionChannel instanceChannel(final List<Importer> importers) {
        return new DistributionChannel(importers.size(), records -> {
            Record lastHandledRecord = records.get(records.size() - 1);
            logPositionManager.updateCurrentPosition(lastHandledRecord.getLogPosition());
            delayMillisecond = System.currentTimeMillis() - lastHandledRecord.getCommitTime();
        });
    }
    
    @Override
    public void stop() {
        if (null != dumper) {
            dumper.stop();
            dumper = null;
        }
    }
    
    @Override
    public SyncProgress getProgress() {
        return new IncrementalDataSyncTaskProgress(syncTaskId, delayMillisecond, logPositionManager.getCurrentPosition());
    }
}
