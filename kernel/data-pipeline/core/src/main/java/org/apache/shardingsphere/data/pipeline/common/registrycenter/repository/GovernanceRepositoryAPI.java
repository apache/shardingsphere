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

package org.apache.shardingsphere.data.pipeline.common.registrycenter.repository;

import org.apache.shardingsphere.data.pipeline.common.job.type.JobType;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.DataConsistencyCheckResult;
import org.apache.shardingsphere.mode.repository.cluster.listener.DataChangedEventListener;

import java.util.Collection;
import java.util.List;
import java.util.Map;
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
     * Persist job offset info.
     *
     * @param jobId job id
     * @param jobOffsetInfo job offset info
     */
    void persistJobOffsetInfo(String jobId, String jobOffsetInfo);
    
    /**
     * Get job offset info.
     *
     * @param jobId job id
     * @return job offset info
     */
    Optional<String> getJobOffsetInfo(String jobId);
    
    /**
     * Persist job item progress.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @param progressValue progress value
     */
    void persistJobItemProgress(String jobId, int shardingItem, String progressValue);
    
    /**
     * Get job item progress.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @return job item progress
     */
    Optional<String> getJobItemProgress(String jobId, int shardingItem);
    
    /**
     * Get latest check job id.
     *
     * @param parentJobId parent job id
     * @return check job id
     */
    Optional<String> getLatestCheckJobId(String parentJobId);
    
    /**
     * Persist latest check job id.
     *
     * @param parentJobId job id
     * @param checkJobId check job id
     */
    void persistLatestCheckJobId(String parentJobId, String checkJobId);
    
    /**
     * Delete latest check job id.
     *
     * @param parentJobId parent job id
     */
    void deleteLatestCheckJobId(String parentJobId);
    
    /**
     * Get check job result.
     *
     * @param parentJobId parent job id
     * @param checkJobId check job id
     * @return check job result
     */
    Map<String, DataConsistencyCheckResult> getCheckJobResult(String parentJobId, String checkJobId);
    
    /**
     * Persist check job result.
     *
     * @param parentJobId parent job id
     * @param checkJobId check job id
     * @param checkResultMap check result map
     */
    void persistCheckJobResult(String parentJobId, String checkJobId, Map<String, DataConsistencyCheckResult> checkResultMap);
    
    /**
     * Delete check job result.
     *
     * @param parentJobId parent job id
     * @param checkJobId check job id
     */
    void deleteCheckJobResult(String parentJobId, String checkJobId);
    
    /**
     * List check job ids.
     *
     * @param parentJobId parent job id
     * @return check job ids
     */
    Collection<String> listCheckJobIds(String parentJobId);
    
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
     * Get meta data data sources.
     *
     * @param jobType job type
     * @return data source properties
     */
    String getMetaDataDataSources(JobType jobType);
    
    /**
     * Persist meta data data sources.
     *
     * @param jobType job type
     * @param metaDataDataSources data source properties
     */
    void persistMetaDataDataSources(JobType jobType, String metaDataDataSources);
    
    /**
     * Get meta data process configuration.
     *
     * @param jobType job type, nullable
     * @return process configuration YAML text
     */
    String getMetaDataProcessConfiguration(JobType jobType);
    
    /**
     * Persist meta data process configuration.
     *
     * @param jobType job type, nullable
     * @param processConfigYamlText process configuration YAML text
     */
    void persistMetaDataProcessConfiguration(JobType jobType, String processConfigYamlText);
    
    /**
     * Get job item error msg.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @return error msg
     */
    String getJobItemErrorMessage(String jobId, int shardingItem);
    
    /**
     * Clean job item error message.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     */
    void cleanJobItemErrorMessage(String jobId, int shardingItem);
}
