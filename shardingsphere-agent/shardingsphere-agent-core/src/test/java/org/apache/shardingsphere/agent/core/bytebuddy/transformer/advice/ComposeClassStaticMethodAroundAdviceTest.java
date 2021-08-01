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
import org.junit.After;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ComposeClassStaticMethodAroundAdviceTest {
    
    private final Collection<ClassStaticMethodAroundAdvice> advices;
    
    @Test
    public void beforeMethod(final Class<?> clazz, final Method method, final Object[] args, final MethodInvocationResult result) {
        assertThat(advices.forEach(each -> each.beforeMethod(clazz, method, args, result)), is(true));
    }
    
    @Test
    public void afterMethod(final Class<?> clazz, final Method method, final Object[] args, final MethodInvocationResult result) {
        assertThat(advices.forEach(each -> each.afterMethod(clazz, method, args, result)), is(true));
    }
    
    @Test
    public void onThrowing(final Class<?> clazz, final Method method, final Object[] args, final Throwable throwable) {
        assertThat(advices.forEach(each -> each.onThrowing(clazz, method, args, throwable)), is(true));
    }
    
    @After
    public void cleanup() {
        Collection.clear();
    }
}
