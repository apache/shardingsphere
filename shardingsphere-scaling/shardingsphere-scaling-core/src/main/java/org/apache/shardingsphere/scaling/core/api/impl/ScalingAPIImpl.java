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

package org.apache.shardingsphere.scaling.core.api.impl;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.scaling.core.api.JobInfo;
import org.apache.shardingsphere.scaling.core.api.ScalingAPI;
import org.apache.shardingsphere.scaling.core.api.ScalingAPIFactory;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.exception.ScalingJobNotFoundException;
import org.apache.shardingsphere.scaling.core.job.check.DataConsistencyCheckResult;
import org.apache.shardingsphere.scaling.core.job.position.JobProgress;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public final class ScalingAPIImpl implements ScalingAPI {
    
    @Override
    public List<JobInfo> list() {
        return ScalingAPIFactory.getJobStatisticsAPI().getAllJobsBriefInfo().stream()
                .filter(each -> !each.getJobName().startsWith("_"))
                .map(each -> getJobInfo(each.getJobName()))
                .collect(Collectors.toList());
    }
    
    private JobInfo getJobInfo(final String jobName) {
        JobInfo result = new JobInfo(Long.parseLong(jobName));
//        result.setStatus(ScalingAPIFactory.getRegistryRepositoryAPI().getStatus(result.getJobId()));
//        JobConfigurationPOJO jobConfiguration = getElasticJobConfigPOJO(result.getJobId());
//        result.setActive(!jobConfiguration.isDisabled());
//        ScalingConfiguration scalingConfig = new Gson().fromJson(jobConfiguration.getJobParameter(), ScalingConfiguration.class);
//        result.setShardingTotalCount(scalingConfig.getJobConfiguration().getShardingTotalCount());
//        result.setTables(scalingConfig.getJobConfiguration().getLogicTables());
//        ScalingAPIFactory.getRegistryRepositoryAPI().setJobProgress(result, scalingConfig);
        return result;
    }
    
    @Override
    public Optional<Long> start(final JobConfiguration jobConfig) {
        return Optional.empty();
    }
    
    @Override
    public void start(final long jobId) {
    
    }
    
    @Override
    public void stop(final long jobId) {
    
    }
    
    @Override
    public void remove(final long jobId) {
    
    }
    
    @Override
    public Map<Integer, JobProgress> getProgress(final long jobId) {
        return null;
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> dataConsistencyCheck(final long jobId) {
        return null;
    }
    
    @Override
    public void resetTargetTable(final long jobId) throws SQLException {
    
    }
    
    private JobConfigurationPOJO getElasticJobConfigPOJO(final long jobId) {
        try {
            return ScalingAPIFactory.getJobConfigurationAPI().getJobConfiguration(String.valueOf(jobId));
        } catch (final NullPointerException ex) {
            log.warn("Get job {} config failed.", jobId);
            throw new ScalingJobNotFoundException(String.format("Can not find job by id %s", jobId));
        }
    }
    
    private JobConfiguration getScalingJobConfig(final long jobId) {
        return getScalingJobConfig(getElasticJobConfigPOJO(jobId));
    }
    
    private JobConfiguration getScalingJobConfig(final JobConfigurationPOJO elasticJobConfigPOJO) {
        return new Gson().fromJson(elasticJobConfigPOJO.getJobParameter(), JobConfiguration.class);
    }
}
