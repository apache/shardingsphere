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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.spi.job.JobType;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPIRegistry;

/**
 * Pipeline job API factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipelineJobAPIFactory {
    
    static {
        ShardingSphereServiceLoader.register(PipelineJobAPI.class);
    }
    
    /**
     * Get pipeline job API.
     *
     * @param jobType job type
     * @return pipeline job API
     */
    public static PipelineJobAPI getPipelineJobAPI(final JobType jobType) {
        return TypedSPIRegistry.getRegisteredService(PipelineJobAPI.class, jobType.getTypeName());
    }
    
    /**
     * Get pipeline job API.
     *
     * @param jobTypeName job type name
     * @return pipeline job API
     */
    public static PipelineJobAPI getPipelineJobAPI(final String jobTypeName) {
        return TypedSPIRegistry.getRegisteredService(PipelineJobAPI.class, jobTypeName);
    }
}
