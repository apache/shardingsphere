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

package org.apache.shardingsphere.metrics.prometheus;

import io.prometheus.client.exporter.HTTPServer;
import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public final class PrometheusMetricsTrackerManagerTest {
    
    @Test
    public void start() {
        PrometheusMetricsTrackerManager manager = new PrometheusMetricsTrackerManager();
        MetricsConfiguration metricsConfiguration = new MetricsConfiguration("metricsName", "", 9190, null);
        manager.start(metricsConfiguration);
        HTTPServer server = manager.getServer();
        assertNotNull(server);
        assertEquals(manager.getType(), "prometheus");
        manager.stop();
    }
}

