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

package org.apache.shardingsphere.metrics.facade.handler;

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
import org.apache.shardingsphere.metrics.enums.MetricsTypeEnum;
import org.apache.shardingsphere.metrics.facade.executor.MetricsThreadPoolExecutor;
import org.apache.shardingsphere.metrics.spi.MetricsTrackerManager;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Metrics tracker handler.
 */
@Slf4j
public final class MetricsTrackerHandler {
    
    private static final String NAME_FORMAT = "ShardingSphere-Metrics-%d";
    
    private static final int FUTURE_GET_TIME_OUT_MILLISECONDS = 500;
    
    private static final int QUEUE_SIZE = 5000;
    
    @Getter
    private MetricsTrackerManager metricsTrackerManager;
    
    @Getter
    private ExecutorService executorService;
    
    private volatile boolean async;
    
    /**
     * Get metrics tracker handler of lazy load singleton.
     *
     * @return Metrics tracker handler
     */
    public static MetricsTrackerHandler getInstance() {
        return MetricsTrackerHandlerHolder.INSTANCE;
    }
    
    /**
     * Init for metrics tracker handler.
     *
     * @param async async
     * @param threadCount thread count
     * @param metricsTrackerManager metrics tracker manager
     */
    public void init(final boolean async, final int threadCount, final MetricsTrackerManager metricsTrackerManager) {
        this.async = async;
        this.metricsTrackerManager = metricsTrackerManager;
        if (this.async) {
            executorService = new MetricsThreadPoolExecutor(NAME_FORMAT, threadCount, QUEUE_SIZE);
        }
    }
    
    /**
     * Increment of counter metrics tracker.
     *
     * @param metricsLabel metrics label
     * @param labelValues  label values
     */
    public void counterInc(final String metricsLabel, final String... labelValues) {
        if (async) {
            executorService.execute(() -> handlerCounter(metricsLabel, labelValues));
        } else {
            handlerCounter(metricsLabel, labelValues);
        }
    }
    
    /**
     * Increment of gauge metrics tracker.
     *
     * @param metricsLabel metrics label
     * @param labelValues  label values
     */
    public void gaugeInc(final String metricsLabel, final String... labelValues) {
        if (async) {
            executorService.execute(() -> handlerGaugeInc(metricsLabel, labelValues));
        } else {
            handlerGaugeInc(metricsLabel, labelValues);
        }
    }
    
    /**
     * Decrement of gauge metrics tracker.
     *
     * @param metricsLabel metrics label
     * @param labelValues  label values
     */
    public void gaugeDec(final String metricsLabel, final String... labelValues) {
        if (async) {
            executorService.execute(() -> handlerGaugeDec(metricsLabel, labelValues));
        } else {
            handlerGaugeDec(metricsLabel, labelValues);
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
        if (async) {
            try {
                return executorService.submit(() -> handlerHistogramStartTimer(metricsLabel, labelValues)).get(FUTURE_GET_TIME_OUT_MILLISECONDS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new IllegalStateException(String.format("Error while fetching histogram metric with metricsLabel= %s and labelValues=%s", metricsLabel, Arrays.toString(labelValues)), e);
            }
        } else {
            return handlerHistogramStartTimer(metricsLabel, labelValues);
        }
    }
    
    /**
     * Observe amount of time since start time with histogram metrics tracker.
     *
     * @param delegate histogram metrics tracker delegate
     */
    public void histogramObserveDuration(final HistogramMetricsTrackerDelegate delegate) {
        if (async) {
            executorService.execute(delegate::observeDuration);
        } else {
            delegate.observeDuration();
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
        if (async) {
            try {
                return executorService.submit(() -> handlerSummaryStartTimer(metricsLabel, labelValues)).get(FUTURE_GET_TIME_OUT_MILLISECONDS, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                throw new IllegalStateException(String.format("Error while fetching summary metric with metricsLabel= %s and labelValues=%s", metricsLabel, Arrays.toString(labelValues)), e);
            }
        } else {
            return handlerSummaryStartTimer(metricsLabel, labelValues);
        }
    }
    
    /**
     * Observe amount of time since start time with summary metrics tracker.
     *
     * @param delegate summary metrics tracker delegate
     */
    public void summaryObserveDuration(final SummaryMetricsTrackerDelegate delegate) {
        if (async) {
            executorService.execute(delegate::observeDuration);
        } else {
            delegate.observeDuration();
        }
    }
    
    /**
     * Executor service close.
     */
    public void close() {
        async = false;
        if (null != executorService && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
    
    private void handlerCounter(final String metricsLabel, final String... labelValues) {
        metricsTrackerManager.getMetricsTrackerFactory().create(MetricsTypeEnum.COUNTER.name(), metricsLabel)
                .ifPresent(metricsTracker -> ((CounterMetricsTracker) metricsTracker).inc(1.0, labelValues));
    }
    
    private void handlerGaugeInc(final String metricsLabel, final String... labelValues) {
        metricsTrackerManager.getMetricsTrackerFactory().create(MetricsTypeEnum.GAUGE.name(), metricsLabel)
                .ifPresent(metricsTracker -> ((GaugeMetricsTracker) metricsTracker).increment(1.0, labelValues));
    }
    
    /**
     * Handler gauge dec.
     *
     * @param metricsLabel the metrics label
     * @param labelValues  the label values
     */
    public void handlerGaugeDec(final String metricsLabel, final String... labelValues) {
        metricsTrackerManager.getMetricsTrackerFactory().create(MetricsTypeEnum.GAUGE.name(), metricsLabel)
                .ifPresent(metricsTracker -> ((GaugeMetricsTracker) metricsTracker).decrement(1.0, labelValues));
    }
    
    private Optional<HistogramMetricsTrackerDelegate> handlerHistogramStartTimer(final String metricsLabel, final String... labelValues) {
        Optional<MetricsTracker> metricsTracker = metricsTrackerManager.getMetricsTrackerFactory().create(MetricsTypeEnum.HISTOGRAM.name(), metricsLabel);
        return metricsTracker.map(tracker -> Optional.of(((HistogramMetricsTracker) tracker).startTimer(labelValues))).orElseGet(() -> Optional.of(new NoneHistogramMetricsTrackerDelegate()));
    }
    
    private Optional<SummaryMetricsTrackerDelegate> handlerSummaryStartTimer(final String metricsLabel, final String... labelValues) {
        Optional<MetricsTracker> metricsTracker = metricsTrackerManager.getMetricsTrackerFactory().create(MetricsTypeEnum.SUMMARY.name(), metricsLabel);
        return metricsTracker.map(tracker -> Optional.of(((SummaryMetricsTracker) tracker).startTimer(labelValues))).orElseGet(() -> Optional.of(new NoneSummaryMetricsTrackerDelegate()));
    }
    
    /**
     * Metrics tracker handler holder.
     */
    private static class MetricsTrackerHandlerHolder {
        
        private static final MetricsTrackerHandler INSTANCE = new MetricsTrackerHandler();
    }
}

