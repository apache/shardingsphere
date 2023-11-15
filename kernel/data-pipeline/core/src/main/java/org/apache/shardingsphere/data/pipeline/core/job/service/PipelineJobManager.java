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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.common.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.common.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.common.pojo.PipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.common.registrycenter.repository.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobCreationWithInvalidShardingCountException;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.domain.JobBriefInfo;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Pipeline job manager.
 */
@RequiredArgsConstructor
@Slf4j
public final class PipelineJobManager {
    
    private final PipelineJobAPI pipelineJobAPI;
    
    /**
     * Start job.
     *
     * @param jobConfig job configuration
     * @return job id
     */
    public Optional<String> start(final PipelineJobConfiguration jobConfig) {
        String jobId = jobConfig.getJobId();
        ShardingSpherePreconditions.checkState(0 != jobConfig.getJobShardingCount(), () -> new PipelineJobCreationWithInvalidShardingCountException(jobId));
        GovernanceRepositoryAPI repositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId));
        String jobConfigKey = PipelineMetaDataNode.getJobConfigPath(jobId);
        if (repositoryAPI.isExisted(jobConfigKey)) {
            log.warn("jobId already exists in registry center, ignore, jobConfigKey={}", jobConfigKey);
            return Optional.of(jobId);
        }
        repositoryAPI.persist(PipelineMetaDataNode.getJobRootPath(jobId), pipelineJobAPI.getPipelineJobClass().getName());
        repositoryAPI.persist(jobConfigKey, YamlEngine.marshal(jobConfig.convertToJobConfigurationPOJO()));
        return Optional.of(jobId);
    }
    
    /**
     * Drop job.
     * 
     * @param jobId to be drooped job id
     */
    public void drop(final String jobId) {
        PipelineContextKey contextKey = PipelineJobIdUtils.parseContextKey(jobId);
        PipelineAPIFactory.getJobOperateAPI(contextKey).remove(String.valueOf(jobId), null);
        PipelineAPIFactory.getGovernanceRepositoryAPI(contextKey).deleteJob(jobId);
    }
    
    /**
     * Get pipeline jobs info.
     *
     * @param contextKey context key
     * @return jobs info
     */
    public List<PipelineJobInfo> getPipelineJobInfos(final PipelineContextKey contextKey) {
        return getJobBriefInfos(contextKey, pipelineJobAPI.getType()).map(each -> pipelineJobAPI.getJobInfo(each.getJobName())).collect(Collectors.toList());
    }
    
    private Stream<JobBriefInfo> getJobBriefInfos(final PipelineContextKey contextKey, final String jobType) {
        return PipelineAPIFactory.getJobStatisticsAPI(contextKey).getAllJobsBriefInfo().stream().filter(each -> !each.getJobName().startsWith("_"))
                .filter(each -> jobType.equals(PipelineJobIdUtils.parseJobType(each.getJobName()).getType()));
    }
}
