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

import org.apache.shardingsphere.metrics.api.HistogramMetricsTrackerDelegate;
import org.apache.shardingsphere.metrics.api.NoneHistogramMetricsTrackerDelegate;
import org.apache.shardingsphere.metrics.api.NoneSummaryMetricsTrackerDelegate;
import org.apache.shardingsphere.metrics.api.SummaryMetricsTrackerDelegate;
import org.apache.shardingsphere.metrics.facade.fixture.SecondMetricsTrackerManagerFixture;
import org.apache.shardingsphere.metrics.facade.util.FieldUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class MetricsTrackerHandlerTest {
    
    private static final String METRICS_LABEL = "test";
    
    private final MetricsTrackerHandler handler = MetricsTrackerHandler.getInstance();
    
    @Before
    public void init() {
        handler.init(true, Runtime.getRuntime().availableProcessors() << 1, new SecondMetricsTrackerManagerFixture());
    }
    
    @Test
    public void counterInc() {
        handler.counterInc(METRICS_LABEL);
        FieldUtil.setField(handler, "async", false);
        handler.counterInc(METRICS_LABEL);
    }
    
    @Test
    public void gaugeInc() {
        handler.gaugeInc(METRICS_LABEL);
        FieldUtil.setField(handler, "async", false);
        handler.gaugeInc(METRICS_LABEL);
    }
    
    @Test
    public void gaugeDec() {
        handler.gaugeDec(METRICS_LABEL);
        FieldUtil.setField(handler, "async", false);
        handler.gaugeDec(METRICS_LABEL);
    }
    
    @Test
    public void assertHistogram() {
        Optional<HistogramMetricsTrackerDelegate> histogramDelegate = handler.histogramStartTimer(METRICS_LABEL);
        assertTrue(histogramDelegate.isPresent());
        histogramDelegate.ifPresent(delegate -> {
            handler.histogramObserveDuration(delegate);
            assertThat(delegate.getClass().getName(), is(NoneHistogramMetricsTrackerDelegate.class.getName()));
            handler.histogramObserveDuration(delegate);
        });
        FieldUtil.setField(handler, "async", false);
        Optional<HistogramMetricsTrackerDelegate> syncHistogram = handler.histogramStartTimer(METRICS_LABEL);
        assertTrue(syncHistogram.isPresent());
        syncHistogram.ifPresent(delegate -> {
            assertThat(delegate.getClass().getName(), is(NoneHistogramMetricsTrackerDelegate.class.getName()));
            handler.histogramObserveDuration(delegate);
        });
    }
    
    @Test
    public void summary() {
        Optional<SummaryMetricsTrackerDelegate> summaryDelegate = handler.summaryStartTimer(METRICS_LABEL);
        assertTrue(summaryDelegate.isPresent());
        summaryDelegate.ifPresent(delegate -> {
            handler.summaryObserveDuration(delegate);
            assertThat(delegate.getClass().getName(), is(NoneSummaryMetricsTrackerDelegate.class.getName()));
            handler.summaryObserveDuration(delegate);
        });
        
        FieldUtil.setField(handler, "async", false);
        Optional<SummaryMetricsTrackerDelegate> syncSummary = handler.summaryStartTimer(METRICS_LABEL);
        assertTrue(syncSummary.isPresent());
        syncSummary.ifPresent(delegate -> {
            assertThat(delegate.getClass().getName(), is(NoneSummaryMetricsTrackerDelegate.class.getName()));
            handler.summaryObserveDuration(delegate);
        });
    }
    
    @After
    public void assertClose() {
        handler.close();
        assertTrue(handler.getExecutorService().isShutdown());
    }
}

