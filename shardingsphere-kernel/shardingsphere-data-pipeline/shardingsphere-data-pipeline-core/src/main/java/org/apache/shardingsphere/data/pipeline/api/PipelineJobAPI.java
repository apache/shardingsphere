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
import org.apache.shardingsphere.spi.required.RequiredSPI;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Pipeline job API.
 */
public interface PipelineJobAPI extends RequiredSPI {
    
    /**
     * List all jobs.
     *
     * @return job infos
     */
    List<JobInfo> list();
    
    /**
     * Get uncompleted job ids of schema.
     *
     * @param schemaName schema name
     * @return uncompleted job ids
     */
    List<Long> getUncompletedJobIds(String schemaName);
    
    /**
     * Start scaling job by id.
     *
     * @param jobId job id
     */
    void start(long jobId);
    
    /**
     * Start scaling job by config.
     *
     * @param jobConfig job config
     * @return job id
     */
    Optional<Long> start(JobConfiguration jobConfig);
    
    /**
     * Stop scaling job.
     *
     * @param jobId job id
     */
    void stop(long jobId);
    
    /**
     * Remove scaling job.
     *
     * @param jobId job id
     */
    void remove(long jobId);
    
    /**
     * Get job progress.
     *
     * @param jobId job id
     * @return each sharding item progress
     */
    Map<Integer, JobProgress> getProgress(long jobId);
    
    /**
     * Stop cluster write to job source schema's underlying DB.
     *
     * @param jobId job id
     */
    void stopClusterWriteDB(long jobId);
    
    /**
     * List all data consistency check algorithms from SPI.
     *
     * @return data consistency check algorithms
     */
    Collection<DataConsistencyCheckAlgorithmInfo> listDataConsistencyCheckAlgorithms();
    
    /**
     * Is data consistency check needed.
     *
     * @return data consistency check needed or not
     */
    boolean isDataConsistencyCheckNeeded();
    
    /**
     * Do data consistency check.
     *
     * @param jobId job id
     * @return each logic table check result
     */
    Map<String, DataConsistencyCheckResult> dataConsistencyCheck(long jobId);
    
    /**
     * Do data consistency check.
     *
     * @param jobId job id
     * @param algorithmType algorithm type
     * @return each logic table check result
     */
    Map<String, DataConsistencyCheckResult> dataConsistencyCheck(long jobId, String algorithmType);
    
    /**
     * Aggregate data consistency check results.
     *
     * @param jobId job id
     * @param checkResultMap check result map
     * @return check success or not
     */
    boolean aggregateDataConsistencyCheckResults(long jobId, Map<String, DataConsistencyCheckResult> checkResultMap);
    
    /**
     * Switch job source schema's configuration to job target configuration.
     *
     * @param jobId job id
     */
    void switchClusterConfiguration(long jobId);
    
    /**
     * Reset scaling job.
     *
     * @param jobId job id
     * @throws SQLException SQL exception
     */
    void reset(long jobId) throws SQLException;
    
    /**
     * Get job configuration.
     *
     * @param jobId job id
     * @return job configuration
     */
    JobConfiguration getJobConfig(long jobId);
}
