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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.common.job.PipelineJob;
import org.apache.shardingsphere.data.pipeline.common.listener.PipelineElasticJobListener;
import org.apache.shardingsphere.data.pipeline.common.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.infra.util.close.QuietlyCloser;
import org.apache.shardingsphere.data.pipeline.common.util.PipelineDistributedBarrier;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineInternalException;
import org.apache.shardingsphere.data.pipeline.core.job.progress.persist.PipelineJobProgressPersistService;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobAPI;
import org.apache.shardingsphere.data.pipeline.core.task.runner.PipelineTasksRunner;
import org.apache.shardingsphere.elasticjob.infra.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.infra.spi.ElasticJobServiceLoader;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.JobBootstrap;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Abstract pipeline job.
 */
@Slf4j
public abstract class AbstractPipelineJob implements PipelineJob {
    
    @Getter
    private final String jobId;
    
    @Getter(AccessLevel.PROTECTED)
    private final PipelineJobAPI jobAPI;
    
    private final AtomicBoolean stopping = new AtomicBoolean(false);
    
    private final AtomicReference<JobBootstrap> jobBootstrap = new AtomicReference<>();
    
    private final Map<Integer, PipelineTasksRunner> tasksRunnerMap = new ConcurrentHashMap<>();
    
    protected AbstractPipelineJob(final String jobId) {
        this.jobId = jobId;
        jobAPI = TypedSPILoader.getService(PipelineJobAPI.class, PipelineJobIdUtils.parseJobType(jobId).getType());
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
    
    protected void prepare(final PipelineJobItemContext jobItemContext) {
        try {
            doPrepare(jobItemContext);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            processFailed(jobItemContext, ex);
            throw ex;
            // CHECKSTYLE:OFF
        } catch (final SQLException ex) {
            // CHECKSTYLE:ON
            processFailed(jobItemContext, ex);
            throw new PipelineInternalException(ex);
        }
    }
    
    protected abstract void doPrepare(PipelineJobItemContext jobItemContext) throws SQLException;
    
    protected void processFailed(final PipelineJobItemContext jobItemContext, final Exception ex) {
        String jobId = jobItemContext.getJobId();
        log.error("job prepare failed, {}-{}", jobId, jobItemContext.getShardingItem(), ex);
        jobAPI.persistJobItemErrorMessage(jobItemContext.getJobId(), jobItemContext.getShardingItem(), ex);
        jobAPI.stop(jobId);
    }
    
    @Override
    public Optional<PipelineTasksRunner> getTasksRunner(final int shardingItem) {
        return Optional.ofNullable(tasksRunnerMap.get(shardingItem));
    }
    
    @Override
    public Collection<Integer> getShardingItems() {
        return new ArrayList<>(tasksRunnerMap.keySet());
    }
    
    protected boolean addTasksRunner(final int shardingItem, final PipelineTasksRunner tasksRunner) {
        if (null != tasksRunnerMap.putIfAbsent(shardingItem, tasksRunner)) {
            log.warn("shardingItem {} tasks runner exists, ignore", shardingItem);
            return false;
        }
        String jobId = tasksRunner.getJobItemContext().getJobId();
        PipelineJobProgressPersistService.add(jobId, shardingItem);
        PipelineDistributedBarrier.getInstance(PipelineJobIdUtils.parseContextKey(jobId)).persistEphemeralChildrenNode(PipelineMetaDataNode.getJobBarrierEnablePath(jobId), shardingItem);
        return true;
    }
    
    @Override
    public void stop() {
        try {
            innerStop();
        } finally {
            innerClean();
            doClean();
        }
    }
    
    private void innerStop() {
        stopping.set(true);
        log.info("stop tasks runner, jobId={}", jobId);
        for (PipelineTasksRunner each : tasksRunnerMap.values()) {
            each.stop();
        }
        Optional<ElasticJobListener> pipelineJobListener = ElasticJobServiceLoader.getCachedTypedServiceInstance(ElasticJobListener.class, PipelineElasticJobListener.class.getName());
        pipelineJobListener.ifPresent(optional -> awaitJobStopped((PipelineElasticJobListener) optional, jobId, TimeUnit.SECONDS.toMillis(2)));
        if (null != jobBootstrap.get()) {
            jobBootstrap.get().shutdown();
        }
    }
    
    private void awaitJobStopped(final PipelineElasticJobListener jobListener, final String jobId, final long timeoutMillis) {
        int time = 0;
        int sleepTime = 50;
        while (time < timeoutMillis) {
            if (!jobListener.isJobRunning(jobId)) {
                break;
            }
            try {
                Thread.sleep(sleepTime);
            } catch (final InterruptedException ignored) {
                Thread.currentThread().interrupt();
                break;
            }
            time += sleepTime;
        }
    }
    
    private void innerClean() {
        PipelineJobProgressPersistService.remove(jobId);
        for (PipelineTasksRunner each : tasksRunnerMap.values()) {
            QuietlyCloser.close(each.getJobItemContext().getJobProcessContext());
        }
    }
    
    protected abstract void doClean();
}
