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

package org.apache.shardingsphere.data.pipeline.core.job;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.task.runner.PipelineTasksRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pipeline job registry.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PipelineJobRegistry {
    
    private static final Map<String, PipelineJob> JOBS = new ConcurrentHashMap<>();
    
    /**
     * Add pipeline job.
     *
     * @param jobId pipeline job id
     * @param job pipeline job
     */
    public static void add(final String jobId, final PipelineJob job) {
        JOBS.put(jobId, job);
    }
    
    /**
     * Judge whether pipeline job existing.
     *
     * @param jobId pipeline job id
     * @return pipeline job exists or not
     */
    public static boolean isExisting(final String jobId) {
        return JOBS.containsKey(jobId);
    }
    
    /**
     * Get pipeline job.
     *
     * @param jobId pipeline job id
     * @return pipeline job
     */
    public static PipelineJob get(final String jobId) {
        return JOBS.get(jobId);
    }
    
    /**
     * Stop pipeline job.
     *
     * @param jobId pipeline job id
     */
    public static void stop(final String jobId) {
        PipelineJob job = JOBS.get(jobId);
        if (null == job) {
            return;
        }
        job.getJobRunnerManager().stop();
        JOBS.remove(jobId);
    }
    
    /**
     * Get pipeline job item context.
     *
     * @param jobId pipeline job id
     * @param shardingItem sharding item
     * @return pipeline job item context
     */
    public static Optional<PipelineJobItemContext> getItemContext(final String jobId, final int shardingItem) {
        return JOBS.containsKey(jobId) ? JOBS.get(jobId).getJobRunnerManager().getTasksRunner(shardingItem).map(PipelineTasksRunner::getJobItemContext) : Optional.empty();
    }
    
    /**
     * Get sharding items.
     *
     * @param jobId pipeline job id
     * @return sharding items
     */
    public static Collection<Integer> getShardingItems(final String jobId) {
        return JOBS.containsKey(jobId) ? JOBS.get(jobId).getJobRunnerManager().getShardingItems() : Collections.emptyList();
    }
}
