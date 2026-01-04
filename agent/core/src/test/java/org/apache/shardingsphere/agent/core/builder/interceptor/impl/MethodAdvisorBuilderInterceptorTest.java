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

package org.apache.shardingsphere.agent.core.builder.interceptor.impl;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodList.Explicit;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import org.apache.shardingsphere.agent.core.advisor.config.AdvisorConfiguration;
import org.apache.shardingsphere.agent.core.advisor.executor.AdviceExecutor;
import org.apache.shardingsphere.agent.core.advisor.executor.AdviceExecutorFactory;
import org.apache.shardingsphere.agent.core.advisor.executor.AdviceFactory;
import org.apache.shardingsphere.agent.core.plugin.classloader.ClassLoaderContext;
import org.apache.shardingsphere.fixture.targeted.TargetObjectFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MethodAdvisorBuilderInterceptorTest {
    
    @AfterEach
    void resetCache() throws ReflectiveOperationException {
        ((Map<?, ?>) Plugins.getMemberAccessor().get(AdviceFactory.class.getDeclaredField("CACHED_ADVICES"), null)).clear();
        ((Map<?, ?>) Plugins.getMemberAccessor().get(ClassLoaderContext.class.getDeclaredField("AGENT_CLASS_LOADERS"), null)).clear();
    }
    
    @Test
    void assertInterceptWhenNoMatchedAdvice() throws ReflectiveOperationException {
        AdvisorConfiguration advisorConfig = new AdvisorConfiguration(TargetObjectFixture.class.getName());
        TypeDescription typeDescription = mock(TypeDescription.class);
        when(typeDescription.getDeclaredMethods()).thenReturn(new Explicit<>(
                Collections.singletonList(new MethodDescription.ForLoadedMethod(TargetObjectFixture.class.getMethod("call", List.class)))));
        MethodAdvisorBuilderInterceptor interceptor = new MethodAdvisorBuilderInterceptor(
                typeDescription, new ClassLoaderContext(new URLClassLoader(new URL[0], getClass().getClassLoader()), Collections.emptyList()), advisorConfig);
        Builder<?> builder = mock(Builder.class);
        assertThat(interceptor.intercept(builder), is(builder));
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void assertInterceptWhenAdviceMatched() throws ReflectiveOperationException {
        TypeDescription typeDescription = mock(TypeDescription.class);
        MethodDescription.InDefinedShape methodDescription = mock(MethodDescription.InDefinedShape.class);
        when(typeDescription.getDeclaredMethods()).thenReturn(new Explicit<>(Collections.singletonList(methodDescription)));
        MethodAdvisorBuilderInterceptor interceptor = new MethodAdvisorBuilderInterceptor(
                typeDescription, new ClassLoaderContext(new URLClassLoader(new URL[0], getClass().getClassLoader()), Collections.emptyList()), new AdvisorConfiguration("ignored"));
        AdviceExecutorFactory factory = mock(AdviceExecutorFactory.class);
        AdviceExecutor adviceExecutor = mock(AdviceExecutor.class);
        Builder intercepted = mock(Builder.class);
        when(factory.findMatchedAdviceExecutor(methodDescription)).thenReturn(Optional.of(adviceExecutor));
        when(adviceExecutor.intercept(any(Builder.class), any(MethodDescription.class))).thenReturn(intercepted);
        Plugins.getMemberAccessor().set(MethodAdvisorBuilderInterceptor.class.getDeclaredField("adviceExecutorFactory"), interceptor, factory);
        Builder<?> builder = mock(Builder.class);
        assertThat(interceptor.intercept(builder), is(intercepted));
    }
    
    @Test
    void assertInterceptWhenAdviceInterceptThrows() throws ReflectiveOperationException {
        TypeDescription typeDescription = mock(TypeDescription.class);
        MethodDescription.InDefinedShape methodDescription = mock(MethodDescription.InDefinedShape.class);
        when(typeDescription.getDeclaredMethods()).thenReturn(new Explicit<>(Collections.singletonList(methodDescription)));
        MethodAdvisorBuilderInterceptor interceptor = new MethodAdvisorBuilderInterceptor(
                typeDescription, new ClassLoaderContext(new URLClassLoader(new URL[0], getClass().getClassLoader()), Collections.emptyList()), new AdvisorConfiguration("ignored"));
        AdviceExecutorFactory factory = mock(AdviceExecutorFactory.class);
        AdviceExecutor adviceExecutor = mock(AdviceExecutor.class);
        when(factory.findMatchedAdviceExecutor(methodDescription)).thenReturn(Optional.of(adviceExecutor));
        when(adviceExecutor.intercept(any(Builder.class), any(MethodDescription.class))).thenThrow(new IllegalStateException("error"));
        Plugins.getMemberAccessor().set(MethodAdvisorBuilderInterceptor.class.getDeclaredField("adviceExecutorFactory"), interceptor, factory);
        Builder<?> builder = mock(Builder.class);
        assertThat(interceptor.intercept(builder), is(builder));
    }
}
