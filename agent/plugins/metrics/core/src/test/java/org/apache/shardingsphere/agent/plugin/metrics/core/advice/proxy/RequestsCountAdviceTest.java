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

import org.apache.shardingsphere.agent.plugin.metrics.core.advice.MockTargetAdviceObject;
import org.apache.shardingsphere.agent.plugin.metrics.core.collector.MetricsCollectorRegistry;
import org.apache.shardingsphere.agent.plugin.metrics.core.fixture.MetricsCollectorFixture;
import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public final class RequestsCountAdviceTest {
    
    private final RequestsCountAdvice advice = new RequestsCountAdvice();
    
    @After
    public void reset() {
        ((MetricsCollectorFixture) MetricsCollectorRegistry.get("proxy_requests_total", "FIXTURE")).reset();
    }
    
    @Test
    public void assertCountRequests() {
        MockTargetAdviceObject targetObject = new MockTargetAdviceObject();
        advice.beforeMethod(targetObject, mock(Method.class), new Object[]{}, "FIXTURE");
        assertThat(((MetricsCollectorFixture) MetricsCollectorRegistry.get("proxy_requests_total", "FIXTURE")).getValue(), is(1d));
    }
}
