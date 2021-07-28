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
import org.apache.shardingsphere.agent.metrics.api.constant.MethodNameConstant;
import org.apache.shardingsphere.agent.metrics.api.util.ReflectiveUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class CommandExecutorTaskAdviceTest extends MetricsAdviceBaseTest {
    
    private final CommandExecutorTaskAdvice commandExecutorTaskAdvice = new CommandExecutorTaskAdvice();
    
    @Mock
    private Method run;
    
    @Mock
    private Method processException;
    
    @Test
    @SuppressWarnings("unchecked")
    public void assertExecuteLatency() {
        when(run.getName()).thenReturn(MethodNameConstant.COMMAND_EXECUTOR_RUN);
        MockAdviceTargetObject targetObject = new MockAdviceTargetObject();
        commandExecutorTaskAdvice.beforeMethod(targetObject, run, new Object[]{}, new MethodInvocationResult());
        commandExecutorTaskAdvice.afterMethod(targetObject, run, new Object[]{}, new MethodInvocationResult());
        Map<String, LongAdder> longAdderMap = (Map<String, LongAdder>) ReflectiveUtil.getFieldValue(getFixturemetricsregister(), "HISTOGRAM_MAP");
        assertThat(longAdderMap.size(), is(1));
        LongAdder longAdder = longAdderMap.get("proxy_execute_latency_millis");
        assertNotNull(longAdder);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void assertExecutorErrorTotal() {
        when(processException.getName()).thenReturn(MethodNameConstant.COMMAND_EXECUTOR_EXCEPTION);
        MockAdviceTargetObject targetObject = new MockAdviceTargetObject();
        commandExecutorTaskAdvice.afterMethod(targetObject, processException, new Object[]{}, new MethodInvocationResult());
        Map<String, DoubleAdder> adderMap = (Map<String, DoubleAdder>) ReflectiveUtil.getFieldValue(getFixturemetricsregister(), "COUNTER_MAP");
        assertThat(adderMap.size(), org.hamcrest.Matchers.greaterThan(0));
        DoubleAdder doubleAdder = adderMap.get("proxy_execute_error_total");
        assertNotNull(doubleAdder);
    }
}
