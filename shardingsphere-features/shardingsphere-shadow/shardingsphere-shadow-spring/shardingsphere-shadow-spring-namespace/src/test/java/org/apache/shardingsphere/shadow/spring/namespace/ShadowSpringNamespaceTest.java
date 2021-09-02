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
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = "classpath:META-INF/spring/shadow-application-context.xml")
public final class ShadowSpringNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @Resource
    private AlgorithmProvidedShadowRuleConfiguration shadowRule;
    
    @Test
    public void assertDataSource() {
        assertThat(shadowRule.isEnable(), is(false));
        assertBasicShadowRule(shadowRule.getColumn(), shadowRule.getSourceDataSourceNames(), shadowRule.getShadowDataSourceNames());
        assertTrue(shadowRule.getDataSources().isEmpty());
        assertTrue(shadowRule.getTables().isEmpty());
        assertTrue(shadowRule.getShadowAlgorithms().isEmpty());
    }
    
    // fixme remove method when the api refactoring is complete
    private void assertBasicShadowRule(final String column, final List<String> sourceDataSourceNames, final List<String> shadowDataSourceNames) {
        assertThat(column, is("shadow"));
        assertThat(sourceDataSourceNames, is(Arrays.asList("ds", "ds1")));
        assertThat(shadowDataSourceNames, is(Arrays.asList("shadow_ds", "shadow_ds1")));
    }
}
