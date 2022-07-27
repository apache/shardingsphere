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
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.api.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Rule altered job scheduler center.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
// TODO extract JobSchedulerCenter
public final class RuleAlteredJobSchedulerCenter {
    
    private static final Map<String, Map<Integer, RuleAlteredJobScheduler>> JOB_SCHEDULER_MAP = new ConcurrentHashMap<>();
    
    private static final ScheduledExecutorService JOB_PERSIST_EXECUTOR = Executors.newSingleThreadScheduledExecutor(ExecutorThreadFactoryBuilder.build("scaling-job-persist-%d"));
    
    static {
        JOB_PERSIST_EXECUTOR.scheduleWithFixedDelay(new PersistJobContextRunnable(), 10, 10, TimeUnit.SECONDS);
    }
    
    /**
     * Start a job.
     *
     * @param jobContext job context
     */
    public static void start(final RuleAlteredJobContext jobContext) {
        String jobId = jobContext.getJobId();
        Map<Integer, RuleAlteredJobScheduler> schedulerMap = JOB_SCHEDULER_MAP.computeIfAbsent(jobId, key -> new ConcurrentHashMap<>());
        int shardingItem = jobContext.getShardingItem();
        if (schedulerMap.containsKey(shardingItem)) {
            log.warn("schedulerMap contains shardingItem {}, ignore", shardingItem);
            return;
        }
        log.info("start RuleAlteredJobScheduler, jobId={}, shardingItem={}", jobId, shardingItem);
        RuleAlteredJobScheduler jobScheduler = new RuleAlteredJobScheduler(jobContext);
        jobScheduler.start();
        schedulerMap.put(shardingItem, jobScheduler);
    }
    
    /**
     * Stop a job.
     *
     * @param jobId job id
     */
    static void stop(final String jobId) {
        log.info("remove and stop {}", jobId);
        Map<Integer, RuleAlteredJobScheduler> schedulerMap = JOB_SCHEDULER_MAP.get(jobId);
        if (null == schedulerMap) {
            log.info("schedulerMap is null, ignore");
            return;
        }
        for (Entry<Integer, RuleAlteredJobScheduler> entry : schedulerMap.entrySet()) {
            entry.getValue().stop();
        }
        JOB_SCHEDULER_MAP.remove(jobId);
    }
    
    /**
     * Update job status for all job sharding.
     *
     * @param jobId job id
     * @param jobStatus job status
     */
    public static void updateJobStatus(final String jobId, final JobStatus jobStatus) {
        Map<Integer, RuleAlteredJobScheduler> schedulerMap = JOB_SCHEDULER_MAP.get(jobId);
        if (null == schedulerMap) {
            log.info("updateJobStatus, schedulerMap is null, ignore");
            return;
        }
        for (Entry<Integer, RuleAlteredJobScheduler> entry : schedulerMap.entrySet()) {
            entry.getValue().getJobContext().setStatus(jobStatus);
        }
    }
    
    private static final class PersistJobContextRunnable implements Runnable {
        
        @Override
        public void run() {
            GovernanceRepositoryAPI repositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI();
            for (Entry<String, Map<Integer, RuleAlteredJobScheduler>> entry : JOB_SCHEDULER_MAP.entrySet()) {
                try {
                    entry.getValue().forEach((shardingItem, jobScheduler) -> repositoryAPI.persistJobProgress(jobScheduler.getJobContext()));
                    // CHECKSTYLE:OFF
                } catch (final Exception ex) {
                    // CHECKSTYLE:ON
                    log.error("persist job {} context failed.", entry.getKey(), ex);
                }
            }
        }
    }
}
