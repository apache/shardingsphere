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

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Sync task control status.
 */
@RequiredArgsConstructor
@Getter
public enum SyncTaskControlStatus {
    
    /**
     * Task is in prepare status.
     */
    PREPARING(false),
    
    /**
     * Task is in migrate history data status.
     */
    MIGRATE_HISTORY_DATA(false),
    
    /**
     * Task is in synchronize realtime data status.
     */
    SYNCHRONIZE_REALTIME_DATA(false),
    
    /**
     * Task is stopping.
     */
    STOPPING(false),
    
    /**
     * Task has stopped.
     */
    STOPPED(true),
    
    /**
     * Task has stopped by failing to migrate history data.
     */
    MIGRATE_HISTORY_DATA_FAILURE(true),
    
    /**
     * Task has stopped by failing to synchronize realtime data.
     */
    SYNCHRONIZE_REALTIME_DATA_FAILURE(true);
    
    private final boolean stoppedStatus;
}
