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

package org.apache.shardingsphere.agent.core.bytebuddy.transformer.advice;

import org.apache.shardingsphere.agent.api.advice.ClassStaticMethodAroundAdvice;
import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class ComposeClassStaticMethodAroundAdviceTest {

    private ComposeClassStaticMethodAroundAdvice composeClassStaticMethodAroundAdvice;

    @Test
    public void beforeMethodTest() {
        ClassStaticMethodAroundAdvice advice = mock(ClassStaticMethodAroundAdvice.class);
        Method method = mock(Method.class);
        MethodInvocationResult methodInvocationResult = mock(MethodInvocationResult.class);
        List<ClassStaticMethodAroundAdvice> adviceList = new ArrayList<>(Arrays.asList(advice));
        composeClassStaticMethodAroundAdvice = new ComposeClassStaticMethodAroundAdvice(adviceList);
        composeClassStaticMethodAroundAdvice.beforeMethod(String.class, method, new Object[2], methodInvocationResult);
    }

    @Test
    public void afterMethodTest() {
        ClassStaticMethodAroundAdvice advice = mock(ClassStaticMethodAroundAdvice.class);
        Method method = mock(Method.class);
        MethodInvocationResult methodInvocationResult = mock(MethodInvocationResult.class);
        List<ClassStaticMethodAroundAdvice> adviceList = new ArrayList<>(Arrays.asList(advice));
        composeClassStaticMethodAroundAdvice = new ComposeClassStaticMethodAroundAdvice(adviceList);
        composeClassStaticMethodAroundAdvice.afterMethod(String.class, method, new Object[2], methodInvocationResult);
    }

    @Test
    public void onThrowingTest() {
        ClassStaticMethodAroundAdvice advice = mock(ClassStaticMethodAroundAdvice.class);
        Method method = mock(Method.class);
        List<ClassStaticMethodAroundAdvice> adviceList = new ArrayList<>(Arrays.asList(advice));
        composeClassStaticMethodAroundAdvice = new ComposeClassStaticMethodAroundAdvice(adviceList);
        composeClassStaticMethodAroundAdvice.onThrowing(String.class, method, new Object[2], new NullPointerException("Null Pointer"));
    }
}
