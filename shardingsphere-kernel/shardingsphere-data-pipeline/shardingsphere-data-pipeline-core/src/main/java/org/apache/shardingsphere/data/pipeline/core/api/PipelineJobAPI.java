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

package org.apache.shardingsphere.data.pipeline.core.api;

import org.apache.shardingsphere.data.pipeline.api.PipelineJobPublicAPI;
import org.apache.shardingsphere.data.pipeline.api.config.PipelineTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.yaml.YamlPipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.api.context.PipelineProcessContext;
import org.apache.shardingsphere.data.pipeline.api.job.PipelineJobId;
import org.apache.shardingsphere.infra.util.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPI;

import java.util.Optional;

/**
 * Pipeline job API.
 */
@SingletonSPI
public interface PipelineJobAPI extends PipelineJobPublicAPI, PipelineJobItemAPI, TypedSPI {
    
    /**
     * Marshal pipeline job id.
     *
     * @param pipelineJobId pipeline job id
     * @return marshaled text
     */
    String marshalJobId(PipelineJobId pipelineJobId);
    
    /**
     * Extend YAML job configuration.
     *
     * @param yamlJobConfig YAML job configuration
     */
    void extendYamlJobConfiguration(YamlPipelineJobConfiguration yamlJobConfig);
    
    /**
     * Build task configuration.
     *
     * @param pipelineJobConfig pipeline job configuration
     * @param jobShardingItem job sharding item
     * @param pipelineProcessConfig pipeline process configuration
     * @return task configuration
     */
    PipelineTaskConfiguration buildTaskConfiguration(PipelineJobConfiguration pipelineJobConfig, int jobShardingItem, PipelineProcessConfiguration pipelineProcessConfig);
    
    /**
     * Build pipeline process context.
     *
     * @param pipelineJobConfig pipeline job configuration
     * @return pipeline process context
     */
    PipelineProcessContext buildPipelineProcessContext(PipelineJobConfiguration pipelineJobConfig);
    
    /**
     * Start job.
     *
     * @param jobConfig job configuration
     * @return job id
     */
    Optional<String> start(PipelineJobConfiguration jobConfig);
    
    /**
     * Get job configuration.
     *
     * @param jobId job id
     * @return job configuration
     */
    PipelineJobConfiguration getJobConfiguration(String jobId);
    
    /**
     * Get job item error msg.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @return map, key is sharding item, value is error msg
     */
    String getJobItemErrorMsg(String jobId, int shardingItem);
    
    /**
     * Persist job item error msg.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @param errorMsg error msg
     */
    void persistJobItemErrorMsg(String jobId, int shardingItem, Throwable errorMsg);
}
