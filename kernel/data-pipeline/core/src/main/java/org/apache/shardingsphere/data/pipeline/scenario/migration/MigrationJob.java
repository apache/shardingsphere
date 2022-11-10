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

package org.apache.shardingsphere.data.pipeline.scenario.migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.job.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.task.PipelineTasksRunner;
import org.apache.shardingsphere.data.pipeline.core.context.InventoryIncrementalJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.datasource.DefaultPipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.job.AbstractSimplePipelineJob;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryIncrementalTasksRunner;
import org.apache.shardingsphere.data.pipeline.yaml.job.YamlMigrationJobConfigurationSwapper;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;

import java.sql.SQLException;

/**
 * Migration job.
 */
@RequiredArgsConstructor
@Slf4j
public final class MigrationJob extends AbstractSimplePipelineJob {
    
    private final MigrationJobAPI jobAPI = MigrationJobAPIFactory.getInstance();
    
    private final PipelineDataSourceManager dataSourceManager = new DefaultPipelineDataSourceManager();
    
    // Shared by all sharding items
    private final MigrationJobPreparer jobPreparer = new MigrationJobPreparer();
    
    @Override
    protected InventoryIncrementalJobItemContext buildPipelineJobItemContext(final ShardingContext shardingContext) {
        int shardingItem = shardingContext.getShardingItem();
        MigrationJobConfiguration jobConfig = new YamlMigrationJobConfigurationSwapper().swapToObject(shardingContext.getJobParameter());
        InventoryIncrementalJobItemProgress initProgress = jobAPI.getJobItemProgress(shardingContext.getJobName(), shardingItem);
        MigrationProcessContext jobProcessContext = jobAPI.buildPipelineProcessContext(jobConfig);
        MigrationTaskConfiguration taskConfig = jobAPI.buildTaskConfiguration(jobConfig, shardingItem, jobProcessContext.getPipelineProcessConfig());
        return new MigrationJobItemContext(jobConfig, shardingItem, initProgress, jobProcessContext, taskConfig, dataSourceManager);
    }
    
    @Override
    protected PipelineTasksRunner buildPipelineTasksRunner(final PipelineJobItemContext pipelineJobItemContext) {
        InventoryIncrementalJobItemContext jobItemContext = (InventoryIncrementalJobItemContext) pipelineJobItemContext;
        return new InventoryIncrementalTasksRunner(jobItemContext, jobItemContext.getInventoryTasks(), jobItemContext.getIncrementalTasks());
    }
    
    @Override
    protected void doPrepare(final PipelineJobItemContext jobItemContext) throws SQLException {
        jobPreparer.prepare((MigrationJobItemContext) jobItemContext);
    }
    
    @Override
    public void doClean() {
        dataSourceManager.close();
    }
}
