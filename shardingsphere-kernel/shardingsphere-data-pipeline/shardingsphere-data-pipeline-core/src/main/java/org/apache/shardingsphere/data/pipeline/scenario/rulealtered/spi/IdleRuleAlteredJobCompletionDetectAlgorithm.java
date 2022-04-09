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

package org.apache.shardingsphere.data.pipeline.scenario.rulealtered.spi;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.apache.shardingsphere.data.pipeline.api.detect.RuleAlteredJobAlmostCompletedParameter;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.FinishedPosition;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.spi.detect.JobCompletionDetectAlgorithm;

import java.util.Collection;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Idle rule altered job completion detect algorithm.
 */
@Getter
@Setter
public final class IdleRuleAlteredJobCompletionDetectAlgorithm implements JobCompletionDetectAlgorithm<RuleAlteredJobAlmostCompletedParameter> {
    
    public static final String IDLE_MINUTE_THRESHOLD_KEY = "incremental-task-idle-minute-threshold";
    
    public static final String IDLE_SECOND_THRESHOLD_KEY = "incremental-task-idle-second-threshold";
    
    public static final long DEFAULT_IDLE_SECOND_THRESHOLD = TimeUnit.MINUTES.toSeconds(30);
    
    private Properties props = new Properties();
    
    private long incrementalTaskIdleSecondThreshold = DEFAULT_IDLE_SECOND_THRESHOLD;
    
    @Override
    public void init() {
        Preconditions.checkArgument(props.containsKey(IDLE_MINUTE_THRESHOLD_KEY) || props.containsKey(IDLE_SECOND_THRESHOLD_KEY), "incremental task idle threshold can not be null.");
        if (props.containsKey(IDLE_SECOND_THRESHOLD_KEY)) {
            incrementalTaskIdleSecondThreshold = Long.parseLong(props.getProperty(IDLE_SECOND_THRESHOLD_KEY));
        } else {
            incrementalTaskIdleSecondThreshold = TimeUnit.MINUTES.toSeconds(Long.parseLong(props.getProperty(IDLE_MINUTE_THRESHOLD_KEY)));
        }
        Preconditions.checkArgument(incrementalTaskIdleSecondThreshold > 0, "incremental task idle threshold must be positive.");
    }
    
    @Override
    public String getType() {
        return "IDLE";
    }
    
    @Override
    public boolean isAlmostCompleted(final RuleAlteredJobAlmostCompletedParameter parameter) {
        int jobShardingCount = parameter.getJobShardingCount();
        Collection<JobProgress> jobProgresses = parameter.getJobProgresses();
        if (!isAllProgressesFilled(jobShardingCount, jobProgresses)) {
            return false;
        }
        if (!isAllInventoryTasksCompleted(jobProgresses)) {
            return false;
        }
        Collection<Long> incrementalTasksIdleSeconds = getIncrementalTasksIdleSeconds(jobProgresses);
        return incrementalTasksIdleSeconds.stream().allMatch(each -> each >= incrementalTaskIdleSecondThreshold);
    }
    
    private static boolean isAllProgressesFilled(final int jobShardingCount, final Collection<JobProgress> jobProgresses) {
        return jobShardingCount == jobProgresses.size()
                && jobProgresses.stream().allMatch(Objects::nonNull);
    }
    
    private static boolean isAllInventoryTasksCompleted(final Collection<JobProgress> jobProgresses) {
        return jobProgresses.stream()
                .flatMap(each -> each.getInventoryTaskProgressMap().values().stream())
                .allMatch(each -> each.getPosition() instanceof FinishedPosition);
    }
    
    private static Collection<Long> getIncrementalTasksIdleSeconds(final Collection<JobProgress> jobProgresses) {
        long currentTimeMillis = System.currentTimeMillis();
        return jobProgresses.stream().flatMap(each -> each.getIncrementalTaskProgressMap().values().stream())
                .map(each -> {
                    long latestActiveTimeMillis = each.getIncrementalTaskDelay().getLatestActiveTimeMillis();
                    return latestActiveTimeMillis > 0 ? TimeUnit.MILLISECONDS.toSeconds(currentTimeMillis - latestActiveTimeMillis) : 0;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public String toString() {
        return "IdleRuleAlteredJobCompletionDetectAlgorithm{" + "props=" + props + '}';
    }
}
