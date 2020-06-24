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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.metrics.api.HistogramMetricsTrackerDelegate;
import org.apache.shardingsphere.metrics.api.SummaryMetricsTrackerDelegate;
import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;
import org.apache.shardingsphere.metrics.facade.handler.MetricsTrackerHandler;
import org.apache.shardingsphere.metrics.spi.MetricsTrackerManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Metrics tracker facade.
 */
@Slf4j
public final class MetricsTrackerFacade {
    
    private static final Map<String, MetricsTrackerManager> METRICS_MAP = new HashMap<>();
    
    @Getter
    private MetricsTrackerManager metricsTrackerManager;
    
    @Getter
    private volatile boolean enabled;
    
    private MetricsTrackerFacade() {
        loadMetricsManager();
    }
    
    /**
     * Get metrics tracker facade of lazy load singleton.
     *
     * @return metrics tracker facade
     */
    public static MetricsTrackerFacade getInstance() {
        return MetricsTrackerFacadeHolder.INSTANCE;
    }
    
    /**
     * Find metrics tracker manager.
     *
     * @param metricsName metrics name
     * @return metrics tracker manager
     */
    public MetricsTrackerManager findMetricsTrackerManager(final String metricsName) {
        return METRICS_MAP.get(metricsName);
    }
    
    /**
     * Init for metrics tracker manager.
     *
     * @param metricsConfiguration metrics configuration
     */
    public void init(final MetricsConfiguration metricsConfiguration) {
        Preconditions.checkNotNull(metricsConfiguration, "metrics configuration can not be null.");
        metricsTrackerManager = findMetricsTrackerManager(metricsConfiguration.getMetricsName());
        Preconditions.checkNotNull(metricsTrackerManager, "Can not find metrics tracker manager with metrics name in metrics configuration.");
        metricsTrackerManager.start(metricsConfiguration);
        MetricsTrackerHandler.getInstance().init(metricsConfiguration.getAsync(), metricsConfiguration.getThreadCount(), metricsTrackerManager);
        enabled = true;
    }
    
    /**
     * Increment of counter metrics tracker.
     *
     * @param metricsLabel metrics label
     * @param labelValues  label values
     */
    public void counterInc(final String metricsLabel, final String... labelValues) {
        if (enabled) {
            MetricsTrackerHandler.getInstance().counterInc(metricsLabel, labelValues);
        }
    }
    
    /**
     * Increment of gauge metrics tracker.
     *
     * @param metricsLabel metrics label
     * @param labelValues  label values
     */
    public void gaugeInc(final String metricsLabel, final String... labelValues) {
        if (enabled) {
            MetricsTrackerHandler.getInstance().gaugeInc(metricsLabel, labelValues);
        }
    }
    
    /**
     * Decrement of gauge metrics tracker.
     *
     * @param metricsLabel metrics label
     * @param labelValues  label values
     */
    public void gaugeDec(final String metricsLabel, final String... labelValues) {
        if (enabled) {
            MetricsTrackerHandler.getInstance().gaugeDec(metricsLabel, labelValues);
        }
    }
    
    /**
     * Start timer of histogram metrics tracker.
     *
     * @param metricsLabel metrics label
     * @param labelValues  label values
     * @return histogram metrics tracker delegate
     */
    public Optional<HistogramMetricsTrackerDelegate> histogramStartTimer(final String metricsLabel, final String... labelValues) {
        if (!enabled) {
            return Optional.empty();
        }
        return MetricsTrackerHandler.getInstance().histogramStartTimer(metricsLabel, labelValues);
    }
    
    /**
     * Observe amount of time since start time with histogram metrics tracker.
     *
     * @param delegate histogram metrics tracker delegate
     */
    public void histogramObserveDuration(final HistogramMetricsTrackerDelegate delegate) {
        if (enabled) {
            MetricsTrackerHandler.getInstance().histogramObserveDuration(delegate);
        }
    }
    
    /**
     * Start timer of summary metrics tracker.
     *
     * @param metricsLabel metrics label
     * @param labelValues  label values
     * @return summary metrics tracker delegate
     */
    public Optional<SummaryMetricsTrackerDelegate> summaryStartTimer(final String metricsLabel, final String... labelValues) {
        if (!enabled) {
            return Optional.empty();
        }
        return MetricsTrackerHandler.getInstance().summaryStartTimer(metricsLabel, labelValues);
    }
    
    /**
     * Observe amount of time since start time with summary metrics tracker.
     *
     * @param delegate summary metrics tracker delegate
     */
    public void summaryObserveDuration(final SummaryMetricsTrackerDelegate delegate) {
        if (enabled) {
            MetricsTrackerHandler.getInstance().summaryObserveDuration(delegate);
        }
    }
    
    /**
     * Stop to metrics.
     */
    public void stop() {
        enabled = false;
        metricsTrackerManager.stop();
        MetricsTrackerHandler.getInstance().close();
    }
    
    private void loadMetricsManager() {
        for (MetricsTrackerManager each : ServiceLoader.load(MetricsTrackerManager.class)) {
            if (METRICS_MAP.containsKey(each.getType())) {
                log.warn("Find more than one {} metricsTracker manager implementation class, use `{}` now",
                        each.getType(), METRICS_MAP.get(each.getType()).getClass().getName());
                continue;
            }
            METRICS_MAP.put(each.getType(), each);
        }
    }
    
    /**
     * Metrics tracker facade holder.
     */
    private static class MetricsTrackerFacadeHolder {
        
        private static final MetricsTrackerFacade INSTANCE = new MetricsTrackerFacade();
    }
}

