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

package org.apache.shardingsphere.shadow.yaml.swapper;

import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.shadow.algorithm.config.AlgorithmProvidedShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.algorithm.shadow.column.ColumnRegexMatchedShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.apache.shardingsphere.shadow.yaml.config.YamlShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.datasource.YamlShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.table.YamlShadowTableConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public final class YamlShadowRuleAlgorithmProviderConfigurationSwapperTest {
    
    private YamlShadowRuleAlgorithmProviderConfigurationSwapper swapper;
    
    @Before
    public void init() {
        swapper = new YamlShadowRuleAlgorithmProviderConfigurationSwapper();
    }
    
    @Test
    public void assertSwapToYamlConfiguration() {
        AlgorithmProvidedShadowRuleConfiguration expected = buildAlgorithmProvidedShadowRuleConfiguration();
        YamlShadowRuleConfiguration actual = swapper.swapToYamlConfiguration(expected);
        actual.getDataSources().forEach((key, value) -> {
            Optional<ShadowDataSourceConfiguration> dataSourceConfig = getDataSourceConfig(expected.getDataSources(), key);
            assertTrue(dataSourceConfig.isPresent());
            assertThat(value.getShadowDataSourceName(), is(dataSourceConfig.get().getShadowDataSourceName()));
            assertThat(value.getProductionDataSourceName(), is(dataSourceConfig.get().getProductionDataSourceName()));
        });
        actual.getTables().forEach((key, value) -> {
            ShadowTableConfiguration shadowTableConfig = expected.getTables().get(key);
            assertThat(value.getShadowAlgorithmNames(), is(shadowTableConfig.getShadowAlgorithmNames()));
        });
        actual.getShadowAlgorithms().forEach((key, value) -> {
            ShadowAlgorithm shadowAlgorithm = expected.getShadowAlgorithms().get(key);
            assertThat(value.getType(), is(shadowAlgorithm.getType()));
        });
    }
    
    private Optional<ShadowDataSourceConfiguration> getDataSourceConfig(final Collection<ShadowDataSourceConfiguration> dataSources, final String name) {
        for (ShadowDataSourceConfiguration each : dataSources) {
            if (name.equals(each.getName())) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private AlgorithmProvidedShadowRuleConfiguration buildAlgorithmProvidedShadowRuleConfiguration() {
        AlgorithmProvidedShadowRuleConfiguration result = new AlgorithmProvidedShadowRuleConfiguration();
        result.getDataSources().add(new ShadowDataSourceConfiguration("shadow-data-source", "ds", "ds-shadow"));
        result.getTables().put("t_order", new ShadowTableConfiguration(Collections.singletonList("shadow-data-source"), Arrays.asList("user-id-match-algorithm", "note-algorithm")));
        result.getShadowAlgorithms().put("user-id-match-algorithm", new ColumnRegexMatchedShadowAlgorithm());
        return result;
    }
    
    @Test
    public void assertSwapToObject() {
        YamlShadowRuleConfiguration expected = buildYamlShadowRuleConfiguration();
        AlgorithmProvidedShadowRuleConfiguration actual = swapper.swapToObject(expected);
        actual.getDataSources().forEach(each -> {
            YamlShadowDataSourceConfiguration yamlShadowDataSourceConfig = expected.getDataSources().get(each.getName());
            assertThat(each.getShadowDataSourceName(), is(yamlShadowDataSourceConfig.getShadowDataSourceName()));
            assertThat(each.getProductionDataSourceName(), is(yamlShadowDataSourceConfig.getProductionDataSourceName()));
        });
        actual.getTables().forEach((key, value) -> assertThat(value.getShadowAlgorithmNames(), is(expected.getTables().get(key).getShadowAlgorithmNames())));
        actual.getShadowAlgorithms().forEach((key, value) -> assertThat(value.getType(), is(expected.getShadowAlgorithms().get(key).getType())));
    }
    
    private YamlShadowRuleConfiguration buildYamlShadowRuleConfiguration() {
        YamlShadowRuleConfiguration result = new YamlShadowRuleConfiguration();
        YamlShadowDataSourceConfiguration yamlShadowDataSourceConfig = new YamlShadowDataSourceConfiguration();
        yamlShadowDataSourceConfig.setProductionDataSourceName("ds");
        yamlShadowDataSourceConfig.setShadowDataSourceName("ds-shadow");
        result.getDataSources().put("shadow-data-source", yamlShadowDataSourceConfig);
        YamlShadowTableConfiguration yamlShadowTableConfig = new YamlShadowTableConfiguration();
        yamlShadowTableConfig.setShadowAlgorithmNames(Arrays.asList("user-id-match-algorithm", "note-algorithm"));
        result.getTables().put("t_order", yamlShadowTableConfig);
        YamlAlgorithmConfiguration yamlAlgorithmConfig = new YamlAlgorithmConfiguration();
        yamlAlgorithmConfig.setType("COLUMN-REGULAR-MATCH");
        result.getShadowAlgorithms().put("user-id-match-algorithm", yamlAlgorithmConfig);
        return result;
    }
}
