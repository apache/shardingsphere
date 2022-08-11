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

package org.apache.shardingsphere.data.pipeline.api;

import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.RuleAlteredJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.pojo.DataConsistencyCheckAlgorithmInfo;
import org.apache.shardingsphere.data.pipeline.api.pojo.JobInfo;
import org.apache.shardingsphere.infra.util.spi.type.required.RequiredSPI;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Rule altered job API.
 */
public interface RuleAlteredJobAPI extends PipelineJobAPI, RequiredSPI {
    
    /**
     * List all jobs.
     *
     * @return job infos
     */
    List<JobInfo> list();
    
    /**
     * Start scaling job by config.
     *
     * @param jobConfig job config
     * @return job id
     */
    Optional<String> start(RuleAlteredJobConfiguration jobConfig);
    
    /**
     * Get job progress.
     *
     * @param jobId job id
     * @return each sharding item progress
     */
    Map<Integer, InventoryIncrementalJobItemProgress> getJobProgress(String jobId);
    
    /**
     * Get job progress.
     *
     * @param jobConfig job configuration
     * @return each sharding item progress
     */
    Map<Integer, InventoryIncrementalJobItemProgress> getJobProgress(RuleAlteredJobConfiguration jobConfig);
    
    /**
     * Stop cluster writing.
     *
     * @param jobId job id
     */
    void stopClusterWriteDB(String jobId);
    
    /**
     * Stop cluster writing.
     *
     * @param jobConfig job configuration
     */
    void stopClusterWriteDB(RuleAlteredJobConfiguration jobConfig);
    
    /**
     * Restore cluster writing.
     *
     * @param jobId job id
     */
    void restoreClusterWriteDB(String jobId);
    
    /**
     * Restore cluster writing.
     *
     * @param jobConfig job configuration
     */
    void restoreClusterWriteDB(RuleAlteredJobConfiguration jobConfig);
    
    /**
     * List all data consistency check algorithms from SPI.
     *
     * @return data consistency check algorithms
     */
    Collection<DataConsistencyCheckAlgorithmInfo> listDataConsistencyCheckAlgorithms();
    
    /**
     * Is data consistency check needed.
     *
     * @param jobId job id
     * @return data consistency check needed or not
     */
    boolean isDataConsistencyCheckNeeded(String jobId);
    
    /**
     * Is data consistency check needed.
     *
     * @param jobConfig job configuration
     * @return data consistency check needed or not
     */
    boolean isDataConsistencyCheckNeeded(RuleAlteredJobConfiguration jobConfig);
    
    /**
     * Do data consistency check.
     *
     * @param jobId job id
     * @return each logic table check result
     */
    Map<String, DataConsistencyCheckResult> dataConsistencyCheck(String jobId);
    
    /**
     * Do data consistency check.
     *
     * @param jobConfig job configuration
     * @return each logic table check result
     */
    Map<String, DataConsistencyCheckResult> dataConsistencyCheck(RuleAlteredJobConfiguration jobConfig);
    
    /**
     * Do data consistency check.
     *
     * @param jobId job id
     * @param algorithmType algorithm type
     * @param algorithmProps algorithm props. Nullable
     * @return each logic table check result
     */
    Map<String, DataConsistencyCheckResult> dataConsistencyCheck(String jobId, String algorithmType, Properties algorithmProps);
    
    /**
     * Aggregate data consistency check results.
     *
     * @param jobId job id
     * @param checkResults check results
     * @return check success or not
     */
    boolean aggregateDataConsistencyCheckResults(String jobId, Map<String, DataConsistencyCheckResult> checkResults);
    
    /**
     * Switch cluster configuration.
     *
     * @param jobId job id
     */
    void switchClusterConfiguration(String jobId);
    
    /**
     * Switch cluster configuration.
     *
     * @param jobConfig job configuration
     */
    void switchClusterConfiguration(RuleAlteredJobConfiguration jobConfig);
    
    /**
     * Reset scaling job.
     *
     * @param jobId job id
     */
    void reset(String jobId);
    
    /**
     * Get job configuration.
     *
     * @param jobId job id
     * @return job configuration
     */
    RuleAlteredJobConfiguration getJobConfig(String jobId);
    
    /**
     * Persist job item progress.
     *
     * @param jobItemContext job item context
     */
    void persistJobItemProgress(PipelineJobItemContext jobItemContext);
    
    /**
     * Get job item progress.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @return job item progress
     */
    InventoryIncrementalJobItemProgress getJobItemProgress(String jobId, int shardingItem);
    
    /**
     * Update job item status.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @param status status
     */
    void updateJobItemStatus(String jobId, int shardingItem, JobStatus status);
}
