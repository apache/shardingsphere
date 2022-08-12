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

import org.apache.shardingsphere.data.pipeline.api.PipelineJobPublicAPI;
import org.apache.shardingsphere.data.pipeline.api.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.YamlPipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.PipelineJobId;
import org.apache.shardingsphere.data.pipeline.api.job.progress.PipelineJobItemProgress;
import org.apache.shardingsphere.infra.util.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPI;

/**
 * Pipeline job API.
 */
@SingletonSPI
public interface PipelineJobAPI extends PipelineJobPublicAPI, TypedSPI {
    
    /**
     * Marshal pipeline job id.
     *
     * @param pipelineJobId pipeline job id
     * @return marshaled text
     */
    String marshalJobId(PipelineJobId pipelineJobId);
    
    /**
     * Extend job configuration.
     *
     * @param yamlJobConfig yaml job configuration
     */
    void extendJobConfiguration(YamlPipelineJobConfiguration yamlJobConfig);
    
    /**
     * Get job configuration.
     *
     * @param jobId job id
     * @return job configuration
     */
    PipelineJobConfiguration getJobConfig(String jobId);
    
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
    PipelineJobItemProgress getJobItemProgress(String jobId, int shardingItem);
    
    /**
     * Update job item status.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @param status status
     */
    void updateJobItemStatus(String jobId, int shardingItem, JobStatus status);
}
