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

package org.apache.shardingsphere.data.pipeline.core.job.engine;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.job.engine.cleaner.PipelineJobRunnerCleaner;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.persist.PipelineJobProgressPersistService;
import org.apache.shardingsphere.data.pipeline.core.listener.PipelineElasticJobListener;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.core.task.runner.PipelineTasksRunner;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineDistributedBarrier;
import org.apache.shardingsphere.elasticjob.infra.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.infra.spi.ElasticJobServiceLoader;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.JobBootstrap;
import org.apache.shardingsphere.infra.util.close.QuietlyCloser;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Pipeline job runner manager.
 */
@RequiredArgsConstructor
@Slf4j
public final class PipelineJobRunnerManager {
    
    private static final long JOB_WAITING_TIMEOUT_MILLIS = 2000L;
    
    private final PipelineJobRunnerCleaner cleaner;
    
    private final AtomicBoolean stopping = new AtomicBoolean(false);
    
    private final AtomicReference<JobBootstrap> jobBootstrap = new AtomicReference<>();
    
    private final Map<Integer, PipelineTasksRunner> tasksRunners = new ConcurrentHashMap<>();
    
    @Getter
    private final PipelineDataSourceManager dataSourceManager = new PipelineDataSourceManager();
    
    public PipelineJobRunnerManager() {
        this(null);
    }
    
    /**
     * Is stopping.
     *
     * @return whether job is stopping
     */
    public boolean isStopping() {
        return stopping.get();
    }
    
    /**
     * Set job bootstrap.
     *
     * @param jobBootstrap job bootstrap
     */
    public void setJobBootstrap(final JobBootstrap jobBootstrap) {
        this.jobBootstrap.set(jobBootstrap);
    }
    
    /**
     * Get tasks runner.
     *
     * @param shardingItem sharding item
     * @return tasks runner
     */
    public Optional<PipelineTasksRunner> getTasksRunner(final int shardingItem) {
        return Optional.ofNullable(tasksRunners.get(shardingItem));
    }
    
    /**
     * Get sharding items.
     *
     * @return sharding items
     */
    public Collection<Integer> getShardingItems() {
        return tasksRunners.keySet();
    }
    
    /**
     * Add tasks runner.
     *
     * @param shardingItem sharding item
     * @param tasksRunner tasks runner
     * @return add success or not
     */
    public boolean addTasksRunner(final int shardingItem, final PipelineTasksRunner tasksRunner) {
        if (null != tasksRunners.putIfAbsent(shardingItem, tasksRunner)) {
            log.warn("Tasks runner on sharding item {} exists, ignore.", shardingItem);
            return false;
        }
        String jobId = tasksRunner.getJobItemContext().getJobId();
        PipelineJobProgressPersistService.add(jobId, shardingItem);
        PipelineDistributedBarrier.getInstance(PipelineJobIdUtils.parseContextKey(jobId)).persistEphemeralChildrenNode(PipelineMetaDataNode.getJobBarrierEnablePath(jobId), shardingItem);
        return true;
    }
    
    /**
     * Stop job.
     */
    public void stop() {
        Optional<String> jobId = tasksRunners.values().stream().findFirst().map(each -> each.getJobItemContext().getJobId());
        try {
            stopping.set(true);
            log.info("Stop tasks runner, jobId={}", jobId);
            tasksRunners.values().forEach(PipelineTasksRunner::stop);
            jobId.ifPresent(this::awaitJobStopped);
            if (null != jobBootstrap.get()) {
                jobBootstrap.get().shutdown();
            }
        } finally {
            jobId.ifPresent(PipelineJobProgressPersistService::remove);
            tasksRunners.values().stream().map(each -> each.getJobItemContext().getJobProcessContext()).forEach(QuietlyCloser::close);
            dataSourceManager.close();
            if (null != cleaner) {
                cleaner.clean();
            }
        }
    }
    
    private void awaitJobStopped(final String jobId) {
        Optional<ElasticJobListener> jobListener = ElasticJobServiceLoader.getCachedTypedServiceInstance(ElasticJobListener.class, PipelineElasticJobListener.class.getName());
        if (!jobListener.isPresent()) {
            return;
        }
        long spentMillis = 0L;
        long sleepMillis = 50L;
        while (spentMillis < JOB_WAITING_TIMEOUT_MILLIS) {
            if (!((PipelineElasticJobListener) jobListener.get()).isJobRunning(jobId)) {
                break;
            }
            try {
                Thread.sleep(sleepMillis);
            } catch (final InterruptedException ignored) {
                Thread.currentThread().interrupt();
                break;
            }
            spentMillis += sleepMillis;
        }
    }
}
