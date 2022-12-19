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

package org.apache.shardingsphere.agent.core.transformer;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.ResettableClassFileTransformer;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.config.advisor.AdvisorConfiguration;
import org.apache.shardingsphere.agent.config.advisor.MethodAdvisorConfiguration;
import org.apache.shardingsphere.agent.core.classloader.AgentClassLoader;
import org.apache.shardingsphere.agent.core.logging.LoggingListener;
import org.apache.shardingsphere.agent.core.transformer.fixture.advice.FooConstructorAdvice;
import org.apache.shardingsphere.agent.core.transformer.fixture.advice.FooInstanceMethodAdvice;
import org.apache.shardingsphere.agent.core.transformer.fixture.advice.BarInstanceMethodAdvice;
import org.apache.shardingsphere.agent.core.transformer.fixture.advice.FooStaticMethodAdvice;
import org.apache.shardingsphere.agent.core.transformer.fixture.targeted.FooTargetObjectFixture;
import org.apache.shardingsphere.agent.core.transformer.fixture.targeted.BarTargetObjectFixture;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;

public final class AgentTransformerTest {
    
    private static ResettableClassFileTransformer byteBuddyAgent;
    
    private final List<String> queue = new LinkedList<>();
    
    @BeforeClass
    public static void setup() throws ReflectiveOperationException {
        ByteBuddyAgent.install();
        AgentClassLoader.init(Collections.emptyList());
        Map<String, AdvisorConfiguration> advisorConfigs = new HashMap<>(2, 1);
        AdvisorConfiguration advisorConfig = createAdvisorConfiguration();
        advisorConfigs.put(advisorConfig.getTargetClassName(), advisorConfig);
        AdvisorConfiguration advisorConfigInTwice = createAdvisorConfigurationInTwice();
        advisorConfigs.put(advisorConfigInTwice.getTargetClassName(), advisorConfigInTwice);
        byteBuddyAgent = new AgentBuilder.Default().with(new ByteBuddy().with(TypeValidation.ENABLED))
                .ignore(ElementMatchers.isSynthetic()).or(ElementMatchers.nameStartsWith("org.apache.shardingsphere.agent.")
                        .and(ElementMatchers.not(ElementMatchers.nameStartsWith("org.apache.shardingsphere.agent.core.fixture"))))
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .with(new LoggingListener())
                .type(new AgentJunction(advisorConfigs))
                .transform(new AgentTransformer(Collections.emptyMap(), Collections.emptyList(), advisorConfigs, true))
                .asTerminalTransformation()
                .installOnByteBuddyAgent();
    }
    
    private static AdvisorConfiguration createAdvisorConfiguration() {
        AdvisorConfiguration result = new AdvisorConfiguration("org.apache.shardingsphere.agent.core.fixture.targeted.FooTargetObjectFixture");
        result.getAdvisors().add(new MethodAdvisorConfiguration(ElementMatchers.isConstructor().and(ElementMatchers.takesArguments(1)), FooConstructorAdvice.class.getTypeName()));
        result.getAdvisors().add(new MethodAdvisorConfiguration(ElementMatchers.named("mock"), FooInstanceMethodAdvice.class.getTypeName()));
        result.getAdvisors().add(new MethodAdvisorConfiguration(ElementMatchers.named("staticMock"), FooStaticMethodAdvice.class.getTypeName()));
        return result;
    }
    
    private static AdvisorConfiguration createAdvisorConfigurationInTwice() {
        AdvisorConfiguration result = new AdvisorConfiguration("org.apache.shardingsphere.agent.core.fixture.targeted.BarTargetObjectFixture");
        result.getAdvisors().add(new MethodAdvisorConfiguration(ElementMatchers.named("mock"), FooInstanceMethodAdvice.class.getTypeName()));
        result.getAdvisors().add(new MethodAdvisorConfiguration(ElementMatchers.named("mock"), BarInstanceMethodAdvice.class.getTypeName()));
        return result;
    }
    
    @Test
    public void assertInstanceMethod() {
        assertThat(new FooTargetObjectFixture().mock(queue), is("invocation"));
        assertArrayEquals(new String[]{"before", "on", "after"}, queue.toArray());
    }
    
    @Test
    public void assertInstanceMethodInRepeatedAdvice() {
        assertThat(new BarTargetObjectFixture().mock(queue), is("invocation"));
        assertArrayEquals(new String[]{"before", "twice_before", "on", "after", "twice_after"}, queue.toArray());
    }
    
    @Test
    public void assertStaticMethod() {
        assertThat(FooTargetObjectFixture.staticMock(queue), is("static invocation"));
        assertArrayEquals(new String[]{"before", "on", "after"}, queue.toArray());
    }
    
    @Test
    public void assertConstructor() {
        new FooTargetObjectFixture(queue);
        assertArrayEquals(new String[]{"constructor", "advice constructor"}, queue.toArray());
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
