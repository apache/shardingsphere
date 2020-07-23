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

package org.apache.shardingsphere.cluster.facade.init;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.cluster.configuration.config.ClusterConfiguration;
import org.apache.shardingsphere.cluster.facade.ClusterFacade;
import org.apache.shardingsphere.control.panel.spi.ControlPanelFacade;
import org.apache.shardingsphere.orchestration.core.facade.OrchestrationFacade;

/**
 * Cluster init facade.
 */
@Slf4j
public final class ClusterInitFacade implements ControlPanelFacade<ClusterConfiguration> {
    
    @Getter
    private static volatile boolean enabled;
    
    /**
     * Init cluster facade.
     *
     * @param clusterConfiguration cluster configuration
     */
    @Override
    public void init(final ClusterConfiguration clusterConfiguration) {
        if (!enabled) {
            doInit(clusterConfiguration);
        }
    }
    
    private static void doInit(final ClusterConfiguration clusterConfiguration) {
        Preconditions.checkNotNull(clusterConfiguration, "cluster configuration can not be null.");
        ClusterFacade.getInstance().init(clusterConfiguration);
        enabled = true;
    }
    
    /**
     * Stop cluster facade.
     */
    public static void stop() {
        if (enabled) {
            ClusterFacade.getInstance().close();
            enabled = false;
            log.info("heart beat detect stopped");
        }
    }
    
    /**
     * Restart cluster facade.
     *
     * @param clusterConfiguration cluster configuration
     */
    public static void restart(final ClusterConfiguration clusterConfiguration) {
        stop();
        doInit(clusterConfiguration);
    }
    
    /**
     * Enable cluster facade.
     *
     * @param clusterEnabled cluster enabled configuration
     */
    public static void enable(final boolean clusterEnabled) {
        if (enabled != clusterEnabled) {
            if (clusterEnabled) {
                doInit(OrchestrationFacade.getInstance().getConfigCenter().loadClusterConfiguration());
            } else {
                stop();
            }
        }
    }
    
    @Override
    public int getOrder() {
        return 1;
    }
    
    @Override
    public Class<ClusterConfiguration> getTypeClass() {
        return ClusterConfiguration.class;
    }
}
