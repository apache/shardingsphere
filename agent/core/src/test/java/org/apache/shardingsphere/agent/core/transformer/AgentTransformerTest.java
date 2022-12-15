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
import org.apache.shardingsphere.agent.config.advisor.ClassAdvisorConfiguration;
import org.apache.shardingsphere.agent.config.advisor.ConstructorAdvisorConfiguration;
import org.apache.shardingsphere.agent.config.advisor.InstanceMethodAdvisorConfiguration;
import org.apache.shardingsphere.agent.config.advisor.StaticMethodAdvisorConfiguration;
import org.apache.shardingsphere.agent.config.plugin.AgentConfiguration;
import org.apache.shardingsphere.agent.core.classloader.AgentClassLoader;
import org.apache.shardingsphere.agent.core.logging.LoggingListener;
import org.apache.shardingsphere.agent.core.mock.advice.MockConstructorAdvice;
import org.apache.shardingsphere.agent.core.mock.advice.MockInstanceMethodAroundAdvice;
import org.apache.shardingsphere.agent.core.mock.advice.MockInstanceMethodAroundRepeatedAdvice;
import org.apache.shardingsphere.agent.core.mock.advice.MockStaticMethodAroundAdvice;
import org.apache.shardingsphere.agent.core.mock.material.Material;
import org.apache.shardingsphere.agent.core.mock.material.RepeatedAdviceMaterial;
import org.apache.shardingsphere.agent.core.plugin.advisor.AgentAdvisors;
import org.apache.shardingsphere.agent.core.plugin.loader.AdviceInstanceLoader;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.internal.util.reflection.FieldReader;
import org.mockito.plugins.MemberAccessor;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;

public final class AgentTransformerTest {
    
    private static final AgentAdvisors AGENT_ADVISORS = new AgentAdvisors(Collections.emptySet(), Collections.emptyList());
    
    private static ResettableClassFileTransformer byteBuddyAgent;
    
    private final List<String> queue = new LinkedList<>();
    
    @BeforeClass
    @SuppressWarnings("unchecked")
    public static void setup() throws ReflectiveOperationException {
        ByteBuddyAgent.install();
        AgentClassLoader.init(Collections.emptyList());
        FieldReader objectPoolReader = new FieldReader(AdviceInstanceLoader.class, AdviceInstanceLoader.class.getDeclaredField("ADVICE_INSTANCE_CACHE"));
        Map<String, Object> objectPool = (Map<String, Object>) objectPoolReader.read();
        objectPool.put(MockConstructorAdvice.class.getTypeName(), new MockConstructorAdvice());
        objectPool.put(MockInstanceMethodAroundAdvice.class.getTypeName(), new MockInstanceMethodAroundAdvice());
        objectPool.put(MockStaticMethodAroundAdvice.class.getTypeName(), new MockStaticMethodAroundAdvice());
        Map<String, ClassAdvisorConfiguration> classAdvisorConfigs = new HashMap<>(2, 1);
        ClassAdvisorConfiguration classAdvisorConfig = createClassAdvisorConfiguration();
        classAdvisorConfigs.put(classAdvisorConfig.getTargetClassName(), classAdvisorConfig);
        ClassAdvisorConfiguration classAdvisorConfigInTwice = createClassAdvisorConfigurationInTwice();
        classAdvisorConfigs.put(classAdvisorConfigInTwice.getTargetClassName(), classAdvisorConfigInTwice);
        MemberAccessor accessor = Plugins.getMemberAccessor();
        accessor.set(AGENT_ADVISORS.getClass().getDeclaredField("advisorConfigs"), AGENT_ADVISORS, classAdvisorConfigs);
        byteBuddyAgent = new AgentBuilder.Default().with(new ByteBuddy().with(TypeValidation.ENABLED))
                .ignore(ElementMatchers.isSynthetic()).or(ElementMatchers.nameStartsWith("org.apache.shardingsphere.agent.")
                        .and(ElementMatchers.not(ElementMatchers.nameStartsWith("org.apache.shardingsphere.agent.core.mock"))))
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .with(new LoggingListener())
                .type(AGENT_ADVISORS.createTypeMatcher())
                .transform(new AgentTransformer(new AgentConfiguration(Collections.emptyMap()), AGENT_ADVISORS, true))
                .asTerminalTransformation()
                .installOnByteBuddyAgent();
    }
    
    private static ClassAdvisorConfiguration createClassAdvisorConfiguration() {
        ClassAdvisorConfiguration result = new ClassAdvisorConfiguration("org.apache.shardingsphere.agent.core.mock.material.Material");
        result.getConstructorAdvisors().add(new ConstructorAdvisorConfiguration(ElementMatchers.takesArguments(1), MockConstructorAdvice.class.getTypeName()));
        result.getInstanceMethodAdvisors().add(new InstanceMethodAdvisorConfiguration(ElementMatchers.named("mock"), MockInstanceMethodAroundAdvice.class.getTypeName()));
        result.getStaticMethodAdvisors().add(new StaticMethodAdvisorConfiguration(ElementMatchers.named("staticMock"), MockStaticMethodAroundAdvice.class.getTypeName()));
        return result;
    }
    
    private static ClassAdvisorConfiguration createClassAdvisorConfigurationInTwice() {
        ClassAdvisorConfiguration result = new ClassAdvisorConfiguration("org.apache.shardingsphere.agent.core.mock.material.RepeatedAdviceMaterial");
        result.getInstanceMethodAdvisors().add(new InstanceMethodAdvisorConfiguration(ElementMatchers.named("mock"), MockInstanceMethodAroundAdvice.class.getTypeName()));
        result.getInstanceMethodAdvisors().add(new InstanceMethodAdvisorConfiguration(ElementMatchers.named("mock"), MockInstanceMethodAroundRepeatedAdvice.class.getTypeName()));
        return result;
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
