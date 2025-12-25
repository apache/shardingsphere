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

package org.apache.shardingsphere.agent.core.builder.interceptor;

import net.bytebuddy.dynamic.DynamicType.Builder;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AgentBuilderInterceptChainEngineTest {
    
    @SuppressWarnings("unchecked")
    @Test
    void assertIntercept() {
        Builder<Object> builder = mock(Builder.class);
        Builder<Object> firstResult = mock(Builder.class);
        Builder<Object> secondResult = mock(Builder.class);
        AgentBuilderInterceptor first = mock(AgentBuilderInterceptor.class);
        AgentBuilderInterceptor second = mock(AgentBuilderInterceptor.class);
        AtomicReference<Builder<?>> firstReceived = new AtomicReference<>();
        AtomicReference<Builder<?>> secondReceived = new AtomicReference<>();
        when(first.intercept(builder)).then(invocation -> {
            firstReceived.set(invocation.getArgument(0));
            return firstResult;
        });
        when(second.intercept(firstResult)).then(invocation -> {
            secondReceived.set(invocation.getArgument(0));
            return secondResult;
        });
        assertThat(AgentBuilderInterceptChainEngine.intercept(builder, first, second), is(secondResult));
        assertThat(firstReceived.get(), is(builder));
        assertThat(secondReceived.get(), is(firstResult));
        inOrder(first, second).verify(first).intercept(builder);
        inOrder(first, second).verify(second).intercept(firstResult);
    }
}
