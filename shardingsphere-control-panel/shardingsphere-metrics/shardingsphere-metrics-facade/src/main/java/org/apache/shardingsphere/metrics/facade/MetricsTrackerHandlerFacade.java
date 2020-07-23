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

import java.util.Optional;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.control.panel.spi.metrics.MetricsHandlerFacade;
import org.apache.shardingsphere.metrics.api.HistogramMetricsTrackerDelegate;
import org.apache.shardingsphere.metrics.api.SummaryMetricsTrackerDelegate;
import org.apache.shardingsphere.metrics.facade.handler.MetricsTrackerHandler;

/**
 * Metrics tracker facade.
 */
@Slf4j
public final class MetricsTrackerHandlerFacade implements MetricsHandlerFacade {
    
    @Override
    public void counterIncrement(final String metricsLabel, final String... labelValues) {
        if (MetricsTrackerManagerFacade.getEnabled()) {
            MetricsTrackerHandler.getInstance().counterInc(metricsLabel, labelValues);
        }
    }
    
    @Override
    public void gaugeIncrement(final String metricsLabel, final String... labelValues) {
        if (MetricsTrackerManagerFacade.getEnabled()) {
            MetricsTrackerHandler.getInstance().gaugeInc(metricsLabel, labelValues);
        }
    }
    
    @Override
    public void gaugeDecrement(final String metricsLabel, final String... labelValues) {
        if (MetricsTrackerManagerFacade.getEnabled()) {
            MetricsTrackerHandler.getInstance().gaugeDec(metricsLabel, labelValues);
        }
    }
    
    @Override
    public Supplier<Boolean> histogramStartTimer(final String metricsLabel, final String... labelValues) {
        if (!MetricsTrackerManagerFacade.getEnabled()) {
            return () -> false;
        }
        Optional<HistogramMetricsTrackerDelegate> histogramMetricsTrackerDelegate = MetricsTrackerHandler.getInstance().histogramStartTimer(metricsLabel, labelValues);
        return () -> {
            histogramMetricsTrackerDelegate.ifPresent(this::histogramObserveDuration);
            return true;
        };
    }
    
    private void histogramObserveDuration(final HistogramMetricsTrackerDelegate delegate) {
        if (MetricsTrackerManagerFacade.getEnabled()) {
            MetricsTrackerHandler.getInstance().histogramObserveDuration(delegate);
        }
    }
    
    @Override
    public Supplier<Boolean> summaryStartTimer(final String metricsLabel, final String... labelValues) {
        if (!MetricsTrackerManagerFacade.getEnabled()) {
            return () -> false;
        }
        Optional<SummaryMetricsTrackerDelegate> optionalSummaryMetricsTrackerDelegate = MetricsTrackerHandler.getInstance().summaryStartTimer(metricsLabel, labelValues);
        return () -> {
            optionalSummaryMetricsTrackerDelegate.ifPresent(this::summaryObserveDuration);
            return true;
        };
    }
    
    private void summaryObserveDuration(final SummaryMetricsTrackerDelegate delegate) {
        if (MetricsTrackerManagerFacade.getEnabled()) {
            MetricsTrackerHandler.getInstance().summaryObserveDuration(delegate);
        }
    }
}

