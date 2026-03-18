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

package org.apache.shardingsphere.agent.core.advisor.executor;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.core.advisor.config.AdvisorConfiguration;
import org.apache.shardingsphere.agent.core.advisor.config.MethodAdvisorConfiguration;
import org.apache.shardingsphere.agent.core.advisor.executor.type.ConstructorAdviceExecutor;
import org.apache.shardingsphere.agent.core.advisor.executor.type.InstanceMethodAdviceExecutor;
import org.apache.shardingsphere.agent.core.advisor.executor.type.StaticMethodAdviceExecutor;
import org.apache.shardingsphere.agent.core.plugin.classloader.ClassLoaderContext;
import org.apache.shardingsphere.fixture.advice.BarAdvice;
import org.apache.shardingsphere.fixture.advice.FooAdvice;
import org.apache.shardingsphere.fixture.targeted.TargetObjectFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdviceExecutorFactoryTest {
    
    @AfterEach
    void resetCache() throws ReflectiveOperationException {
        ((Map<?, ?>) Plugins.getMemberAccessor().get(AdviceFactory.class.getDeclaredField("CACHED_ADVICES"), null)).clear();
        ((Map<?, ?>) Plugins.getMemberAccessor().get(ClassLoaderContext.class.getDeclaredField("AGENT_CLASS_LOADERS"), null)).clear();
    }
    
    @Test
    void assertFindMatchedAdviceExecutorWithEmptyAdvisors() throws NoSuchMethodException {
        AdvisorConfiguration advisorConfig = new AdvisorConfiguration(TargetObjectFixture.class.getName());
        AdviceExecutorFactory factory = new AdviceExecutorFactory(new ClassLoaderContext(new URLClassLoader(new URL[0], getClass().getClassLoader()), Collections.emptyList()), advisorConfig);
        assertFalse(factory.findMatchedAdviceExecutor(new MethodDescription.ForLoadedMethod(TargetObjectFixture.class.getMethod("call", List.class))).isPresent());
    }
    
    @Test
    void assertFindMatchedAdviceExecutorWithUnmatchedPointcut() throws NoSuchMethodException {
        AdvisorConfiguration advisorConfig = new AdvisorConfiguration(TargetObjectFixture.class.getName());
        MethodDescription.InDefinedShape instanceMethod = new MethodDescription.ForLoadedMethod(TargetObjectFixture.class.getMethod("call", List.class));
        advisorConfig.getAdvisors().add(new MethodAdvisorConfiguration(ElementMatchers.none(), FooAdvice.class.getName(), "foo"));
        AdviceExecutorFactory factory = new AdviceExecutorFactory(new ClassLoaderContext(new URLClassLoader(new URL[0], getClass().getClassLoader()), Collections.emptyList()), advisorConfig);
        assertFalse(factory.findMatchedAdviceExecutor(instanceMethod).isPresent());
    }
    
    @Test
    void assertFindMatchedAdviceExecutorWithConstructor() throws NoSuchMethodException {
        MethodDescription.InDefinedShape constructor = new MethodDescription.ForLoadedConstructor(TargetObjectFixture.class.getDeclaredConstructor(List.class));
        AdvisorConfiguration advisorConfig = new AdvisorConfiguration(TargetObjectFixture.class.getName());
        advisorConfig.getAdvisors().add(new MethodAdvisorConfiguration(ElementMatchers.is(constructor), FooAdvice.class.getName(), "foo"));
        advisorConfig.getAdvisors().add(new MethodAdvisorConfiguration(ElementMatchers.is(constructor), BarAdvice.class.getName(), "bar"));
        AdviceExecutorFactory factory = new AdviceExecutorFactory(new ClassLoaderContext(new URLClassLoader(new URL[0], getClass().getClassLoader()), Collections.emptyList()), advisorConfig);
        Optional<AdviceExecutor> actual = factory.findMatchedAdviceExecutor(constructor);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(ConstructorAdviceExecutor.class));
    }
    
    @Test
    void assertFindMatchedAdviceExecutorWithStaticMethod() throws NoSuchMethodException {
        MethodDescription.InDefinedShape staticMethod = new MethodDescription.ForLoadedMethod(TargetObjectFixture.class.getMethod("staticCall", List.class));
        AdvisorConfiguration advisorConfig = new AdvisorConfiguration(TargetObjectFixture.class.getName());
        advisorConfig.getAdvisors().add(new MethodAdvisorConfiguration(ElementMatchers.is(staticMethod), FooAdvice.class.getName(), "foo"));
        AdviceExecutorFactory factory = new AdviceExecutorFactory(new ClassLoaderContext(new URLClassLoader(new URL[0], getClass().getClassLoader()), Collections.emptyList()), advisorConfig);
        Optional<AdviceExecutor> actual = factory.findMatchedAdviceExecutor(staticMethod);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(StaticMethodAdviceExecutor.class));
    }
    
    @Test
    void assertFindMatchedAdviceExecutorWithInstanceMethod() throws NoSuchMethodException {
        MethodDescription.InDefinedShape instanceMethod = new MethodDescription.ForLoadedMethod(TargetObjectFixture.class.getMethod("call", List.class));
        AdvisorConfiguration advisorConfig = new AdvisorConfiguration(TargetObjectFixture.class.getName());
        advisorConfig.getAdvisors().add(new MethodAdvisorConfiguration(ElementMatchers.is(instanceMethod), FooAdvice.class.getName(), "foo"));
        AdviceExecutorFactory factory = new AdviceExecutorFactory(new ClassLoaderContext(new URLClassLoader(new URL[0], getClass().getClassLoader()), Collections.emptyList()), advisorConfig);
        Optional<AdviceExecutor> actual = factory.findMatchedAdviceExecutor(instanceMethod);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(InstanceMethodAdviceExecutor.class));
    }
    
    @Test
    void assertFindMatchedAdviceExecutorWithAbstractMethod() throws NoSuchMethodException {
        MethodDescription.InDefinedShape abstractMethod = new MethodDescription.ForLoadedMethod(Runnable.class.getMethod("run"));
        AdvisorConfiguration advisorConfig = new AdvisorConfiguration(Runnable.class.getName());
        advisorConfig.getAdvisors().add(new MethodAdvisorConfiguration(target -> true, FooAdvice.class.getName(), "foo"));
        AdviceExecutorFactory factory = new AdviceExecutorFactory(new ClassLoaderContext(new URLClassLoader(new URL[0], getClass().getClassLoader()), Collections.emptyList()), advisorConfig);
        assertFalse(factory.findMatchedAdviceExecutor(abstractMethod).isPresent());
    }
}
