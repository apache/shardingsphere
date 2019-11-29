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

package org.apache.shardingsphere.shardingscaling.core.controller;

import org.apache.shardingsphere.shardingscaling.core.ShardingScalingJob;
import org.apache.shardingsphere.shardingscaling.core.config.SyncConfiguration;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Scaling job controller.
 *
 * @author avalon566
 */
public class ScalingJobController {
    
    private final ConcurrentMap<String, List<SyncTaskController>> syncTaskControllerMaps = new ConcurrentHashMap<>();

    /**
     * Start data nodes migrate.
     *
     * @param shardingScalingJob sharding scaling job
     */
    public void start(final ShardingScalingJob shardingScalingJob) {
        List<SyncTaskController> syncTaskControllers = new LinkedList<>();
        for (SyncConfiguration syncConfiguration : shardingScalingJob.getSyncConfigurations()) {
            SyncTaskController syncTaskController = new SyncTaskController(syncConfiguration);
            syncTaskController.start();
            syncTaskControllers.add(syncTaskController);
        }
        syncTaskControllerMaps.put(shardingScalingJob.getJobId(), syncTaskControllers);
    }

    /**
     * Stop data nodes migrate.
     *
     * @param shardingScalingJobId sharding scaling job id
     */
    public void stop(final String shardingScalingJobId) {
        if (!syncTaskControllerMaps.containsKey(shardingScalingJobId)) {
            return;
        }
        for (SyncTaskController syncTaskController : syncTaskControllerMaps.get(shardingScalingJobId)) {
            syncTaskController.stop();
        }
    }

    /**
     * Get data nodes migrate progresses.
     *
     * @param shardingScalingJobId sharding scaling job id
     * @return data nodes migrate progress
     */
    public List<SyncProgress> getProgresses(final String shardingScalingJobId) {
        List<SyncProgress> result = new LinkedList<>();
        if (syncTaskControllerMaps.containsKey(shardingScalingJobId)) {
            for (SyncTaskController syncTaskController : syncTaskControllerMaps.get(shardingScalingJobId)) {
                result.add(syncTaskController.getProgress());
            }
        }
        return result;
    }
}
