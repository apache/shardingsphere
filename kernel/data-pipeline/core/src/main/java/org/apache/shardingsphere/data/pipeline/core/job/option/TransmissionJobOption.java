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

package org.apache.shardingsphere.data.pipeline.core.job.option;

import org.apache.shardingsphere.data.pipeline.common.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.common.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.common.job.progress.yaml.YamlTransmissionJobItemProgressSwapper;
import org.apache.shardingsphere.data.pipeline.common.pojo.PipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.PipelineDataConsistencyChecker;
import org.apache.shardingsphere.data.pipeline.core.task.config.PipelineTaskConfiguration;

/**
 * Transmission job option.
 */
public interface TransmissionJobOption extends PipelineJobOption {
    
    @SuppressWarnings("unchecked")
    @Override
    default YamlTransmissionJobItemProgressSwapper getYamlJobItemProgressSwapper() {
        return new YamlTransmissionJobItemProgressSwapper();
    }
    
    /**
     * Get pipeline job info.
     *
     * @param jobId job ID
     * @return pipeline job info
     */
    PipelineJobInfo getJobInfo(String jobId);
    
    /**
     * Build task configuration.
     *
     * @param jobConfig pipeline job configuration
     * @param jobShardingItem job sharding item
     * @param processConfig pipeline process configuration
     * @return task configuration
     */
    PipelineTaskConfiguration buildTaskConfiguration(PipelineJobConfiguration jobConfig, int jobShardingItem, PipelineProcessConfiguration processConfig);
    
    /**
     * Build transmission process context.
     *
     * @param jobConfig pipeline job configuration
     * @return transmission process context
     */
    TransmissionProcessContext buildProcessContext(PipelineJobConfiguration jobConfig);
    
    /**
     * Build pipeline data consistency checker.
     *
     * @param jobConfig job configuration
     * @param processContext process context
     * @param progressContext consistency check job item progress context
     * @return all logic tables check result
     */
    PipelineDataConsistencyChecker buildDataConsistencyChecker(PipelineJobConfiguration jobConfig, TransmissionProcessContext processContext, ConsistencyCheckJobItemProgressContext progressContext);
}
