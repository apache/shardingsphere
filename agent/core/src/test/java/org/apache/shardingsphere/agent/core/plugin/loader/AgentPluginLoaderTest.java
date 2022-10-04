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

package org.apache.shardingsphere.agent.core.plugin.loader;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;
import org.apache.shardingsphere.agent.api.point.PluginInterceptorPoint;
import org.apache.shardingsphere.agent.core.mock.advice.MockClassStaticMethodAroundAdvice;
import org.apache.shardingsphere.agent.core.mock.advice.MockConstructorAdvice;
import org.apache.shardingsphere.agent.core.mock.advice.MockInstanceMethodAroundAdvice;
import org.apache.shardingsphere.agent.core.plugin.AgentPluginLoader;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.internal.util.reflection.FieldReader;
import org.mockito.plugins.MemberAccessor;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Category(AgentPluginLoaderTest.class)
public final class AgentPluginLoaderTest {
    
    private static final AgentPluginLoader LOADER = AgentPluginLoader.getInstance();
    
    private static final TypePool POOL = TypePool.Default.ofSystemLoader();
    
    private static final TypeDescription FAKE = POOL.describe("java.lang.String").resolve();
    
    private static final TypeDescription MATERIAL = POOL.describe("org.apache.shardingsphere.agent.core.mock.material.Material").resolve();
    
    @BeforeClass
    @SuppressWarnings("unchecked")
    public static void setup() throws NoSuchFieldException, IllegalAccessException {
        FieldReader objectPoolReader = new FieldReader(LOADER, LOADER.getClass().getDeclaredField("objectPool"));
        Map<String, Object> objectPool = (Map<String, Object>) objectPoolReader.read();
        objectPool.put(MockConstructorAdvice.class.getTypeName(), new MockConstructorAdvice());
        objectPool.put(MockInstanceMethodAroundAdvice.class.getTypeName(), new MockInstanceMethodAroundAdvice());
        objectPool.put(MockClassStaticMethodAroundAdvice.class.getTypeName(), new MockClassStaticMethodAroundAdvice());
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
        MemberAccessor accessor = Plugins.getMemberAccessor();
        accessor.set(LOADER.getClass().getDeclaredField("interceptorPointMap"), LOADER, Collections.singletonMap(interceptorPoint.getClassNameOfTarget(), interceptorPoint));
    }
    
    @Test
    public void assertTypeMatcher() {
        assertTrue(LOADER.typeMatcher().matches(MATERIAL));
        assertFalse(LOADER.typeMatcher().matches(FAKE));
    }
    
    @Test
    public void assertContainsType() {
        assertTrue(LOADER.containsType(MATERIAL));
        assertFalse(LOADER.containsType(FAKE));
    }
    
    @Test
    public void assertLoadPluginInterceptorPoint() {
        assertNotNull(LOADER.loadPluginInterceptorPoint(MATERIAL));
    }
}
