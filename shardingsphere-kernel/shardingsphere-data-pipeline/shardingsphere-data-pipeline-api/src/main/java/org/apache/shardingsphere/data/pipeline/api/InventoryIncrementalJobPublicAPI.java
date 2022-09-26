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
import org.apache.shardingsphere.data.pipeline.api.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.pojo.DataConsistencyCheckAlgorithmInfo;
import org.apache.shardingsphere.infra.util.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPI;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * Inventory incremental job public API.
 */
@SingletonSPI
public interface InventoryIncrementalJobPublicAPI extends PipelineJobPublicAPI, TypedSPI {
    
    /**
     * Create process configuration.
     *
     * @param processConfig process configuration
     */
    void createProcessConfiguration(PipelineProcessConfiguration processConfig);
    
    /**
     * Alter process configuration.
     *
     * @param processConfig process configuration
     */
    void alterProcessConfiguration(PipelineProcessConfiguration processConfig);
    
    /**
     * Drop process configuration.
     *
     * @param confPath configuration path. e.g. <code>/</code>, <code>/READ</code>, <code>/READ/RATE_LIMITER</code>
     */
    void dropProcessConfiguration(String confPath);
    
    /**
     * Show process configuration.
     *
     * @return process configuration, non-null
     */
    PipelineProcessConfiguration showProcessConfiguration();
    
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
    
    /**
     * Get job progress.
     *
     * @param jobId job id
     * @return each sharding item progress
     */
    Map<Integer, InventoryIncrementalJobItemProgress> getJobProgress(String jobId);
    
    /**
     * List all data consistency check algorithms from SPI.
     *
     * @return data consistency check algorithms
     */
    Collection<DataConsistencyCheckAlgorithmInfo> listDataConsistencyCheckAlgorithms();
    
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
     * @param jobId job id
     * @param algorithmType algorithm type
     * @param algorithmProps algorithm props. Nullable
     * @return each logic table check result
     */
    Map<String, DataConsistencyCheckResult> dataConsistencyCheck(String jobId, String algorithmType, Properties algorithmProps);
}
