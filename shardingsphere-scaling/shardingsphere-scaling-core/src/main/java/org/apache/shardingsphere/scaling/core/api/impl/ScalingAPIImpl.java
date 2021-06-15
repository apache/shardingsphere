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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.scaling.core.api.JobInfo;
import org.apache.shardingsphere.scaling.core.api.ScalingAPI;
import org.apache.shardingsphere.scaling.core.api.ScalingAPIFactory;
import org.apache.shardingsphere.scaling.core.common.constant.ScalingConstant;
import org.apache.shardingsphere.scaling.core.common.exception.ScalingJobNotFoundException;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.job.JobContext;
import org.apache.shardingsphere.scaling.core.job.ScalingJob;
import org.apache.shardingsphere.scaling.core.job.check.EnvironmentCheckerFactory;
import org.apache.shardingsphere.scaling.core.job.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.scaling.core.job.check.consistency.DataConsistencyChecker;
import org.apache.shardingsphere.scaling.core.job.environment.ScalingEnvironmentManager;
import org.apache.shardingsphere.scaling.core.job.progress.JobProgress;
import org.apache.shardingsphere.scaling.core.util.JobConfigurationUtil;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
public final class ScalingAPIImpl implements ScalingAPI {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public List<JobInfo> list() {
        return ScalingAPIFactory.getJobStatisticsAPI().getAllJobsBriefInfo().stream()
                .filter(each -> !each.getJobName().startsWith("_"))
                .map(each -> getJobInfo(each.getJobName()))
                .collect(Collectors.toList());
    }
    
    private JobInfo getJobInfo(final String jobName) {
        JobInfo result = new JobInfo(Long.parseLong(jobName));
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(result.getJobId());
        JobConfiguration jobConfig = getJobConfig(jobConfigPOJO);
        result.setActive(!jobConfigPOJO.isDisabled());
        result.setShardingTotalCount(jobConfig.getHandleConfig().getShardingTotalCount());
        result.setTables(jobConfig.getHandleConfig().getLogicTables());
        result.setCreateTime(jobConfigPOJO.getProps().getProperty("create_time"));
        result.setStopTime(jobConfigPOJO.getProps().getProperty("stop_time"));
        return result;
    }
    
    @Override
    public void start(final long jobId) {
        log.info("Start scaling job {}", jobId);
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        jobConfigPOJO.setDisabled(false);
        jobConfigPOJO.getProps().remove("stop_time");
        ScalingAPIFactory.getJobConfigurationAPI().updateJobConfiguration(jobConfigPOJO);
    }
    
    @Override
    public Optional<Long> start(final JobConfiguration jobConfig) {
        JobConfigurationUtil.fillInProperties(jobConfig);
        if (jobConfig.getHandleConfig().getShardingTotalCount() == 0) {
            log.warn("Invalid scaling job config!");
            return Optional.empty();
        }
        log.info("Start scaling job by {}", YamlEngine.marshal(jobConfig));
        ScalingAPIFactory.getGovernanceRepositoryAPI().persist(String.format("%s/%d", ScalingConstant.SCALING_ROOT, jobConfig.getHandleConfig().getJobId()), ScalingJob.class.getCanonicalName());
        ScalingAPIFactory.getGovernanceRepositoryAPI().persist(String.format("%s/%d/config", ScalingConstant.SCALING_ROOT, jobConfig.getHandleConfig().getJobId()), createJobConfig(jobConfig));
        return Optional.of(jobConfig.getHandleConfig().getJobId());
    }
    
    private String createJobConfig(final JobConfiguration jobConfig) {
        JobConfigurationPOJO jobConfigPOJO = new JobConfigurationPOJO();
        jobConfigPOJO.setJobName(String.valueOf(jobConfig.getHandleConfig().getJobId()));
        jobConfigPOJO.setShardingTotalCount(jobConfig.getHandleConfig().getShardingTotalCount());
        jobConfigPOJO.setJobParameter(YamlEngine.marshal(jobConfig));
        jobConfigPOJO.getProps().setProperty("create_time", LocalDateTime.now().format(DATE_TIME_FORMATTER));
        return YamlEngine.marshal(jobConfigPOJO);
    }
    
    @Override
    public void stop(final long jobId) {
        log.info("Stop scaling job {}", jobId);
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        jobConfigPOJO.setDisabled(true);
        jobConfigPOJO.getProps().setProperty("stop_time", LocalDateTime.now().format(DATE_TIME_FORMATTER));
        ScalingAPIFactory.getJobConfigurationAPI().updateJobConfiguration(jobConfigPOJO);
    }
    
    @Override
    public void remove(final long jobId) {
        log.info("Remove scaling job {}", jobId);
        ScalingAPIFactory.getJobOperateAPI().remove(String.valueOf(jobId), null);
        ScalingAPIFactory.getGovernanceRepositoryAPI().deleteJob(jobId);
    }
    
    @Override
    public Map<Integer, JobProgress> getProgress(final long jobId) {
        return IntStream.range(0, getJobConfig(jobId).getHandleConfig().getShardingTotalCount()).boxed()
                .collect(LinkedHashMap::new, (map, each) -> map.put(each, ScalingAPIFactory.getGovernanceRepositoryAPI().getJobProgress(jobId, each)), LinkedHashMap::putAll);
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> dataConsistencyCheck(final long jobId) {
        DataConsistencyChecker dataConsistencyChecker = EnvironmentCheckerFactory.newInstance(new JobContext(getJobConfig(jobId)));
        Map<String, DataConsistencyCheckResult> result = dataConsistencyChecker.countCheck();
        if (result.values().stream().allMatch(DataConsistencyCheckResult::isCountValid)) {
            Map<String, Boolean> dataCheckResult = dataConsistencyChecker.dataCheck();
            result.forEach((key, value) -> value.setDataValid(dataCheckResult.getOrDefault(key, false)));
        }
        log.info("Scaling job {} data consistency checker result {}", jobId, result);
        return result;
    }
    
    @Override
    public void reset(final long jobId) throws SQLException {
        log.info("Scaling job {} reset target table", jobId);
        ScalingAPIFactory.getGovernanceRepositoryAPI().deleteJobProgress(jobId);
        new ScalingEnvironmentManager().resetTargetTable(new JobContext(getJobConfig(jobId)));
    }
    
    @Override
    public JobConfiguration getJobConfig(final long jobId) {
        return getJobConfig(getElasticJobConfigPOJO(jobId));
    }
    
    private JobConfiguration getJobConfig(final JobConfigurationPOJO elasticJobConfigPOJO) {
        return YamlEngine.unmarshal(elasticJobConfigPOJO.getJobParameter(), JobConfiguration.class);
    }
    
    private JobConfigurationPOJO getElasticJobConfigPOJO(final long jobId) {
        try {
            return ScalingAPIFactory.getJobConfigurationAPI().getJobConfiguration(String.valueOf(jobId));
        } catch (final NullPointerException ex) {
            throw new ScalingJobNotFoundException(String.format("Can not find scaling job %s", jobId), jobId);
        }
    }
}
