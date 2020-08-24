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

package org.apache.shardingsphere.scaling.core.job.task.inventory;

import lombok.Getter;
import org.apache.shardingsphere.scaling.core.job.SyncProgress;

import java.util.LinkedList;
import java.util.List;

/**
 * Inventory data sync task group progress.
 */
@Getter
public final class InventoryDataSyncTaskProgressGroup implements SyncProgress {
    
    private final List<SyncProgress> innerTaskProgresses = new LinkedList<>();
    
    /**
     * Add sync progress to group.
     *
     * @param syncProgress sync progress
     */
    public void addSyncProgress(final SyncProgress syncProgress) {
        innerTaskProgresses.add(syncProgress);
    }
}
