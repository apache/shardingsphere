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

package org.apache.shardingsphere.scaling.core.api;

import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEventListener;
import org.apache.shardingsphere.scaling.core.job.JobContext;
import org.apache.shardingsphere.scaling.core.job.progress.JobProgress;

import java.util.List;

/**
 * Governance repository API.
 */
public interface GovernanceRepositoryAPI {
    
    /**
     * persist job progress.
     *
     * @param jobContext job context
     */
    void persistJobProgress(JobContext jobContext);
    
    /**
     * Get job progress.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @return job progress
     */
    JobProgress getJobProgress(long jobId, int shardingItem);
    
    /**
     * Delete job progress.
     *
     * @param jobId job id
     */
    void deleteJobProgress(long jobId);
    
    /**
     * Delete job.
     *
     * @param jobId job id
     */
    void deleteJob(long jobId);
    
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
}
