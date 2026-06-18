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

package org.apache.shardingsphere.data.pipeline.core.job.progress;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.finished.IngestFinishedPosition;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTask;

import java.util.Collection;
import java.util.Objects;

/**
 * Pipeline job progress detector.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class PipelineJobProgressDetector {
    
    /**
     * Whether all inventory tasks is finished.
     *
     * @param inventoryTasks to check inventory tasks
     * @return finished or not
     */
    public static boolean isAllInventoryTasksFinished(final Collection<PipelineTask> inventoryTasks) {
        if (inventoryTasks.isEmpty()) {
            log.warn("inventoryTasks is empty");
        }
        return inventoryTasks.stream().allMatch(each -> each.getTaskProgress().getPosition() instanceof IngestFinishedPosition);
    }
    
    /**
     * Whether inventory is finished or not.
     *
     * @param jobShardingCount job sharding count
     * @param jobItemProgresses job item progresses
     * @return finished or not
     */
    public static boolean isInventoryFinished(final int jobShardingCount, final Collection<TransmissionJobItemProgress> jobItemProgresses) {
        return isAllProgressesFilled(jobShardingCount, jobItemProgresses) && isAllInventoryTasksCompleted(jobItemProgresses);
    }
    
    private static boolean isAllProgressesFilled(final int jobShardingCount, final Collection<TransmissionJobItemProgress> jobItemProgresses) {
        return jobShardingCount == jobItemProgresses.size() && jobItemProgresses.stream().allMatch(Objects::nonNull);
    }
    
    private static boolean isAllInventoryTasksCompleted(final Collection<TransmissionJobItemProgress> jobItemProgresses) {
        if (jobItemProgresses.stream().allMatch(each -> each.getInventory().getProgresses().isEmpty())) {
            return false;
        }
        return jobItemProgresses.stream().flatMap(each -> each.getInventory().getProgresses().values().stream()).allMatch(each -> each.getPosition() instanceof IngestFinishedPosition);
    }
}
