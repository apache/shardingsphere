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

package org.apache.shardingsphere.scaling.core.service;

import org.apache.shardingsphere.scaling.core.check.DataConsistencyCheckResult;
import org.apache.shardingsphere.scaling.core.config.ScalingConfiguration;
import org.apache.shardingsphere.scaling.core.job.ScalingJobProgress;
import org.apache.shardingsphere.scaling.core.job.ShardingScalingJob;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Scaling job service.
 */
public interface ScalingJobService {
    
    /**
     * Get {@code ShardingScalingJob} list.
     *
     * @return scaling job service list
     */
    List<ShardingScalingJob> listJobs();
    
    /**
     * Check new yaml proxy configuration if should scaling.
     *
     * @param oldYamlProxyConfiguration old yaml proxy configuration
     * @param newYamlProxyConfiguration new yaml proxy configuration
     * @return if should scaling
     */
    boolean shouldScaling(String oldYamlProxyConfiguration, String newYamlProxyConfiguration);
    
    /**
     * Start scaling job.
     *
     * @param scalingConfiguration scaling job configuration
     * @return scaling job
     */
    Optional<ShardingScalingJob> start(ScalingConfiguration scalingConfiguration);
    
    
    /**
     * Start scaling job if it should scaling.
     *
     * @param oldYamlProxyConfiguration old yaml proxy configuration
     * @param newYamlProxyConfiguration new yaml proxy configuration
     * @return scaling job
     */
    Optional<ShardingScalingJob> start(String oldYamlProxyConfiguration, String newYamlProxyConfiguration);
    
    /**
     * Stop a job.
     *
     * @param jobId job id
     */
    void stop(long jobId);
    
    /**
     * Get {@code ShardingScalingJob} by id.
     *
     * @param jobId job id
     * @return {@code ShardingScalingJob} instance
     */
    ShardingScalingJob getJob(long jobId);
    
    /**
     * Get job progress.
     *
     * @param jobId job id
     * @return scaling job progress
     */
    ScalingJobProgress getProgress(long jobId);
    
    /**
     * Do data consistency check.
     *
     * @param jobId job id
     * @return data consistency check result
     */
    Map<String, DataConsistencyCheckResult> check(long jobId);
    
    /**
     * Reset target tables.
     *
     * @param jobId job id
     */
    void reset(long jobId);
    
    /**
     * remove job.
     *
     * @param jobId job id
     */
    void remove(long jobId);
}
