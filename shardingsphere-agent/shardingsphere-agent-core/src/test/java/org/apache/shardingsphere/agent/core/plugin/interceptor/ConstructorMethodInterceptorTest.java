/*
 * Licensed to the Apache Software Foundation (final ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, final Version 2.0
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

package org.apache.shardingsphere.agent.core.plugin.interceptor;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.api.advice.TargetObject;
import org.apache.shardingsphere.agent.core.bytebuddy.listener.LoggingListener;
import org.apache.shardingsphere.agent.core.mock.Material;
import org.apache.shardingsphere.agent.core.mock.advice.MockConstructor;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Deque;
import java.util.LinkedList;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@Category(ConstructorMethodInterceptorTest.class)
public final class ConstructorMethodInterceptorTest {
    
    private static final String EXTRA_DATA = "_$EXTRA_DATA$_";
    
    private static final Deque<String> QUEUE = new LinkedList<>();
    
    @BeforeClass
    public static void setup() {
        ByteBuddyAgent.install();
        new AgentBuilder.Default()
                .with(new ByteBuddy().with(TypeValidation.ENABLED))
                .ignore(ElementMatchers.isSynthetic())
                .with(new LoggingListener())
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .type(ElementMatchers.named("org.apache.shardingsphere.agent.core.mock.Material"))
                .transform((builder, typeDescription, classLoader, module) -> {
                    if ("org.apache.shardingsphere.agent.core.mock.Material".equals(typeDescription.getTypeName())) {
                        return builder.defineField(EXTRA_DATA, Object.class, Opcodes.ACC_PRIVATE | Opcodes.ACC_VOLATILE)
                                .implement(TargetObject.class)
                                .intercept(FieldAccessor.ofField(EXTRA_DATA))
                                .constructor(ElementMatchers.isConstructor())
                                .intercept(SuperMethodCall.INSTANCE.andThen(MethodDelegation.withDefaultConfiguration().to(new ConstructorMethodInterceptor(new MockConstructor(QUEUE)))));
                    }
                    return builder;
                })
                .installOnByteBuddyAgent();
    }
    
    @Test
    public void assertNoArgConstructor() {
        Object material = new Material();
        assertTrue(material instanceof TargetObject);
    }
    
    @Test
    public void assertConstructor() {
        new Material(QUEUE);
        assertThat(QUEUE, Matchers.hasItems("constructor", "on constructor"));
    }
    
    @After
    public void cleanup() {
        QUEUE.clear();
    }
}
