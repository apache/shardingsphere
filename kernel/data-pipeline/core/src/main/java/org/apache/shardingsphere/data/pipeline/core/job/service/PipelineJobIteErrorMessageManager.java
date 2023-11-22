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

package org.apache.shardingsphere.data.pipeline.core.job.service;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shardingsphere.data.pipeline.common.registrycenter.repository.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;

import java.util.Optional;

/**
 * Pipeline job item error message manager.
 */
public final class PipelineJobIteErrorMessageManager {
    
    private final String jobId;
    
    private final int shardingItem;
    
    private final GovernanceRepositoryAPI governanceRepositoryAPI;
    
    public PipelineJobIteErrorMessageManager(final String jobId, final int shardingItem) {
        this.jobId = jobId;
        this.shardingItem = shardingItem;
        governanceRepositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId));
    }
    
    /**
     * Get job item error message.
     *
     * @return map, key is sharding item, value is error message
     */
    public String getErrorMessage() {
        return Optional.ofNullable(governanceRepositoryAPI.getJobItemErrorMessageGovernanceRepository().load(jobId, shardingItem)).orElse("");
    }
    
    /**
     * Update job item error message.
     *
     * @param error error
     */
    public void updateErrorMessage(final Object error) {
        governanceRepositoryAPI.getJobItemErrorMessageGovernanceRepository().update(jobId, shardingItem, null == error ? "" : buildErrorMessage(error));
    }
    
    private String buildErrorMessage(final Object error) {
        return error instanceof Throwable ? ExceptionUtils.getStackTrace((Throwable) error) : error.toString();
    }
    
    /**
     * Clean job item error message.
     */
    public void cleanErrorMessage() {
        governanceRepositoryAPI.getJobItemErrorMessageGovernanceRepository().update(jobId, shardingItem, "");
    }
}
