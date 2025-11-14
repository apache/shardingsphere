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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJob;
import org.apache.shardingsphere.data.pipeline.core.job.config.yaml.swapper.YamlPipelineJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.swapper.YamlConsistencyCheckJobItemProgressSwapper;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.swapper.YamlPipelineJobItemProgressSwapper;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.swapper.YamlTransmissionJobItemProgressSwapper;

/**
 * Pipeline job type.
 */
@RequiredArgsConstructor
@Getter
public final class PipelineJobOption {
    
    private final String code;
    
    private final Class<? extends PipelineJob> jobClass;
    
    private final boolean isTransmissionJob;
    
    private final YamlPipelineJobConfigurationSwapper<?, ?> yamlJobConfigurationSwapper;
    
    private final boolean isIgnoreToStartDisabledJobWhenJobItemProgressIsFinished;
    
    private final String toBeStartDisabledNextJobType;
    
    private final String toBeStoppedPreviousJobType;
    
    private final boolean isForceNoShardingWhenConvertToJobConfigurationPOJO;
    
    /**
     * Get YAML job item progress swapper.
     *
     * @return YAML job item progress swapper
     */
    public YamlPipelineJobItemProgressSwapper<?, ?> getYamlJobItemProgressSwapper() {
        return isTransmissionJob ? new YamlTransmissionJobItemProgressSwapper() : new YamlConsistencyCheckJobItemProgressSwapper();
    }
}
