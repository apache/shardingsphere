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

import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ImplementationDefinition;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ReceiverTypeDefinition;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.api.advice.type.ConstructorAdvice;
import org.apache.shardingsphere.agent.api.plugin.AgentPluginEnable;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConstructorAdviceExecutorTest {
    
    @Test
    void assertAdviceInvokeEnabledAdvice() {
        List<String> queue = new LinkedList<>();
        Map<String, Collection<ConstructorAdvice>> advices = Collections.singletonMap("foo", Arrays.asList(new RecordingConstructorAdvice(queue, true), new PluginDisabledConstructorAdvice(queue)));
        new ConstructorAdviceExecutor(advices).advice(new SimpleTargetAdviceObject(), new Object[]{queue});
        assertThat(queue, is(Collections.singletonList("foo constructor")));
    }
    
    @Test
    void assertAdviceCatchException() {
        Map<String, Collection<ConstructorAdvice>> advices = Collections.singletonMap("foo", Collections.singletonList(new ThrowingConstructorAdvice()));
        ConstructorAdviceExecutor executor = new ConstructorAdviceExecutor(advices);
        assertDoesNotThrow(() -> executor.advice(new SimpleTargetAdviceObject(), new Object[]{new LinkedList<>()}));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertIntercept() {
        Builder<?> builder = mock(Builder.class);
        ImplementationDefinition<?> implementationDefinition = mock(ImplementationDefinition.class);
        ReceiverTypeDefinition<?> intercepted = mock(ReceiverTypeDefinition.class);
        when(builder.constructor(any())).thenReturn((ImplementationDefinition) implementationDefinition);
        when(implementationDefinition.intercept(any())).thenReturn((ReceiverTypeDefinition) intercepted);
        ConstructorAdviceExecutor executor = new ConstructorAdviceExecutor(Collections.emptyMap());
        assertThat(executor.intercept(builder, mock()), is(intercepted));
    }
    
    private static final class RecordingConstructorAdvice implements ConstructorAdvice, AgentPluginEnable {
        
        private final List<String> queue;
        
        private final boolean pluginEnabled;
        
        RecordingConstructorAdvice(final List<String> queue, final boolean pluginEnabled) {
            this.queue = queue;
            this.pluginEnabled = pluginEnabled;
        }
        
        @Override
        public boolean isPluginEnabled() {
            return pluginEnabled;
        }
        
        @Override
        public void onConstructor(final TargetAdviceObject target, final Object[] args, final String pluginType) {
            queue.add(pluginType + " constructor");
        }
    }
    
    private static final class PluginDisabledConstructorAdvice implements ConstructorAdvice, AgentPluginEnable {
        
        private final List<String> queue;
        
        PluginDisabledConstructorAdvice(final List<String> queue) {
            this.queue = queue;
        }
        
        @Override
        public boolean isPluginEnabled() {
            return false;
        }
        
        @Override
        public void onConstructor(final TargetAdviceObject target, final Object[] args, final String pluginType) {
            queue.add("disabled");
        }
    }
    
    private static final class ThrowingConstructorAdvice implements ConstructorAdvice {
        
        @Override
        public void onConstructor(final TargetAdviceObject target, final Object[] args, final String pluginType) {
            throw new IllegalStateException("error");
        }
    }
    
    private static final class SimpleTargetAdviceObject implements TargetAdviceObject {
        
        private Object attachment;
        
        @Override
        public Object getAttachment() {
            return attachment;
        }
        
        @Override
        public void setAttachment(final Object attachment) {
            this.attachment = attachment;
        }
    }
}
