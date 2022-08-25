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
import org.apache.shardingsphere.data.pipeline.api.detect.RuleAlteredJobAlmostCompletedParameter;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.FinishedPosition;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.spi.detect.JobCompletionDetectAlgorithm;

import java.util.Collection;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Idle rule altered job completion detect algorithm.
 */
public final class IdleRuleAlteredJobCompletionDetectAlgorithm implements JobCompletionDetectAlgorithm<RuleAlteredJobAlmostCompletedParameter> {
    
    private static final String IDLE_SECOND_THRESHOLD_KEY = "incremental-task-idle-seconds-threshold";
    
    private static final long DEFAULT_IDLE_SECONDS_THRESHOLD = 1800L;
    
    @Getter
    private Properties props;
    
    private volatile long incrementalTaskIdleSecondsThreshold;
    
    @Override
    public void init(final Properties props) {
        this.props = props;
        incrementalTaskIdleSecondsThreshold = getIncrementalTaskIdleSecondsThreshold(props);
    }
    
    private long getIncrementalTaskIdleSecondsThreshold(final Properties props) {
        long result = Long.parseLong(props.getOrDefault(IDLE_SECOND_THRESHOLD_KEY, DEFAULT_IDLE_SECONDS_THRESHOLD).toString());
        Preconditions.checkArgument(result > 0, "Incremental task idle threshold seconds must be positive.");
        return result;
    }
    
    @Override
    public boolean isAlmostCompleted(final RuleAlteredJobAlmostCompletedParameter parameter) {
        int jobShardingCount = parameter.getJobShardingCount();
        Collection<InventoryIncrementalJobItemProgress> jobItemProgresses = parameter.getJobItemProgresses();
        if (!isAllProgressesFilled(jobShardingCount, jobItemProgresses)) {
            return false;
        }
        if (!isAllInventoryTasksCompleted(jobItemProgresses)) {
            return false;
        }
        Collection<Long> incrementalTasksIdleSeconds = getIncrementalTasksIdleSeconds(jobItemProgresses);
        return incrementalTasksIdleSeconds.stream().allMatch(each -> each >= incrementalTaskIdleSecondsThreshold);
    }
    
    private static boolean isAllProgressesFilled(final int jobShardingCount, final Collection<InventoryIncrementalJobItemProgress> jobItemProgresses) {
        return jobShardingCount == jobItemProgresses.size() && jobItemProgresses.stream().allMatch(Objects::nonNull);
    }
    
    private static boolean isAllInventoryTasksCompleted(final Collection<InventoryIncrementalJobItemProgress> jobItemProgresses) {
        return jobItemProgresses.stream().flatMap(each -> each.getInventory().getInventoryTaskProgressMap().values().stream()).allMatch(each -> each.getPosition() instanceof FinishedPosition);
    }
    
    private static Collection<Long> getIncrementalTasksIdleSeconds(final Collection<InventoryIncrementalJobItemProgress> jobItemProgresses) {
        long currentTimeMillis = System.currentTimeMillis();
        return jobItemProgresses.stream().flatMap(each -> each.getIncremental().getIncrementalTaskProgressMap().values().stream())
                .map(each -> {
                    long latestActiveTimeMillis = each.getIncrementalTaskDelay().getLatestActiveTimeMillis();
                    return latestActiveTimeMillis > 0 ? TimeUnit.MILLISECONDS.toSeconds(currentTimeMillis - latestActiveTimeMillis) : 0;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public String getType() {
        return "IDLE";
    }
    
    @Override
    public String toString() {
        return "IdleRuleAlteredJobCompletionDetectAlgorithm{" + "props=" + props + '}';
    }
}
