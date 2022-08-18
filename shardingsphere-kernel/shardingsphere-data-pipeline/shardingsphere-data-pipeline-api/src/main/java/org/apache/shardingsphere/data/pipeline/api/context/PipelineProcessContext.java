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

package org.apache.shardingsphere.data.pipeline.api.context;

import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelCreator;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.infra.config.rule.data.pipeline.PipelineProcessConfiguration;

/**
 * Pipeline process context.
 */
public interface PipelineProcessContext {
    
    /**
     * Get pipeline process config.
     *
     * @return pipeline process config
     */
    PipelineProcessConfiguration getPipelineProcessConfig();
    
    /**
     * Get pipeline channel creator.
     *
     * @return pipeline channel creator
     */
    PipelineChannelCreator getPipelineChannelCreator();
    
    /**
     * Get job read rate limit algorithm.
     *
     * @return job read rate limit algorithm
     */
    JobRateLimitAlgorithm getReadRateLimitAlgorithm();
}
