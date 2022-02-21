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

package org.apache.shardingsphere.spring.namespace.registry;

import org.apache.shardingsphere.spring.namespace.fixture.factorybean.ShardingSphereAlgorithmFixtureFactoryBean;
import org.junit.Test;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.xml.ParserContext;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingSphereAlgorithmBeanRegistryTest {
    
    @Test
    public void assertGetAlgorithmBeanReferences() {
        ParserContext parserContext = mock(ParserContext.class, RETURNS_DEEP_STUBS);
        when(parserContext.getRegistry().getBeanDefinitionNames()).thenReturn(new String[] {"includeBean", "excludeBean"});
        when(parserContext.getRegistry().getBeanDefinition("includeBean").getBeanClassName()).thenReturn(ShardingSphereAlgorithmFixtureFactoryBean.class.getName());
        when(parserContext.getRegistry().getBeanDefinition("excludeBean").getBeanClassName()).thenReturn(Object.class.getName());
        Map<String, RuntimeBeanReference> actual = ShardingSphereAlgorithmBeanRegistry.getAlgorithmBeanReferences(parserContext, ShardingSphereAlgorithmFixtureFactoryBean.class);
        assertThat(actual.size(), is(1));
        assertThat(actual.get("includeBean").getBeanName(), is("includeBean"));
    }
}
