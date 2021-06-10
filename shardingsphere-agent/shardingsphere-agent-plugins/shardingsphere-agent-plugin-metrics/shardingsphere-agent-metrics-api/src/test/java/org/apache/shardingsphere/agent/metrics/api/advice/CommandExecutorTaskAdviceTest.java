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

package org.apache.shardingsphere.agent.metrics.api.advice;

import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.apache.shardingsphere.agent.metrics.api.util.ReflectiveUtil;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class CommandExecutorTaskAdviceTest extends MetricsAdviceBaseTest {
    
    private final CommandExecutorTaskAdvice commandExecutorTaskAdvice = new CommandExecutorTaskAdvice();
    
    @Test
    @SuppressWarnings("unchecked")
    public void assertMethod() {
        MockAdviceTargetObject targetObject = new MockAdviceTargetObject();
        commandExecutorTaskAdvice.beforeMethod(targetObject, null, new Object[]{}, new MethodInvocationResult());
        commandExecutorTaskAdvice.afterMethod(targetObject, null, new Object[]{}, new MethodInvocationResult());
        Map<String, LongAdder> longAdderMap = (Map<String, LongAdder>) ReflectiveUtil.getFieldValue(getFixturemetricsregister(), "HISTOGRAM_MAP");
        assertThat(longAdderMap.size(), is(1));
        LongAdder longAdder = longAdderMap.get("proxy_execute_latency_millis");
        assertNotNull(longAdder);
    }
}
