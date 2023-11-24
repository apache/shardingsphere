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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.common.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.job.yaml.YamlPipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;

/**
 * Pipeline job configuration loader.
 */
@RequiredArgsConstructor
public final class PipelineJobConfigurationLoader {
    
    private final PipelineJobAPI jobAPI;
    
    /**
     * Get job configuration.
     *
     * @param jobId job ID
     * @param <T> type of pipeline job configuration
     * @return pipeline job configuration
     */
    @SuppressWarnings("unchecked")
    public <T extends PipelineJobConfiguration> T getJobConfiguration(final String jobId) {
        return (T) jobAPI.getYamlJobConfigurationSwapper().swapToObject(PipelineJobIdUtils.getElasticJobConfigurationPOJO(jobId).getJobParameter());
    }
    
    /**
     * Swap to YAML pipeline job configuration.
     *
     * @param jobConfig pipeline job configuration
     * @param <T> type of pipeline job configuration
     * @return swapped YAML pipeline job configuration
     */
    public <T extends PipelineJobConfiguration> YamlPipelineJobConfiguration swapToYamlJobConfiguration(final T jobConfig) {
        return (YamlPipelineJobConfiguration) jobAPI.getYamlJobConfigurationSwapper().swapToYamlConfiguration(jobConfig);
    }
}
