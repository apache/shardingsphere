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
import org.apache.shardingsphere.shadow.algorithm.shadow.column.ColumnRegexMatchShadowAlgorithm;
import org.apache.shardingsphere.shadow.algorithm.shadow.column.ColumnValueMatchShadowAlgorithm;
import org.apache.shardingsphere.shadow.algorithm.shadow.hint.SimpleHintShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "classpath:META-INF/spring/shadow-algorithm-application-context.xml")
public final class ShadowAlgorithmSpringNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @Resource
    private AlgorithmProvidedShadowRuleConfiguration shadowRule;
    
    @Test
    public void assertDataSource() {
        assertShadowDataSources(shadowRule.getDataSources());
        assertShadowTables(shadowRule.getTables());
        assertShadowAlgorithms(shadowRule.getShadowAlgorithms());
    }
    
    private void assertShadowAlgorithms(final Map<String, ShadowAlgorithm> shadowAlgorithms) {
        ShadowAlgorithm userIdRegexMatchAlgorithm = shadowAlgorithms.get("user-id-regex-match-algorithm");
        assertThat(userIdRegexMatchAlgorithm instanceof ColumnRegexMatchShadowAlgorithm, is(true));
        assertThat(userIdRegexMatchAlgorithm.getType(), is("REGEX_MATCH"));
        assertThat(userIdRegexMatchAlgorithm.getProps().get("operation"), is("insert"));
        assertThat(userIdRegexMatchAlgorithm.getProps().get("column"), is("user_id"));
        assertThat(userIdRegexMatchAlgorithm.getProps().get("regex"), is("[1]"));
        ShadowAlgorithm userIdValueMatchAlgorithm = shadowAlgorithms.get("user-id-value-match-algorithm");
        assertThat(userIdValueMatchAlgorithm instanceof ColumnValueMatchShadowAlgorithm, is(true));
        assertThat(userIdValueMatchAlgorithm.getType(), is("VALUE_MATCH"));
        assertThat(userIdValueMatchAlgorithm.getProps().get("operation"), is("insert"));
        assertThat(userIdValueMatchAlgorithm.getProps().get("column"), is("user_id"));
        assertThat(userIdValueMatchAlgorithm.getProps().get("value"), is("1"));
        ShadowAlgorithm simpleHintAlgorithm = shadowAlgorithms.get("simple-hint-algorithm");
        assertThat(simpleHintAlgorithm instanceof SimpleHintShadowAlgorithm, is(true));
        assertThat(simpleHintAlgorithm.getType(), is("SIMPLE_HINT"));
        assertThat(simpleHintAlgorithm.getProps().get("shadow"), is("true"));
        assertThat(simpleHintAlgorithm.getProps().get("foo"), is("bar"));
    }
    
    private void assertShadowTables(final Map<String, ShadowTableConfiguration> shadowTables) {
        assertThat(shadowTables.size(), is(2));
        assertThat(shadowTables.get("t_order").getDataSourceNames().size(), is(2));
        assertThat(shadowTables.get("t_order").getShadowAlgorithmNames(), is(Arrays.asList("user-id-regex-match-algorithm", "simple-hint-algorithm")));
        assertThat(shadowTables.get("t_user").getDataSourceNames().size(), is(1));
        assertThat(shadowTables.get("t_user").getShadowAlgorithmNames(), is(Arrays.asList("user-id-value-match-algorithm", "simple-hint-algorithm")));
    }
    
    private void assertShadowDataSources(final Map<String, ShadowDataSourceConfiguration> dataSources) {
        assertThat(dataSources.size(), is(2));
        assertThat(dataSources.get("shadow-data-source-0").getSourceDataSourceName(), is("ds"));
        assertThat(dataSources.get("shadow-data-source-0").getShadowDataSourceName(), is("ds-shadow"));
        assertThat(dataSources.get("shadow-data-source-1").getSourceDataSourceName(), is("ds1"));
        assertThat(dataSources.get("shadow-data-source-1").getShadowDataSourceName(), is("ds1-shadow"));
    }
}
