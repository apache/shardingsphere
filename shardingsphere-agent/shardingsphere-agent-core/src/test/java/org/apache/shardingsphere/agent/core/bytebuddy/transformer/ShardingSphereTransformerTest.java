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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.api.point.PluginInterceptorPoint;
import org.apache.shardingsphere.agent.core.bytebuddy.listener.LoggingListener;
import org.apache.shardingsphere.agent.core.mock.Material;
import org.apache.shardingsphere.agent.core.mock.advice.MockConstructor;
import org.apache.shardingsphere.agent.core.mock.advice.MockMethodAroundAdvice;
import org.apache.shardingsphere.agent.core.mock.advice.MockStaticMethodAroundAdvice;
import org.apache.shardingsphere.agent.core.plugin.loader.PluginLoader;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.internal.util.reflection.FieldReader;
import org.mockito.internal.util.reflection.FieldSetter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@Category(ShardingSphereTransformerTest.class)
public final class ShardingSphereTransformerTest {
    
    private static final PluginLoader PLUGIN_LOADER = PluginLoader.getInstance();
    
    private final List<String> queue = new LinkedList<>();
    
    @BeforeClass
    @SneakyThrows
    public static void setup() {
        ByteBuddyAgent.install();
        final AgentBuilder agentBuilder = new AgentBuilder.Default().with(new ByteBuddy().with(TypeValidation.ENABLED)).ignore(ElementMatchers.isSynthetic())
                .or(ElementMatchers.nameStartsWith("org.apache.shardingsphere.agent.").and(ElementMatchers.not(ElementMatchers.nameStartsWith("org.apache.shardingsphere.agent.core.mock"))));
        FieldReader objectPoolReader = new FieldReader(PLUGIN_LOADER, PLUGIN_LOADER.getClass().getDeclaredField("objectPool"));
        Map<String, Object> objectPool = (Map<String, Object>) objectPoolReader.read();
        objectPool.put(MockConstructor.class.getTypeName(), new MockConstructor());
        objectPool.put(MockMethodAroundAdvice.class.getTypeName(), new MockMethodAroundAdvice());
        objectPool.put(MockStaticMethodAroundAdvice.class.getTypeName(), new MockStaticMethodAroundAdvice());
        Map<String, PluginInterceptorPoint> interceptorPointMap = Maps.newHashMap();
        PluginInterceptorPoint interceptorPoint = PluginInterceptorPoint.intercept("org.apache.shardingsphere.agent.core.mock.Material")
                .aroundInstanceMethod(ElementMatchers.named("mock"))
                .implement(MockMethodAroundAdvice.class.getTypeName())
                .build()
                .aroundClassStaticMethod(ElementMatchers.named("staticMock"))
                .implement(MockStaticMethodAroundAdvice.class.getTypeName())
                .build()
                .onConstructor(ElementMatchers.takesArguments(1))
                .implement(MockConstructor.class.getTypeName())
                .build()
                .install();
        interceptorPointMap.put(interceptorPoint.getClassNameOfTarget(), interceptorPoint);
        FieldSetter.setField(PLUGIN_LOADER, PLUGIN_LOADER.getClass().getDeclaredField("interceptorPointMap"), interceptorPointMap);
        agentBuilder.type(PLUGIN_LOADER.typeMatcher())
                .transform(new ShardingSphereTransformer(PLUGIN_LOADER))
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .with(new LoggingListener())
                .installOn(ByteBuddyAgent.getInstrumentation());
    }
    
    @Test
    public void assertInstanceMethod() {
        assertThat(new Material().mock(queue), is("invocation"));
        assertThat(queue, is(Lists.newArrayList("before", "on", "after")));
    }
    
    @Test
    public void assertStaticMethod() {
        assertThat(Material.staticMock(queue), is("static invocation"));
        assertThat(queue, hasItems("before", "on", "after"));
    }
    
    @Test
    public void assertConstructor() {
        new Material(queue);
        assertThat(queue, hasItems("constructor", "on constructor"));
    }
    
    @After
    public void cleanup() {
        queue.clear();
    }
    
}
