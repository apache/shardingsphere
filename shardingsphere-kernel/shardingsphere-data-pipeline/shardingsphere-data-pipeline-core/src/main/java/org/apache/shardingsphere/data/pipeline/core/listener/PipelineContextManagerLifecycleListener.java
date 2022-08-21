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

package org.apache.shardingsphere.data.pipeline.core.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.execute.PipelineJobWorker;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.listener.ContextManagerLifecycleListener;

/**
 * Pipeline context manager lifecycle listener.
 */
@Slf4j
public final class PipelineContextManagerLifecycleListener implements ContextManagerLifecycleListener {
    
    @Override
    public void onInitialized(final ModeConfiguration modeConfig, final ContextManager contextManager) {
        if (null == modeConfig) {
            return;
        }
        if (!contextManager.getInstanceContext().isCluster()) {
            log.info("mode type is not Cluster, mode type='{}', ignore", modeConfig.getType());
            return;
        }
        PipelineContext.initModeConfig(modeConfig);
        PipelineContext.initContextManager(contextManager);
        // TODO init worker only if necessary, e.g. 1) rule altered action configured, 2) enabled job exists, 3) stopped job restarted
        PipelineJobWorker.initialize();
    }
}
