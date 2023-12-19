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

package org.apache.shardingsphere.data.pipeline.core.job.type;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.PipelineDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJob;
import org.apache.shardingsphere.data.pipeline.core.job.config.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.config.yaml.swapper.YamlPipelineJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.core.job.progress.PipelineJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.config.YamlPipelineJobItemProgressConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.swapper.YamlPipelineJobItemProgressSwapper;
import org.apache.shardingsphere.data.pipeline.core.pojo.PipelineJobInfo;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.infra.util.yaml.YamlConfiguration;

import java.util.Optional;

/**
 * Pipeline job type.
 */
@SingletonSPI
@JsonIgnoreType
public interface PipelineJobType extends TypedSPI {
    
    /**
     * Get job type code.
     *
     * @return job type code
     */
    String getCode();
    
    /**
     * Get YAML pipeline job configuration swapper.
     *
     * @param <T> type of YAML configuration
     * @param <Y> type of pipeline job configuration
     * @return YAML pipeline job configuration swapper
     */
    <Y extends YamlConfiguration, T extends PipelineJobConfiguration> YamlPipelineJobConfigurationSwapper<Y, T> getYamlJobConfigurationSwapper();
    
    /**
     * Get YAML pipeline job item progress swapper.
     *
     * @param <T> type of pipeline job item progress
     * @return YAML pipeline job item progress swapper
     */
    <T extends PipelineJobItemProgress> YamlPipelineJobItemProgressSwapper<YamlPipelineJobItemProgressConfiguration, T> getYamlJobItemProgressSwapper();
    
    /**
     * Get pipeline job class.
     *
     * @return pipeline job class
     */
    Class<? extends PipelineJob> getJobClass();
    
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
     * Whether to force no sharding when convert to job configuration POJO.
     *
     * @return without sharding or not
     */
    default boolean isForceNoShardingWhenConvertToJobConfigurationPOJO() {
        return false;
    }
    
    /**
     * Get pipeline job info.
     *
     * @param jobId job ID
     * @return pipeline job info
     */
    PipelineJobInfo getJobInfo(String jobId);
    
    /**
     * Build pipeline data consistency checker.
     *
     * @param jobConfig job configuration
     * @param processContext process context
     * @param progressContext consistency check job item progress context
     * @return all logic tables check result
     */
    PipelineDataConsistencyChecker buildDataConsistencyChecker(PipelineJobConfiguration jobConfig, TransmissionProcessContext processContext, ConsistencyCheckJobItemProgressContext progressContext);
    
    @Override
    String getType();
}
