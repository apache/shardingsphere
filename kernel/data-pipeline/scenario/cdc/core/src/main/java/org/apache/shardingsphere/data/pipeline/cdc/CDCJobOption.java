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

package org.apache.shardingsphere.data.pipeline.cdc;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.cdc.config.job.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.yaml.YamlCDCJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.common.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.common.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.common.pojo.PipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.common.pojo.PipelineJobMetaData;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.PipelineDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.option.TransmissionJobOption;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobConfigurationManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.TransmissionJobManager;

/**
 * CDC job option.
 */
@Slf4j
public final class CDCJobOption implements TransmissionJobOption {
    
    @SuppressWarnings("unchecked")
    @Override
    public YamlCDCJobConfigurationSwapper getYamlJobConfigurationSwapper() {
        return new YamlCDCJobConfigurationSwapper();
    }
    
    @Override
    public Class<CDCJob> getJobClass() {
        return CDCJob.class;
    }
    
    @Override
    public boolean isForceNoShardingWhenConvertToJobConfigurationPOJO() {
        return true;
    }
    
    @Override
    public PipelineJobInfo getJobInfo(final String jobId) {
        PipelineJobMetaData jobMetaData = new PipelineJobMetaData(PipelineJobIdUtils.getElasticJobConfigurationPOJO(jobId));
        CDCJobConfiguration jobConfig = new PipelineJobConfigurationManager(this).getJobConfiguration(jobId);
        return new PipelineJobInfo(jobMetaData, jobConfig.getDatabaseName(), String.join(", ", jobConfig.getSchemaTableNames()));
    }
    
    @Override
    public TransmissionProcessContext buildProcessContext(final PipelineJobConfiguration jobConfig) {
        TransmissionJobManager jobManager = new TransmissionJobManager(this);
        return new TransmissionProcessContext(jobConfig.getJobId(), jobManager.showProcessConfiguration(PipelineJobIdUtils.parseContextKey(jobConfig.getJobId())));
    }
    
    @Override
    public PipelineDataConsistencyChecker buildDataConsistencyChecker(final PipelineJobConfiguration jobConfig, final TransmissionProcessContext processContext,
                                                                      final ConsistencyCheckJobItemProgressContext progressContext) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String getType() {
        return "STREAMING";
    }
}
