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

import org.apache.shardingsphere.data.pipeline.cdc.config.job.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.yaml.swapper.YamlCDCJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.core.job.config.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.swapper.YamlTransmissionJobItemProgressSwapper;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.pojo.PipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.core.pojo.PipelineJobMetaData;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.PipelineDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobConfigurationManager;

/**
 * CDC job type.
 */
public final class CDCJobType implements PipelineJobType {
    
    @Override
    public String getCode() {
        return "03";
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public YamlCDCJobConfigurationSwapper getYamlJobConfigurationSwapper() {
        return new YamlCDCJobConfigurationSwapper();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public YamlTransmissionJobItemProgressSwapper getYamlJobItemProgressSwapper() {
        return new YamlTransmissionJobItemProgressSwapper();
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
        CDCJobConfiguration jobConfig = new PipelineJobConfigurationManager(new CDCJobType()).getJobConfiguration(jobId);
        return new PipelineJobInfo(jobMetaData, jobConfig.getDatabaseName(), String.join(", ", jobConfig.getSchemaTableNames()));
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
