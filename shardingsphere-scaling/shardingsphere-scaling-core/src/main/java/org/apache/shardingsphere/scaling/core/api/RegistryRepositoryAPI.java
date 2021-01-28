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

package org.apache.shardingsphere.scaling.core.api;

import org.apache.shardingsphere.scaling.core.job.ScalingJob;
import org.apache.shardingsphere.scaling.core.job.position.JobPosition;

/**
 * Registry repository API.
 */
public interface RegistryRepositoryAPI {
    
    /**
     * persist job position.
     *
     * @param scalingJob scaling job
     */
    void persistJobPosition(ScalingJob scalingJob);
    
    /**
     * Get job position.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @return job position
     */
    JobPosition getJobPosition(long jobId, int shardingItem);
}
