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
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.PipelineJob;
import org.apache.shardingsphere.data.pipeline.api.task.PipelineTasksRunner;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.spi.barrier.PipelineDistributedBarrierFactory;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineJobAPI;
import org.apache.shardingsphere.data.pipeline.core.job.progress.persist.PipelineJobProgressPersistService;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.JobBootstrap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract pipeline job.
 */
@Slf4j
public abstract class AbstractPipelineJob implements PipelineJob {
    
    @Getter
    private volatile String jobId;
    
    @Getter(value = AccessLevel.PROTECTED)
    private volatile PipelineJobAPI jobAPI;
    
    @Getter
    private volatile boolean stopping;
    
    @Setter
    private volatile JobBootstrap jobBootstrap;
    
    private final Map<Integer, PipelineTasksRunner> tasksRunnerMap = new ConcurrentHashMap<>();
    
    protected void setJobId(final String jobId) {
        this.jobId = jobId;
        jobAPI = PipelineAPIFactory.getPipelineJobAPI(PipelineJobIdUtils.parseJobType(jobId));
    }
    
    protected void prepare(final PipelineJobItemContext jobItemContext) {
        try {
            doPrepare(jobItemContext);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            String jobId = jobItemContext.getJobId();
            log.error("job prepare failed, {}-{}", jobId, jobItemContext.getShardingItem(), ex);
            jobItemContext.setStatus(JobStatus.PREPARING_FAILURE);
            jobAPI.persistJobItemProgress(jobItemContext);
            jobAPI.persistJobItemErrorMessage(jobItemContext.getJobId(), jobItemContext.getShardingItem(), ex);
            jobAPI.stop(jobId);
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }
            throw new RuntimeException(ex);
        }
    }
    
    protected abstract void doPrepare(PipelineJobItemContext jobItemContext) throws Exception;
    
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
        PipelineJobProgressPersistService.addJobProgressPersistContext(getJobId(), shardingItem);
        PipelineDistributedBarrierFactory.getInstance().persistEphemeralChildrenNode(PipelineMetaDataNode.getJobBarrierEnablePath(getJobId()), shardingItem);
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
        if (null != jobBootstrap) {
            jobBootstrap.shutdown();
        }
        log.info("stop tasks runner, jobId={}", getJobId());
        for (PipelineTasksRunner each : tasksRunnerMap.values()) {
            each.stop();
        }
    }
    
    private void innerClean() {
        tasksRunnerMap.clear();
        PipelineJobProgressPersistService.removeJobProgressPersistContext(getJobId());
    }
    
    protected abstract void doClean();
}
