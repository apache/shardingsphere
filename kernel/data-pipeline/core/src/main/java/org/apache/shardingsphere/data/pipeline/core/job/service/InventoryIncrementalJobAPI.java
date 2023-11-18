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
import org.apache.shardingsphere.data.pipeline.common.config.job.yaml.YamlPipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.common.context.InventoryIncrementalProcessContext;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.common.job.progress.yaml.YamlInventoryIncrementalJobItemProgressSwapper;
import org.apache.shardingsphere.data.pipeline.common.pojo.InventoryIncrementalJobItemInfo;
import org.apache.shardingsphere.data.pipeline.common.pojo.PipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.PipelineDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.task.config.PipelineTaskConfiguration;

import java.sql.SQLException;
import java.util.List;

/**
 * Inventory incremental job API.
 */
public interface InventoryIncrementalJobAPI extends PipelineJobAPI {
    
    @SuppressWarnings("unchecked")
    @Override
    default YamlInventoryIncrementalJobItemProgressSwapper getYamlJobItemProgressSwapper() {
        return new YamlInventoryIncrementalJobItemProgressSwapper();
    }
    
    /**
     * Get pipeline job info.
     *
     * @param jobId job ID
     * @return pipeline job info
     */
    PipelineJobInfo getJobInfo(String jobId);
    
    /**
     * Build task configuration.
     *
     * @param pipelineJobConfig pipeline job configuration
     * @param jobShardingItem job sharding item
     * @param pipelineProcessConfig pipeline process configuration
     * @return task configuration
     */
    PipelineTaskConfiguration buildTaskConfiguration(PipelineJobConfiguration pipelineJobConfig, int jobShardingItem, PipelineProcessConfiguration pipelineProcessConfig);
    
    /**
     * Build pipeline process context.
     *
     * @param pipelineJobConfig pipeline job configuration
     * @return pipeline process context
     */
    InventoryIncrementalProcessContext buildPipelineProcessContext(PipelineJobConfiguration pipelineJobConfig);
    
    /**
     * Extend YAML job configuration.
     *
     * @param contextKey context key
     * @param yamlJobConfig YAML job configuration
     */
    void extendYamlJobConfiguration(PipelineContextKey contextKey, YamlPipelineJobConfiguration yamlJobConfig);
    
    /**
     * Get job infos.
     *
     * @param jobId job ID
     * @return job item infos
     */
    List<InventoryIncrementalJobItemInfo> getJobItemInfos(String jobId);
    
    /**
     * Build pipeline data consistency checker.
     *
     * @param pipelineJobConfig job configuration
     * @param processContext process context
     * @param progressContext consistency check job item progress context
     * @return all logic tables check result
     */
    PipelineDataConsistencyChecker buildPipelineDataConsistencyChecker(PipelineJobConfiguration pipelineJobConfig, InventoryIncrementalProcessContext processContext,
                                                                       ConsistencyCheckJobItemProgressContext progressContext);
    
    /**
     * Commit pipeline job.
     *
     * @param jobId job ID
     * @throws SQLException sql exception
     */
    void commit(String jobId) throws SQLException;
    
    /**
     * Rollback pipeline job.
     *
     * @param jobId job ID
     * @throws SQLException when rollback underlying database data
     */
    void rollback(String jobId) throws SQLException;
}
