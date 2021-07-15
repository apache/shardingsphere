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

package org.apache.shardingsphere.agent.core.bytebuddy.transformer.advice;

import com.google.common.collect.Maps;
import lombok.SneakyThrows;
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
import org.apache.shardingsphere.agent.core.plugin.PluginLoader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.internal.util.reflection.FieldReader;
import org.mockito.internal.util.reflection.FieldSetter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

@RunWith(ComposeClassStaticMethodAroundAdvice.class)
public final class ComposeClassStaticMethodAroundAdviceTest {
    
    private final Collection<ClassStaticMethodAroundAdvice> advices;
    
    @Test
    public void beforeMethod(final Class<?> clazz, final Method method, final Object[] args, final MethodInvocationResult result) {
        assertThat(advices.forEach(each -> each.beforeMethod(clazz, method, args, result)),is(true));
    }
    
    @Test
    public void afterMethod(final Class<?> clazz, final Method method, final Object[] args, final MethodInvocationResult result) {
        assertThat(advices.forEach(each -> each.beforeMethod(clazz, method, args, result)),is(true));
    }
    
    @Test
    public void onThrowing(final Class<?> clazz, final Method method, final Object[] args, final Throwable throwable) {
        assertThat(advices.forEach(each -> each.beforeMethod(clazz, method, args, throwable)),is(true));
    }
    
    @After
    public void cleanup() {
        Collection.clear();
    }
}
