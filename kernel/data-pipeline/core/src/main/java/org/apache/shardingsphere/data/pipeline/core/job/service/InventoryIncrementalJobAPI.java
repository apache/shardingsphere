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

package org.apache.shardingsphere.data.pipeline.core.job.service;

import org.apache.shardingsphere.data.pipeline.common.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.common.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.common.job.progress.JobOffsetInfo;
import org.apache.shardingsphere.data.pipeline.common.pojo.DataConsistencyCheckAlgorithmInfo;
import org.apache.shardingsphere.data.pipeline.common.pojo.InventoryIncrementalJobItemInfo;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.algorithm.DataConsistencyCalculateAlgorithm;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.DataConsistencyCheckResult;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

/**
 * Inventory incremental job API.
 */
public interface InventoryIncrementalJobAPI extends PipelineJobAPI {
    
    /**
     * Alter process configuration.
     *
     * @param contextKey context key
     * @param processConfig process configuration
     */
    void alterProcessConfiguration(PipelineContextKey contextKey, PipelineProcessConfiguration processConfig);
    
    /**
     * Show process configuration.
     *
     * @param contextKey context key
     * @return process configuration, non-null
     */
    PipelineProcessConfiguration showProcessConfiguration(PipelineContextKey contextKey);
    
    /**
     * Persist job offset info.
     *
     * @param jobId job ID
     * @param jobOffsetInfo job offset info
     */
    void persistJobOffsetInfo(String jobId, JobOffsetInfo jobOffsetInfo);
    
    /**
     * Get job offset info.
     *
     * @param jobId job ID
     * @return job offset progress
     */
    JobOffsetInfo getJobOffsetInfo(String jobId);
    
    /**
     * Get job progress.
     *
     * @param pipelineJobConfig job configuration
     * @return each sharding item progress
     */
    Map<Integer, InventoryIncrementalJobItemProgress> getJobProgress(PipelineJobConfiguration pipelineJobConfig);
    
    @Override
    Optional<InventoryIncrementalJobItemProgress> getJobItemProgress(String jobId, int shardingItem);
    
    /**
     * Get job infos.
     *
     * @param jobId job ID
     * @return job item infos
     */
    List<InventoryIncrementalJobItemInfo> getJobItemInfos(String jobId);
    
    /**
     * List all data consistency check algorithms from SPI.
     *
     * @return data consistency check algorithms
     */
    Collection<DataConsistencyCheckAlgorithmInfo> listDataConsistencyCheckAlgorithms();
    
    /**
     * Build data consistency calculate algorithm.
     *
     * @param algorithmType algorithm type
     * @param algorithmProps algorithm properties
     * @return calculate algorithm
     */
    DataConsistencyCalculateAlgorithm buildDataConsistencyCalculateAlgorithm(String algorithmType, Properties algorithmProps);
    
    /**
     * Do data consistency check.
     *
     * @param pipelineJobConfig job configuration
     * @param calculateAlgorithm calculate algorithm
     * @param progressContext consistency check job item progress context
     * @return each logic table check result
     */
    Map<String, DataConsistencyCheckResult> dataConsistencyCheck(PipelineJobConfiguration pipelineJobConfig, DataConsistencyCalculateAlgorithm calculateAlgorithm,
                                                                 ConsistencyCheckJobItemProgressContext progressContext);
    
    /**
     * Aggregate data consistency check results.
     *
     * @param jobId job ID
     * @param checkResults check results
     * @return check success or not
     */
    boolean aggregateDataConsistencyCheckResults(String jobId, Map<String, DataConsistencyCheckResult> checkResults);
    
    /**
     * Commit pipeline job.
     *
     * @param jobId job ID
     */
    void commit(String jobId);
    
    /**
     * Rollback pipeline job.
     *
     * @param jobId job ID
     * @throws SQLException when rollback underlying database data
     */
    void rollback(String jobId) throws SQLException;
}
