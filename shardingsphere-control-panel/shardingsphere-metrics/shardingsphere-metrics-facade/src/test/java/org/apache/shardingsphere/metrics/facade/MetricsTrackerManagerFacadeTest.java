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

import java.util.LinkedList;
import java.util.List;
import org.apache.shardingsphere.control.panel.spi.FacadeConfiguration;
import org.apache.shardingsphere.control.panel.spi.engine.ControlPanelFacadeEngine;
import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;
import org.apache.shardingsphere.metrics.facade.fixture.SecondMetricsTrackerManagerFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class MetricsTrackerManagerFacadeTest {
    
    @Before
    public void setUp() {
        MetricsConfiguration metricsConfiguration = new MetricsConfiguration("fixture", null, 0, false, true, 8, null);
        List<FacadeConfiguration> facadeConfigurations = new LinkedList<>();
        facadeConfigurations.add(metricsConfiguration);
        new ControlPanelFacadeEngine().init(facadeConfigurations);
    }
    
    @Test
    public void assertInit() {
        assertThat(MetricsTrackerManagerFacade.getEnabled(), is(true));
    }
    
    @Test
    public void assertFindMetricsTrackerManager() {
        assertNotNull(MetricsTrackerManagerFacade.getMetricsTrackerManager());
        assertThat(MetricsTrackerManagerFacade.getMetricsTrackerManager().getClass().getName(), is(SecondMetricsTrackerManagerFixture.class.getName()));
    }
    
    @Test
    public void testClose() {
        MetricsTrackerManagerFacade.close();
        assertThat(MetricsTrackerManagerFacade.getEnabled(), is(false));
    }
    
    @Test
    public void restart() {
        MetricsConfiguration metricsConfiguration = new MetricsConfiguration("fixture", null, 0, false, true, 8, null);
        MetricsTrackerManagerFacade.restart(metricsConfiguration);
        assertThat(MetricsTrackerManagerFacade.getEnabled(), is(true));
    }
    
    @After
    public void clean() {
        MetricsTrackerManagerFacade.close();
    }
}

