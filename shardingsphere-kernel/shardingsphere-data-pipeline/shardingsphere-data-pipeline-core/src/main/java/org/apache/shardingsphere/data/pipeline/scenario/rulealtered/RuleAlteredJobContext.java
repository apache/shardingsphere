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

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.RuleAlteredJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.TaskConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.task.IncrementalTask;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Rule altered job context.
 */
@Getter
@Setter
@Slf4j
// TODO extract JobContext
public final class RuleAlteredJobContext {
    
    private final String jobId;
    
    private final int shardingItem;
    
    private volatile boolean stopping;
    
    private volatile JobStatus status = JobStatus.RUNNING;
    
    private final JobProgress initProgress;
    
    private final TaskConfiguration taskConfig;
    
    private final Collection<InventoryTask> inventoryTasks = new LinkedList<>();
    
    private final Collection<IncrementalTask> incrementalTasks = new LinkedList<>();
    
    private final RuleAlteredJobConfiguration jobConfig;
    
    private final RuleAlteredContext ruleAlteredContext;
    
    private final PipelineDataSourceManager dataSourceManager;
    
    private final LazyInitializer<PipelineDataSourceWrapper> sourceDataSourceLazyInitializer = new LazyInitializer<PipelineDataSourceWrapper>() {
        
        @Override
        protected PipelineDataSourceWrapper initialize() {
            return dataSourceManager.getDataSource(taskConfig.getDumperConfig().getDataSourceConfig());
        }
    };
    
    private final LazyInitializer<PipelineTableMetaDataLoader> sourceMetaDataLoaderLazyInitializer = new LazyInitializer<PipelineTableMetaDataLoader>() {
        
        @Override
        protected PipelineTableMetaDataLoader initialize() throws ConcurrentException {
            return new PipelineTableMetaDataLoader(sourceDataSourceLazyInitializer.get());
        }
    };
    
    private final RuleAlteredJobPreparer jobPreparer;
    
    public RuleAlteredJobContext(final RuleAlteredJobConfiguration jobConfig, final int jobShardingItem, final JobProgress initProgress,
                                 final PipelineDataSourceManager dataSourceManager, final RuleAlteredJobPreparer jobPreparer) {
        ruleAlteredContext = RuleAlteredJobWorker.createRuleAlteredContext(jobConfig);
        this.jobConfig = jobConfig;
        jobId = jobConfig.getJobId();
        this.shardingItem = jobShardingItem;
        this.initProgress = initProgress;
        this.dataSourceManager = dataSourceManager;
        this.jobPreparer = jobPreparer;
        taskConfig = RuleAlteredJobWorker.buildTaskConfig(jobConfig, jobShardingItem, ruleAlteredContext.getOnRuleAlteredActionConfig());
    }
    
    /**
     * Get source data source.
     *
     * @return source data source
     */
    @SneakyThrows(ConcurrentException.class)
    public PipelineDataSourceWrapper getSourceDataSource() {
        return sourceDataSourceLazyInitializer.get();
    }
    
    /**
     * Get source metadata loader.
     *
     * @return source metadata loader
     */
    @SneakyThrows(ConcurrentException.class)
    public PipelineTableMetaDataLoader getSourceMetaDataLoader() {
        return sourceMetaDataLoaderLazyInitializer.get();
    }
}
