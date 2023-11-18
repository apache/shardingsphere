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

import org.apache.shardingsphere.data.pipeline.common.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.common.job.PipelineJob;
import org.apache.shardingsphere.data.pipeline.common.job.progress.PipelineJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.yaml.YamlPipelineJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.core.job.yaml.YamlPipelineJobItemProgressSwapper;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;

import java.util.Optional;

/**
 * Pipeline job API.
 */
@SingletonSPI
public interface PipelineJobAPI extends TypedSPI {
    
    /**
     * Get YAML pipeline job configuration swapper.
     * 
     * @return YAML pipeline job configuration swapper
     */
    YamlPipelineJobConfigurationSwapper<?, ?> getYamlJobConfigurationSwapper();
    
    /**
     * Get YAML pipeline job item progress swapper.
     * 
     * @return YAML pipeline job item progress swapper
     */
    @SuppressWarnings("rawtypes")
    YamlPipelineJobItemProgressSwapper getYamlJobItemProgressSwapper();
    
    /**
     * Whether to ignore to start disabled job when job item progress is finished.
     * 
     * @return ignore to start disabled job when job item progress is finished or not
     */
    default boolean isIgnoreToStartDisabledJobWhenJobItemProgressIsFinished() {
        return false;
    }
    
    /**
     * Get to be start disabled next job type.
     *
     * @return to be start disabled next job type
     */
    default Optional<String> getToBeStartDisabledNextJobType() {
        return Optional.empty();
    }
    
    /**
     * Get to be stopped previous job type.
     *
     * @return to be stopped previous job type
     */
    default Optional<String> getToBeStoppedPreviousJobType() {
        return Optional.empty();
    }
    
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
     * Get pipeline job class.
     * 
     * @return pipeline job class
     */
    Class<? extends PipelineJob> getJobClass();
    
    @Override
    String getType();
}
