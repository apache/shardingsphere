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

import org.apache.shardingsphere.data.pipeline.api.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;

/**
 * Pipeline job item context.
 */
public interface PipelineJobItemContext {
    
    /**
     * Get job id.
     *
     * @return job id
     */
    String getJobId();
    
    /**
     * Get sharding item.
     *
     * @return sharding item
     */
    int getShardingItem();
    
    /**
     * Get job status.
     *
     * @return job status
     */
    JobStatus getStatus();
    
    /**
     * Set job status.
     *
     * @param status job status
     */
    void setStatus(JobStatus status);
    
    /**
     * Get job configuration.
     *
     * @return job configuration
     */
    PipelineJobConfiguration getJobConfig();
    
    /**
     * Get job process context.
     *
     * @return job process context
     */
    PipelineProcessContext getJobProcessContext();
    
    /**
     * Set stopping.
     * @param stopping stopping
     */
    void setStopping(boolean stopping);
    
    /**
     * Get stopping.
     * @return stopping
     */
    boolean isStopping();
}
