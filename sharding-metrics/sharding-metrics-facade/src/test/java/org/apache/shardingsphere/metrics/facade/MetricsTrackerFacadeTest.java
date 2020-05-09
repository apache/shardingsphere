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
import org.apache.shardingsphere.metrics.facade.fixture.FirstMetricsTrackerFactoryFixture;
import org.apache.shardingsphere.metrics.facade.fixture.SecondMetricsTrackerFactoryFixture;
import org.apache.shardingsphere.metrics.facade.fixture.SecondMetricsTrackerManagerFixture;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class MetricsTrackerFacadeTest {
    
    private MetricsTrackerFacade metricsTrackerFacade = MetricsTrackerFacade.getInstance();
    
    @Before
    public void setUp() {
        MetricsConfiguration metricsConfiguration = new MetricsConfiguration("fixture", null, null, null);
        metricsTrackerFacade.init(metricsConfiguration);
        assertThat(metricsTrackerFacade.getMetricsTrackerManager().getClass().getName(), is(SecondMetricsTrackerManagerFixture.class.getName()));
        assertThat(metricsTrackerFacade.getEnabled(), is(true));
    }
    
    @Test
    public void assertFindMetricsTrackerManager() {
        assertNull(metricsTrackerFacade.findMetricsTrackerManager("fixture1"));
        assertNotNull(metricsTrackerFacade.findMetricsTrackerManager("fixture"));
    }
    
    @Test
    public void counterInc() {
        metricsTrackerFacade.counterInc("request_total");
    }
    
    @Test
    public void gaugeInc() {
        metricsTrackerFacade.gaugeInc("request_total");
    }
    
    @Test
    public void gaugeDec() {
        metricsTrackerFacade.gaugeDec("request_total");
    }
    
    @Test
    public void histogram() {
        assertThat(metricsTrackerFacade.getMetricsTrackerManager().getClass().getName(), is(SecondMetricsTrackerManagerFixture.class.getName()));
        ((SecondMetricsTrackerManagerFixture) metricsTrackerFacade.getMetricsTrackerManager()).setMetricsTrackerFactory(new SecondMetricsTrackerFactoryFixture());
        HistogramMetricsTrackerDelegate delegate = metricsTrackerFacade.histogramStartTimer("request");
        metricsTrackerFacade.histogramObserveDuration(delegate);
        assertThat(delegate.getClass().getName(), is(NoneHistogramMetricsTrackerDelegate.class.getName()));
    }
    
    @Test
    public void summary() {
        assertThat(metricsTrackerFacade.getMetricsTrackerManager().getClass().getName(), is(SecondMetricsTrackerManagerFixture.class.getName()));
        ((SecondMetricsTrackerManagerFixture) metricsTrackerFacade.getMetricsTrackerManager()).setMetricsTrackerFactory(new SecondMetricsTrackerFactoryFixture());
        SummaryMetricsTrackerDelegate delegate = metricsTrackerFacade.summaryStartTimer("request");
        metricsTrackerFacade.summaryObserveDuration(delegate);
        assertThat(delegate.getClass().getName(), is(NoneSummaryMetricsTrackerDelegate.class.getName()));
    }
    
    @Test
    public void testNoneDelegate() {
        ((SecondMetricsTrackerManagerFixture) metricsTrackerFacade.getMetricsTrackerManager()).setMetricsTrackerFactory(new FirstMetricsTrackerFactoryFixture());
        SummaryMetricsTrackerDelegate summaryMetricsTrackerDelegate = metricsTrackerFacade.summaryStartTimer("request");
        assertThat(summaryMetricsTrackerDelegate.getClass().getName(), is(NoneSummaryMetricsTrackerDelegate.class.getName()));
        HistogramMetricsTrackerDelegate histogramMetricsTrackerDelegate = metricsTrackerFacade.histogramStartTimer("request");
        assertThat(histogramMetricsTrackerDelegate.getClass().getName(), is(NoneHistogramMetricsTrackerDelegate.class.getName()));
    }
}

