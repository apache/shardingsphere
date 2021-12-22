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

package org.apache.shardingsphere.data.pipeline.scenario.rulealtered;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.HandleConfiguration;
import org.apache.shardingsphere.data.pipeline.api.detect.AllIncrementalTasksAlmostFinishedParameter;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.FinishedPosition;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;
import org.apache.shardingsphere.data.pipeline.spi.rulealtered.RuleAlteredJobCompletionDetectAlgorithm;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Rule altered job progress detector.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class RuleAlteredJobProgressDetector {
    
    /**
     * All inventory tasks is finished and all incremental tasks is almost finished.
     *
     * @param jobProgressMap job progress map
     * @param handleConfig handle configuration
     * @param ruleAlteredContext rule altered context
     * @return almost finished or not
     */
    public static boolean almostFinished(final Map<Integer, JobProgress> jobProgressMap, final HandleConfiguration handleConfig, final RuleAlteredContext ruleAlteredContext) {
        return isProgressCompleted(jobProgressMap, handleConfig)
                && allInventoryTasksFinished(jobProgressMap)
                && allIncrementalTasksAlmostFinished(jobProgressMap, ruleAlteredContext.getCompletionDetectAlgorithm());
    }
    
    private static boolean isProgressCompleted(final Map<Integer, JobProgress> jobProgressMap, final HandleConfiguration handleConfig) {
        return handleConfig.getJobShardingCount() == jobProgressMap.size()
                && jobProgressMap.values().stream().allMatch(Objects::nonNull);
    }
    
    private static boolean allIncrementalTasksAlmostFinished(final Map<Integer, JobProgress> jobProgressMap, final RuleAlteredJobCompletionDetectAlgorithm completionDetectAlgorithm) {
        long currentTimeMillis = System.currentTimeMillis();
        Collection<Long> incrementalTaskIdleMinutes = jobProgressMap.values().stream().flatMap(each -> each.getIncrementalTaskProgressMap().values().stream())
                .map(each -> {
                    long latestActiveTimeMillis = each.getIncrementalTaskDelay().getLatestActiveTimeMillis();
                    return latestActiveTimeMillis > 0 ? TimeUnit.MILLISECONDS.toMinutes(currentTimeMillis - latestActiveTimeMillis) : 0;
                })
                .collect(Collectors.toList());
        AllIncrementalTasksAlmostFinishedParameter parameter = AllIncrementalTasksAlmostFinishedParameter.builder().incrementalTaskIdleMinutes(incrementalTaskIdleMinutes).build();
        return completionDetectAlgorithm.allIncrementalTasksAlmostFinished(parameter);
    }
    
    /**
     * All inventory tasks is finished.
     *
     * @param inventoryTasks to check inventory tasks
     * @return is finished
     */
    public static boolean allInventoryTasksFinished(final List<InventoryTask> inventoryTasks) {
        if (inventoryTasks.isEmpty()) {
            log.warn("inventoryTasks is empty");
        }
        return inventoryTasks.stream().allMatch(each -> each.getProgress().getPosition() instanceof FinishedPosition);
    }
    
    private static boolean allInventoryTasksFinished(final Map<Integer, JobProgress> jobProgress) {
        return jobProgress.values().stream()
                .flatMap(each -> each.getInventoryTaskProgressMap().values().stream())
                .allMatch(each -> each.getPosition() instanceof FinishedPosition);
    }
}
