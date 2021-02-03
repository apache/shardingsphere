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
import com.google.gson.GsonBuilder;
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
import org.apache.shardingsphere.scaling.core.job.check.DataConsistencyCheckResult;
import org.apache.shardingsphere.scaling.core.job.check.DataConsistencyChecker;
import org.apache.shardingsphere.scaling.core.job.check.DataConsistencyCheckerFactory;
import org.apache.shardingsphere.scaling.core.job.environmental.ScalingEnvironmentalManager;
import org.apache.shardingsphere.scaling.core.job.position.FinishedPosition;
import org.apache.shardingsphere.scaling.core.job.progress.JobProgress;
import org.apache.shardingsphere.scaling.core.util.JobConfigurationUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
public final class ScalingAPIImpl implements ScalingAPI {
    
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
    
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
        Map<Integer, JobProgress> jobProgressMap = getProgress(result.getJobId());
        result.setActive(!jobConfigPOJO.isDisabled());
        result.setShardingTotalCount(jobConfig.getHandleConfig().getShardingTotalCount());
        result.setTables(jobConfig.getHandleConfig().getShardingTables());
        result.setStatus(getStatus(jobProgressMap));
        result.setInventoryFinishedPercentage(getInventoryFinishedPercentage(jobProgressMap));
        result.setIncrementalAverageDelayMilliseconds(getIncrementalAverageDelayMilliseconds(jobProgressMap));
        return result;
    }
    
    private String getStatus(final Map<Integer, JobProgress> jobProgressMap) {
        Stream<JobProgress> jobPositionStream = jobProgressMap.values().stream()
                .filter(Objects::nonNull);
        Optional<JobProgress> jobPositionOptional = jobPositionStream
                .filter(each -> !each.getStatus().isRunning())
                .reduce((a, b) -> a);
        return jobPositionOptional.orElse(jobPositionStream.findAny().orElse(new JobProgress())).getStatus().name();
    }
    
    private int getInventoryFinishedPercentage(final Map<Integer, JobProgress> jobProgressMap) {
        long isNull = jobProgressMap.values().stream()
                .filter(Objects::isNull).count();
        long total = jobProgressMap.values().stream()
                .filter(Objects::nonNull).count();
        long finished = jobProgressMap.values().stream()
                .filter(Objects::nonNull)
                .flatMap(each -> each.getIncrementalTaskProgressMap().values().stream())
                .filter(each -> each.getPosition() instanceof FinishedPosition)
                .count();
        return (int) ((finished * 100 / total) * (jobProgressMap.size() - isNull) / jobProgressMap.size());
    }
    
    private long getIncrementalAverageDelayMilliseconds(final Map<Integer, JobProgress> jobProgressMap) {
        List<Long> delays = jobProgressMap.values().stream()
                .filter(Objects::nonNull)
                .flatMap(each -> each.getIncrementalTaskProgressMap().values().stream())
                .map(each -> each.getIncrementalTaskDelay().getDelayMilliseconds())
                .collect(Collectors.toList());
        return delays.isEmpty() ? -1 : delays.stream().reduce(Long::sum).orElse(0L) / delays.size();
    }
    
    @Override
    public void start(final long jobId) {
        log.info("Start scaling job {}", jobId);
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        jobConfigPOJO.setDisabled(false);
        ScalingAPIFactory.getJobConfigurationAPI().updateJobConfiguration(jobConfigPOJO);
    }
    
    @Override
    public Optional<Long> start(final JobConfiguration jobConfig) {
        log.info("Start scaling job by {}", jobConfig);
        JobConfigurationUtil.fillInProperties(jobConfig);
        if (jobConfig.getHandleConfig().getShardingTotalCount() == 0) {
            return Optional.empty();
        }
        ScalingAPIFactory.getRegistryRepositoryAPI().persist(String.format("%s/%d", ScalingConstant.SCALING_ROOT, jobConfig.getHandleConfig().getJobId()), ScalingJob.class.getCanonicalName());
        ScalingAPIFactory.getRegistryRepositoryAPI().persist(String.format("%s/%d/config", ScalingConstant.SCALING_ROOT, jobConfig.getHandleConfig().getJobId()), createElasticJobConfig(jobConfig));
        return Optional.of(jobConfig.getHandleConfig().getJobId());
    }
    
    private String createElasticJobConfig(final JobConfiguration jobConfig) {
        JobConfigurationPOJO jobConfigPOJO = new JobConfigurationPOJO();
        jobConfigPOJO.setJobName(String.valueOf(jobConfig.getHandleConfig().getJobId()));
        jobConfigPOJO.setShardingTotalCount(jobConfig.getHandleConfig().getShardingTotalCount());
        jobConfigPOJO.setJobParameter(GSON.toJson(jobConfig));
        return YamlEngine.marshal(jobConfigPOJO);
    }
    
    @Override
    public void stop(final long jobId) {
        log.info("Stop scaling job {}", jobId);
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        jobConfigPOJO.setDisabled(true);
        ScalingAPIFactory.getJobConfigurationAPI().updateJobConfiguration(jobConfigPOJO);
    }
    
    @Override
    public void remove(final long jobId) {
        log.info("Remove scaling job {}", jobId);
        ScalingAPIFactory.getRegistryRepositoryAPI().deleteJob(jobId);
    }
    
    @Override
    public Map<Integer, JobProgress> getProgress(final long jobId) {
        return IntStream.range(0, getJobConfig(jobId).getHandleConfig().getShardingTotalCount()).boxed()
                .collect(HashMap::new, (map, each) -> map.put(each, ScalingAPIFactory.getRegistryRepositoryAPI().getJobProgress(jobId, each)), HashMap::putAll);
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> dataConsistencyCheck(final long jobId) {
        DataConsistencyChecker dataConsistencyChecker = DataConsistencyCheckerFactory.newInstance(new JobContext(getJobConfig(jobId)));
        Map<String, DataConsistencyCheckResult> result = dataConsistencyChecker.countCheck();
        if (result.values().stream().allMatch(DataConsistencyCheckResult::isCountValid)) {
            Map<String, Boolean> dataCheckResult = dataConsistencyChecker.dataCheck();
            result.forEach((key, value) -> value.setDataValid(dataCheckResult.getOrDefault(key, false)));
        }
        log.info("Scaling job {} data consistency checker result {}", jobId, result);
        return result;
    }
    
    @Override
    public void resetTargetTable(final long jobId) throws SQLException {
        log.info("Scaling job {} reset target table", jobId);
        new ScalingEnvironmentalManager().resetTargetTable(new JobContext(getJobConfig(jobId)));
    }
    
    @Override
    public JobConfiguration getJobConfig(final long jobId) {
        return getJobConfig(getElasticJobConfigPOJO(jobId));
    }
    
    private JobConfiguration getJobConfig(final JobConfigurationPOJO elasticJobConfigPOJO) {
        return GSON.fromJson(elasticJobConfigPOJO.getJobParameter(), JobConfiguration.class);
    }
    
    private JobConfigurationPOJO getElasticJobConfigPOJO(final long jobId) {
        try {
            return ScalingAPIFactory.getJobConfigurationAPI().getJobConfiguration(String.valueOf(jobId));
        } catch (final NullPointerException ex) {
            log.warn("Get job {} config failed.", jobId);
            throw new ScalingJobNotFoundException(String.format("Can not find job by id %s", jobId));
        }
    }
}
