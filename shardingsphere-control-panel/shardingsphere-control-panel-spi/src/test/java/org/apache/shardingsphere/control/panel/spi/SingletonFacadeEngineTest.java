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

package org.apache.shardingsphere.control.panel.spi;

import org.apache.shardingsphere.control.panel.spi.engine.SingletonFacadeEngine;
import org.apache.shardingsphere.control.panel.spi.fixture.FirstMetricsTrackerFacade;
import org.apache.shardingsphere.control.panel.spi.metrics.MetricsHandlerFacade;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class SingletonFacadeEngineTest {
    
    @Test
    public void assertBuild() {
        Optional<MetricsHandlerFacade> facade = SingletonFacadeEngine.buildMetrics();
        assertTrue(facade.isPresent());
        MetricsHandlerFacade first = facade.get();
        assertThat(first.getClass().getName(), is(FirstMetricsTrackerFacade.class.getName()));
        Optional<MetricsHandlerFacade> facadeSecond = SingletonFacadeEngine.buildMetrics();
        assertTrue(facadeSecond.isPresent());
        MetricsHandlerFacade second = facadeSecond.get();
        assertThat(first, is(second));
    }
}
