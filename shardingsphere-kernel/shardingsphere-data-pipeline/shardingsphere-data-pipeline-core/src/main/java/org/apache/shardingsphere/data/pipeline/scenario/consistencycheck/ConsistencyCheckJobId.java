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

package org.apache.shardingsphere.data.pipeline.scenario.consistencycheck;

import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.apache.shardingsphere.data.pipeline.core.job.AbstractPipelineJobId;

/**
 * Consistency check job id.
 */
@Getter
@ToString(callSuper = true)
public final class ConsistencyCheckJobId extends AbstractPipelineJobId {
    
    public static final String CURRENT_VERSION = "01";
    
    private static final int MAX_CONSISTENCY_CHECK_VERSION = 9;
    
    private final String pipelineJobId;
    
    private final Integer consistencyCheckVersion;
    
    public ConsistencyCheckJobId(final @NonNull String pipelineJobId, final int consistencyCheckVersion) {
        super(JobType.CONSISTENCY_CHECK, CURRENT_VERSION);
        this.pipelineJobId = pipelineJobId;
        if (consistencyCheckVersion > MAX_CONSISTENCY_CHECK_VERSION) {
            this.consistencyCheckVersion = 0;
        } else {
            this.consistencyCheckVersion = consistencyCheckVersion;
        }
    }
    
    /**
     * Get consistency check version.
     *
     * @param consistencyCheckJobId consistency check job id.
     * @return consistency check version
     */
    public static int getConsistencyCheckVersion(final @NonNull String consistencyCheckJobId) {
        String versionString = consistencyCheckJobId.substring(consistencyCheckJobId.length() - 1);
        int version = Integer.parseInt(versionString);
        return version > MAX_CONSISTENCY_CHECK_VERSION ? 0 : version;
    }
}
