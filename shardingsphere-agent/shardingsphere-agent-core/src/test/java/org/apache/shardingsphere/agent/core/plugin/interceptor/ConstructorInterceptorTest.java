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
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.api.advice.AdviceTargetObject;
import org.apache.shardingsphere.agent.core.bytebuddy.listener.LoggingListener;
import org.apache.shardingsphere.agent.core.mock.material.ConstructorMaterial;
import org.apache.shardingsphere.agent.core.mock.advice.MockConstructorAdvice;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

public final class ConstructorInterceptorTest {
    
    private static final String EXTRA_DATA = "_$EXTRA_DATA$_";
    
    private static final String CLASS_PATH = "org.apache.shardingsphere.agent.core.mock.material.ConstructorMaterial";
    
    private static final List<String> QUEUE = new LinkedList<>();
    
    private static ResettableClassFileTransformer byteBuddyAgent;
    
    @BeforeClass
    public static void setup() {
        ByteBuddyAgent.install();
        byteBuddyAgent = new AgentBuilder.Default()
                .with(new ByteBuddy().with(TypeValidation.ENABLED))
                .ignore(ElementMatchers.isSynthetic())
                .with(new LoggingListener())
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .type(ElementMatchers.named(CLASS_PATH))
                .transform((builder, typeDescription, classLoader, module) -> {
                    if (CLASS_PATH.equals(typeDescription.getTypeName())) {
                        return builder.defineField(EXTRA_DATA, Object.class, Opcodes.ACC_PRIVATE | Opcodes.ACC_VOLATILE)
                                .implement(AdviceTargetObject.class)
                                .intercept(FieldAccessor.ofField(EXTRA_DATA))
                                .constructor(ElementMatchers.isConstructor())
                                .intercept(SuperMethodCall.INSTANCE.andThen(MethodDelegation.withDefaultConfiguration().to(new ConstructorInterceptor(new MockConstructorAdvice(QUEUE)))));
                    }
                    return builder;
                })
                .asTerminalTransformation()
                .installOnByteBuddyAgent();
    }
    
    @Test
    @SuppressWarnings("all")
    public void assertNoArgConstructor() {
        Object material = new ConstructorMaterial();
        assertTrue(material instanceof AdviceTargetObject);
    }
    
    @Test
    public void assertConstructor() {
        new ConstructorMaterial(QUEUE);
        assertArrayEquals(new String[]{"constructor", "on constructor"}, QUEUE.toArray());
    }
    
    @After
    public void cleanup() {
        QUEUE.clear();
    }
    
    @AfterClass
    public static void destroy() {
        byteBuddyAgent.reset(ByteBuddyAgent.getInstrumentation(), AgentBuilder.RedefinitionStrategy.RETRANSFORMATION);
    }
}
