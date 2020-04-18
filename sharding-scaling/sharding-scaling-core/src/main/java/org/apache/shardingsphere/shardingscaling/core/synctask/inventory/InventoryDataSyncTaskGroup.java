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

package org.apache.shardingsphere.shardingscaling.core.synctask.inventory;

import org.apache.shardingsphere.shardingscaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.shardingscaling.core.controller.task.ReportCallback;
import org.apache.shardingsphere.shardingscaling.core.controller.SyncProgress;
import org.apache.shardingsphere.shardingscaling.core.exception.SyncTaskExecuteException;
import org.apache.shardingsphere.shardingscaling.core.execute.Event;
import org.apache.shardingsphere.shardingscaling.core.execute.EventType;
import org.apache.shardingsphere.shardingscaling.core.synctask.SyncTask;
import org.apache.shardingsphere.underlying.common.database.metadata.DataSourceMetaData;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Inventory data sync task group.
 */
@Slf4j
public final class InventoryDataSyncTaskGroup implements SyncTask {
    
    private final Collection<SyncTask> syncTasks;
    
    private final String syncTaskId;
    
    private final Queue<SyncTask> submitFailureTasks = new LinkedList<>();
    
    public InventoryDataSyncTaskGroup(final SyncConfiguration syncConfiguration, final Collection<SyncTask> inventoryDataSyncTasks) {
        DataSourceMetaData dataSourceMetaData = syncConfiguration.getDumperConfiguration().getDataSourceConfiguration().getDataSourceMetaData();
        syncTaskId = String.format("InventoryGroup-%s", null != dataSourceMetaData.getCatalog() ? dataSourceMetaData.getCatalog() : dataSourceMetaData.getSchema());
        syncTasks = inventoryDataSyncTasks;
    }
    
    @Override
    public void start(final ReportCallback callback) {
        final AtomicInteger finishedTask = new AtomicInteger();
        for (final SyncTask each : syncTasks) {
            try {
                each.start(new ReportCallback() {
        
                    @Override
                    public void report(final Event event) {
                        if (EventType.FINISHED == event.getEventType()) {
                            finishedTask.incrementAndGet();
                        } else {
                            callback.report(new Event(syncTaskId, EventType.EXCEPTION_EXIT));
                        }
                        if (syncTasks.size() == finishedTask.get() && submitFailureTasks.isEmpty()) {
                            callback.report(new Event(syncTaskId, EventType.FINISHED));
                        } else if (!submitFailureTasks.isEmpty()) {
                            submitFailureTasks.peek().start(this);
                            submitFailureTasks.poll();
                        }
                    }
                });
            } catch (RejectedExecutionException ex) {
                submitFailureTasks.offer(each);
            } catch (SyncTaskExecuteException syncTaskEx) {
                stop();
                callback.report(new Event(syncTaskId, EventType.EXCEPTION_EXIT));
                break;
            }
        }
    }
    
    @Override
    public void stop() {
        for (SyncTask each : syncTasks) {
            each.stop();
        }
    }
    
    @Override
    public SyncProgress getProgress() {
        InventoryDataSyncTaskProgressGroup result = new InventoryDataSyncTaskProgressGroup();
        for (SyncTask each : syncTasks) {
            result.addSyncProgress(each.getProgress());
        }
        return result;
    }
}
