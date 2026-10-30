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

package org.apache.shardingsphere.distsql.handler.executor.configuration.yaml.swapper;

import org.apache.shardingsphere.distsql.handler.executor.configuration.yaml.YamlDataSourceConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.scope.DatabaseRuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.scope.GlobalRuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(OrderedSPILoader.class)
class YamlDatabaseConfigurationSwapperTest {
    
    private final YamlDatabaseConfigurationSwapper swapper = new YamlDatabaseConfigurationSwapper();
    
    @Test
    void assertSwapDataSourceProperties() {
        YamlDataSourceConfiguration yamlDataSource = new YamlDataSourceConfiguration();
        yamlDataSource.setDataSourceClassName("foo.pool.DataSource");
        yamlDataSource.setUrl("jdbc:mock://localhost/foo_db");
        yamlDataSource.setUsername("foo_user");
        yamlDataSource.setPassword("foo_password");
        yamlDataSource.setCustomPoolProps(PropertiesBuilder.build(new Property("foo_prop", "foo_value")));
        Map<String, DataSourcePoolProperties> actual = swapper.swapDataSourceProperties(Collections.singletonMap("foo_ds", yamlDataSource));
        assertThat(actual.keySet(), contains("foo_ds"));
        DataSourcePoolProperties actualProps = actual.get("foo_ds");
        assertThat(actualProps.getPoolClassName(), is("foo.pool.DataSource"));
        assertThat(actualProps.getConnectionPropertySynonyms().getStandardProperties().get("url"), is("jdbc:mock://localhost/foo_db"));
        assertThat(actualProps.getConnectionPropertySynonyms().getStandardProperties().get("username"), is("foo_user"));
        assertThat(actualProps.getConnectionPropertySynonyms().getStandardProperties().get("password"), is("foo_password"));
        assertThat(actualProps.getCustomProperties().getProperties().get("fooProp"), is("foo_value"));
    }
    
    @Test
    void assertSwapRuleConfigurationsWithMissingRules() {
        assertTrue(swapper.swapRuleConfigurations(null).isEmpty());
    }
    
    @Test
    void assertSwapRuleConfigurationsWithEmptyRules() {
        assertTrue(swapper.swapRuleConfigurations(Collections.emptyList()).isEmpty());
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertSwapRuleConfigurationsWithOrderedRules() {
        YamlRuleConfiguration firstYamlRule = mock(YamlRuleConfiguration.class);
        YamlRuleConfiguration secondYamlRule = mock(YamlRuleConfiguration.class);
        when(firstYamlRule.getRuleConfigurationType()).thenReturn((Class) DatabaseRuleConfiguration.class);
        when(secondYamlRule.getRuleConfigurationType()).thenReturn((Class) GlobalRuleConfiguration.class);
        RuleConfiguration firstRule = mock(DatabaseRuleConfiguration.class);
        RuleConfiguration secondRule = mock(GlobalRuleConfiguration.class);
        YamlRuleConfigurationSwapper firstSwapper = mock(YamlRuleConfigurationSwapper.class);
        YamlRuleConfigurationSwapper secondSwapper = mock(YamlRuleConfigurationSwapper.class);
        when(firstSwapper.getOrder()).thenReturn(1);
        when(secondSwapper.getOrder()).thenReturn(2);
        when(firstSwapper.swapToObject(firstYamlRule)).thenReturn(firstRule);
        when(secondSwapper.swapToObject(secondYamlRule)).thenReturn(secondRule);
        Map<Class<?>, YamlRuleConfigurationSwapper> swappers = new HashMap<>(2, 1F);
        swappers.put(DatabaseRuleConfiguration.class, firstSwapper);
        swappers.put(GlobalRuleConfiguration.class, secondSwapper);
        when(OrderedSPILoader.getServicesByClass(eq(YamlRuleConfigurationSwapper.class), anyCollection())).thenReturn(swappers);
        Collection<RuleConfiguration> actual = swapper.swapRuleConfigurations(Arrays.asList(firstYamlRule, secondYamlRule));
        assertThat(actual, contains(secondRule, firstRule));
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertSwapRuleConfigurationsWithSameOrderRules() {
        YamlRuleConfiguration firstYamlRule = mock(YamlRuleConfiguration.class);
        YamlRuleConfiguration secondYamlRule = mock(YamlRuleConfiguration.class);
        when(firstYamlRule.getRuleConfigurationType()).thenReturn((Class) DatabaseRuleConfiguration.class);
        when(secondYamlRule.getRuleConfigurationType()).thenReturn((Class) DatabaseRuleConfiguration.class);
        RuleConfiguration firstRule = mock(DatabaseRuleConfiguration.class);
        RuleConfiguration secondRule = mock(DatabaseRuleConfiguration.class);
        YamlRuleConfigurationSwapper ruleSwapper = mock(YamlRuleConfigurationSwapper.class);
        when(ruleSwapper.getOrder()).thenReturn(1);
        when(ruleSwapper.swapToObject(firstYamlRule)).thenReturn(firstRule);
        when(ruleSwapper.swapToObject(secondYamlRule)).thenReturn(secondRule);
        when(OrderedSPILoader.getServicesByClass(eq(YamlRuleConfigurationSwapper.class), anyCollection())).thenReturn(Collections.singletonMap(DatabaseRuleConfiguration.class, ruleSwapper));
        Collection<RuleConfiguration> actual = swapper.swapRuleConfigurations(Arrays.asList(firstYamlRule, secondYamlRule));
        assertThat(actual, contains(secondRule));
    }
}
