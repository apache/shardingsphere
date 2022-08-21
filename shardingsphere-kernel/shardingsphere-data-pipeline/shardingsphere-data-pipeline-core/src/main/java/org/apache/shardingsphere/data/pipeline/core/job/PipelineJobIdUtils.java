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

package org.apache.shardingsphere.data.pipeline.core.job;

import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.apache.shardingsphere.data.pipeline.api.job.PipelineJobId;

/**
 * Pipeline job id utils.
 */
public final class PipelineJobIdUtils {
    
    /**
     * Marshal job id common prefix.
     *
     * @param pipelineJobId pipeline job id
     * @return job id common prefix
     */
    public static String marshalJobIdCommonPrefix(final PipelineJobId pipelineJobId) {
        return 'j' + pipelineJobId.getTypeCode();
    }
    
    /**
     * Parse job type.
     *
     * @param jobId job id
     * @return job type
     * @throws IllegalArgumentException if job id is invalid
     */
    public static JobType parseJobType(final String jobId) {
        if (jobId.length() <= 3) {
            throw new IllegalArgumentException("Invalid jobId length, jobId=" + jobId);
        }
        if ('j' != jobId.charAt(0)) {
            throw new IllegalArgumentException("Invalid jobId, first char=" + jobId.charAt(0));
        }
        String typeCode = jobId.substring(1, 3);
        JobType result = JobType.valueOfByCode(typeCode);
        if (null == result) {
            throw new IllegalArgumentException("Could not get JobType by '" + typeCode + "', jobId: " + jobId);
        }
        return result;
    }
}
