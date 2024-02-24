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

package org.apache.shardingsphere.agent.core.builder;

import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.core.advisor.config.AdvisorConfiguration;
import org.apache.shardingsphere.agent.core.advisor.config.MethodAdvisorConfiguration;
import org.apache.shardingsphere.fixture.advice.BarAdvice;
import org.apache.shardingsphere.fixture.advice.FooAdvice;
import org.apache.shardingsphere.fixture.targeted.TargetObjectFixture;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class AgentBuilderFactoryTest {
    
    private static ResettableClassFileTransformer agent;
    
    @BeforeAll
    static void setup() {
        ByteBuddyAgent.install();
        AdvisorConfiguration advisorConfig = createAdvisorConfiguration();
        Map<String, AdvisorConfiguration> advisorConfigs = Collections.singletonMap(advisorConfig.getTargetClassName(), advisorConfig);
        AgentBuilder agentBuilder = AgentBuilderFactory.create(Collections.emptyMap(), Collections.emptyList(), advisorConfigs, true);
        agent = agentBuilder.installOnByteBuddyAgent();
    }
    
    private static AdvisorConfiguration createAdvisorConfiguration() {
        AdvisorConfiguration result = new AdvisorConfiguration("org.apache.shardingsphere.fixture.targeted.TargetObjectFixture");
        result.getAdvisors().add(new MethodAdvisorConfiguration(ElementMatchers.isConstructor().and(ElementMatchers.takesArguments(1)), FooAdvice.class.getName(), "FIXTURE"));
        result.getAdvisors().add(new MethodAdvisorConfiguration(ElementMatchers.isConstructor().and(ElementMatchers.takesArguments(1)), BarAdvice.class.getName(), "FIXTURE"));
        result.getAdvisors().add(new MethodAdvisorConfiguration(ElementMatchers.named("call"), FooAdvice.class.getName(), "FIXTURE"));
        result.getAdvisors().add(new MethodAdvisorConfiguration(ElementMatchers.named("call"), BarAdvice.class.getName(), "FIXTURE"));
        result.getAdvisors().add(new MethodAdvisorConfiguration(ElementMatchers.named("callWhenExceptionThrown"), FooAdvice.class.getName(), "FIXTURE"));
        result.getAdvisors().add(new MethodAdvisorConfiguration(ElementMatchers.named("callWhenExceptionThrown"), BarAdvice.class.getName(), "FIXTURE"));
        result.getAdvisors().add(new MethodAdvisorConfiguration(ElementMatchers.named("staticCall"), FooAdvice.class.getName(), "FIXTURE"));
        result.getAdvisors().add(new MethodAdvisorConfiguration(ElementMatchers.named("staticCall"), BarAdvice.class.getName(), "FIXTURE"));
        result.getAdvisors().add(new MethodAdvisorConfiguration(ElementMatchers.named("staticCallWhenExceptionThrown"), FooAdvice.class.getName(), "FIXTURE"));
        result.getAdvisors().add(new MethodAdvisorConfiguration(ElementMatchers.named("staticCallWhenExceptionThrown"), BarAdvice.class.getName(), "FIXTURE"));
        return result;
    }
    
    @AfterAll
    static void destroy() {
        agent.reset(ByteBuddyAgent.getInstrumentation(), AgentBuilder.RedefinitionStrategy.RETRANSFORMATION);
    }
    
    @Test
    void assertAdviceConstructor() {
        List<String> queue = new LinkedList<>();
        new TargetObjectFixture(queue);
        assertThat(queue, is(Arrays.asList("on constructor", "foo constructor", "bar constructor")));
    }
    
    @Test
    void assertAdviceInstanceMethod() {
        List<String> queue = new LinkedList<>();
        new TargetObjectFixture(new LinkedList<>()).call(queue);
        assertThat(queue, is(Arrays.asList("foo before instance method", "bar before instance method", "on instance method", "foo after instance method", "bar after instance method")));
    }
    
    @Test
    void assertAdviceInstanceMethodWhenExceptionThrown() {
        List<String> queue = new LinkedList<>();
        try {
            new TargetObjectFixture(new LinkedList<>()).callWhenExceptionThrown(queue);
        } catch (final UnsupportedOperationException ignored) {
        }
        assertThat(queue, is(Arrays.asList("foo before instance method", "bar before instance method",
                "foo throw instance method exception", "bar throw instance method exception", "foo after instance method", "bar after instance method")));
    }
    
    @Test
    void assertAdviceStaticMethod() {
        List<String> queue = new LinkedList<>();
        TargetObjectFixture.staticCall(queue);
        assertThat(queue, is(Arrays.asList("foo before static method", "bar before static method", "on static method", "foo after static method", "bar after static method")));
    }
    
    @Test
    void assertAdviceStaticMethodWhenExceptionThrown() {
        List<String> queue = new LinkedList<>();
        try {
            TargetObjectFixture.staticCallWhenExceptionThrown(queue);
        } catch (final UnsupportedOperationException ignored) {
        }
        assertThat(queue, is(Arrays.asList("foo before static method", "bar before static method",
                "foo throw static method exception", "bar throw static method exception", "foo after static method", "bar after static method")));
    }
}
