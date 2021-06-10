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

package org.apache.shardingsphere.scaling.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.scaling.core.config.HandleConfiguration;
import org.apache.shardingsphere.scaling.core.job.position.FinishedPosition;
import org.apache.shardingsphere.scaling.core.job.progress.JobProgress;
import org.apache.shardingsphere.scaling.core.job.task.inventory.InventoryTask;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Scaling task util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ScalingTaskUtil {
    
    /**
     * All inventory tasks is finished and all incremental tasks is almost finished.
     *
     * @param jobProgressMap job progress map
     * @param handleConfig handle configuration
     * @return almost finished or not
     */
    public static boolean almostFinished(final Map<Integer, JobProgress> jobProgressMap, final HandleConfiguration handleConfig) {
        return isProgressCompleted(jobProgressMap, handleConfig)
                && allInventoryTasksFinished(jobProgressMap)
                && allIncrementalTasksAlmostFinished(jobProgressMap, handleConfig);
    }
    
    private static boolean isProgressCompleted(final Map<Integer, JobProgress> jobProgressMap, final HandleConfiguration handleConfig) {
        return handleConfig.getShardingTotalCount() == jobProgressMap.size()
                && jobProgressMap.values().stream().allMatch(Objects::nonNull);
    }
    
    private static boolean allIncrementalTasksAlmostFinished(final Map<Integer, JobProgress> jobProgressMap, final HandleConfiguration handleConfig) {
        return jobProgressMap.values().stream()
                .flatMap(each -> each.getIncrementalTaskProgressMap().values().stream())
                .allMatch(each -> each.getIncrementalTaskDelay().getDelayMilliseconds() <= handleConfig.getWorkflowConfig().getAllowDelayMilliseconds());
    }
    
    /**
     * All inventory tasks is finished.
     *
     * @param inventoryTasks to check inventory tasks
     * @return is finished
     */
    public static boolean allInventoryTasksFinished(final List<InventoryTask> inventoryTasks) {
        return inventoryTasks.stream().allMatch(each -> each.getProgress().getPosition() instanceof FinishedPosition);
    }
    
    private static boolean allInventoryTasksFinished(final Map<Integer, JobProgress> jobProgress) {
        return jobProgress.values().stream()
                .flatMap(each -> each.getInventoryTaskProgressMap().values().stream())
                .allMatch(each -> each.getPosition() instanceof FinishedPosition);
    }
}
