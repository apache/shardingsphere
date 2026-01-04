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

package org.apache.shardingsphere.agent.core.advisor.executor.type;

import lombok.RequiredArgsConstructor;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ImplementationDefinition;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceMethod;
import org.apache.shardingsphere.agent.api.advice.type.StaticMethodAdvice;
import org.apache.shardingsphere.agent.api.plugin.AgentPluginEnable;
import org.apache.shardingsphere.agent.core.advisor.executor.AdviceExecutor;
import org.apache.shardingsphere.fixture.targeted.TargetObjectFixture;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StaticMethodAdviceExecutorTest {
    
    @Test
    void assertAdviceWhenCallableSucceeded() throws ReflectiveOperationException {
        List<String> queue = new LinkedList<>();
        Map<String, Collection<StaticMethodAdvice>> advices = new LinkedHashMap<>(2, 1F);
        advices.put("foo", Collections.singletonList(new RecordingStaticMethodAdvice(queue, "plain")));
        advices.put("bar", Collections.singletonList(new ConfigurableStaticMethodAdvice(queue, "config", false, false, false, false)));
        StaticMethodAdviceExecutor executor = new StaticMethodAdviceExecutor(advices);
        Method method = TargetObjectFixture.class.getMethod("staticCall", List.class);
        Callable<Object> callable = () -> {
            queue.add("origin call");
            return "result";
        };
        Object actualResult = executor.advice(TargetObjectFixture.class, method, new Object[]{queue}, callable);
        assertThat(actualResult, is("result"));
        assertThat(queue, is(Arrays.asList("plain before foo", "origin call", "plain after foo")));
    }
    
    @Test
    void assertAdviceWhenCallableThrows() throws ReflectiveOperationException {
        List<String> queue = new LinkedList<>();
        Map<String, Collection<StaticMethodAdvice>> advices = new LinkedHashMap<>(2, 1F);
        advices.put("foo", Collections.singletonList(new RecordingStaticMethodAdvice(queue, "plain")));
        advices.put("bar", Collections.singletonList(new ConfigurableStaticMethodAdvice(queue, "config", true, false, false, true)));
        StaticMethodAdviceExecutor executor = new StaticMethodAdviceExecutor(advices);
        Method method = TargetObjectFixture.class.getMethod("staticCallWhenExceptionThrown", List.class);
        Callable<Object> callable = () -> {
            throw new IllegalStateException("callable error");
        };
        assertThrows(IllegalStateException.class, () -> executor.advice(TargetObjectFixture.class, method, new Object[]{queue}, callable));
        assertThat(queue, is(Arrays.asList("plain before foo", "config before bar", "plain throw foo", "plain after foo", "config after bar")));
    }
    
    @Test
    void assertAdviceWhenCallableThrowsWithPluginDisabled() throws ReflectiveOperationException {
        List<String> queue = new LinkedList<>();
        Map<String, Collection<StaticMethodAdvice>> advices = Collections.singletonMap(
                "foo", Collections.singletonList(new ConfigurableStaticMethodAdvice(queue, "disabled", false, false, false, false)));
        StaticMethodAdviceExecutor executor = new StaticMethodAdviceExecutor(advices);
        Method method = TargetObjectFixture.class.getMethod("staticCallWhenExceptionThrown", List.class);
        Callable<Object> callable = () -> {
            throw new IllegalStateException("callable error");
        };
        assertThrows(IllegalStateException.class, () -> executor.advice(TargetObjectFixture.class, method, new Object[]{queue}, callable));
        assertTrue(queue.isEmpty());
    }
    
    @Test
    void assertAdviceWhenBeforeThrows() throws ReflectiveOperationException {
        List<String> queue = new LinkedList<>();
        Map<String, Collection<StaticMethodAdvice>> advices = new LinkedHashMap<>(2, 1F);
        advices.put("foo", Collections.singletonList(new ConfigurableStaticMethodAdvice(queue, "first", true, true, false, false)));
        advices.put("bar", Collections.singletonList(new RecordingStaticMethodAdvice(queue, "second")));
        StaticMethodAdviceExecutor executor = new StaticMethodAdviceExecutor(advices);
        Method method = TargetObjectFixture.class.getMethod("staticCall", List.class);
        Callable<Object> callable = () -> {
            queue.add("origin call");
            return "result";
        };
        Object actualResult = executor.advice(TargetObjectFixture.class, method, new Object[]{queue}, callable);
        assertThat(actualResult, is("result"));
        assertThat(queue, is(Arrays.asList("origin call", "first after foo", "second after bar")));
    }
    
    @Test
    void assertAdviceWhenAfterThrows() throws ReflectiveOperationException {
        List<String> queue = new LinkedList<>();
        Map<String, Collection<StaticMethodAdvice>> advices = new LinkedHashMap<>(2, 1F);
        advices.put("foo", Collections.singletonList(new ConfigurableStaticMethodAdvice(queue, "first", true, false, true, false)));
        advices.put("bar", Collections.singletonList(new RecordingStaticMethodAdvice(queue, "second")));
        StaticMethodAdviceExecutor executor = new StaticMethodAdviceExecutor(advices);
        Method method = TargetObjectFixture.class.getMethod("staticCall", List.class);
        Callable<Object> callable = () -> {
            queue.add("origin call");
            return "result";
        };
        Object actualResult = executor.advice(TargetObjectFixture.class, method, new Object[]{queue}, callable);
        assertThat(actualResult, is("result"));
        assertThat(queue, is(Arrays.asList("first before foo", "second before bar", "origin call")));
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Test
    void assertIntercept() {
        Builder<?> builder = mock(Builder.class);
        ImplementationDefinition<?> implementationDefinition = mock(ImplementationDefinition.class);
        ReceiverTypeDefinition<?> intercepted = mock(ReceiverTypeDefinition.class);
        MethodDescription methodDescription = mock(MethodDescription.class);
        when(builder.method(any())).thenReturn((ImplementationDefinition) implementationDefinition);
        when(implementationDefinition.intercept(any())).thenReturn((ReceiverTypeDefinition) intercepted);
        AdviceExecutor executor = new StaticMethodAdviceExecutor(Collections.emptyMap());
        assertThat(executor.intercept(builder, methodDescription), is(intercepted));
    }
    
    @RequiredArgsConstructor
    private static final class RecordingStaticMethodAdvice implements StaticMethodAdvice {
        
        private final List<String> queue;
        
        private final String label;
        
        @Override
        public void beforeMethod(final Class<?> clazz, final TargetAdviceMethod method, final Object[] args, final String pluginType) {
            queue.add(label + " before " + pluginType);
        }
        
        @Override
        public void afterMethod(final Class<?> clazz, final TargetAdviceMethod method, final Object[] args, final Object result, final String pluginType) {
            queue.add(label + " after " + pluginType);
        }
        
        @Override
        public void onThrowing(final Class<?> clazz, final TargetAdviceMethod method, final Object[] args, final Throwable throwable, final String pluginType) {
            queue.add(label + " throw " + pluginType);
        }
    }
    
    @RequiredArgsConstructor
    private static final class ConfigurableStaticMethodAdvice implements StaticMethodAdvice, AgentPluginEnable {
        
        private final List<String> queue;
        
        private final String label;
        
        private final boolean pluginEnabled;
        
        private final boolean throwBefore;
        
        private final boolean throwAfter;
        
        private final boolean throwOnThrow;
        
        @Override
        public boolean isPluginEnabled() {
            return pluginEnabled;
        }
        
        @Override
        public void beforeMethod(final Class<?> clazz, final TargetAdviceMethod method, final Object[] args, final String pluginType) {
            if (throwBefore) {
                throw new IllegalStateException("before");
            }
            queue.add(label + " before " + pluginType);
        }
        
        @Override
        public void afterMethod(final Class<?> clazz, final TargetAdviceMethod method, final Object[] args, final Object result, final String pluginType) {
            if (throwAfter) {
                throw new IllegalStateException("after");
            }
            queue.add(label + " after " + pluginType);
        }
        
        @Override
        public void onThrowing(final Class<?> clazz, final TargetAdviceMethod method, final Object[] args, final Throwable throwable, final String pluginType) {
            if (throwOnThrow) {
                throw new IllegalStateException("throw");
            }
            queue.add(label + " throw " + pluginType);
        }
    }
}
