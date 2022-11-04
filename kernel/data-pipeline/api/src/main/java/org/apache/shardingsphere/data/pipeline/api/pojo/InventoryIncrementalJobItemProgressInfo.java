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

package org.apache.shardingsphere.data.pipeline.api.pojo;

import lombok.Getter;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;

@Getter
public final class InventoryIncrementalJobItemProgressInfo {
    
    private final int shardingItem;
    
    private final String errorMessage;
    
    private final long startTimeMillis;
    
    private final JobStatus status;
    
    private final String sourceDatabaseType;
    
    private final String dataSourceName;
    
    private final boolean active;
    
    private final int inventoryFinishedPercentage;
    
    private final long incrementalLatestActiveTimeMillis;
    
    private final long processedRecordsCount;
    
    private final long inventoryRecordsCount;
    
    public InventoryIncrementalJobItemProgressInfo(final int shardingItem, final String errorMessage, final long startTimeMills, 
                                                   final InventoryIncrementalJobItemProgress progress) {
        this.shardingItem = shardingItem;
        this.errorMessage = errorMessage;
        this.startTimeMillis = startTimeMills;
        this.status = progress.getStatus();
        this.sourceDatabaseType = progress.getSourceDatabaseType();
        this.dataSourceName = progress.getDataSourceName();
        this.active = progress.isActive();
        this.inventoryFinishedPercentage = progress.getInventory().getInventoryFinishedPercentage();
        this.incrementalLatestActiveTimeMillis = progress.getIncremental().getIncrementalLatestActiveTimeMillis();
        this.processedRecordsCount = progress.getProcessedRecordsCount();
        this.inventoryRecordsCount = progress.getInventoryRecordsCount();
    }
}
