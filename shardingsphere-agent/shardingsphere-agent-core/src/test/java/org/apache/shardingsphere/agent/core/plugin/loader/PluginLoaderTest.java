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

import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;
import org.apache.shardingsphere.agent.api.point.PluginInterceptorPoint;
import org.apache.shardingsphere.agent.core.mock.advice.MockConstructorAdvice;
import org.apache.shardingsphere.agent.core.mock.advice.MockInstanceMethodAroundAdvice;
import org.apache.shardingsphere.agent.core.mock.advice.MockClassStaticMethodAroundAdvice;
import org.apache.shardingsphere.agent.core.plugin.PluginLoader;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.internal.util.reflection.FieldReader;
import org.mockito.internal.util.reflection.FieldSetter;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@Category(PluginLoaderTest.class)
public final class PluginLoaderTest {
    
    private static final PluginLoader PLUGIN_LOADER = PluginLoader.getInstance();
    
    private static final TypePool POOL = TypePool.Default.ofSystemLoader();
    
    private static final TypeDescription FAKE = POOL.describe("java.lang.String").resolve();
    
    private static final TypeDescription MATERIAL = POOL.describe("org.apache.shardingsphere.agent.core.mock.material.Material").resolve();
    
    @BeforeClass
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static void setup() {
        FieldReader objectPoolReader = new FieldReader(PLUGIN_LOADER, PLUGIN_LOADER.getClass().getDeclaredField("objectPool"));
        Map<String, Object> objectPool = (Map<String, Object>) objectPoolReader.read();
        objectPool.put(MockConstructorAdvice.class.getTypeName(), new MockConstructorAdvice());
        objectPool.put(MockInstanceMethodAroundAdvice.class.getTypeName(), new MockInstanceMethodAroundAdvice());
        objectPool.put(MockClassStaticMethodAroundAdvice.class.getTypeName(), new MockClassStaticMethodAroundAdvice());
        Map<String, PluginInterceptorPoint> interceptorPointMap = Maps.newHashMap();
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
        FieldSetter.setField(PLUGIN_LOADER, PLUGIN_LOADER.getClass().getDeclaredField("interceptorPointMap"), interceptorPointMap);
    }
    
    @Test
    public void assertTypeMatcher() {
        assertThat(PLUGIN_LOADER.typeMatcher().matches(MATERIAL), is(true));
        assertThat(PLUGIN_LOADER.typeMatcher().matches(FAKE), is(false));
    }
    
    @Test
    public void assertContainsType() {
        assertThat(PLUGIN_LOADER.containsType(MATERIAL), is(true));
        assertThat(PLUGIN_LOADER.containsType(FAKE), is(false));
    }
    
    @Test
    public void assertLoadPluginInterceptorPoint() {
        assertNotNull(PLUGIN_LOADER.loadPluginInterceptorPoint(MATERIAL));
    }
}
