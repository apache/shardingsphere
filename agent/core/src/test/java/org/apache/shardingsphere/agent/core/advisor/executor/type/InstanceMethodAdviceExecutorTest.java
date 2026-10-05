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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ImplementationDefinition;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceMethod;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.api.advice.type.InstanceMethodAdvice;
import org.apache.shardingsphere.agent.api.plugin.AgentPluginEnable;
import org.apache.shardingsphere.agent.core.advisor.executor.AdviceExecutor;
import org.apache.shardingsphere.fixture.targeted.TargetObjectFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InstanceMethodAdviceExecutorTest {
    
    private final Logger logger = Logger.getLogger(InstanceMethodAdviceExecutor.class.getName());
    
    private final List<LogRecord> records = new LinkedList<>();
    
    private final Handler handler = new RecordingHandler(records);
    
    private boolean originalUseParentHandlers;
    
    @BeforeEach
    void setUp() {
        originalUseParentHandlers = logger.getUseParentHandlers();
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
    }
    
    @AfterEach
    void tearDown() {
        logger.removeHandler(handler);
        logger.setUseParentHandlers(originalUseParentHandlers);
        handler.close();
        records.forEach(logger::log);
    }
    
    @Test
    void assertAdviceWhenCallableSucceeded() throws ReflectiveOperationException {
        List<String> queue = new LinkedList<>();
        Map<String, Collection<InstanceMethodAdvice>> advices = new LinkedHashMap<>(2, 1F);
        advices.put("foo", Collections.singletonList(new RecordingInstanceMethodAdvice(queue, "plain")));
        advices.put("bar", Collections.singletonList(new ConfigurableInstanceMethodAdvice(queue, "config", false, false, false, false)));
        InstanceMethodAdviceExecutor executor = new InstanceMethodAdviceExecutor(advices);
        Method method = TargetObjectFixture.class.getMethod("call", List.class);
        Callable<Object> callable = () -> {
            queue.add("origin call");
            return "result";
        };
        assertThat(executor.advice(new SimpleTargetAdviceObject(), method, new Object[]{queue}, callable), is("result"));
        assertThat(queue, is(Arrays.asList("plain before foo", "origin call", "plain after foo")));
    }
    
    @Test
    void assertAdviceWhenCallableThrows() throws ReflectiveOperationException {
        List<String> queue = new LinkedList<>();
        Map<String, Collection<InstanceMethodAdvice>> advices = new LinkedHashMap<>(2, 1F);
        advices.put("foo", Collections.singletonList(new RecordingInstanceMethodAdvice(queue, "plain")));
        advices.put("bar", Collections.singletonList(new ConfigurableInstanceMethodAdvice(queue, "config", true, false, false, true)));
        InstanceMethodAdviceExecutor executor = new InstanceMethodAdviceExecutor(advices);
        Method method = TargetObjectFixture.class.getMethod("callWhenExceptionThrown", List.class);
        Callable<Object> callable = () -> {
            throw new IllegalStateException("callable error");
        };
        assertThrows(IllegalStateException.class, () -> executor.advice(new SimpleTargetAdviceObject(), method, new Object[]{queue}, callable));
        assertThat(queue, is(Arrays.asList("plain before foo", "config before bar", "plain throw foo", "plain after foo", "config after bar")));
        assertLog("Failed to execute the error handler of method `{0}` in class `{1}`, {2}.", method.getName(), "callable error");
    }
    
    private void assertLog(final String expectedMessage, final String methodName, final String errorMessage) {
        assertThat(records.size(), is(1));
        LogRecord actual = records.get(0);
        assertThat(actual.getLevel(), is(Level.SEVERE));
        assertThat(actual.getMessage(), is(expectedMessage));
        assertThat(actual.getParameters(), is(new String[]{methodName, SimpleTargetAdviceObject.class.getName(), errorMessage}));
        records.remove(0);
    }
    
    @Test
    void assertAdviceWhenCallableThrowsWithoutOnThrowingException() throws ReflectiveOperationException {
        List<String> queue = new LinkedList<>();
        Map<String, Collection<InstanceMethodAdvice>> advices = new LinkedHashMap<>(1, 1F);
        advices.put("foo", Collections.singletonList(new RecordingInstanceMethodAdvice(queue, "plain")));
        InstanceMethodAdviceExecutor executor = new InstanceMethodAdviceExecutor(advices);
        Method method = TargetObjectFixture.class.getMethod("callWhenExceptionThrown", List.class);
        Callable<Object> callable = () -> {
            throw new IllegalStateException("callable error");
        };
        assertThrows(IllegalStateException.class, () -> executor.advice(new SimpleTargetAdviceObject(), method, new Object[]{queue}, callable));
        assertThat(queue, is(Arrays.asList("plain before foo", "plain throw foo", "plain after foo")));
    }
    
    @Test
    void assertAdviceWhenCallableThrowsWithPluginDisabled() throws ReflectiveOperationException {
        List<String> queue = new LinkedList<>();
        Map<String, Collection<InstanceMethodAdvice>> advices = new LinkedHashMap<>(1, 1F);
        advices.put("foo", Collections.singletonList(new ConfigurableInstanceMethodAdvice(queue, "disabled", false, false, false, false)));
        InstanceMethodAdviceExecutor executor = new InstanceMethodAdviceExecutor(advices);
        Method method = TargetObjectFixture.class.getMethod("callWhenExceptionThrown", List.class);
        Callable<Object> callable = () -> {
            throw new IllegalStateException("callable error");
        };
        assertThrows(IllegalStateException.class, () -> executor.advice(new SimpleTargetAdviceObject(), method, new Object[]{queue}, callable));
        assertThat(queue, is(Collections.emptyList()));
    }
    
    @Test
    void assertAdviceWhenBeforeThrows() throws ReflectiveOperationException {
        List<String> queue = new LinkedList<>();
        Map<String, Collection<InstanceMethodAdvice>> advices = new LinkedHashMap<>(2, 1F);
        advices.put("foo", Collections.singletonList(new ConfigurableInstanceMethodAdvice(queue, "first", true, true, false, false)));
        advices.put("bar", Collections.singletonList(new RecordingInstanceMethodAdvice(queue, "second")));
        InstanceMethodAdviceExecutor executor = new InstanceMethodAdviceExecutor(advices);
        Method method = TargetObjectFixture.class.getMethod("call", List.class);
        Callable<Object> callable = () -> {
            queue.add("origin call");
            return "result";
        };
        assertThat(executor.advice(new SimpleTargetAdviceObject(), method, new Object[]{queue}, callable), is("result"));
        assertThat(queue, is(Arrays.asList("origin call", "first after foo", "second after bar")));
        assertLog("Failed to execute the pre-method of method `{0}` in class `{1}`, {2}.", method.getName(), "before");
    }
    
    @Test
    void assertAdviceWhenAfterThrows() throws ReflectiveOperationException {
        List<String> queue = new LinkedList<>();
        Map<String, Collection<InstanceMethodAdvice>> advices = new LinkedHashMap<>(2, 1F);
        advices.put("foo", Collections.singletonList(new ConfigurableInstanceMethodAdvice(queue, "first", true, false, true, false)));
        advices.put("bar", Collections.singletonList(new RecordingInstanceMethodAdvice(queue, "second")));
        InstanceMethodAdviceExecutor executor = new InstanceMethodAdviceExecutor(advices);
        Method method = TargetObjectFixture.class.getMethod("call", List.class);
        Callable<Object> callable = () -> {
            queue.add("origin call");
            return "result";
        };
        assertThat(executor.advice(new SimpleTargetAdviceObject(), method, new Object[]{queue}, callable), is("result"));
        assertThat(queue, is(Arrays.asList("first before foo", "second before bar", "origin call")));
        assertLog("Failed to execute the post-method of method `{0}` in class `{1}`, {2}.", method.getName(), "after");
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
        AdviceExecutor executor = new InstanceMethodAdviceExecutor(Collections.emptyMap());
        Builder<?> actual = executor.intercept(builder, methodDescription);
        assertThat(actual, is(intercepted));
    }
    
    @RequiredArgsConstructor
    private static final class RecordingHandler extends Handler {
        
        private final List<LogRecord> records;
        
        @Override
        public void publish(final LogRecord record) {
            records.add(record);
        }
        
        @Override
        public void flush() {
        }
        
        @Override
        public void close() {
        }
    }
    
    @RequiredArgsConstructor
    private static final class RecordingInstanceMethodAdvice implements InstanceMethodAdvice {
        
        private final List<String> queue;
        
        private final String label;
        
        @Override
        public void beforeMethod(final TargetAdviceObject target, final TargetAdviceMethod method, final Object[] args, final String pluginType) {
            queue.add(label + " before " + pluginType);
        }
        
        @Override
        public void afterMethod(final TargetAdviceObject target, final TargetAdviceMethod method, final Object[] args, final Object result, final String pluginType) {
            queue.add(label + " after " + pluginType);
        }
        
        @Override
        public void onThrowing(final TargetAdviceObject target, final TargetAdviceMethod method, final Object[] args, final Throwable throwable, final String pluginType) {
            queue.add(label + " throw " + pluginType);
        }
    }
    
    @RequiredArgsConstructor
    private static final class ConfigurableInstanceMethodAdvice implements InstanceMethodAdvice, AgentPluginEnable {
        
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
        public void beforeMethod(final TargetAdviceObject target, final TargetAdviceMethod method, final Object[] args, final String pluginType) {
            if (throwBefore) {
                throw new IllegalStateException("before");
            }
            queue.add(label + " before " + pluginType);
        }
        
        @Override
        public void afterMethod(final TargetAdviceObject target, final TargetAdviceMethod method, final Object[] args, final Object result, final String pluginType) {
            if (throwAfter) {
                throw new IllegalStateException("after");
            }
            queue.add(label + " after " + pluginType);
        }
        
        @Override
        public void onThrowing(final TargetAdviceObject target, final TargetAdviceMethod method, final Object[] args, final Throwable throwable, final String pluginType) {
            if (throwOnThrow) {
                throw new IllegalStateException("throw");
            }
            queue.add(label + " throw " + pluginType);
        }
    }
    
    @Getter
    @Setter
    private static final class SimpleTargetAdviceObject implements TargetAdviceObject {
        
        private Object attachment;
    }
}
