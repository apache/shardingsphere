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

package org.apache.shardingsphere.shadow.spring.namespace;

import org.apache.shardingsphere.shadow.algorithm.config.AlgorithmProvidedShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.algorithm.shadow.hint.SimpleHintShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.annotation.Resource;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = "classpath:META-INF/spring/shadow-default-algorithm-application-context.xml")
public final class ShadowDefaultAlgorithmSpringNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @Resource
    private AlgorithmProvidedShadowRuleConfiguration shadowRule;
    
    @Test
    public void assertDataSource() {
        assertShadowDataSources(shadowRule.getDataSources());
        assertTrue(shadowRule.getTables().isEmpty());
        assertThat(shadowRule.getDefaultShadowAlgorithmName(), is("simple-hint-algorithm"));
        assertShadowAlgorithms(shadowRule.getShadowAlgorithms());
    }
    
    private void assertShadowAlgorithms(final Map<String, ShadowAlgorithm> shadowAlgorithms) {
        ShadowAlgorithm simpleHintAlgorithm = shadowAlgorithms.get("simple-hint-algorithm");
        assertThat(simpleHintAlgorithm, instanceOf(SimpleHintShadowAlgorithm.class));
        assertThat(simpleHintAlgorithm.getType(), is("SIMPLE_HINT"));
        assertTrue(Boolean.parseBoolean(simpleHintAlgorithm.getProps().getProperty("shadow")));
        assertThat(simpleHintAlgorithm.getProps().getProperty("foo"), is("bar"));
    }
    
    private void assertShadowDataSources(final Map<String, ShadowDataSourceConfiguration> dataSources) {
        assertThat(dataSources.size(), is(1));
        assertThat(dataSources.get("shadow-data-source").getProductionDataSourceName(), is("ds"));
        assertThat(dataSources.get("shadow-data-source").getShadowDataSourceName(), is("ds-shadow"));
    }
}
