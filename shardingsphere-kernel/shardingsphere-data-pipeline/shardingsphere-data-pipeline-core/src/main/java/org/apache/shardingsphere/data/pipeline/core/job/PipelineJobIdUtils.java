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

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.apache.shardingsphere.data.pipeline.api.job.PipelineJobId;

/**
 * Pipeline job id utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipelineJobIdUtils {
    
    /**
     * Marshal job id common prefix.
     *
     * @param pipelineJobId pipeline job id
     * @return job id common prefix
     */
    public static String marshalJobIdCommonPrefix(final PipelineJobId pipelineJobId) {
        return 'j' + pipelineJobId.getJobTypeCode() + pipelineJobId.getFormatVersion();
    }
    
    /**
     * Parse job type.
     *
     * @param jobId job id
     * @return job type
     */
    public static JobType parseJobType(final String jobId) {
        Preconditions.checkArgument(jobId.length() > 3, "Invalid jobId length, jobId=%s", jobId);
        Preconditions.checkArgument('j' == jobId.charAt(0), "Invalid jobId, first char=%s", jobId.charAt(0));
        String typeCode = jobId.substring(1, 3);
        JobType result = JobType.valueOfByCode(typeCode);
        Preconditions.checkNotNull(result, "Can not get job type by `%s`, job ID is `%s`", typeCode, jobId);
        return result;
    }
}
