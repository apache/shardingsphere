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

package org.apache.shardingsphere.agent.core.plugin.advisor;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.pool.TypePool;
import org.apache.shardingsphere.agent.config.advisor.ClassAdvisorConfiguration;
import org.apache.shardingsphere.agent.config.advisor.ConstructorAdvisorConfiguration;
import org.apache.shardingsphere.agent.config.advisor.InstanceMethodAdvisorConfiguration;
import org.apache.shardingsphere.agent.config.advisor.StaticMethodAdvisorConfiguration;
import org.apache.shardingsphere.agent.core.mock.advice.MockConstructorAdvice;
import org.apache.shardingsphere.agent.core.mock.advice.MockInstanceMethodAroundAdvice;
import org.apache.shardingsphere.agent.core.mock.advice.MockStaticMethodAroundAdvice;
import org.apache.shardingsphere.agent.core.plugin.loader.AdviceInstanceLoader;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.internal.util.reflection.FieldReader;
import org.mockito.plugins.MemberAccessor;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public final class AgentAdvisorsTest {
    
    private static final AgentAdvisors AGENT_ADVISORS = new AgentAdvisors(Collections.emptyList());
    
    private static final TypePool POOL = TypePool.Default.ofSystemLoader();
    
    private static final TypeDescription FAKE = POOL.describe("java.lang.String").resolve();
    
    private static final TypeDescription MATERIAL = POOL.describe("org.apache.shardingsphere.agent.core.mock.material.Material").resolve();
    
    @BeforeClass
    @SuppressWarnings("unchecked")
    public static void setup() throws NoSuchFieldException, IllegalAccessException {
        FieldReader objectPoolReader = new FieldReader(AdviceInstanceLoader.class, AdviceInstanceLoader.class.getDeclaredField("ADVICE_INSTANCE_CACHE"));
        Map<String, Object> objectPool = (Map<String, Object>) objectPoolReader.read();
        objectPool.put(MockConstructorAdvice.class.getTypeName(), new MockConstructorAdvice());
        objectPool.put(MockInstanceMethodAroundAdvice.class.getTypeName(), new MockInstanceMethodAroundAdvice());
        objectPool.put(MockStaticMethodAroundAdvice.class.getTypeName(), new MockStaticMethodAroundAdvice());
        ClassAdvisorConfiguration classAdvisorConfig = new ClassAdvisorConfiguration("org.apache.shardingsphere.agent.core.mock.material.Material");
        classAdvisorConfig.getConstructorAdvisors().add(new ConstructorAdvisorConfiguration(ElementMatchers.takesArguments(1), MockConstructorAdvice.class.getTypeName()));
        classAdvisorConfig.getInstanceMethodAdvisors().add(new InstanceMethodAdvisorConfiguration(ElementMatchers.named("mock"), MockInstanceMethodAroundAdvice.class.getTypeName()));
        classAdvisorConfig.getStaticMethodAdvisors().add(new StaticMethodAdvisorConfiguration(ElementMatchers.named("staticMock"), MockStaticMethodAroundAdvice.class.getTypeName()));
        MemberAccessor accessor = Plugins.getMemberAccessor();
        accessor.set(AGENT_ADVISORS.getClass().getDeclaredField("advisorConfigs"), AGENT_ADVISORS, Collections.singletonMap(classAdvisorConfig.getTargetClassName(), classAdvisorConfig));
    }
    
    @Test
    public void assertTypeMatcher() {
        assertTrue(AGENT_ADVISORS.createTypeMatcher().matches(MATERIAL));
        assertFalse(AGENT_ADVISORS.createTypeMatcher().matches(FAKE));
    }
    
    @Test
    public void assertContainsType() {
        assertTrue(AGENT_ADVISORS.containsType(MATERIAL));
        assertFalse(AGENT_ADVISORS.containsType(FAKE));
    }
    
    @Test
    public void assertGetPluginAdvisor() {
        assertNotNull(AGENT_ADVISORS.getClassAdvisorConfiguration(MATERIAL));
    }
}
