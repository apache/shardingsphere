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

package org.apache.shardingsphere.data.pipeline.core.job.type;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import java.util.HashMap;
import java.util.Map;

/**
 * Job code registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class JobCodeRegistry {
    
    private static final Map<String, PipelineJobType> JOB_CODE_AND_TYPE_MAP = new HashMap<>();
    
    static {
        for (PipelineJobType each : ShardingSphereServiceLoader.getServiceInstances(PipelineJobType.class)) {
            Preconditions.checkArgument(2 == each.getCode().length(), "Job type code length is not 2.");
            JOB_CODE_AND_TYPE_MAP.put(each.getCode(), each);
        }
    }
    
    /**
     * Get job type.
     *
     * @param jobTypeCode job type code
     * @return job type
     */
    public static PipelineJobType getJobType(final String jobTypeCode) {
        Preconditions.checkArgument(JOB_CODE_AND_TYPE_MAP.containsKey(jobTypeCode), "Can not get job type by `%s`.", jobTypeCode);
        return JOB_CODE_AND_TYPE_MAP.get(jobTypeCode);
    }
}
