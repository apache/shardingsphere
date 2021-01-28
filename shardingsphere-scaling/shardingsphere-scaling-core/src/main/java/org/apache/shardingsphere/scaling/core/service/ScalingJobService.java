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

import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.job.JobContext;
import org.apache.shardingsphere.scaling.core.job.JobProgress;
import org.apache.shardingsphere.scaling.core.job.check.DataConsistencyCheckResult;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Scaling job service.
 */
public interface ScalingJobService {
    
    /**
     * Get job context list.
     *
     * @return job list
     */
    List<JobContext> listJobs();
    
    /**
     * Start scaling job.
     *
     * @param jobConfig job configuration
     * @return job context
     */
    Optional<JobContext> start(JobConfiguration jobConfig);
    
    /**
     * Stop job.
     *
     * @param jobId job id
     */
    void stop(long jobId);
    
    /**
     * Get job context by id.
     *
     * @param jobId job id
     * @return job context
     */
    JobContext getJob(long jobId);
    
    /**
     * Get job progress.
     *
     * @param jobId job id
     * @return scaling job progress
     */
    JobProgress getProgress(long jobId);
    
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
     * @throws SQLException SQL exception
     */
    void reset(long jobId) throws SQLException;
    
    /**
     * remove job.
     *
     * @param jobId job id
     */
    void remove(long jobId);
}
