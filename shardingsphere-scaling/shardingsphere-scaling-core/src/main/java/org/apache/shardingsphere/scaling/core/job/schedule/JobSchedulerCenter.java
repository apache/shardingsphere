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

package org.apache.shardingsphere.scaling.core.job.schedule;

import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;
import org.apache.shardingsphere.scaling.core.api.GovernanceRepositoryAPI;
import org.apache.shardingsphere.scaling.core.api.ScalingAPIFactory;
import org.apache.shardingsphere.scaling.core.job.JobContext;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Job scheduler center.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class JobSchedulerCenter {
    
    private static final Map<Long, Map<Integer, JobScheduler>> JOB_SCHEDULER_MAP = Maps.newConcurrentMap();
    
    private static final ScheduledExecutorService JOB_PERSIST_EXECUTOR = Executors.newSingleThreadScheduledExecutor(ExecutorThreadFactoryBuilder.build("scaling-job-persist-%d"));
    
    private static final GovernanceRepositoryAPI REGISTRY_REPOSITORY_API = ScalingAPIFactory.getGovernanceRepositoryAPI();
    
    static {
        JOB_PERSIST_EXECUTOR.scheduleWithFixedDelay(new PersistJobContextRunnable(), 1, 1, TimeUnit.MINUTES);
    }
    
    /**
     * Start a job.
     *
     * @param jobContext job context
     */
    public static void start(final JobContext jobContext) {
        long jobId = jobContext.getJobId();
        Map<Integer, JobScheduler> schedulerMap = JOB_SCHEDULER_MAP.computeIfAbsent(jobId, key -> Maps.newConcurrentMap());
        int shardingItem = jobContext.getShardingItem();
        if (schedulerMap.containsKey(shardingItem)) {
            return;
        }
        JobScheduler jobScheduler = new JobScheduler(jobContext);
        jobScheduler.start();
        schedulerMap.put(shardingItem, jobScheduler);
    }
    
    /**
     * Stop a job.
     *
     * @param jobId job id
     */
    public static void stop(final long jobId) {
        Map<Integer, JobScheduler> schedulerMap = JOB_SCHEDULER_MAP.remove(jobId);
        if (null == schedulerMap) {
            return;
        }
        for (Entry<Integer, JobScheduler> entry : schedulerMap.entrySet()) {
            entry.getValue().stop();
        }
    }
    
    /**
     * Get job contexts.
     *
     * @param jobId job id
     * @return job context
     */
    public static Optional<Collection<JobContext>> getJobContexts(final long jobId) {
        Map<Integer, JobScheduler> schedulerMap = JOB_SCHEDULER_MAP.get(jobId);
        if (null == schedulerMap) {
            return Optional.empty();
        }
        return Optional.of(schedulerMap.values().stream().map(JobScheduler::getJobContext).collect(Collectors.toList()));
    }
    
    /**
     * Persist job progress.
     *
     * @param jobContext job context
     */
    public static void persistJobProgress(final JobContext jobContext) {
        REGISTRY_REPOSITORY_API.persistJobProgress(jobContext);
    }
    
    private static final class PersistJobContextRunnable implements Runnable {
        
        @Override
        public void run() {
            for (Entry<Long, Map<Integer, JobScheduler>> entry : JOB_SCHEDULER_MAP.entrySet()) {
                try {
                    entry.getValue().forEach((shardingItem, jobScheduler) -> REGISTRY_REPOSITORY_API.persistJobProgress(jobScheduler.getJobContext()));
                    // CHECKSTYLE:OFF
                } catch (final Exception ex) {
                    // CHECKSTYLE:ON
                    log.error("persist job {} context failed.", entry.getKey(), ex);
                }
            }
        }
    }
}
