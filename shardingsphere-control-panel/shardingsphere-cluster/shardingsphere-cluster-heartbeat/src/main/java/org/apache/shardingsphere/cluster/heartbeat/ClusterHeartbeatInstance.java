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

package org.apache.shardingsphere.cluster.heartbeat;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.cluster.configuration.config.HeartbeatConfiguration;
import org.apache.shardingsphere.cluster.heartbeat.detect.HeartbeatHandler;
import org.apache.shardingsphere.cluster.heartbeat.event.HeartbeatDetectNoticeEvent;
import org.apache.shardingsphere.cluster.heartbeat.response.HeartbeatResponse;
import org.apache.shardingsphere.cluster.heartbeat.task.HeartbeatTask;
import org.apache.shardingsphere.cluster.heartbeat.task.HeartbeatTaskManager;
import org.apache.shardingsphere.kernel.context.SchemaContext;
import org.apache.shardingsphere.orchestration.core.facade.OrchestrationFacade;
import org.apache.shardingsphere.orchestration.core.registry.RegistryCenter;

import java.util.Collection;
import java.util.Map;

/**
 * Cluster heartbeat instance.
 */
public final class ClusterHeartbeatInstance {
    
    private HeartbeatTaskManager heartbeatTaskManager;
    
    private HeartbeatHandler heartbeatHandler = HeartbeatHandler.getInstance();
    
    private RegistryCenter registryCenter = OrchestrationFacade.getInstance().getRegistryCenter();
    
    /**
     * Get cluster heartbeat instance.
     *
     * @return cluster heartbeat instance
     */
    public static ClusterHeartbeatInstance getInstance() {
        return ClusterHeartbeatInstanceHolder.INSTANCE;
    }
    
    /**
     * Init heartbeat task manager.
     *
     * @param configuration heartbeat configuration
     */
    public void init(final HeartbeatConfiguration configuration) {
        Preconditions.checkNotNull(configuration, "heartbeat configuration can not be null.");
        heartbeatHandler.init(configuration);
        heartbeatTaskManager = new HeartbeatTaskManager(configuration.getInterval());
        HeartbeatTask task = new HeartbeatTask(new HeartbeatDetectNoticeEvent());
        heartbeatTaskManager.start(task);
    }
    
    /**
     * Detect heartbeat.
     *
     * @param schemaContexts schema contexts
     * @return heartbeat response
     */
    public HeartbeatResponse detect(final Map<String, SchemaContext> schemaContexts) {
        return heartbeatHandler.handle(schemaContexts, getDisabledDataSources());
    }
    
    private Collection<String> getDisabledDataSources() {
        return registryCenter.loadDisabledDataSources();
    }
    
    /**
     * Close cluster heartbeat instance.
     */
    public void close() {
        heartbeatTaskManager.close();
        heartbeatHandler.close();
    }
    
    private static final class ClusterHeartbeatInstanceHolder {
        
        private static final ClusterHeartbeatInstance INSTANCE = new ClusterHeartbeatInstance();
    }
}
