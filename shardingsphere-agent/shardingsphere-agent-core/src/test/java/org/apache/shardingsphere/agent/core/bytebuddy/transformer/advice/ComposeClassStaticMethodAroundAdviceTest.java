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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class ComposeClassStaticMethodAroundAdviceTest {
    
    @Mock
    private ClassStaticMethodAroundAdvice classStaticMethodAroundAdvice;
    
    private ComposeClassStaticMethodAroundAdvice actual;
    
    @Before
    public void setUp() {
        actual = new ComposeClassStaticMethodAroundAdvice(new ArrayList<>(Collections.singleton(classStaticMethodAroundAdvice)));
    }
    
    @Test
    public void assertBeforeMethod() {
        Method method = mock(Method.class);
        Object[] args = new Object[2];
        MethodInvocationResult methodInvocationResult = mock(MethodInvocationResult.class);
        actual.beforeMethod(String.class, method, args, methodInvocationResult);
        verify(classStaticMethodAroundAdvice).beforeMethod(String.class, method, args, methodInvocationResult);
    }
    
    @Test
    public void assertAfterMethod() {
        Method method = mock(Method.class);
        Object[] args = new Object[2];
        MethodInvocationResult methodInvocationResult = mock(MethodInvocationResult.class);
        actual.afterMethod(String.class, method, args, methodInvocationResult);
        verify(classStaticMethodAroundAdvice).afterMethod(String.class, method, args, methodInvocationResult);
    }
    
    @Test
    public void assertOnThrowing() {
        Method method = mock(Method.class);
        Object[] args = new Object[2];
        NullPointerException exception = new NullPointerException("");
        actual.onThrowing(String.class, method, args, exception);
        verify(classStaticMethodAroundAdvice).onThrowing(String.class, method, args, exception);
    }
}
