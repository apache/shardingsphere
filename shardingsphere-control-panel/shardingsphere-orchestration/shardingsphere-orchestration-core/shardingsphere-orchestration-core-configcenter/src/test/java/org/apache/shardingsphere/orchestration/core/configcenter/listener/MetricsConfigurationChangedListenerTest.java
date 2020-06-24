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

package org.apache.shardingsphere.orchestration.core.configcenter.listener;

import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;
import org.apache.shardingsphere.orchestration.center.ConfigCenterRepository;
import org.apache.shardingsphere.orchestration.center.listener.DataChangedEvent;
import org.apache.shardingsphere.orchestration.core.common.event.MetricsConfigurationChangedEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class MetricsConfigurationChangedListenerTest {
    
    private static final String METRICS_YAML = ""
            + "  name: prometheus\n"
            + "  host: 127.0.0.1\n"
            + "  port: 9190\n";
    
    @Mock
    private ConfigCenterRepository configCenterRepository;
    
    private MetricsConfigurationChangedListener metricsConfigurationChangedListener;
    
    @Before
    public void setUp() {
        metricsConfigurationChangedListener = new MetricsConfigurationChangedListener("test", configCenterRepository);
    }
    
    @Test
    public void assertCreateShardingOrchestrationEvent() {
        MetricsConfigurationChangedEvent event = metricsConfigurationChangedListener.createShardingOrchestrationEvent(new DataChangedEvent("test", METRICS_YAML, DataChangedEvent.ChangedType.UPDATED));
        MetricsConfiguration actual = event.getMetricsConfiguration();
        assertThat(actual, notNullValue());
        assertThat(actual.getMetricsName(), is("prometheus"));
        assertThat(actual.getPort(), is(9190));
        assertThat(actual.getHost(), is("127.0.0.1"));
        assertThat(actual.getAsync(), is(true));
    }
}
