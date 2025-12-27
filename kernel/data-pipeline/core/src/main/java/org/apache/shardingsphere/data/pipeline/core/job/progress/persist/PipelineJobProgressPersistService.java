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

package org.apache.shardingsphere.data.pipeline.core.job.progress.persist;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobRegistry;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Pipeline job progress persist service.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class PipelineJobProgressPersistService {
    
    private static final Map<String, Map<Integer, PipelineJobProgressPersistContext>> JOB_PROGRESS_PERSIST_MAP = new ConcurrentHashMap<>();
    
    private static final ScheduledExecutorService JOB_PERSIST_EXECUTOR = Executors.newSingleThreadScheduledExecutor(ExecutorThreadFactoryBuilder.build("pipeline-progress-persist-%d"));
    
    private static final long DELAY_SECONDS = 1L;
    
    static {
        JOB_PERSIST_EXECUTOR.scheduleWithFixedDelay(new PersistJobContextRunnable(), 0L, DELAY_SECONDS, TimeUnit.SECONDS);
    }
    
    /**
     * Add job progress persist context.
     *
     * @param jobId job ID
     * @param shardingItem sharding item
     */
    public static void add(final String jobId, final int shardingItem) {
        JOB_PROGRESS_PERSIST_MAP.computeIfAbsent(jobId, key -> new ConcurrentHashMap<>()).put(shardingItem, new PipelineJobProgressPersistContext(jobId, shardingItem));
    }
    
    /**
     * Remove job progress persist context.
     *
     * @param jobId job ID
     */
    public static void remove(final String jobId) {
        JOB_PROGRESS_PERSIST_MAP.remove(jobId);
    }
    
    /**
     * Notify persist.
     *
     * @param jobId job ID
     * @param shardingItem sharding item
     */
    public static void notifyPersist(final String jobId, final int shardingItem) {
        getPersistContext(jobId, shardingItem).ifPresent(optional -> optional.getUnhandledEventCount().incrementAndGet());
    }
    
    /**
     * Persist now.
     *
     * @param jobId job ID
     * @param shardingItem sharding item
     */
    public static void persistNow(final String jobId, final int shardingItem) {
        getPersistContext(jobId, shardingItem).ifPresent(optional -> PersistJobContextRunnable.persist(jobId, shardingItem, optional));
    }
    
    private static Optional<PipelineJobProgressPersistContext> getPersistContext(final String jobId, final int shardingItem) {
        return Optional.ofNullable(JOB_PROGRESS_PERSIST_MAP.getOrDefault(jobId, Collections.emptyMap()).get(shardingItem));
    }
    
    private static final class PersistJobContextRunnable implements Runnable {
        
        @Override
        public void run() {
            for (Entry<String, Map<Integer, PipelineJobProgressPersistContext>> entry : JOB_PROGRESS_PERSIST_MAP.entrySet()) {
                entry.getValue().forEach((shardingItem, persistContext) -> persist(entry.getKey(), shardingItem, persistContext));
            }
        }
        
        private static synchronized void persist(final String jobId, final int shardingItem, final PipelineJobProgressPersistContext persistContext) {
            try {
                persist0(jobId, shardingItem, persistContext);
                // CHECKSTYLE:OFF
            } catch (final RuntimeException ex) {
                // CHECKSTYLE:ON
                if (!persistContext.getFirstExceptionLogged().get()) {
                    log.error("Persist job progress failed, jobId={}, shardingItem={}", jobId, shardingItem, ex);
                    persistContext.getFirstExceptionLogged().set(true);
                } else if (5 == ThreadLocalRandom.current().nextInt(60)) {
                    log.error("Persist job progress failed, jobId={}, shardingItem={}", jobId, shardingItem, ex);
                }
            }
        }

        private static void persist0(final String jobId, final int shardingItem, final PipelineJobProgressPersistContext persistContext) {
            long currentUnhandledEventCount = persistContext.getUnhandledEventCount().get();

            // Negative count is an error path (tests rely on this)
            if (currentUnhandledEventCount < 0L) {
                throw new IllegalStateException("Current unhandled event count must be greater than or equal to 0");
            }

            if (0L == currentUnhandledEventCount) {
                return;
            }

            Optional<PipelineJobItemContext> jobItemContext = PipelineJobRegistry.getItemContext(jobId, shardingItem);
            if (!jobItemContext.isPresent()) {
                return;
            }

            long startTimeMillis = System.currentTimeMillis();
            new PipelineJobItemManager<>(
                    TypedSPILoader.getService(
                            PipelineJobType.class,
                            PipelineJobIdUtils.parseJobType(jobId).getType()
                    ).getOption().getYamlJobItemProgressSwapper()
            ).updateProgress(jobItemContext.get());

            // Reset ONLY after successful persist
            persistContext.getUnhandledEventCount().set(0L);

            if (6 == ThreadLocalRandom.current().nextInt(100)) {
                log.info("persist, jobId={}, shardingItem={}, cost {} ms",
                        jobId, shardingItem, System.currentTimeMillis() - startTimeMillis);
            }
        }
    }
}
