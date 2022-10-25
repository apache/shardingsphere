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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.api.job.PipelineJob;
import org.apache.shardingsphere.data.pipeline.api.task.PipelineTasksRunner;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineDistributedBarrier;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Pipeline job center.
 */
@Slf4j
public final class PipelineJobCenter {
    
    private static final Map<String, PipelineJob> JOB_MAP = new ConcurrentHashMap<>();
    
    private static final PipelineDistributedBarrier DISTRIBUTED_BARRIER = PipelineDistributedBarrier.getInstance();
    
    
    /**
     * Add job.
     *
     * @param jobId job id
     * @param job job
     */
    public static void addJob(final String jobId, final PipelineJob job) {
        log.info("add job, jobId={}", jobId);
        JOB_MAP.put(jobId, job);
    }
    
    /**
     * Is job existing.
     *
     * @param jobId job id
     * @return true when job exists, else false
     */
    public static boolean isJobExisting(final String jobId) {
        return JOB_MAP.containsKey(jobId);
    }
    
    /**
     * Stop job.
     *
     * @param jobId job id
     * @param isAsync is async to stop
     */
    public static void stop(final String jobId, final boolean isAsync) {
        PipelineJob job = JOB_MAP.get(jobId);
        if (null == job) {
            log.info("job is null, ignore, jobId={}", jobId);
            return;
        }
        job.stop();
        if (!isAsync) {
            String jobBarrierDisablePath = PipelineMetaDataNode.getJobBarrierDisablePath(jobId);
            // all item already stopped, only need persist 0, because
            DISTRIBUTED_BARRIER.persistEphemeralChildrenNode(jobBarrierDisablePath, 0);
        }
        log.info("remove job, jobId={}", jobId);
        JOB_MAP.remove(jobId);
    }
    
    /**
     * Get job item context.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @return job item context
     */
    public static Optional<PipelineJobItemContext> getJobItemContext(final String jobId, final int shardingItem) {
        PipelineJob job = JOB_MAP.get(jobId);
        if (null == job) {
            return Optional.empty();
        }
        Optional<PipelineTasksRunner> tasksRunner = job.getTasksRunner(shardingItem);
        return tasksRunner.map(PipelineTasksRunner::getJobItemContext);
    }
}
