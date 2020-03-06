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

package org.apache.shardingsphere.shardingscaling.core.controller.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.shardingscaling.core.config.DataSourceConfiguration;
import org.apache.shardingsphere.shardingscaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.shardingscaling.core.controller.SyncProgress;
import org.apache.shardingsphere.shardingscaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.shardingscaling.core.execute.EventType;
import org.apache.shardingsphere.shardingscaling.core.synctask.DefaultSyncTaskFactory;
import org.apache.shardingsphere.shardingscaling.core.synctask.SyncTask;
import org.apache.shardingsphere.shardingscaling.core.synctask.SyncTaskFactory;
import org.apache.shardingsphere.spi.database.metadata.DataSourceMetaData;

/**
 * Sync task controller, synchronize history data and realtime data.
 */
@Slf4j
public final class SyncTaskController implements Runnable {
    
    private final SyncTask historyDataSyncTaskGroup;
    
    private final SyncTask realtimeDataSyncTask;
    
    private final DataSourceManager dataSourceManager = new DataSourceManager();
    
    private final String syncTaskId;
    
    private SyncTaskControlStatus syncTaskControlStatus;
    
    public SyncTaskController(final SyncConfiguration syncConfiguration) {
        SyncTaskFactory syncTaskFactory = new DefaultSyncTaskFactory();
        syncTaskId = generateSyncTaskId(syncConfiguration.getReaderConfiguration().getDataSourceConfiguration());
        this.historyDataSyncTaskGroup = syncTaskFactory.createHistoryDataSyncTaskGroup(syncConfiguration, dataSourceManager);
        this.realtimeDataSyncTask = syncTaskFactory.createRealtimeDataSyncTask(syncConfiguration, dataSourceManager);
        syncTaskControlStatus = SyncTaskControlStatus.PREPARING;
    }
    
    private String generateSyncTaskId(final DataSourceConfiguration dataSourceConfiguration) {
        DataSourceMetaData dataSourceMetaData = dataSourceConfiguration.getDataSourceMetaData();
        return String.format("%s-%s-%s", dataSourceMetaData.getHostName(), dataSourceMetaData.getPort(),
            null != dataSourceMetaData.getCatalog() ? dataSourceMetaData.getCatalog() : dataSourceMetaData.getSchema());
    }
    
    /**
     * Start synchronize data.
     */
    public void start() {
        new Thread(this).start();
    }
    
    /**
     * Stop synchronize data.
     */
    public void stop() {
        if (!syncTaskControlStatus.isStoppedStatus()) {
            syncTaskControlStatus = SyncTaskControlStatus.STOPPING;
        }
        historyDataSyncTaskGroup.stop();
        realtimeDataSyncTask.stop();
    }
    
    /**
     * Get synchronize progress.
     *
     * @return migrate progress
     */
    public SyncProgress getProgress() {
        SyncTaskProgress result = new SyncTaskProgress(syncTaskId, syncTaskControlStatus.name());
        result.setHistorySyncTaskProgress(historyDataSyncTaskGroup.getProgress());
        result.setRealTimeSyncTaskProgress(realtimeDataSyncTask.getProgress());
        return result;
    }
    
    @Override
    public void run() {
        realtimeDataSyncTask.prepare();
        historyDataSyncTaskGroup.prepare();
        syncTaskControlStatus = SyncTaskControlStatus.MIGRATE_HISTORY_DATA;
        historyDataSyncTaskGroup.start(event -> {
            log.info("history data migrate task {} finished, execute result: {}", event.getTaskId(), event.getEventType().name());
            if (EventType.EXCEPTION_EXIT.equals(event.getEventType())) {
                stop();
                dataSourceManager.close();
                syncTaskControlStatus = SyncTaskControlStatus.MIGRATE_HISTORY_DATA_FAILURE;
            } else {
                executeRealTimeSyncTask();
            }
        });
    }
    
    private void executeRealTimeSyncTask() {
        if (!SyncTaskControlStatus.MIGRATE_HISTORY_DATA.equals(syncTaskControlStatus)) {
            dataSourceManager.close();
            syncTaskControlStatus = SyncTaskControlStatus.STOPPED;
            return;
        }
        realtimeDataSyncTask.start(event -> {
            log.info("realtime data sync task {} finished, execute result: {}", syncTaskId, event.getEventType().name());
            dataSourceManager.close();
            syncTaskControlStatus = EventType.FINISHED.equals(event.getEventType()) ? SyncTaskControlStatus.STOPPED : SyncTaskControlStatus.SYNCHRONIZE_REALTIME_DATA_FAILURE;
        });
        syncTaskControlStatus = SyncTaskControlStatus.SYNCHRONIZE_REALTIME_DATA;
    }
}
