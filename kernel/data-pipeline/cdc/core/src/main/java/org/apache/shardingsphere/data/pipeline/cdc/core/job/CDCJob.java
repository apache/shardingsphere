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

package org.apache.shardingsphere.data.pipeline.cdc.core.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.task.PipelineTasksRunner;
import org.apache.shardingsphere.data.pipeline.cdc.api.impl.CDCJobAPI;
import org.apache.shardingsphere.data.pipeline.cdc.config.job.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.task.CDCTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.context.CDCProcessContext;
import org.apache.shardingsphere.data.pipeline.cdc.context.job.CDCJobItemContext;
import org.apache.shardingsphere.data.pipeline.cdc.core.prepare.CDCJobPreparer;
import org.apache.shardingsphere.data.pipeline.cdc.core.task.CDCTasksRunner;
import org.apache.shardingsphere.data.pipeline.cdc.yaml.job.YamlCDCJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.core.datasource.DefaultPipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineInternalException;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteCallback;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.job.AbstractPipelineJob;
import org.apache.shardingsphere.data.pipeline.core.util.CloseUtils;
import org.apache.shardingsphere.data.pipeline.spi.importer.sink.PipelineSink;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * CDC job.
 */
@Slf4j
public final class CDCJob extends AbstractPipelineJob implements SimpleJob {
    
    private final PipelineSink sink;
    
    private final CDCJobAPI jobAPI = new CDCJobAPI();
    
    private final CDCJobPreparer jobPreparer = new CDCJobPreparer();
    
    private final PipelineDataSourceManager dataSourceManager = new DefaultPipelineDataSourceManager();
    
    public CDCJob(final String jobId, final PipelineSink sink) {
        super(jobId);
        this.sink = sink;
    }
    
    @Override
    public void execute(final ShardingContext shardingContext) {
        String jobId = shardingContext.getJobName();
        log.info("Execute job {}", jobId);
        CDCJobConfiguration jobConfig = new YamlCDCJobConfigurationSwapper().swapToObject(shardingContext.getJobParameter());
        Collection<CDCJobItemContext> jobItemContexts = new LinkedList<>();
        Collection<PipelineTasksRunner> tasksRunners = new LinkedList<>();
        for (int shardingItem = 0; shardingItem < jobConfig.getJobShardingCount(); shardingItem++) {
            if (isStopping()) {
                log.info("stopping true, ignore");
                return;
            }
            CDCJobItemContext jobItemContext = buildPipelineJobItemContext(jobConfig, shardingItem);
            PipelineTasksRunner tasksRunner = buildPipelineTasksRunner(jobItemContext);
            if (!addTasksRunner(shardingItem, tasksRunner)) {
                continue;
            }
            jobItemContexts.add(jobItemContext);
            tasksRunners.add(tasksRunner);
            jobAPI.cleanJobItemErrorMessage(jobId, shardingItem);
            log.info("start tasks runner, jobId={}, shardingItem={}", jobId, shardingItem);
        }
        Collection<CompletableFuture<?>> futures = new ArrayList<>(jobConfig.getJobShardingCount());
        prepare(jobItemContexts);
        for (PipelineTasksRunner each : tasksRunners) {
            futures.add(CompletableFuture.runAsync(each::start));
        }
        ExecuteEngine.trigger(futures, new ExecuteCallback() {
            
            @Override
            public void onSuccess() {
                log.info("onSuccess");
            }
            
            @Override
            public void onFailure(final Throwable throwable) {
                log.error("onFailure", throwable);
            }
        });
    }
    
    private CDCJobItemContext buildPipelineJobItemContext(final CDCJobConfiguration jobConfig, final int shardingItem) {
        Optional<InventoryIncrementalJobItemProgress> initProgress = jobAPI.getJobItemProgress(jobConfig.getJobId(), shardingItem);
        CDCProcessContext jobProcessContext = jobAPI.buildPipelineProcessContext(jobConfig);
        CDCTaskConfiguration taskConfig = jobAPI.buildTaskConfiguration(jobConfig, shardingItem, jobProcessContext.getPipelineProcessConfig());
        return new CDCJobItemContext(jobConfig, shardingItem, initProgress.orElse(null), jobProcessContext, taskConfig, dataSourceManager, sink);
    }
    
    private PipelineTasksRunner buildPipelineTasksRunner(final CDCJobItemContext jobItemContext) {
        return new CDCTasksRunner(jobItemContext, jobItemContext.getInventoryTasks(), jobItemContext.getIncrementalTasks());
    }
    
    private void prepare(final Collection<CDCJobItemContext> jobItemContexts) {
        try {
            jobPreparer.initTasks(jobItemContexts);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            for (PipelineJobItemContext each : jobItemContexts) {
                processFailed(each, ex);
            }
            throw ex;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            for (PipelineJobItemContext each : jobItemContexts) {
                processFailed(each, ex);
            }
            throw new PipelineInternalException(ex);
        }
    }
    
    @Override
    protected void doPrepare(final PipelineJobItemContext jobItemContext) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected void doClean() {
        dataSourceManager.close();
        CloseUtils.closeQuietly(sink);
    }
}
