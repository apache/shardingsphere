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

import org.apache.shardingsphere.agent.core.plugin.classloader.AgentPluginClassLoader;
import org.apache.shardingsphere.agent.core.plugin.classloader.ClassLoaderContext;
import org.apache.shardingsphere.fixture.advice.BarAdvice;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.isA;

class AdviceFactoryTest {
    
    @AfterEach
    void resetCache() throws ReflectiveOperationException {
        ((Map<?, ?>) Plugins.getMemberAccessor().get(AdviceFactory.class.getDeclaredField("CACHED_ADVICES"), null)).clear();
        ((Map<?, ?>) Plugins.getMemberAccessor().get(ClassLoaderContext.class.getDeclaredField("AGENT_CLASS_LOADERS"), null)).clear();
    }
    
    @Test
    void assertGetAdvice() {
        ClassLoaderContext classLoaderContext = new ClassLoaderContext(new URLClassLoader(new URL[0], getClass().getClassLoader()), Collections.emptyList());
        AdviceFactory adviceFactory = new AdviceFactory(classLoaderContext);
        Object first = adviceFactory.getAdvice(BarAdvice.class.getName());
        Object second = adviceFactory.getAdvice(BarAdvice.class.getName());
        assertThat(first, isA(BarAdvice.class));
        assertThat(first, is(second));
        assertThat(classLoaderContext.getPluginClassLoader(), isA(AgentPluginClassLoader.class));
    }
}
