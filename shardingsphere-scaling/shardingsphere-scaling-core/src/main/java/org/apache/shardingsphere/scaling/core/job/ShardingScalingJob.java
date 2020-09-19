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

package org.apache.shardingsphere.scaling.core.job;

import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.scaling.core.config.ScalingConfiguration;
import org.apache.shardingsphere.scaling.core.config.SyncConfiguration;
import org.apache.shardingsphere.scaling.core.job.position.IncrementalPosition;
import org.apache.shardingsphere.scaling.core.job.position.InventoryPosition;
import org.apache.shardingsphere.scaling.core.job.task.ScalingTask;
import org.apache.shardingsphere.scaling.core.schedule.SyncTaskControlStatus;
import org.apache.shardingsphere.scaling.core.check.DataConsistencyChecker;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Sharding scaling out job.
 */
@Getter
@Setter
public final class ShardingScalingJob {
    
    private static final AtomicInteger ID_AUTO_INCREASE_GENERATOR = new AtomicInteger();
    
    private final int jobId = ID_AUTO_INCREASE_GENERATOR.incrementAndGet();
    
    private final transient List<SyncConfiguration> syncConfigurations = new LinkedList<>();
    
    private final transient List<ScalingTask<InventoryPosition>> inventoryDataTasks = new LinkedList<>();
    
    private final transient List<ScalingTask<IncrementalPosition>> incrementalDataTasks = new LinkedList<>();
    
    private final transient ScalingConfiguration scalingConfiguration;
    
    private transient DataConsistencyChecker dataConsistencyChecker;
    
    private String jobName = "ScalingJob";
    
    private int shardingItem;
    
    private String status = SyncTaskControlStatus.RUNNING.name();
    
    public ShardingScalingJob(final ScalingConfiguration scalingConfiguration) {
        this.scalingConfiguration = scalingConfiguration;
        jobName = Optional.ofNullable(scalingConfiguration.getJobConfiguration().getJobName()).orElse(jobName);
        shardingItem = scalingConfiguration.getJobConfiguration().getShardingItem();
    }
}
