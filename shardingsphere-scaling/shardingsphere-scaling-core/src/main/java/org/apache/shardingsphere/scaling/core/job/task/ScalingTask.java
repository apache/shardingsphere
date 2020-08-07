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

package org.apache.shardingsphere.scaling.core.job.task;

import org.apache.shardingsphere.scaling.core.execute.executor.ShardingScalingExecutor;
import org.apache.shardingsphere.scaling.core.job.SyncProgress;
import org.apache.shardingsphere.scaling.core.job.position.Position;
import org.apache.shardingsphere.scaling.core.job.position.PositionManager;

/**
 * Sync task interface.
 */
public interface ScalingTask<T extends Position> extends ShardingScalingExecutor {
    
    /**
     * Get synchronize progress.
     *
     * @return migrate progress
     */
    SyncProgress getProgress();
    
    /**
     * Get position manager.
     *
     * @return position manager
     */
    PositionManager<T> getPositionManager();
    
    /**
     * Get task id.
     *
     * @return task id
     */
    String getTaskId();
}
