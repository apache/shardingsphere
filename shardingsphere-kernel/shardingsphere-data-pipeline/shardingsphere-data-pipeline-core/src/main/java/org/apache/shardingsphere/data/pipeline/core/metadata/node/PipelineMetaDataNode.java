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

package org.apache.shardingsphere.data.pipeline.core.metadata.node;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.constant.DataPipelineConstants;

/**
 * Scaling meta data node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipelineMetaDataNode {
    
    /**
     * Get job config path.
     *
     * @param jobId job id.
     * @return job config path.
     */
    public static String getJobConfigPath(final String jobId) {
        return String.join("/", DataPipelineConstants.DATA_PIPELINE_ROOT, jobId, "config");
    }
    
    /**
     * Get scaling root path.
     *
     * @param jobId job id.
     * @return root path
     */
    public static String getScalingJobPath(final String jobId) {
        return String.join("/", DataPipelineConstants.DATA_PIPELINE_ROOT, jobId);
    }
    
    /**
     * Get scaling job offset path, include job id and sharding item.
     *
     * @param jobId job id.
     * @param shardingItem sharding item.
     * @return job offset path.
     */
    public static String getScalingJobOffsetPath(final String jobId, final int shardingItem) {
        return String.join("/", getScalingJobOffsetPath(jobId), Integer.toString(shardingItem));
    }
    
    /**
     * Get scaling job offset path.
     *
     * @param jobId job id.
     * @return job offset path.
     */
    public static String getScalingJobOffsetPath(final String jobId) {
        return String.join("/", DataPipelineConstants.DATA_PIPELINE_ROOT, jobId, "offset");
    }
    
    /**
     * Get scaling job config path.
     *
     * @param jobId job id.
     * @return job config path.
     */
    public static String getScalingJobConfigPath(final String jobId) {
        return String.join("/", DataPipelineConstants.DATA_PIPELINE_ROOT, jobId, "config");
    }
    
    /**
     * Get scaling job config path.
     *
     * @param jobId job id.
     * @return job config path.
     */
    public static String getScalingCheckResultPath(final String jobId) {
        return String.join("/", DataPipelineConstants.DATA_PIPELINE_ROOT, jobId, "check", "result");
    }
}
