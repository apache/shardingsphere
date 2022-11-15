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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineDistributedBarrier;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineDistributedBarrierFactory;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Pipeline context.
 */
public final class PipelineContext {
    
    private static volatile ModeConfiguration modeConfig;
    
    private static volatile ContextManager contextManager;
    
    private static final ExecutorService EVENT_LISTENER_EXECUTOR = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).setNameFormat("Pipeline-EventListener-%d").build());
    
    private static PipelineDistributedBarrier pipelineDistributedBarrier;
    
    /**
     * Get mode configuration.
     *
     * @return mode configuration
     */
    public static ModeConfiguration getModeConfig() {
        return modeConfig;
    }
    
    /**
     * Initialize mode configuration.
     *
     * @param modeConfig configuration
     */
    public static void initModeConfig(final ModeConfiguration modeConfig) {
        PipelineContext.modeConfig = modeConfig;
    }
    
    /**
     * Get pipeline distributed barrier.
     *
     * @return pipeline distributed barrier
     */
    public static PipelineDistributedBarrier getPipelineDistributedBarrier() {
        return pipelineDistributedBarrier;
    }
    
    /**
     * Initialize pipeline distributed barrier.
     *
     * @param type type
     */
    public static void initPipelineDistributedBarrier(final String type) {
        pipelineDistributedBarrier = PipelineDistributedBarrierFactory.getInstance(type);
    }
    
    /**
     * Get context manager.
     *
     * @return context manager
     */
    public static ContextManager getContextManager() {
        return contextManager;
    }
    
    /**
     * Initialize context manager.
     *
     * @param contextManager context manager
     */
    public static void initContextManager(final ContextManager contextManager) {
        PipelineContext.contextManager = contextManager;
    }
    
    /**
     * Get pipeline executor.
     *
     * @return pipeline executor
     */
    public static ExecutorService getEventListenerExecutor() {
        return EVENT_LISTENER_EXECUTOR;
    }
}
