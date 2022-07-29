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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.job.persist.JobPersistIntervalParameter;
import org.apache.shardingsphere.data.pipeline.core.api.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Rule altered job persist service.
 */

@Slf4j
public final class RuleAlteredJobPersistService {
    
    private static final GovernanceRepositoryAPI REPOSITORY_API = PipelineAPIFactory.getGovernanceRepositoryAPI();
    
    private static final Executor SINGLE_EXECUTOR = Executors.newSingleThreadExecutor(ExecutorThreadFactoryBuilder.build("scaling-job-single-%d"));
    
    private static final ScheduledExecutorService JOB_PERSIST_EXECUTOR = Executors.newSingleThreadScheduledExecutor(ExecutorThreadFactoryBuilder.build("scaling-job-schedule-%d"));
    
    private static final Map<String, Map<Integer, RuleAlteredJobScheduler>> JOB_SCHEDULER_MAP = new ConcurrentHashMap<>();
    
    private static final Map<String, Map<Integer, JobPersistIntervalParameter>> JOB_PERSIST_MAP = new ConcurrentHashMap<>();
    
    private static final int MIN_PERSIST_INTERVAL_MILLIS = 1000;
    
    static {
        JOB_PERSIST_EXECUTOR.scheduleWithFixedDelay(new PersistJobContextRunnable(), 5, 2, TimeUnit.SECONDS);
    }
    
    /**
     * Get job schedule.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @return job schedule
     */
    public static Optional<RuleAlteredJobScheduler> getJobSchedule(final String jobId, final int shardingItem) {
        Map<Integer, RuleAlteredJobScheduler> schedulerMap = JOB_SCHEDULER_MAP.computeIfAbsent(jobId, key -> new ConcurrentHashMap<>());
        return Optional.ofNullable(schedulerMap.get(shardingItem));
    }
    
    /**
     * List job schedules.
     *
     * @param jobId job id
     * @return job schedules belong this job id
     */
    public static Collection<RuleAlteredJobScheduler> listJobSchedule(final String jobId) {
        Map<Integer, RuleAlteredJobScheduler> schedulerMap = JOB_SCHEDULER_MAP.computeIfAbsent(jobId, key -> new ConcurrentHashMap<>());
        return schedulerMap.values();
    }
    
    /**
     * Remove job schedule map by job id.
     *
     * @param jobId job id
     */
    public static void removeJobSchedule(final String jobId) {
        log.info("Remove job schedule by job id: {}", jobId);
        JOB_SCHEDULER_MAP.remove(jobId);
        JOB_PERSIST_MAP.remove(jobId);
    }
    
    /**
     * Add job schedule.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @param jobScheduler job schedule
     */
    public static void addJobSchedule(final String jobId, final int shardingItem, final RuleAlteredJobScheduler jobScheduler) {
        log.info("Add job schedule, jobId={}, shardingItem={}", jobId, shardingItem);
        JOB_SCHEDULER_MAP.computeIfAbsent(jobId, key -> new ConcurrentHashMap<>()).put(shardingItem, jobScheduler);
        JOB_PERSIST_MAP.computeIfAbsent(jobId, key -> new ConcurrentHashMap<>()).put(shardingItem, new JobPersistIntervalParameter(jobId, shardingItem));
    }
    
    /**
     * Persist job process, may not be implemented immediately, depending on persist interval.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @param currentTimeMills current time mills
     */
    public static void triggerPersist(final String jobId, final int shardingItem, final long currentTimeMills) {
        Map<Integer, JobPersistIntervalParameter> intervalParamMap = JOB_PERSIST_MAP.getOrDefault(jobId, Collections.emptyMap());
        JobPersistIntervalParameter parameter = intervalParamMap.get(shardingItem);
        if (null == parameter) {
            log.debug("Persist interval parameter is null, jobId={}, shardingItem={}", jobId, shardingItem);
            return;
        }
        parameter.getAlreadyPersisted().compareAndSet(true, false);
        if ((currentTimeMills - parameter.getPersistTime().get()) < MIN_PERSIST_INTERVAL_MILLIS) {
            return;
        }
        if (parameter.getLock().tryLock()) {
            try {
                SINGLE_EXECUTOR.execute(() -> persist(jobId, shardingItem, currentTimeMills, parameter));
                // CHECKSTYLE:OFF
            } catch (final RuntimeException ex) {
                // CHECKSTYLE:ON
                log.error("Persist job process failed, job id: {}, sharding item: {}", jobId, shardingItem, ex);
            } finally {
                parameter.getLock().unlock();
            }
        }
    }
    
    private static void persist(final String jobId, final int shardingItem, final long persistTimeMillis, final JobPersistIntervalParameter param) {
        Optional<RuleAlteredJobScheduler> jobSchedule = getJobSchedule(jobId, shardingItem);
        if (!jobSchedule.isPresent()) {
            log.warn("job schedule not exists, job id: {}, sharding item: {}", jobId, shardingItem);
            return;
        }
        log.info("execute persist, job id={}, sharding item={}, persistTimeMillis={}", jobId, shardingItem, persistTimeMillis);
        REPOSITORY_API.persistJobProgress(jobSchedule.get().getJobContext());
        param.getPersistTime().set(persistTimeMillis);
        param.getAlreadyPersisted().set(true);
    }
    
    private static final class PersistJobContextRunnable implements Runnable {
        
        @Override
        public void run() {
            long currentTimeMillis = System.currentTimeMillis();
            for (Entry<String, Map<Integer, JobPersistIntervalParameter>> entry : JOB_PERSIST_MAP.entrySet()) {
                entry.getValue().forEach((shardingItem, param) -> {
                    if (param.getAlreadyPersisted().get() || currentTimeMillis - param.getPersistTime().get() < MIN_PERSIST_INTERVAL_MILLIS) {
                        return;
                    }
                    boolean tryLock = false;
                    try {
                        tryLock = param.getLock().tryLock();
                        if (tryLock) {
                            persist(entry.getKey(), shardingItem, currentTimeMillis, param);
                        }
                    } finally {
                        if (tryLock) {
                            param.getLock().unlock();
                        }
                    }
                });
            }
        }
    }
}
