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

import org.apache.shardingsphere.data.pipeline.api.MigrationJobPublicAPI;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.RuleAlteredJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.infra.util.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.util.spi.type.required.RequiredSPI;

import java.util.Map;

/**
 * Rule altered job API.
 */
@SingletonSPI
public interface RuleAlteredJobAPI extends PipelineJobAPI, MigrationJobPublicAPI, RequiredSPI {
    
    @Override
    RuleAlteredJobConfiguration getJobConfiguration(String jobId);
    
    /**
     * Get job progress.
     *
     * @param jobConfig job configuration
     * @return each sharding item progress
     */
    Map<Integer, InventoryIncrementalJobItemProgress> getJobProgress(RuleAlteredJobConfiguration jobConfig);
    
    @Override
    InventoryIncrementalJobItemProgress getJobItemProgress(String jobId, int shardingItem);
    
    /**
     * Stop cluster writing.
     *
     * @param jobConfig job configuration
     */
    void stopClusterWriteDB(RuleAlteredJobConfiguration jobConfig);
    
    /**
     * Restore cluster writing.
     *
     * @param jobConfig job configuration
     */
    void restoreClusterWriteDB(RuleAlteredJobConfiguration jobConfig);
    
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
     * @param jobConfig job configuration
     * @return each logic table check result
     */
    Map<String, DataConsistencyCheckResult> dataConsistencyCheck(RuleAlteredJobConfiguration jobConfig);
    
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
     * @param jobConfig job configuration
     */
    void switchClusterConfiguration(RuleAlteredJobConfiguration jobConfig);
}
