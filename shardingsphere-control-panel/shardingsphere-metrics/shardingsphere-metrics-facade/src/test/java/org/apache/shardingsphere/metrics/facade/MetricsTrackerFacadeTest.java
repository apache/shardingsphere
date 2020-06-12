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

import org.apache.shardingsphere.metrics.api.HistogramMetricsTrackerDelegate;
import org.apache.shardingsphere.metrics.api.NoneHistogramMetricsTrackerDelegate;
import org.apache.shardingsphere.metrics.api.NoneSummaryMetricsTrackerDelegate;
import org.apache.shardingsphere.metrics.api.SummaryMetricsTrackerDelegate;
import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;
import org.apache.shardingsphere.metrics.facade.fixture.SecondMetricsTrackerManagerFixture;
import org.apache.shardingsphere.metrics.facade.util.FieldUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class MetricsTrackerFacadeTest {
    
    private final MetricsTrackerFacade metricsTrackerFacade = MetricsTrackerFacade.getInstance();
    
    @Before
    public void setUp() {
        MetricsConfiguration metricsConfiguration = new MetricsConfiguration("fixture", null, null, false, 8, null);
        metricsTrackerFacade.init(metricsConfiguration);
    }
    
    @Test
    public void assertInit() {
        MetricsConfiguration metricsConfiguration = new MetricsConfiguration("fixture", null, null, false, 8, null);
        metricsTrackerFacade.init(metricsConfiguration);
        assertThat(metricsTrackerFacade.isEnabled(), is(true));
    }
    
    @Test
    public void assertFindMetricsTrackerManager() {
        assertNull(metricsTrackerFacade.findMetricsTrackerManager("fixture1"));
        assertNotNull(metricsTrackerFacade.findMetricsTrackerManager("fixture"));
    }
    
    @Test
    public void assertCounterInc() {
        metricsTrackerFacade.counterInc("request_total");
    }
    
    @Test
    public void assertGaugeInc() {
        metricsTrackerFacade.gaugeInc("request_total");
    }
    
    @Test
    public void assertGaugeDec() {
        metricsTrackerFacade.gaugeDec("request_total");
    }
    
    @Test
    public void assertHistogram() {
        assertThat(metricsTrackerFacade.getMetricsTrackerManager().getClass().getName(), is(SecondMetricsTrackerManagerFixture.class.getName()));
        Optional<HistogramMetricsTrackerDelegate> histogramDelegate = metricsTrackerFacade.histogramStartTimer("request");
        assertThat(histogramDelegate.isPresent(), is(true));
        histogramDelegate.ifPresent(delegate -> {
            metricsTrackerFacade.histogramObserveDuration(delegate);
            assertThat(delegate.getClass().getName(), is(NoneHistogramMetricsTrackerDelegate.class.getName()));
        });
    
        FieldUtil.setField(metricsTrackerFacade, "enabled", false);
        Optional<HistogramMetricsTrackerDelegate> empty = metricsTrackerFacade.histogramStartTimer("request");
        assertThat(empty, is(Optional.empty()));
    }
    
    @Test
    public void summary() {
        assertThat(metricsTrackerFacade.getMetricsTrackerManager().getClass().getName(), is(SecondMetricsTrackerManagerFixture.class.getName()));
        Optional<SummaryMetricsTrackerDelegate> summaryDelegate = metricsTrackerFacade.summaryStartTimer("request");
        assertThat(summaryDelegate.isPresent(), is(true));
        summaryDelegate.ifPresent(delegate -> {
            metricsTrackerFacade.summaryObserveDuration(delegate);
            assertThat(delegate.getClass().getName(), is(NoneSummaryMetricsTrackerDelegate.class.getName()));
        });
    
        FieldUtil.setField(metricsTrackerFacade, "enabled", false);
        Optional<SummaryMetricsTrackerDelegate> empty = metricsTrackerFacade.summaryStartTimer("request");
        assertThat(empty, is(Optional.empty()));
    }
    
    @Test
    public void testStop() {
        metricsTrackerFacade.stop();
        assertThat(metricsTrackerFacade.isEnabled(), is(false));
    }
}

