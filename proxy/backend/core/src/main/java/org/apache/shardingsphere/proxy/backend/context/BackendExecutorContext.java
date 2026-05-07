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

package org.apache.shardingsphere.proxy.backend.context;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;

/**
 * Backend executor context.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BackendExecutorContext {
    
    private static final BackendExecutorContext INSTANCE = new BackendExecutorContext();
    
    private volatile ExecutorEngine executorEngine;
    
    private LifecycleState lifecycleState = LifecycleState.UNINITIALIZED;
    
    /**
     * Get executor context instance.
     *
     * @return instance of executor context
     */
    public static BackendExecutorContext getInstance() {
        return INSTANCE;
    }
    
    /**
     * Initialize backend executor context.
     */
    public synchronized void init() {
        if (null != executorEngine) {
            executorEngine.close();
        }
        executorEngine = ExecutorEngine.createExecutorEngineWithSize(
                ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE));
        lifecycleState = LifecycleState.RUNNING;
    }
    
    /**
     * Get executor engine.
     *
     * @return executor engine
     * @throws IllegalStateException backend executor is unavailable in current lifecycle state
     */
    public synchronized ExecutorEngine getExecutorEngine() {
        if (null == executorEngine) {
            if (LifecycleState.CLOSED == lifecycleState) {
                throw new IllegalStateException(String.format("Backend executor engine is unavailable in `%s` lifecycle state.", lifecycleState));
            }
            init();
        }
        if (LifecycleState.RUNNING != lifecycleState) {
            throw new IllegalStateException(String.format("Backend executor engine is unavailable in `%s` lifecycle state.", lifecycleState));
        }
        return executorEngine;
    }
    
    /**
     * Close backend executor context.
     */
    public synchronized void close() {
        if (null != executorEngine) {
            executorEngine.close();
            executorEngine = null;
        }
        lifecycleState = LifecycleState.UNINITIALIZED;
    }
    
    /**
     * Shutdown backend executor context.
     */
    public synchronized void shutdown() {
        close();
        lifecycleState = LifecycleState.CLOSED;
    }
    
    private enum LifecycleState {
        
        UNINITIALIZED,
        
        RUNNING,
        
        CLOSED
    }
}
