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
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.TaskConfiguration;
import org.apache.shardingsphere.scaling.core.job.position.JobPosition;
import org.apache.shardingsphere.scaling.core.job.task.ScalingTask;
import org.apache.shardingsphere.scaling.core.schedule.JobStatus;
import org.apache.shardingsphere.scaling.core.utils.TaskConfigurationUtil;
import org.apache.shardingsphere.sharding.algorithm.keygen.SnowflakeKeyGenerateAlgorithm;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Scaling job context.
 */
@Getter
@Setter
public final class JobContext {
    
    private static final SnowflakeKeyGenerateAlgorithm ID_AUTO_INCREASE_GENERATOR = initIdAutoIncreaseGenerator();
    
    private long jobId;
    
    private Integer shardingItem;
    
    private String databaseType;
    
    private JobPosition initPosition;
    
    private final transient List<TaskConfiguration> taskConfigs = new LinkedList<>();
    
    private final transient List<ScalingTask> inventoryTasks = new LinkedList<>();
    
    private final transient List<ScalingTask> incrementalTasks = new LinkedList<>();
    
    private transient JobConfiguration jobConfig;
    
    private String status = JobStatus.RUNNING.name();
    
    public JobContext() {
        this(generateKey());
    }
    
    public JobContext(final long jobId) {
        this.jobId = jobId;
    }
    
    public JobContext(final JobConfiguration jobConfig) {
        this(Optional.ofNullable(jobConfig.getHandleConfig().getJobId()).orElse(generateKey()));
        this.jobConfig = jobConfig;
        shardingItem = jobConfig.getHandleConfig().getShardingItem();
        taskConfigs.addAll(TaskConfigurationUtil.toTaskConfigs(jobConfig));
    }
    
    private static SnowflakeKeyGenerateAlgorithm initIdAutoIncreaseGenerator() {
        SnowflakeKeyGenerateAlgorithm result = new SnowflakeKeyGenerateAlgorithm();
        result.init();
        return result;
    }
    
    private static Long generateKey() {
        return (Long) ID_AUTO_INCREASE_GENERATOR.generateKey();
    }
    
    /**
     * Get database type.
     *
     * @return database type
     */
    public String getDatabaseType() {
        if (null == databaseType && !taskConfigs.isEmpty()) {
            databaseType = taskConfigs.get(0).getDumperConfig().getDataSourceConfig().getDatabaseType().getName();
        }
        return databaseType;
    }
}
