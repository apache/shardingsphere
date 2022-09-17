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

package org.apache.shardingsphere.agent.core.plugin.interceptor;

import lombok.RequiredArgsConstructor;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.api.advice.AdviceTargetObject;
import org.apache.shardingsphere.agent.core.mock.advice.MockInstanceMethodAroundAdvice;
import org.apache.shardingsphere.agent.core.mock.material.InstanceMaterial;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
@RequiredArgsConstructor
public final class InstanceMethodAroundInterceptorTest {
    
    private static final String EXTRA_DATA = "_$EXTRA_DATA$_";
    
    private static final String CLASS_PATH = "org.apache.shardingsphere.agent.core.mock.material.InstanceMaterial";
    
    private static ResettableClassFileTransformer byteBuddyAgent;
    
    private final boolean rebase;
    
    private final String methodName;
    
    private final String result;
    
    private final String[] expected;
    
    @Parameters
    public static Collection<Object[]> prepareData() {
        return Arrays.asList(
                new Object[]{false, "mock", "invocation", new String[]{"before", "on", "after"}},
                new Object[]{true, "mock", "rebase invocation method", new String[]{"before", "after"}},
                new Object[]{false, "mockWithException", null, new String[]{"before", "exception", "after"}});
    }
    
    @BeforeClass
    public static void setup() {
        ByteBuddyAgent.install();
        byteBuddyAgent = new AgentBuilder.Default().with(new ByteBuddy().with(TypeValidation.ENABLED))
                .with(new ByteBuddy())
                .type(ElementMatchers.named(CLASS_PATH))
                .transform((builder, typeDescription, classLoader, module) -> {
                    if (CLASS_PATH.equals(typeDescription.getTypeName())) {
                        return builder.defineField(EXTRA_DATA, Object.class, Opcodes.ACC_PRIVATE | Opcodes.ACC_VOLATILE)
                                .implement(AdviceTargetObject.class)
                                .intercept(FieldAccessor.ofField(EXTRA_DATA));
                    }
                    return builder;
                }).asTerminalTransformation()
                .installOnByteBuddyAgent();
    }
    
    @Test
    public void assertInterceptedMethod() throws ReflectiveOperationException {
        InstanceMaterial material = new ByteBuddy()
                .subclass(InstanceMaterial.class)
                .method(ElementMatchers.named(methodName))
                .intercept(MethodDelegation.withDefaultConfiguration().to(new InstanceMethodAroundInterceptor(new MockInstanceMethodAroundAdvice(rebase))))
                .make()
                .load(new MockClassLoader())
                .getLoaded()
                .getDeclaredConstructor().newInstance();
        List<String> queues = new LinkedList<>();
        if ("mockWithException".equals(methodName)) {
            try {
                material.mockWithException(queues);
            } catch (IOException ignored) {
            }
        } else {
            assertThat(material.mock(queues), is(result));
        }
        assertArrayEquals(expected, queues.toArray());
    }
    
    @AfterClass
    public static void destroy() {
        byteBuddyAgent.reset(ByteBuddyAgent.getInstrumentation(), AgentBuilder.RedefinitionStrategy.RETRANSFORMATION);
    }
    
    private static class MockClassLoader extends ClassLoader {
    }
}
