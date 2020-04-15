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

package org.apache.shardingsphere.shardingscaling.core.preparer;

import org.apache.shardingsphere.shardingscaling.core.ShardingScalingJob;
import org.apache.shardingsphere.shardingscaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.shardingscaling.core.controller.task.SyncTaskControlStatus;
import org.apache.shardingsphere.shardingscaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.shardingscaling.core.exception.DatasourceCheckFailedException;
import org.apache.shardingsphere.shardingscaling.core.preparer.checker.DataSourceChecker;
import org.apache.shardingsphere.shardingscaling.core.preparer.checker.DataSourceCheckerCheckerFactory;
import org.apache.shardingsphere.shardingscaling.core.preparer.splitter.InventoryDataTaskSplitter;
import org.apache.shardingsphere.shardingscaling.core.synctask.inventory.InventoryDataSyncTaskGroup;

import lombok.extern.slf4j.Slf4j;

/**
 * Sharding scaling job preparer.
 */
@Slf4j
public final class ShardingScalingJobPreparer {
    
    private final InventoryDataTaskSplitter inventoryDataTaskSplitter = new InventoryDataTaskSplitter();
    
    /**
     * Do prepare work for sharding scaling job.
     *
     * @param shardingScalingJob sharding scaling job
     */
    public void prepare(final ShardingScalingJob shardingScalingJob) {
        String databaseType = shardingScalingJob.getSyncConfigurations().get(0).getDumperConfiguration().getDataSourceConfiguration().getDatabaseType().getName();
        try (DataSourceManager dataSourceManager = new DataSourceManager(shardingScalingJob.getSyncConfigurations())) {
            checkDatasources(databaseType, dataSourceManager);
            splitTasks(shardingScalingJob, dataSourceManager);
        } catch (DatasourceCheckFailedException ex) {
            log.warn("Preparing sharding scaling job {} : {} failed", shardingScalingJob.getJobId(), shardingScalingJob.getJobName(), ex);
            shardingScalingJob.setStatus(SyncTaskControlStatus.PREPARING_FAILURE.name());
        }
    }
    
    private void checkDatasources(final String databaseType, final DataSourceManager dataSourceManager) {
        DataSourceChecker dataSourceChecker = DataSourceCheckerCheckerFactory.newInstanceDataSourceChecker(databaseType);
        dataSourceChecker.checkConnection(dataSourceManager.getCachedDataSources().values());
        dataSourceChecker.checkPrivilege(dataSourceManager.getSourceDatasources().values());
    }
    
    private void splitTasks(final ShardingScalingJob shardingScalingJob, final DataSourceManager dataSourceManager) {
        for (SyncConfiguration each : shardingScalingJob.getSyncConfigurations()) {
            shardingScalingJob.getInventoryDataTasks().add(new InventoryDataSyncTaskGroup(each, inventoryDataTaskSplitter.splitInventoryData(each, dataSourceManager)));
        }
    }
}
