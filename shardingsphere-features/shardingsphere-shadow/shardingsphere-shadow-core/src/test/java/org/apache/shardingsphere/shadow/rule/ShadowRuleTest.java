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

import com.google.common.collect.Lists;
import org.apache.shardingsphere.shadow.algorithm.config.AlgorithmProvidedShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.algorithm.shadow.column.ColumnRegexMatchShadowAlgorithm;
import org.apache.shardingsphere.shadow.algorithm.shadow.note.SimpleSQLNoteShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShadowRuleTest {
    
    private ShadowRule shadowRuleWithAlgorithm;
    
    @Before
    public void init() {
        shadowRuleWithAlgorithm = new ShadowRule(createAlgorithmProvidedShadowRuleConfiguration());
    }
    
    private AlgorithmProvidedShadowRuleConfiguration createAlgorithmProvidedShadowRuleConfiguration() {
        AlgorithmProvidedShadowRuleConfiguration result = new AlgorithmProvidedShadowRuleConfiguration();
        result.setEnable(true);
        result.setDataSources(createDataSources());
        result.setTables(createTables());
        result.setShadowAlgorithms(createShadowAlgorithms());
        return result;
    }
    
    private Map<String, ShadowAlgorithm> createShadowAlgorithms() {
        Map<String, ShadowAlgorithm> result = new LinkedHashMap<>();
        result.put("simple-note-algorithm", createNoteShadowAlgorithm());
        result.put("user-id-insert-regex-algorithm", createColumnShadowAlgorithm("user_id", "insert"));
        result.put("user-id-update-regex-algorithm", createColumnShadowAlgorithm("user_id", "update"));
        result.put("order-id-insert-regex-algorithm", createColumnShadowAlgorithm("order_id", "insert"));
        return result;
    }
    
    private ShadowAlgorithm createNoteShadowAlgorithm() {
        SimpleSQLNoteShadowAlgorithm simpleSQLNoteShadowAlgorithm = new SimpleSQLNoteShadowAlgorithm();
        simpleSQLNoteShadowAlgorithm.setProps(createNoteProperties());
        simpleSQLNoteShadowAlgorithm.init();
        return simpleSQLNoteShadowAlgorithm;
    }
    
    private Properties createNoteProperties() {
        Properties properties = new Properties();
        properties.setProperty("shadow", "true");
        return properties;
    }
    
    private ShadowAlgorithm createColumnShadowAlgorithm(final String column, final String operation) {
        ColumnRegexMatchShadowAlgorithm columnRegexMatchShadowAlgorithm = new ColumnRegexMatchShadowAlgorithm();
        columnRegexMatchShadowAlgorithm.setProps(createColumnProperties(column, operation));
        columnRegexMatchShadowAlgorithm.init();
        return columnRegexMatchShadowAlgorithm;
    }
    
    private Properties createColumnProperties(final String column, final String operation) {
        Properties properties = new Properties();
        properties.setProperty("column", column);
        properties.setProperty("operation", operation);
        properties.setProperty("regex", "[1]");
        return properties;
    }
    
    private Map<String, ShadowTableConfiguration> createTables() {
        Map<String, ShadowTableConfiguration> result = new LinkedHashMap<>();
        result.put("t_user", new ShadowTableConfiguration(Collections.singletonList("shadow-data-source-0"), createShadowAlgorithmNames("t_user")));
        result.put("t_order", new ShadowTableConfiguration(Collections.singletonList("shadow-data-source-1"), createShadowAlgorithmNames("t_order")));
        return result;
    }
    
    private Collection<String> createShadowAlgorithmNames(final String tableName) {
        Collection<String> result = new LinkedList<>();
        result.add("simple-note-algorithm");
        if ("t_user".equals(tableName)) {
            result.add("user-id-insert-regex-algorithm");
            result.add("user-id-update-regex-algorithm");
        } else {
            result.add("order-id-insert-regex-algorithm");
        }
        return result;
    }
    
    private Map<String, ShadowDataSourceConfiguration> createDataSources() {
        Map<String, ShadowDataSourceConfiguration> result = new LinkedHashMap<>();
        result.put("shadow-data-source-0", new ShadowDataSourceConfiguration("ds", "ds_shadow"));
        result.put("shadow-data-source-1", new ShadowDataSourceConfiguration("ds1", "ds1_shadow"));
        return result;
    }
    
    @Test
    public void assertNewShadowRulSuccessByShadowRuleConfiguration() {
        ShadowRule shadowRule = new ShadowRule(new ShadowRuleConfiguration());
        assertThat(shadowRule.isEnable(), is(false));
    }
    
    @Test
    public void assertNewShadowRulSuccessByAlgorithmProvidedShadowRuleConfiguration() {
        assertThat(shadowRuleWithAlgorithm.isEnable(), is(true));
        assertShadowDataSourceMappings(shadowRuleWithAlgorithm.getShadowDataSourceMappings());
        assertShadowTableRules(shadowRuleWithAlgorithm.getShadowTableRules());
    }
    
    private void assertShadowTableRules(final Map<String, ShadowTableRule> shadowTableRules) {
        assertThat(shadowTableRules.size(), is(2));
        shadowTableRules.forEach(this::assertShadowTableRule);
    }
    
    private void assertShadowTableRule(final String tableName, final ShadowTableRule shadowTableRule) {
        if ("t_user".equals(tableName)) {
            assertThat(shadowTableRule.getNoteShadowAlgorithmNames().size(), is(1));
            assertThat(shadowTableRule.getColumnShadowAlgorithmNames().size(), is(2));
        } else {
            assertThat(shadowTableRule.getNoteShadowAlgorithmNames().size(), is(1));
            assertThat(shadowTableRule.getColumnShadowAlgorithmNames().size(), is(1));
        }
    }
    
    private void assertShadowDataSourceMappings(final Map<String, ShadowDataSourceRule> shadowDataSourceMappings) {
        assertThat(shadowDataSourceMappings.size(), is(2));
        assertThat(shadowDataSourceMappings.get("shadow-data-source-0").getSourceDataSource(), is("ds"));
        assertThat(shadowDataSourceMappings.get("shadow-data-source-0").getShadowDataSource(), is("ds_shadow"));
        assertThat(shadowDataSourceMappings.get("shadow-data-source-1").getSourceDataSource(), is("ds1"));
        assertThat(shadowDataSourceMappings.get("shadow-data-source-1").getShadowDataSource(), is("ds1_shadow"));
    }
    
    @Test
    public void assertGetRelatedShadowTables() {
        Collection<String> relatedShadowTables = shadowRuleWithAlgorithm.getRelatedShadowTables(Lists.newArrayList("t_user", "t_auto"));
        assertThat(relatedShadowTables.size(), is(1));
        assertThat(relatedShadowTables.iterator().next(), is("t_user"));
    }
    
    @Test
    public void assertGetAllShadowTableNames() {
        Collection<String> allShadowTableNames = shadowRuleWithAlgorithm.getAllShadowTableNames();
        assertThat(allShadowTableNames.size(), is(2));
        Iterator<String> iterator = allShadowTableNames.iterator();
        assertThat(iterator.next(), is("t_user"));
        assertThat(iterator.next(), is("t_order"));
    }
    
    @Test
    public void assertGetRuleType() {
        assertThat(shadowRuleWithAlgorithm.getType(), is(ShadowRule.class.getSimpleName()));
    }
}
