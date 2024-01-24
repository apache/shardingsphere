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

package org.apache.shardingsphere.data.pipeline.cdc.context;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.shardingsphere.data.pipeline.cdc.config.job.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.task.CDCTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.listener.PipelineJobProgressUpdatedParameter;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.StandardPipelineTableMetaDataLoader;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineSink;
import org.apache.shardingsphere.data.pipeline.core.job.progress.persist.PipelineJobProgressPersistService;
import org.apache.shardingsphere.data.pipeline.core.task.PipelineTask;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * CDC job item context.
 */
@Getter
public final class CDCJobItemContext implements TransmissionJobItemContext {
    
    private final CDCJobConfiguration jobConfig;
    
    private final int shardingItem;
    
    @Setter
    private volatile boolean stopping;
    
    @Setter
    private volatile JobStatus status = JobStatus.RUNNING;
    
    private final TransmissionJobItemProgress initProgress;
    
    private final TransmissionProcessContext jobProcessContext;
    
    private final CDCTaskConfiguration taskConfig;
    
    private final PipelineDataSourceManager dataSourceManager;
    
    private final PipelineSink sink;
    
    private final Collection<PipelineTask> inventoryTasks = new LinkedList<>();
    
    private final Collection<PipelineTask> incrementalTasks = new LinkedList<>();
    
    private final AtomicLong processedRecordsCount = new AtomicLong(0);
    
    private final AtomicLong inventoryRecordsCount = new AtomicLong(0);
    
    private final LazyInitializer<PipelineDataSourceWrapper> sourceDataSourceLazyInitializer = new LazyInitializer<PipelineDataSourceWrapper>() {
        
        @Override
        protected PipelineDataSourceWrapper initialize() {
            return dataSourceManager.getDataSource(taskConfig.getDumperContext().getCommonContext().getDataSourceConfig());
        }
    };
    
    private final LazyInitializer<PipelineTableMetaDataLoader> sourceMetaDataLoaderLazyInitializer = new LazyInitializer<PipelineTableMetaDataLoader>() {
        
        @Override
        protected PipelineTableMetaDataLoader initialize() throws ConcurrentException {
            return new StandardPipelineTableMetaDataLoader(sourceDataSourceLazyInitializer.get());
        }
    };
    
    public CDCJobItemContext(final CDCJobConfiguration jobConfig, final int shardingItem, final TransmissionJobItemProgress initProgress, final TransmissionProcessContext jobProcessContext,
                             final CDCTaskConfiguration taskConfig, final PipelineDataSourceManager dataSourceManager, final PipelineSink sink) {
        this.jobConfig = jobConfig;
        this.shardingItem = shardingItem;
        this.initProgress = initProgress;
        if (null != initProgress) {
            processedRecordsCount.set(initProgress.getProcessedRecordsCount());
            inventoryRecordsCount.set(initProgress.getInventoryRecordsCount());
        }
        this.jobProcessContext = jobProcessContext;
        this.taskConfig = taskConfig;
        this.dataSourceManager = dataSourceManager;
        this.sink = sink;
    }
    
    @Override
    public String getJobId() {
        return jobConfig.getJobId();
    }
    
    @Override
    public String getDataSourceName() {
        return taskConfig.getDumperContext().getCommonContext().getDataSourceName();
    }
    
    @Override
    public void onProgressUpdated(final PipelineJobProgressUpdatedParameter param) {
        processedRecordsCount.addAndGet(param.getProcessedRecordsCount());
        PipelineJobProgressPersistService.notifyPersist(jobConfig.getJobId(), shardingItem);
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
    
    @Override
    @SneakyThrows(ConcurrentException.class)
    public PipelineTableMetaDataLoader getSourceMetaDataLoader() {
        return sourceMetaDataLoaderLazyInitializer.get();
    }
    
    @Override
    public long getProcessedRecordsCount() {
        return processedRecordsCount.get();
    }
    
    @Override
    public void updateInventoryRecordsCount(final long recordsCount) {
        inventoryRecordsCount.addAndGet(recordsCount);
    }
    
    @Override
    public long getInventoryRecordsCount() {
        return inventoryRecordsCount.get();
    }
}
