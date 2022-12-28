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

import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.pojo.DataConsistencyCheckAlgorithmInfo;
import org.apache.shardingsphere.data.pipeline.api.pojo.InventoryIncrementalJobItemInfo;
import org.apache.shardingsphere.data.pipeline.core.check.consistency.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCalculateAlgorithm;

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
     * @param processConfig process configuration
     */
    void alterProcessConfiguration(PipelineProcessConfiguration processConfig);
    
    /**
     * Show process configuration.
     *
     * @return process configuration, non-null
     */
    PipelineProcessConfiguration showProcessConfiguration();
    
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
     * @param jobId job id
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
     * @param jobConfig job configuration
     * @param algorithmType algorithm type
     * @param algorithmProps algorithm properties
     * @return calculate algorithm
     */
    DataConsistencyCalculateAlgorithm buildDataConsistencyCalculateAlgorithm(PipelineJobConfiguration jobConfig, String algorithmType, Properties algorithmProps);
    
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
     * @param jobId job id
     * @param checkResults check results
     * @return check success or not
     */
    boolean aggregateDataConsistencyCheckResults(String jobId, Map<String, DataConsistencyCheckResult> checkResults);
    
    /**
     * Rollback pipeline job.
     *
     * @param jobId job id
     * @throws SQLException when rollback underlying database data
     */
    void rollback(String jobId) throws SQLException;
    
    /**
     * Commit pipeline job.
     *
     * @param jobId job id
     */
    void commit(String jobId);
}
