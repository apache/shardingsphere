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

package org.apache.shardingsphere.agent.core.bytebuddy.transformer;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.api.point.PluginInterceptorPoint;
import org.apache.shardingsphere.agent.core.bytebuddy.listener.LoggingListener;
import org.apache.shardingsphere.agent.core.mock.advice.MockClassStaticMethodAroundAdvice;
import org.apache.shardingsphere.agent.core.mock.advice.MockConstructorAdvice;
import org.apache.shardingsphere.agent.core.mock.advice.MockInstanceMethodAroundAdvice;
import org.apache.shardingsphere.agent.core.mock.advice.MockInstanceMethodAroundRepeatedAdvice;
import org.apache.shardingsphere.agent.core.mock.material.Material;
import org.apache.shardingsphere.agent.core.mock.material.RepeatedAdviceMaterial;
import org.apache.shardingsphere.agent.core.plugin.AgentPluginLoader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.internal.util.reflection.FieldReader;
import org.mockito.plugins.MemberAccessor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ShardingSphereTransformerTest {
    
    private static final AgentPluginLoader LOADER = AgentPluginLoader.getInstance();
    
    private static ResettableClassFileTransformer byteBuddyAgent;
    
    private final List<String> queue = new LinkedList<>();
    
    @BeforeClass
    @SuppressWarnings("unchecked")
    public static void setup() throws ReflectiveOperationException {
        ByteBuddyAgent.install();
        FieldReader objectPoolReader = new FieldReader(LOADER, LOADER.getClass().getDeclaredField("objectPool"));
        Map<String, Object> objectPool = (Map<String, Object>) objectPoolReader.read();
        objectPool.put(MockConstructorAdvice.class.getTypeName(), new MockConstructorAdvice());
        objectPool.put(MockInstanceMethodAroundAdvice.class.getTypeName(), new MockInstanceMethodAroundAdvice());
        objectPool.put(MockClassStaticMethodAroundAdvice.class.getTypeName(), new MockClassStaticMethodAroundAdvice());
        Map<String, PluginInterceptorPoint> interceptorPointMap = new HashMap<>(2, 1);
        PluginInterceptorPoint interceptorPoint = PluginInterceptorPoint.intercept("org.apache.shardingsphere.agent.core.mock.material.Material")
                .aroundInstanceMethod(ElementMatchers.named("mock"))
                .implement(MockInstanceMethodAroundAdvice.class.getTypeName())
                .build()
                .aroundClassStaticMethod(ElementMatchers.named("staticMock"))
                .implement(MockClassStaticMethodAroundAdvice.class.getTypeName())
                .build()
                .onConstructor(ElementMatchers.takesArguments(1))
                .implement(MockConstructorAdvice.class.getTypeName())
                .build()
                .install();
        interceptorPointMap.put(interceptorPoint.getClassNameOfTarget(), interceptorPoint);
        PluginInterceptorPoint interceptorPointInTwice = PluginInterceptorPoint.intercept("org.apache.shardingsphere.agent.core.mock.material.RepeatedAdviceMaterial")
                .aroundInstanceMethod(ElementMatchers.named("mock"))
                .implement(MockInstanceMethodAroundAdvice.class.getTypeName())
                .build()
                .aroundInstanceMethod(ElementMatchers.named("mock"))
                .implement(MockInstanceMethodAroundRepeatedAdvice.class.getTypeName())
                .build()
                .install();
        interceptorPointMap.put(interceptorPointInTwice.getClassNameOfTarget(), interceptorPointInTwice);
        MemberAccessor accessor = Plugins.getMemberAccessor();
        accessor.set(LOADER.getClass().getDeclaredField("interceptorPointMap"), LOADER, interceptorPointMap);
        byteBuddyAgent = new AgentBuilder.Default().with(new ByteBuddy().with(TypeValidation.ENABLED))
                .ignore(ElementMatchers.isSynthetic()).or(ElementMatchers.nameStartsWith("org.apache.shardingsphere.agent.")
                        .and(ElementMatchers.not(ElementMatchers.nameStartsWith("org.apache.shardingsphere.agent.core.mock"))))
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .with(new LoggingListener())
                .type(LOADER.typeMatcher())
                .transform(new ShardingSphereTransformer(LOADER))
                .asTerminalTransformation()
                .installOnByteBuddyAgent();
    }
    
    @Test
    public void assertInstanceMethod() {
        assertThat(new Material().mock(queue), is("invocation"));
        assertArrayEquals(new String[]{"before", "on", "after"}, queue.toArray());
    }
    
    @Test
    public void assertInstanceMethodInRepeatedAdvice() {
        assertThat(new RepeatedAdviceMaterial().mock(queue), is("invocation"));
        assertArrayEquals(new String[]{"before", "twice_before", "on", "after", "twice_after"}, queue.toArray());
    }
    
    @Test
    public void assertStaticMethod() {
        assertThat(Material.staticMock(queue), is("static invocation"));
        assertArrayEquals(new String[]{"before", "on", "after"}, queue.toArray());
    }
    
    @Test
    public void assertConstructor() {
        new Material(queue);
        assertArrayEquals(new String[]{"constructor", "on constructor"}, queue.toArray());
    }
    
    @After
    public void cleanup() {
        queue.clear();
    }
    
    @AfterClass
    public static void destroy() {
        byteBuddyAgent.reset(ByteBuddyAgent.getInstrumentation(), AgentBuilder.RedefinitionStrategy.RETRANSFORMATION);
    }
}
