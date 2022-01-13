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

import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.mode.manager.ContextManager;

/**
 * Pipeline context.
 */
public final class PipelineContext {
    
    private static volatile ModeConfiguration modeConfig;
    
    private static volatile ContextManager contextManager;
    
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
}
