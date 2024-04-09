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

package org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.context;

import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfigurationUtils;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineProcessContext;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineLazyInitializer;

/**
 * Consistency check process context.
 */
@Getter
public final class ConsistencyCheckProcessContext implements PipelineProcessContext {
    
    private final PipelineLazyInitializer<ExecuteEngine> consistencyCheckExecuteEngineLazyInitializer;
    
    public ConsistencyCheckProcessContext(final String jobId) {
        consistencyCheckExecuteEngineLazyInitializer = new PipelineLazyInitializer<ExecuteEngine>() {
            
            @Override
            protected ExecuteEngine doInitialize() {
                return ExecuteEngine.newFixedThreadInstance(1, jobId + "-check");
            }
        };
    }
    
    @Override
    public PipelineProcessConfiguration getProcessConfig() {
        return PipelineProcessConfigurationUtils.convertWithDefaultValue(null);
    }
    
    /**
     * Get consistency check execute engine.
     *
     * @return consistency check execute engine
     */
    @SneakyThrows(ConcurrentException.class)
    public ExecuteEngine getConsistencyCheckExecuteEngine() {
        return consistencyCheckExecuteEngineLazyInitializer.get();
    }
    
    @Override
    public void close() throws Exception {
        if (consistencyCheckExecuteEngineLazyInitializer.isInitialized()) {
            consistencyCheckExecuteEngineLazyInitializer.get().shutdown();
        }
    }
}
