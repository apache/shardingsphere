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

import org.apache.shardingsphere.shardingscaling.core.controller.SyncProgressGroup;
import org.apache.shardingsphere.shardingscaling.core.controller.SyncProgress;

import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Sync task progress.
 */
@Getter
@Setter
@RequiredArgsConstructor
public final class SyncTaskProgress implements SyncProgress {
    
    private final String id;
    
    private final String status;
    
    private final List<SyncProgress> historySyncTaskProgress = new LinkedList<>();
    
    private SyncProgress realTimeSyncTaskProgress;
    
    /**
     * Set history sync task progress.
     *
     * @param historySyncTaskProgress history sync task progress
     */
    public void setHistorySyncTaskProgress(final SyncProgress historySyncTaskProgress) {
        if (historySyncTaskProgress instanceof SyncProgressGroup) {
            this.historySyncTaskProgress.addAll(((SyncProgressGroup) historySyncTaskProgress).getSyncProgresses());
        } else {
            this.historySyncTaskProgress.add(historySyncTaskProgress);
        }
    }
}
