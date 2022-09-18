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

package org.apache.shardingsphere.shadow.rule;

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.shadow.algorithm.config.AlgorithmProvidedShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.factory.ShadowAlgorithmFactory;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class ShadowRuleTest {
    
    private ShadowRule shadowRule;
    
    @Before
    public void init() {
        shadowRule = new ShadowRule(createAlgorithmProvidedShadowRuleConfiguration());
    }
    
    private AlgorithmProvidedShadowRuleConfiguration createAlgorithmProvidedShadowRuleConfiguration() {
        AlgorithmProvidedShadowRuleConfiguration result = new AlgorithmProvidedShadowRuleConfiguration();
        result.setDataSources(createDataSources());
        result.setTables(createTables());
        result.setShadowAlgorithms(createShadowAlgorithms());
        return result;
    }
    
    private Map<String, ShadowAlgorithm> createShadowAlgorithms() {
        Map<String, ShadowAlgorithm> result = new LinkedHashMap<>();
        result.put("simple-hint-algorithm", ShadowAlgorithmFactory.newInstance(new AlgorithmConfiguration("SIMPLE_HINT", createHintProperties())));
        result.put("user-id-insert-regex-algorithm", ShadowAlgorithmFactory.newInstance(new AlgorithmConfiguration("REGEX_MATCH", createColumnProperties("user_id", "insert"))));
        result.put("user-id-update-regex-algorithm", ShadowAlgorithmFactory.newInstance(new AlgorithmConfiguration("REGEX_MATCH", createColumnProperties("user_id", "update"))));
        result.put("order-id-insert-regex-algorithm", ShadowAlgorithmFactory.newInstance(new AlgorithmConfiguration("REGEX_MATCH", createColumnProperties("order_id", "insert"))));
        return result;
    }
    
    private Properties createHintProperties() {
        Properties result = new Properties();
        result.setProperty("shadow", Boolean.TRUE.toString());
        return result;
    }
    
    private Properties createColumnProperties(final String column, final String operation) {
        Properties result = new Properties();
        result.setProperty("column", column);
        result.setProperty("operation", operation);
        result.setProperty("regex", "[1]");
        return result;
    }
    
    private Map<String, ShadowTableConfiguration> createTables() {
        Map<String, ShadowTableConfiguration> result = new LinkedHashMap<>();
        result.put("t_user", new ShadowTableConfiguration(Collections.singletonList("shadow-data-source-0"), createShadowAlgorithmNames("t_user")));
        result.put("t_order", new ShadowTableConfiguration(Collections.singletonList("shadow-data-source-1"), createShadowAlgorithmNames("t_order")));
        return result;
    }
    
    private Collection<String> createShadowAlgorithmNames(final String tableName) {
        Collection<String> result = new LinkedList<>();
        result.add("simple-hint-algorithm");
        if ("t_user".equals(tableName)) {
            result.add("user-id-insert-regex-algorithm");
            result.add("user-id-update-regex-algorithm");
        } else {
            result.add("order-id-insert-regex-algorithm");
        }
        return result;
    }
    
    private Map<String, ShadowDataSourceConfiguration> createDataSources() {
        Map<String, ShadowDataSourceConfiguration> result = new LinkedHashMap<>(2, 1);
        result.put("shadow-data-source-0", new ShadowDataSourceConfiguration("ds", "ds_shadow"));
        result.put("shadow-data-source-1", new ShadowDataSourceConfiguration("ds1", "ds1_shadow"));
        return result;
    }
    
    @Test
    public void assertNewShadowRulSuccessByAlgorithmProvidedShadowRuleConfiguration() {
        assertShadowDataSourceMappings(shadowRule.getShadowDataSourceMappings());
        assertShadowTableRules(shadowRule.getShadowTableRules());
    }
    
    private void assertShadowTableRules(final Map<String, ShadowTableRule> shadowTableRules) {
        assertThat(shadowTableRules.size(), is(2));
        shadowTableRules.forEach(this::assertShadowTableRule);
    }
    
    private void assertShadowTableRule(final String tableName, final ShadowTableRule shadowTableRule) {
        if ("t_user".equals(tableName)) {
            assertThat(shadowTableRule.getHintShadowAlgorithmNames().size(), is(1));
            assertThat(shadowTableRule.getColumnShadowAlgorithmNames().size(), is(2));
        } else {
            assertThat(shadowTableRule.getHintShadowAlgorithmNames().size(), is(1));
            assertThat(shadowTableRule.getColumnShadowAlgorithmNames().size(), is(1));
        }
    }
    
    private void assertShadowDataSourceMappings(final Map<String, ShadowDataSourceRule> shadowDataSourceMappings) {
        assertThat(shadowDataSourceMappings.size(), is(2));
        assertThat(shadowDataSourceMappings.get("shadow-data-source-0").getProductionDataSource(), is("ds"));
        assertThat(shadowDataSourceMappings.get("shadow-data-source-0").getShadowDataSource(), is("ds_shadow"));
        assertThat(shadowDataSourceMappings.get("shadow-data-source-1").getProductionDataSource(), is("ds1"));
        assertThat(shadowDataSourceMappings.get("shadow-data-source-1").getShadowDataSource(), is("ds1_shadow"));
    }
    
    @Test
    public void assertGetRelatedShadowTables() {
        Collection<String> relatedShadowTables = shadowRule.getRelatedShadowTables(Arrays.asList("t_user", "t_auto"));
        assertThat(relatedShadowTables.size(), is(1));
        assertThat(relatedShadowTables.iterator().next(), is("t_user"));
    }
    
    @Test
    public void assertGetAllShadowTableNames() {
        Collection<String> allShadowTableNames = shadowRule.getAllShadowTableNames();
        assertThat(allShadowTableNames.size(), is(2));
        Iterator<String> iterator = allShadowTableNames.iterator();
        assertThat(iterator.next(), is("t_user"));
        assertThat(iterator.next(), is("t_order"));
    }
}
