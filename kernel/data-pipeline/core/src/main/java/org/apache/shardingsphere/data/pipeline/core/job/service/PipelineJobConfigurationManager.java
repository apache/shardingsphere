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
import org.apache.shardingsphere.data.pipeline.core.execute.ShardingTotalCountUsageJobExecutorServiceHandler;
import org.apache.shardingsphere.data.pipeline.core.job.config.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.config.yaml.swapper.YamlPipelineJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobOption;
import org.apache.shardingsphere.data.pipeline.core.listener.PipelineElasticJobListener;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.util.datetime.DateTimeFormatterFactory;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;

import java.time.LocalDateTime;
import java.util.Collections;

/**
 * Pipeline job configuration manager.
 */
@RequiredArgsConstructor
public final class PipelineJobConfigurationManager {
    
    private final PipelineJobOption jobOption;
    
    /**
     * Get job configuration.
     *
     * @param jobId job ID
     * @param <T> type of pipeline job configuration
     * @return pipeline job configuration
     */
    @SuppressWarnings("unchecked")
    public <T extends PipelineJobConfiguration> T getJobConfiguration(final String jobId) {
        return (T) jobOption.getYamlJobConfigurationSwapper().swapToObject(PipelineJobIdUtils.getElasticJobConfigurationPOJO(jobId).getJobParameter());
    }
    
    /**
     * Convert to job configuration POJO.
     *
     * @param jobConfig pipeline job configuration
     * @return converted job configuration POJO
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public JobConfigurationPOJO convertToJobConfigurationPOJO(final PipelineJobConfiguration jobConfig) {
        JobConfigurationPOJO result = new JobConfigurationPOJO();
        result.setJobName(jobConfig.getJobId());
        result.setShardingTotalCount(jobOption.isForceNoShardingWhenConvertToJobConfigurationPOJO() ? 1 : jobConfig.getJobShardingCount());
        YamlPipelineJobConfigurationSwapper swapper = jobOption.getYamlJobConfigurationSwapper();
        result.setJobParameter(YamlEngine.marshal(swapper.swapToYamlConfiguration(jobConfig)));
        String createTimeFormat = LocalDateTime.now().format(DateTimeFormatterFactory.getDatetimeFormatter());
        result.getProps().setProperty("create_time", createTimeFormat);
        result.getProps().setProperty("start_time_millis", String.valueOf(System.currentTimeMillis()));
        result.getProps().setProperty("run_count", "1");
        result.setJobListenerTypes(Collections.singletonList(PipelineElasticJobListener.class.getName()));
        result.setJobExecutorServiceHandlerType(ShardingTotalCountUsageJobExecutorServiceHandler.TYPE);
        return result;
    }
}
