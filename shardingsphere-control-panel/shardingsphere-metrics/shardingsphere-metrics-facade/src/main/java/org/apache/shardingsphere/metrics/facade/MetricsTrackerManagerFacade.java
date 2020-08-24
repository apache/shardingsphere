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

package org.apache.shardingsphere.metrics.facade;

import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.control.panel.spi.ControlPanelFacade;
import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;
import org.apache.shardingsphere.metrics.facade.handler.MetricsTrackerHandler;
import org.apache.shardingsphere.metrics.spi.MetricsTrackerManager;

/**
 * Metrics tracker facade.
 */
@Slf4j
public final class MetricsTrackerManagerFacade implements ControlPanelFacade<MetricsConfiguration> {
    
    private static MetricsTrackerManager metricsTrackerManager;
    
    private static volatile boolean enabled;
    
    /**
     * Get enabled boolean.
     *
     * @return boolean enabled
     */
    public static boolean getEnabled() {
        return enabled;
    }
    
    /**
     * Get metrics tracker manager.
     *
     * @return metrics tracker manager
     */
    public static MetricsTrackerManager getMetricsTrackerManager() {
        return metricsTrackerManager;
    }
    
    /**
     * Init for metrics tracker manager.
     *
     * @param metricsConfiguration metrics configuration
     */
    @Override
    public void init(final MetricsConfiguration metricsConfiguration) {
        if (!enabled) {
            doInit(metricsConfiguration);
        }
    }
    
    private static void doInit(final MetricsConfiguration metricsConfiguration) {
        Preconditions.checkNotNull(metricsConfiguration, "metrics configuration can not be null.");
        metricsTrackerManager = loadMetricsManager().get(metricsConfiguration.getMetricsName());
        Preconditions.checkNotNull(metricsTrackerManager, "Can not find metrics tracker manager with metrics name in metrics configuration.");
        metricsTrackerManager.start(metricsConfiguration);
        MetricsTrackerHandler.getInstance().init(metricsConfiguration.getAsync(), metricsConfiguration.getThreadCount(), metricsTrackerManager);
        enabled = true;
    }
    
    /**
     * Stop to metrics.
     */
    public static void close() {
        if (!enabled) {
            return;
        }
        if (null != metricsTrackerManager) {
            metricsTrackerManager.stop();
        }
        MetricsTrackerHandler.getInstance().close();
        enabled = false;
    }
    
    
    /**
     * Restart.
     *
     * @param metricsConfiguration metrics configuration
     */
    public static void restart(final MetricsConfiguration metricsConfiguration) {
        close();
        doInit(metricsConfiguration);
    }
    
    private static Map<String, MetricsTrackerManager> loadMetricsManager() {
        Map<String, MetricsTrackerManager> metricsMap = new HashMap<>();
        for (MetricsTrackerManager each : ServiceLoader.load(MetricsTrackerManager.class)) {
            if (metricsMap.containsKey(each.getType())) {
                log.warn("Find more than one {} metricsTracker manager implementation class, use `{}` now",
                        each.getType(), metricsMap.get(each.getType()).getClass().getName());
                continue;
            }
            metricsMap.put(each.getType(), each);
        }
        return metricsMap;
    }
    
    @Override
    public int getOrder() {
        return 0;
    }
    
    @Override
    public Class<MetricsConfiguration> getTypeClass() {
        return MetricsConfiguration.class;
    }
    
}

