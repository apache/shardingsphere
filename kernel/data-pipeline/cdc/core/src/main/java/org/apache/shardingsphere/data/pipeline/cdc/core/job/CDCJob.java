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

import lombok.RequiredArgsConstructor;
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
import org.apache.shardingsphere.data.pipeline.core.context.InventoryIncrementalJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.datasource.DefaultPipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.job.AbstractSimplePipelineJob;
import org.apache.shardingsphere.data.pipeline.spi.importer.connector.ImporterConnector;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;

import java.util.Optional;

/**
 * CDC job.
 */
@RequiredArgsConstructor
@Slf4j
public final class CDCJob extends AbstractSimplePipelineJob {
    
    private final ImporterConnector importerConnector;
    
    private final CDCJobAPI jobAPI = new CDCJobAPI();
    
    private final CDCJobPreparer jobPreparer = new CDCJobPreparer();
    
    private final PipelineDataSourceManager dataSourceManager = new DefaultPipelineDataSourceManager();
    
    @Override
    protected void doPrepare(final PipelineJobItemContext jobItemContext) {
        jobPreparer.prepare((CDCJobItemContext) jobItemContext);
    }
    
    @Override
    protected PipelineJobItemContext buildPipelineJobItemContext(final ShardingContext shardingContext) {
        int shardingItem = shardingContext.getShardingItem();
        CDCJobConfiguration jobConfig = new YamlCDCJobConfigurationSwapper().swapToObject(shardingContext.getJobParameter());
        Optional<InventoryIncrementalJobItemProgress> initProgress = jobAPI.getJobItemProgress(shardingContext.getJobName(), shardingItem);
        CDCProcessContext jobProcessContext = jobAPI.buildPipelineProcessContext(jobConfig);
        CDCTaskConfiguration taskConfig = jobAPI.buildTaskConfiguration(jobConfig, shardingItem, jobProcessContext.getPipelineProcessConfig());
        return new CDCJobItemContext(jobConfig, shardingItem, initProgress.orElse(null), jobProcessContext, taskConfig, dataSourceManager, importerConnector);
    }
    
    protected PipelineTasksRunner buildPipelineTasksRunner(final PipelineJobItemContext pipelineJobItemContext) {
        InventoryIncrementalJobItemContext jobItemContext = (InventoryIncrementalJobItemContext) pipelineJobItemContext;
        return new CDCTasksRunner(jobItemContext, jobItemContext.getInventoryTasks(), jobItemContext.getIncrementalTasks());
    }
    
    @Override
    protected void doClean() {
        dataSourceManager.close();
    }
}
