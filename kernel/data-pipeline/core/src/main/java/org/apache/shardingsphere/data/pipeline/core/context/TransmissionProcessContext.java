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

package org.apache.shardingsphere.data.pipeline.core.context;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfigurationUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineReadConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineWriteConfiguration;
import org.apache.shardingsphere.data.pipeline.core.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineLazyInitializer;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

/**
 * Transmission process context.
 */
public final class TransmissionProcessContext implements PipelineProcessContext {
    
    @Getter
    private final PipelineProcessConfiguration processConfig;
    
    @Getter
    private final JobRateLimitAlgorithm readRateLimitAlgorithm;
    
    @Getter
    private final JobRateLimitAlgorithm writeRateLimitAlgorithm;
    
    private final PipelineLazyInitializer<ExecuteEngine> inventoryDumperExecuteEngineLazyInitializer;
    
    private final PipelineLazyInitializer<ExecuteEngine> inventoryImporterExecuteEngineLazyInitializer;
    
    private final PipelineLazyInitializer<ExecuteEngine> incrementalExecuteEngineLazyInitializer;
    
    public TransmissionProcessContext(final String jobId, final PipelineProcessConfiguration originalProcessConfig) {
        this.processConfig = PipelineProcessConfigurationUtils.convertWithDefaultValue(originalProcessConfig);
        PipelineReadConfiguration readConfig = processConfig.getRead();
        AlgorithmConfiguration readRateLimiter = readConfig.getRateLimiter();
        readRateLimitAlgorithm = null == readRateLimiter ? null : TypedSPILoader.getService(JobRateLimitAlgorithm.class, readRateLimiter.getType(), readRateLimiter.getProps());
        PipelineWriteConfiguration writeConfig = processConfig.getWrite();
        AlgorithmConfiguration writeRateLimiter = writeConfig.getRateLimiter();
        writeRateLimitAlgorithm = null == writeRateLimiter ? null : TypedSPILoader.getService(JobRateLimitAlgorithm.class, writeRateLimiter.getType(), writeRateLimiter.getProps());
        inventoryDumperExecuteEngineLazyInitializer = new PipelineLazyInitializer<ExecuteEngine>() {
            
            @Override
            protected ExecuteEngine doInitialize() {
                return ExecuteEngine.newFixedThreadInstance(readConfig.getWorkerThread(), "Inventory-" + jobId);
            }
        };
        inventoryImporterExecuteEngineLazyInitializer = new PipelineLazyInitializer<ExecuteEngine>() {
            
            @Override
            protected ExecuteEngine doInitialize() {
                return ExecuteEngine.newFixedThreadInstance(writeConfig.getWorkerThread(), "Importer-" + jobId);
            }
        };
        incrementalExecuteEngineLazyInitializer = new PipelineLazyInitializer<ExecuteEngine>() {
            
            @Override
            protected ExecuteEngine doInitialize() {
                return ExecuteEngine.newCachedThreadInstance("Incremental-" + jobId);
            }
        };
    }
    
    /**
     * Get inventory dumper execute engine.
     *
     * @return inventory dumper execute engine
     */
    @SneakyThrows(ConcurrentException.class)
    public ExecuteEngine getInventoryDumperExecuteEngine() {
        return inventoryDumperExecuteEngineLazyInitializer.get();
    }
    
    /**
     * Get inventory importer execute engine.
     *
     * @return inventory importer execute engine
     */
    @SneakyThrows(ConcurrentException.class)
    public ExecuteEngine getInventoryImporterExecuteEngine() {
        return inventoryImporterExecuteEngineLazyInitializer.get();
    }
    
    /**
     * Get incremental execute engine.
     *
     * @return incremental execute engine
     */
    @SneakyThrows(ConcurrentException.class)
    public ExecuteEngine getIncrementalExecuteEngine() {
        return incrementalExecuteEngineLazyInitializer.get();
    }
    
    @Override
    public void close() throws Exception {
        shutdownExecuteEngine(inventoryDumperExecuteEngineLazyInitializer);
        shutdownExecuteEngine(inventoryImporterExecuteEngineLazyInitializer);
        shutdownExecuteEngine(incrementalExecuteEngineLazyInitializer);
    }
    
    private void shutdownExecuteEngine(final PipelineLazyInitializer<ExecuteEngine> lazyInitializer) throws ConcurrentException {
        if (lazyInitializer.isInitialized()) {
            lazyInitializer.get().shutdown();
        }
    }
}
