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
import org.apache.shardingsphere.metrics.api.CounterMetricsTracker;
import org.apache.shardingsphere.metrics.api.GaugeMetricsTracker;
import org.apache.shardingsphere.metrics.api.HistogramMetricsTracker;
import org.apache.shardingsphere.metrics.api.HistogramMetricsTrackerDelegate;
import org.apache.shardingsphere.metrics.api.MetricsTracker;
import org.apache.shardingsphere.metrics.api.NoneHistogramMetricsTrackerDelegate;
import org.apache.shardingsphere.metrics.api.NoneSummaryMetricsTrackerDelegate;
import org.apache.shardingsphere.metrics.api.SummaryMetricsTracker;
import org.apache.shardingsphere.metrics.api.SummaryMetricsTrackerDelegate;
import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;
import org.apache.shardingsphere.metrics.enums.MetricsTypeEnum;
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
    
    private static final MetricsTrackerFacade INSTANCE = new MetricsTrackerFacade();
    
    @Getter
    private MetricsTrackerManager metricsTrackerManager;
    
    @Getter
    private volatile Boolean enabled = false;
    
    private MetricsTrackerFacade() {
        loadMetricsManager();
    }
    
    /**
     * Get metrics tracker facade.
     *
     * @return metrics tracker facade
     */
    public static MetricsTrackerFacade getInstance() {
        return INSTANCE;
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
        metricsTrackerManager = findMetricsTrackerManager(metricsConfiguration.getMetricsName());
        Preconditions.checkNotNull(metricsTrackerManager, "Can not find metrics tracker manager with metrics name in metrics configuration.");
        metricsTrackerManager.start(metricsConfiguration);
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
            metricsTrackerManager.getMetricsTrackerFactory().create(MetricsTypeEnum.COUNTER.name(), metricsLabel)
                .ifPresent(metricsTracker -> ((CounterMetricsTracker) metricsTracker).inc(1.0, labelValues));
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
            metricsTrackerManager.getMetricsTrackerFactory().create(MetricsTypeEnum.GAUGE.name(), metricsLabel)
                .ifPresent(metricsTracker -> ((GaugeMetricsTracker) metricsTracker).inc(1.0, labelValues));
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
            metricsTrackerManager.getMetricsTrackerFactory().create(MetricsTypeEnum.GAUGE.name(), metricsLabel)
                .ifPresent(metricsTracker -> ((GaugeMetricsTracker) metricsTracker).dec(1.0, labelValues));
        }
    }
    
    /**
     * Start timer of histogram metrics tracker.
     *
     * @param metricsLabel metrics label
     * @param labelValues  label values
     * @return histogram metrics tracker delegate
     */
    public HistogramMetricsTrackerDelegate histogramStartTimer(final String metricsLabel, final String... labelValues) {
        Optional<MetricsTracker> metricsTracker = metricsTrackerManager.getMetricsTrackerFactory().create(MetricsTypeEnum.HISTOGRAM.name(), metricsLabel);
        if (metricsTracker.isPresent()) {
            return ((HistogramMetricsTracker) metricsTracker.get()).startTimer(labelValues);
        } else {
            return new NoneHistogramMetricsTrackerDelegate();
        }
    }
    
    /**
     * Observe amount of time since start time with histogram metrics tracker.
     *
     * @param delegate histogram metrics tracker delegate
     */
    public void histogramObserveDuration(final HistogramMetricsTrackerDelegate delegate) {
        delegate.observeDuration();
    }
    
    /**
     * Start timer of summary metrics tracker.
     *
     * @param metricsLabel metrics label
     * @param labelValues  label values
     * @return summary metrics tracker delegate
     */
    public SummaryMetricsTrackerDelegate summaryStartTimer(final String metricsLabel, final String... labelValues) {
        Optional<MetricsTracker> metricsTracker = metricsTrackerManager.getMetricsTrackerFactory().create(MetricsTypeEnum.SUMMARY.name(), metricsLabel);
        if (metricsTracker.isPresent()) {
            return ((SummaryMetricsTracker) metricsTracker.get()).startTimer(labelValues);
        } else {
            return new NoneSummaryMetricsTrackerDelegate();
        }
    }
    
    /**
     * Observe amount of time since start time with summary metrics tracker.
     *
     * @param delegate summary metrics tracker delegate
     */
    public void summaryObserveDuration(final SummaryMetricsTrackerDelegate delegate) {
        delegate.observeDuration();
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
}

