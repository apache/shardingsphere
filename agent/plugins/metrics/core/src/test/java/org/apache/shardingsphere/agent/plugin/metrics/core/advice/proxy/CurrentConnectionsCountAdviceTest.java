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

package org.apache.shardingsphere.agent.plugin.metrics.core.advice.proxy;

import org.apache.shardingsphere.agent.plugin.metrics.core.collector.MetricsCollectorRegistry;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricCollectorType;
import org.apache.shardingsphere.agent.plugin.metrics.core.config.MetricConfiguration;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.collector.MetricsCollectorFixture;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.TargetAdviceObjectFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CurrentConnectionsCountAdviceTest {
    
    private final MetricConfiguration config = new MetricConfiguration("proxy_current_connections", MetricCollectorType.GAUGE, null, Collections.emptyList(), Collections.emptyMap());
    
    private final CurrentConnectionsCountAdvice advice = new CurrentConnectionsCountAdvice();
    
    @AfterEach
    void reset() {
        ((MetricsCollectorFixture) MetricsCollectorRegistry.get(config, "FIXTURE")).reset();
    }
    
    @Test
    void assertCountCurrentConnections() {
        TargetAdviceObjectFixture targetObject = new TargetAdviceObjectFixture();
        advice.beforeMethod(targetObject, mockMethod("channelActive"), new Object[]{}, "FIXTURE");
        advice.beforeMethod(targetObject, mockMethod("channelActive"), new Object[]{}, "FIXTURE");
        advice.beforeMethod(targetObject, mockMethod("channelInactive"), new Object[]{}, "FIXTURE");
        assertThat(MetricsCollectorRegistry.get(config, "FIXTURE").toString(), is("1"));
    }
    
    private Method mockMethod(final String methodName) {
        Method result = mock(Method.class);
        when(result.getName()).thenReturn(methodName);
        return result;
    }
}
