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

package org.apache.shardingsphere.shadow.yaml;

import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.YamlShadowRuleConfiguration;
import org.apache.shardingsphere.test.it.yaml.YamlRuleConfigurationIT;

import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShadowRuleConfigurationYamlIT extends YamlRuleConfigurationIT {
    
    ShadowRuleConfigurationYamlIT() {
        super("yaml/shadow-rule.yaml");
    }
    
    @Override
    protected void assertYamlRootConfiguration(final YamlRootConfiguration actual) {
        assertDataSourceMap(actual);
        Optional<YamlShadowRuleConfiguration> shadowRuleConfig = actual.getRules().stream()
                .filter(each -> each instanceof YamlShadowRuleConfiguration).findFirst().map(optional -> (YamlShadowRuleConfiguration) optional);
        assertTrue(shadowRuleConfig.isPresent());
        assertThat(shadowRuleConfig.get().getTables().size(), is(3));
        assertTOrder(shadowRuleConfig.get());
        assertTOrderItem(shadowRuleConfig.get());
        assertTAddress(shadowRuleConfig.get());
        assertThat(shadowRuleConfig.get().getShadowAlgorithms().size(), is(4));
        assertUserIdInsertMatchAlgorithm(shadowRuleConfig.get());
        assertUserIdUpdateMatchAlgorithm(shadowRuleConfig.get());
        assertUserIdSelectMatchAlgorithm(shadowRuleConfig.get());
        assertSqlHintAlgorithm(shadowRuleConfig.get());
    }
    
    private void assertDataSourceMap(final YamlRootConfiguration actual) {
        assertThat(actual.getDataSources().size(), is(2));
        assertTrue(actual.getDataSources().containsKey("ds"));
        assertTrue(actual.getDataSources().containsKey("shadow_ds"));
    }
    
    private void assertTOrder(final YamlShadowRuleConfiguration actual) {
        assertThat(actual.getTables().get("t_order").getDataSourceNames().iterator().next(), is("shadowDataSource"));
        assertThat(actual.getTables().get("t_order").getShadowAlgorithmNames().size(), is(2));
        assertTrue(actual.getTables().get("t_order").getShadowAlgorithmNames().containsAll(Arrays.asList("user-id-insert-match-algorithm", "user-id-select-match-algorithm")));
    }
    
    private void assertTOrderItem(final YamlShadowRuleConfiguration actual) {
        assertThat(actual.getTables().get("t_order_item").getDataSourceNames().iterator().next(), is("shadowDataSource"));
        assertThat(actual.getTables().get("t_order_item").getShadowAlgorithmNames().size(), is(3));
        assertTrue(actual.getTables().get("t_order_item").getShadowAlgorithmNames().containsAll(
                Arrays.asList("user-id-insert-match-algorithm", "user-id-update-match-algorithm", "user-id-select-match-algorithm")));
    }
    
    private void assertTAddress(final YamlShadowRuleConfiguration actual) {
        assertThat(actual.getTables().get("t_address").getDataSourceNames().iterator().next(), is("shadowDataSource"));
        assertThat(actual.getTables().get("t_address").getShadowAlgorithmNames().size(), is(3));
        assertTrue(actual.getTables().get("t_address").getShadowAlgorithmNames().containsAll(Arrays.asList("user-id-insert-match-algorithm", "user-id-select-match-algorithm", "sql-hint-algorithm")));
    }
    
    private void assertUserIdInsertMatchAlgorithm(final YamlShadowRuleConfiguration actual) {
        assertThat(actual.getShadowAlgorithms().get("user-id-insert-match-algorithm").getType(), is("REGEX_MATCH"));
        Properties props = actual.getShadowAlgorithms().get("user-id-insert-match-algorithm").getProps();
        assertThat(props.size(), is(3));
        assertTrue(props.containsKey("operation"));
        assertThat(props.getProperty("operation"), is("insert"));
        assertTrue(props.containsKey("column"));
        assertThat(props.getProperty("column"), is("user_id"));
        assertTrue(props.containsKey("regex"));
        assertThat(props.getProperty("regex"), is("[1]"));
    }
    
    private void assertUserIdUpdateMatchAlgorithm(final YamlShadowRuleConfiguration actual) {
        assertThat(actual.getShadowAlgorithms().get("user-id-update-match-algorithm").getType(), is("REGEX_MATCH"));
        Properties props = actual.getShadowAlgorithms().get("user-id-update-match-algorithm").getProps();
        assertThat(props.size(), is(3));
        assertTrue(props.containsKey("operation"));
        assertThat(props.getProperty("operation"), is("update"));
        assertTrue(props.containsKey("column"));
        assertThat(props.getProperty("column"), is("user_id"));
        assertTrue(props.containsKey("regex"));
        assertThat(props.getProperty("regex"), is("[1]"));
    }
    
    private void assertUserIdSelectMatchAlgorithm(final YamlShadowRuleConfiguration actual) {
        assertThat(actual.getShadowAlgorithms().get("user-id-select-match-algorithm").getType(), is("REGEX_MATCH"));
        Properties props = actual.getShadowAlgorithms().get("user-id-select-match-algorithm").getProps();
        assertThat(props.size(), is(3));
        assertTrue(props.containsKey("operation"));
        assertThat(props.getProperty("operation"), is("select"));
        assertTrue(props.containsKey("column"));
        assertThat(props.getProperty("column"), is("user_id"));
        assertTrue(props.containsKey("regex"));
        assertThat(props.getProperty("regex"), is("[1]"));
    }
    
    private void assertSqlHintAlgorithm(final YamlShadowRuleConfiguration actual) {
        assertThat(actual.getShadowAlgorithms().get("sql-hint-algorithm").getType(), is("SQL_HINT"));
        Properties props = actual.getShadowAlgorithms().get("sql-hint-algorithm").getProps();
        assertThat(props.size(), is(2));
        assertTrue((boolean) props.get("shadow"));
        assertThat(props.get("foo"), is("bar"));
    }
}
