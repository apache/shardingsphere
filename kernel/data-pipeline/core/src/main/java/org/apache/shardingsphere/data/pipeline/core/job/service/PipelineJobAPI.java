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

import org.apache.shardingsphere.data.pipeline.common.config.PipelineTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.job.yaml.YamlPipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineProcessContext;
import org.apache.shardingsphere.data.pipeline.common.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.common.job.PipelineJobId;
import org.apache.shardingsphere.data.pipeline.common.job.progress.PipelineJobItemProgress;
import org.apache.shardingsphere.data.pipeline.common.job.type.JobType;
import org.apache.shardingsphere.data.pipeline.common.pojo.PipelineJobInfo;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;

import java.util.List;
import java.util.Optional;

/**
 * Pipeline job API.
 */
@SingletonSPI
public interface PipelineJobAPI extends TypedSPI {
    
    /**
     * Get job type.
     *
     * @return job type
     */
    JobType getJobType();
    
    /**
     * Marshal pipeline job id.
     *
     * @param pipelineJobId pipeline job id
     * @return marshaled text
     */
    String marshalJobId(PipelineJobId pipelineJobId);
    
    /**
     * Extend YAML job configuration.
     *
     * @param contextKey context key
     * @param yamlJobConfig YAML job configuration
     */
    void extendYamlJobConfiguration(PipelineContextKey contextKey, YamlPipelineJobConfiguration yamlJobConfig);
    
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
    PipelineProcessContext buildPipelineProcessContext(PipelineJobConfiguration pipelineJobConfig);
    
    /**
     * Start job.
     *
     * @param jobConfig job configuration
     * @return job id
     */
    Optional<String> start(PipelineJobConfiguration jobConfig);
    
    /**
     * Start disabled job.
     *
     * @param jobId job id
     */
    void startDisabledJob(String jobId);
    
    /**
     * Stop pipeline job.
     *
     * @param jobId job id
     */
    void stop(String jobId);
    
    /**
     * Get job configuration.
     *
     * @param jobId job id
     * @return job configuration
     */
    PipelineJobConfiguration getJobConfiguration(String jobId);
    
    /**
     * Get pipeline job info.
     *
     * @param contextKey context key
     * @return job info list
     */
    List<PipelineJobInfo> list(PipelineContextKey contextKey);
    
    /**
     * Persist job item progress.
     *
     * @param jobItemContext job item context
     */
    void persistJobItemProgress(PipelineJobItemContext jobItemContext);
    
    /**
     * Update job item progress.
     *
     * @param jobItemContext job item context
     */
    void updateJobItemProgress(PipelineJobItemContext jobItemContext);
    
    /**
     * Get job item progress.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @return job item progress, may be null
     */
    Optional<? extends PipelineJobItemProgress> getJobItemProgress(String jobId, int shardingItem);
    
    /**
     * Update job item status.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @param status status
     */
    void updateJobItemStatus(String jobId, int shardingItem, JobStatus status);
    
    /**
     * Get job item error message.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @return map, key is sharding item, value is error message
     */
    String getJobItemErrorMessage(String jobId, int shardingItem);
    
    /**
     * Persist job item error message.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @param error error
     */
    void persistJobItemErrorMessage(String jobId, int shardingItem, Object error);
    
    /**
     * Clean job item error message.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     */
    void cleanJobItemErrorMessage(String jobId, int shardingItem);
}
