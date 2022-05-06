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

package org.apache.shardingsphere.data.pipeline.core.api;

import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobContext;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEventListener;

import java.util.List;
import java.util.Optional;

/**
 * Governance repository API.
 */
public interface GovernanceRepositoryAPI {
    
    /**
     * Whether key existing or not.
     *
     * @param key registry center key
     * @return true if job exists, else false
     */
    boolean isExisted(String key);
    
    /**
     * Persist job progress.
     *
     * @param jobContext job context
     */
    void persistJobProgress(RuleAlteredJobContext jobContext);
    
    /**
     * Get job progress.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @return job progress
     */
    JobProgress getJobProgress(String jobId, int shardingItem);
    
    /**
     * Persist job check result.
     *
     * @param jobId job id
     * @param checkSuccess check success
     */
    void persistJobCheckResult(String jobId, boolean checkSuccess);
    
    /**
     * Get job check result.
     *
     * @param jobId job id
     * @return job check result
     */
    Optional<Boolean> getJobCheckResult(String jobId);
    
    /**
     * Delete job.
     *
     * @param jobId job id
     */
    void deleteJob(String jobId);
    
    /**
     * Get node's sub-nodes list.
     *
     * @param key key of data
     * @return sub-nodes name list
     */
    List<String> getChildrenKeys(String key);
    
    /**
     * Watch key or path of governance server.
     *
     * @param key key of data
     * @param listener data changed event listener
     */
    void watch(String key, DataChangedEventListener listener);
    
    /**
     * Persist data.
     *
     * @param key key of data
     * @param value value of data
     */
    void persist(String key, String value);
    
    /**
     * Get sharding items of job.
     *
     * @param jobId job id
     * @return sharding items
     */
    List<Integer> getShardingItems(String jobId);
    
    /**
     * Update sharding job status.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @param status status
     */
    void updateShardingJobStatus(String jobId, int shardingItem, JobStatus status);
}
