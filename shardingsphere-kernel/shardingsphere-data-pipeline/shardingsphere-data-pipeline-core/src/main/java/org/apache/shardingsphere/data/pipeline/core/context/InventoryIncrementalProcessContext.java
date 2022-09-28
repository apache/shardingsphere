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

package org.apache.shardingsphere.data.pipeline.core.context;

import org.apache.shardingsphere.data.pipeline.api.context.PipelineProcessContext;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelCreator;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;

/**
 * Inventory incremental process context.
 */
public interface InventoryIncrementalProcessContext extends PipelineProcessContext {
    
    /**
     * Get pipeline channel creator.
     *
     * @return pipeline channel creator
     */
    PipelineChannelCreator getPipelineChannelCreator();
    
    /**
     * Get inventory dumper execute engine.
     *
     * @return inventory dumper execute engine
     */
    ExecuteEngine getInventoryDumperExecuteEngine();
    
    /**
     * Get inventory importer execute engine.
     *
     * @return inventory importer execute engine
     */
    ExecuteEngine getInventoryImporterExecuteEngine();
    
    /**
     * Get job read rate limit algorithm.
     *
     * @return job read rate limit algorithm
     */
    JobRateLimitAlgorithm getReadRateLimitAlgorithm();
    
    /**
     * Get job write rate limit algorithm.
     *
     * @return job write rate limit algorithm
     */
    JobRateLimitAlgorithm getWriteRateLimitAlgorithm();
    
    /**
     * Close.
     */
    void close();
}
