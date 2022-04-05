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
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.JobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.api.pojo.DataConsistencyCheckAlgorithmInfo;
import org.apache.shardingsphere.data.pipeline.api.pojo.JobInfo;
import org.apache.shardingsphere.spi.type.required.RequiredSPI;
import org.apache.shardingsphere.spi.type.singleton.SingletonSPI;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Rule altered job API.
 */
public interface RuleAlteredJobAPI extends PipelineJobAPI, RequiredSPI, SingletonSPI {
    
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
    Optional<String> start(JobConfiguration jobConfig);
    
    /**
     * Get job progress.
     *
     * @param jobId job id
     * @return each sharding item progress
     */
    Map<Integer, JobProgress> getProgress(String jobId);
    
    /**
     * Get job progress.
     *
     * @param jobConfig job configuration
     * @return each sharding item progress
     */
    Map<Integer, JobProgress> getProgress(JobConfiguration jobConfig);
    
    /**
     * Stop cluster write to job source schema's underlying DB.
     *
     * @param jobId job id
     */
    void stopClusterWriteDB(String jobId);
    
    /**
     * Stop cluster write to job source schema's underlying DB.
     *
     * @param schemaName schema name
     * @param jobId job id
     */
    void stopClusterWriteDB(String schemaName, String jobId);
    
    /**
     * Restore cluster write to job source schema's underlying DB.
     *
     * @param jobId job id
     */
    void restoreClusterWriteDB(String jobId);
    
    /**
     * Restore cluster write to job source schema's underlying DB.
     *
     * @param schemaName schema name
     * @param jobId job id
     */
    void restoreClusterWriteDB(String schemaName, String jobId);
    
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
    boolean isDataConsistencyCheckNeeded(JobConfiguration jobConfig);
    
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
    Map<String, DataConsistencyCheckResult> dataConsistencyCheck(JobConfiguration jobConfig);
    
    /**
     * Do data consistency check.
     *
     * @param jobId job id
     * @param algorithmType algorithm type
     * @return each logic table check result
     */
    Map<String, DataConsistencyCheckResult> dataConsistencyCheck(String jobId, String algorithmType);
    
    /**
     * Aggregate data consistency check results.
     *
     * @param jobId job id
     * @param checkResultMap check result map
     * @return check success or not
     */
    boolean aggregateDataConsistencyCheckResults(String jobId, Map<String, DataConsistencyCheckResult> checkResultMap);
    
    /**
     * Switch job source schema's configuration to job target configuration.
     *
     * @param jobId job id
     */
    void switchClusterConfiguration(String jobId);
    
    /**
     * Switch job source schema's configuration to job target configuration.
     *
     * @param jobConfig job configuration
     */
    void switchClusterConfiguration(JobConfiguration jobConfig);
    
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
    JobConfiguration getJobConfig(String jobId);
}
