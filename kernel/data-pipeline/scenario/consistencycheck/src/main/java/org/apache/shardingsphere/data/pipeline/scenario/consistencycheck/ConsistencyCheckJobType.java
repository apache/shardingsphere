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

package org.apache.shardingsphere.data.pipeline.scenario.consistencycheck;

import org.apache.shardingsphere.data.pipeline.core.job.config.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.swapper.YamlConsistencyCheckJobItemProgressSwapper;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.pojo.PipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.PipelineDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.config.yaml.swapper.YamlConsistencyCheckJobConfigurationSwapper;

/**
 * Consistency check job type.
 */
public final class ConsistencyCheckJobType implements PipelineJobType {
    
    @Override
    public String getCode() {
        return "02";
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public YamlConsistencyCheckJobConfigurationSwapper getYamlJobConfigurationSwapper() {
        return new YamlConsistencyCheckJobConfigurationSwapper();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public YamlConsistencyCheckJobItemProgressSwapper getYamlJobItemProgressSwapper() {
        return new YamlConsistencyCheckJobItemProgressSwapper();
    }
    
    @Override
    public Class<ConsistencyCheckJob> getJobClass() {
        return ConsistencyCheckJob.class;
    }
    
    @Override
    public boolean isIgnoreToStartDisabledJobWhenJobItemProgressIsFinished() {
        return true;
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
        return "CONSISTENCY_CHECK";
    }
}
