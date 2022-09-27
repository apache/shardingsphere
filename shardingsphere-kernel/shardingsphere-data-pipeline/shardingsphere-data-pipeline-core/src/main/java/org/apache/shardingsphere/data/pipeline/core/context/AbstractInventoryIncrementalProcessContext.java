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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.commons.lang3.concurrent.LazyInitializer;
import org.apache.shardingsphere.data.pipeline.api.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.process.PipelineReadConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.process.PipelineWriteConfiguration;
import org.apache.shardingsphere.data.pipeline.core.config.process.PipelineProcessConfigurationUtil;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelCreator;
import org.apache.shardingsphere.data.pipeline.spi.ingest.channel.PipelineChannelCreatorFactory;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithm;
import org.apache.shardingsphere.data.pipeline.spi.ratelimit.JobRateLimitAlgorithmFactory;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;

/**
 * Abstract inventory incremental process context.
 */
@Getter
@Slf4j
public abstract class AbstractInventoryIncrementalProcessContext implements InventoryIncrementalProcessContext {
    
    private final PipelineProcessConfiguration pipelineProcessConfig;
    
    private final JobRateLimitAlgorithm readRateLimitAlgorithm;
    
    private final JobRateLimitAlgorithm writeRateLimitAlgorithm;
    
    private final PipelineChannelCreator pipelineChannelCreator;
    
    private final LazyInitializer<ExecuteEngine> inventoryDumperExecuteEngineLazyInitializer;
    
    private final LazyInitializer<ExecuteEngine> incrementalDumperExecuteEngineLazyInitializer;
    
    private final LazyInitializer<ExecuteEngine> importerExecuteEngineLazyInitializer;
    
    public AbstractInventoryIncrementalProcessContext(final String jobId, final PipelineProcessConfiguration originalProcessConfig) {
        PipelineProcessConfiguration processConfig = PipelineProcessConfigurationUtil.convertWithDefaultValue(originalProcessConfig);
        this.pipelineProcessConfig = processConfig;
        PipelineReadConfiguration readConfig = processConfig.getRead();
        AlgorithmConfiguration readRateLimiter = readConfig.getRateLimiter();
        readRateLimitAlgorithm = null != readRateLimiter ? JobRateLimitAlgorithmFactory.newInstance(readRateLimiter) : null;
        PipelineWriteConfiguration writeConfig = processConfig.getWrite();
        AlgorithmConfiguration writeRateLimiter = writeConfig.getRateLimiter();
        writeRateLimitAlgorithm = null != writeRateLimiter ? JobRateLimitAlgorithmFactory.newInstance(writeRateLimiter) : null;
        AlgorithmConfiguration streamChannel = processConfig.getStreamChannel();
        pipelineChannelCreator = PipelineChannelCreatorFactory.newInstance(streamChannel);
        inventoryDumperExecuteEngineLazyInitializer = new LazyInitializer<ExecuteEngine>() {
            
            @Override
            protected ExecuteEngine initialize() {
                return ExecuteEngine.newFixedThreadInstance(readConfig.getWorkerThread(), "Inventory-" + jobId);
            }
        };
        incrementalDumperExecuteEngineLazyInitializer = new LazyInitializer<ExecuteEngine>() {
            
            @Override
            protected ExecuteEngine initialize() {
                return ExecuteEngine.newCachedThreadInstance("Incremental-" + jobId);
            }
        };
        importerExecuteEngineLazyInitializer = new LazyInitializer<ExecuteEngine>() {
            
            @Override
            protected ExecuteEngine initialize() {
                return ExecuteEngine.newFixedThreadInstance(writeConfig.getWorkerThread(), "Importer-" + jobId);
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
     * Get incremental dumper execute engine.
     *
     * @return incremental dumper execute engine
     */
    @SneakyThrows(ConcurrentException.class)
    public ExecuteEngine getIncrementalDumperExecuteEngine() {
        return incrementalDumperExecuteEngineLazyInitializer.get();
    }
    
    /**
     * Get importer execute engine.
     *
     * @return importer execute engine
     */
    @SneakyThrows(ConcurrentException.class)
    public ExecuteEngine getImporterExecuteEngine() {
        return importerExecuteEngineLazyInitializer.get();
    }
    
    @Override
    public void close() {
        getInventoryDumperExecuteEngine().shutdown();
        getIncrementalDumperExecuteEngine().shutdown();
        getImporterExecuteEngine().shutdown();
    }
}
