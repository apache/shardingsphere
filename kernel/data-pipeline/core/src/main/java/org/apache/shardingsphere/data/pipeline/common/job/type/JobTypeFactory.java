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

package org.apache.shardingsphere.data.pipeline.common.job.type;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Job type factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class JobTypeFactory {
    
    private static final Map<String, JobType> CODE_JOB_TYPE_MAP = new ConcurrentHashMap<>();
    
    static {
        for (JobType each : ShardingSphereServiceLoader.getServiceInstances(JobType.class)) {
            String typeCode = each.getTypeCode();
            JobType replaced = CODE_JOB_TYPE_MAP.put(typeCode, each);
            if (null != replaced) {
                log.error("Type code already exists, typeCode={}, replaced={}, current={}", typeCode, replaced, each, new Exception());
            }
        }
    }
    
    /**
     * Get job type instance.
     *
     * @param jobTypeCode job type code
     * @return job type
     */
    public static JobType getInstance(final String jobTypeCode) {
        JobType result = CODE_JOB_TYPE_MAP.get(jobTypeCode);
        Preconditions.checkNotNull(result, "Can not get job type by `%s`", jobTypeCode);
        return result;
    }
}
