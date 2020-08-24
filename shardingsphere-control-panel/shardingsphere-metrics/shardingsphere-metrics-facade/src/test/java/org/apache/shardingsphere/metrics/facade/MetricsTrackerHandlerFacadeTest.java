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

import org.apache.shardingsphere.control.panel.spi.ControlPanelConfiguration;
import org.apache.shardingsphere.control.panel.spi.engine.ControlPanelFacadeEngine;
import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public final class MetricsTrackerHandlerFacadeTest {
    
    private final MetricsTrackerHandlerFacade metricsTrackerHandlerFacade = new MetricsTrackerHandlerFacade();
    
    @Test
    public void assertCounterIncrement() {
        metricsTrackerHandlerFacade.counterIncrement("request_total");
    }
    
    @Test
    public void assertGaugeIncrement() {
        metricsTrackerHandlerFacade.gaugeIncrement("request_total");
    }
    
    @Test
    public void assertGaugeDecrement() {
        metricsTrackerHandlerFacade.gaugeDecrement("request_total");
    }
    
    @Test
    public void assertHistogramAndSummary() {
        Supplier<Boolean> histogramDelegate = metricsTrackerHandlerFacade.histogramStartTimer("request");
        assertFalse(histogramDelegate.get());
        Supplier<Boolean> summaryDelegate = metricsTrackerHandlerFacade.summaryStartTimer("request");
        assertFalse(summaryDelegate.get());
        init();
        Supplier<Boolean> emptyHistogram = metricsTrackerHandlerFacade.histogramStartTimer("request");
        assertTrue(emptyHistogram.get());
        Supplier<Boolean> empty = metricsTrackerHandlerFacade.summaryStartTimer("request");
        assertTrue(empty.get());
    }
    
    private void init() {
        MetricsConfiguration metricsConfiguration = new MetricsConfiguration("fixture", null, 0, false, true, 8, null);
        List<ControlPanelConfiguration> controlPanelConfigs = new LinkedList<>();
        controlPanelConfigs.add(metricsConfiguration);
        new ControlPanelFacadeEngine().init(controlPanelConfigs);
    }
}
