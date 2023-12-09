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

import org.apache.shardingsphere.data.pipeline.core.job.config.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.PipelineDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJob;
import org.apache.shardingsphere.data.pipeline.core.job.progress.PipelineJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.config.yaml.swapper.YamlPipelineJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.config.YamlPipelineJobItemProgressConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.swapper.YamlPipelineJobItemProgressSwapper;
import org.apache.shardingsphere.data.pipeline.core.pojo.PipelineJobInfo;
import org.apache.shardingsphere.infra.util.yaml.YamlConfiguration;

/**
 * Fixture job type.
 */
public final class FixtureJobType implements PipelineJobType {
    
    @Override
    public String getCode() {
        return "00";
    }
    
    @Override
    public <Y extends YamlConfiguration, T extends PipelineJobConfiguration> YamlPipelineJobConfigurationSwapper<Y, T> getYamlJobConfigurationSwapper() {
        return null;
    }
    
    @Override
    public <T extends PipelineJobItemProgress> YamlPipelineJobItemProgressSwapper<YamlPipelineJobItemProgressConfiguration, T> getYamlJobItemProgressSwapper() {
        return null;
    }
    
    @Override
    public Class<? extends PipelineJob> getJobClass() {
        return null;
    }
    
    @Override
    public PipelineJobInfo getJobInfo(final String jobId) {
        return null;
    }
    
    @Override
    public PipelineDataConsistencyChecker buildDataConsistencyChecker(final PipelineJobConfiguration jobConfig,
                                                                      final TransmissionProcessContext processContext, final ConsistencyCheckJobItemProgressContext progressContext) {
        return null;
    }
    
    @Override
    public String getType() {
        return "FIXTURE";
    }
}
