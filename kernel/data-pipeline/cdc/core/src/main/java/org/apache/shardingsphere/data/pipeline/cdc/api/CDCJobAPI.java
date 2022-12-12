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

package org.apache.shardingsphere.data.pipeline.cdc.api;

import org.apache.shardingsphere.data.pipeline.api.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.api.pojo.CreateSubscriptionJobParameter;
import org.apache.shardingsphere.data.pipeline.cdc.config.task.CDCTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.context.CDCProcessContext;
import org.apache.shardingsphere.data.pipeline.core.api.InventoryIncrementalJobAPI;
import org.apache.shardingsphere.infra.util.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.util.spi.type.required.RequiredSPI;

/**
 * CDC job api.
 */
@SingletonSPI
public interface CDCJobAPI extends InventoryIncrementalJobAPI, RequiredSPI {
    
    @Override
    CDCTaskConfiguration buildTaskConfiguration(PipelineJobConfiguration pipelineJobConfig, int jobShardingItem, PipelineProcessConfiguration pipelineProcessConfig);
    
    @Override
    CDCProcessContext buildPipelineProcessContext(PipelineJobConfiguration pipelineJobConfig);
    
    /**
     * Create CDC job config and start.
     *
     * @param event create CDC job event
     * @return job id
     */
    String createJobAndStart(CreateSubscriptionJobParameter event);
}
