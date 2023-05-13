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
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.api.job.PipelineJob;
import org.apache.shardingsphere.data.pipeline.api.task.PipelineTasksRunner;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineJobAPI;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineInternalException;
import org.apache.shardingsphere.data.pipeline.core.job.progress.persist.PipelineJobProgressPersistService;
import org.apache.shardingsphere.data.pipeline.core.listener.PipelineElasticJobListener;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineDistributedBarrier;
import org.apache.shardingsphere.elasticjob.infra.listener.ElasticJobListener;
import org.apache.shardingsphere.elasticjob.infra.spi.ElasticJobServiceLoader;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.JobBootstrap;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Abstract pipeline job.
 */
@Slf4j
public abstract class AbstractPipelineJob implements PipelineJob {
    
    @Getter
    private final String jobId;
    
    @Getter(AccessLevel.PROTECTED)
    private final PipelineJobAPI jobAPI;
    
    @Getter
    private volatile boolean stopping;
    
    @Setter
    private volatile JobBootstrap jobBootstrap;
    
    private final Map<Integer, PipelineTasksRunner> tasksRunnerMap = new ConcurrentHashMap<>();
    
    protected AbstractPipelineJob(final String jobId) {
        this.jobId = jobId;
        jobAPI = TypedSPILoader.getService(PipelineJobAPI.class, PipelineJobIdUtils.parseJobType(jobId).getTypeName());
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
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            processFailed(jobItemContext, ex);
            throw new PipelineInternalException(ex);
        }
    }
    
    protected abstract void doPrepare(PipelineJobItemContext jobItemContext) throws Exception;
    
    private void processFailed(final PipelineJobItemContext jobItemContext, final Exception ex) {
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
        PipelineJobProgressPersistService.addJobProgressPersistContext(jobId, shardingItem);
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
        stopping = true;
        log.info("stop tasks runner, jobId={}", jobId);
        for (PipelineTasksRunner each : tasksRunnerMap.values()) {
            each.stop();
        }
        Optional<ElasticJobListener> pipelineJobListener = ElasticJobServiceLoader.getCachedTypedServiceInstance(ElasticJobListener.class, PipelineElasticJobListener.class.getName());
        pipelineJobListener.ifPresent(jobListener -> awaitJobStopped((PipelineElasticJobListener) jobListener, jobId, TimeUnit.SECONDS.toMillis(2)));
        if (null != jobBootstrap) {
            jobBootstrap.shutdown();
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
        tasksRunnerMap.clear();
        PipelineJobProgressPersistService.removeJobProgressPersistContext(jobId);
    }
    
    protected abstract void doClean();
}
