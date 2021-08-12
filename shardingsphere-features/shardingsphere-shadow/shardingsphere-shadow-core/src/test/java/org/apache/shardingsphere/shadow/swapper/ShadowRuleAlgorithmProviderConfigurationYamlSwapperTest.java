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

package org.apache.shardingsphere.shadow.swapper;

import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.shadow.algorithm.ColumnRegularMatchShadowAlgorithm;
import org.apache.shardingsphere.shadow.algorithm.config.AlgorithmProvidedShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.apache.shardingsphere.shadow.yaml.config.YamlShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.datasource.YamlShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.table.YamlShadowTableConfiguration;
import org.apache.shardingsphere.shadow.yaml.swapper.ShadowRuleAlgorithmProviderConfigurationYamlSwapper;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class ShadowRuleAlgorithmProviderConfigurationYamlSwapperTest {
    
    private ShadowRuleAlgorithmProviderConfigurationYamlSwapper swapper;
    
    @Before
    public void init() {
        swapper = new ShadowRuleAlgorithmProviderConfigurationYamlSwapper();
    }
    
    @Test
    public void assertSwapToYamlConfiguration() {
        AlgorithmProvidedShadowRuleConfiguration expectedConfiguration = buildAlgorithmProvidedShadowRuleConfiguration();
        YamlShadowRuleConfiguration actualConfiguration = swapper.swapToYamlConfiguration(expectedConfiguration);
        assertThat(actualConfiguration.isEnable(), is(expectedConfiguration.isEnable()));
        assertBasicYamlShadowRule(actualConfiguration, expectedConfiguration);
        actualConfiguration.getDataSources().entrySet().forEach(each -> {
            ShadowDataSourceConfiguration dataSourceConfiguration = expectedConfiguration.getDataSources().get(each.getKey());
            assertNotNull(dataSourceConfiguration);
            assertThat(each.getValue().getShadowDataSourceName(), is(dataSourceConfiguration.getShadowDataSourceName()));
            assertThat(each.getValue().getSourceDataSourceName(), is(dataSourceConfiguration.getSourceDataSourceName()));
        });
        actualConfiguration.getTables().entrySet().forEach(each -> {
            ShadowTableConfiguration shadowTableConfiguration = expectedConfiguration.getTables().get(each.getKey());
            assertNotNull(shadowTableConfiguration);
            assertThat(each.getValue().getShadowAlgorithmNames(), is(shadowTableConfiguration.getShadowAlgorithmNames()));
        });
        actualConfiguration.getShadowAlgorithms().entrySet().forEach(each -> {
            ShadowAlgorithm shadowAlgorithm = expectedConfiguration.getShadowAlgorithms().get(each.getKey());
            assertNotNull(shadowAlgorithm);
            assertThat(each.getValue().getType(), is(shadowAlgorithm.getType()));
        });
    }
    
    private AlgorithmProvidedShadowRuleConfiguration buildAlgorithmProvidedShadowRuleConfiguration() {
        AlgorithmProvidedShadowRuleConfiguration result = createAlgorithmProvidedShadowRuleConfiguration();
        result.setEnable(true);
        result.getDataSources().put("shadow-data-source", new ShadowDataSourceConfiguration("ds", "ds-shadow"));
        result.getTables().put("t_order", new ShadowTableConfiguration(Arrays.asList("user-id-match-algorithm", "note-algorithm")));
        result.getShadowAlgorithms().put("user-id-match-algorithm", new ColumnRegularMatchShadowAlgorithm());
        return result;
    }
    
    // fixme remove method when the api refactoring is complete
    private void assertBasicYamlShadowRule(final YamlShadowRuleConfiguration actualConfiguration, final AlgorithmProvidedShadowRuleConfiguration expectedConfiguration) {
        assertThat(actualConfiguration.getColumn(), is(expectedConfiguration.getColumn()));
        assertThat(actualConfiguration.getShadowDataSourceNames(), is(expectedConfiguration.getShadowDataSourceNames()));
        assertThat(actualConfiguration.getSourceDataSourceNames(), is(expectedConfiguration.getSourceDataSourceNames()));
    }
    
    // fixme remove method when the api refactoring is complete
    private AlgorithmProvidedShadowRuleConfiguration createAlgorithmProvidedShadowRuleConfiguration() {
        return new AlgorithmProvidedShadowRuleConfiguration("id", Arrays.asList("ds"), Arrays.asList("ds-shadow"));
    }
    
    @Test
    public void assertSwapToObject() {
        YamlShadowRuleConfiguration expectedConfiguration = buildYamlShadowRuleConfiguration();
        AlgorithmProvidedShadowRuleConfiguration actualConfiguration = swapper.swapToObject(expectedConfiguration);
        assertBasicAlgorithmShadowRule(actualConfiguration, expectedConfiguration);
        assertThat(actualConfiguration.isEnable(), is(expectedConfiguration.isEnable()));
        actualConfiguration.getDataSources().entrySet().forEach(each -> {
            YamlShadowDataSourceConfiguration yamlShadowDataSourceConfiguration = expectedConfiguration.getDataSources().get(each.getKey());
            assertNotNull(yamlShadowDataSourceConfiguration);
            assertThat(each.getValue().getShadowDataSourceName(), is(yamlShadowDataSourceConfiguration.getShadowDataSourceName()));
            assertThat(each.getValue().getSourceDataSourceName(), is(yamlShadowDataSourceConfiguration.getSourceDataSourceName()));
        });
        actualConfiguration.getTables().entrySet().forEach(each -> {
            YamlShadowTableConfiguration yamlShadowTableConfiguration = expectedConfiguration.getTables().get(each.getKey());
            assertNotNull(yamlShadowTableConfiguration);
            assertThat(each.getValue().getShadowAlgorithmNames(), is(yamlShadowTableConfiguration.getShadowAlgorithmNames()));
        });
        actualConfiguration.getShadowAlgorithms().entrySet().forEach(each -> {
            YamlShardingSphereAlgorithmConfiguration yamlShardingSphereAlgorithmConfiguration = expectedConfiguration.getShadowAlgorithms().get(each.getKey());
            assertNotNull(yamlShardingSphereAlgorithmConfiguration);
            assertThat(each.getValue().getType(), is(yamlShardingSphereAlgorithmConfiguration.getType()));
        });
    }
    
    private YamlShadowRuleConfiguration buildYamlShadowRuleConfiguration() {
        YamlShadowRuleConfiguration result = new YamlShadowRuleConfiguration();
        buildBasicYamlShadowRule(result);
        result.setEnable(true);
        YamlShadowDataSourceConfiguration yamlShadowDataSourceConfiguration = new YamlShadowDataSourceConfiguration();
        yamlShadowDataSourceConfiguration.setSourceDataSourceName("ds");
        yamlShadowDataSourceConfiguration.setShadowDataSourceName("ds-shadow");
        result.getDataSources().put("shadow-data-source", yamlShadowDataSourceConfiguration);
        YamlShadowTableConfiguration yamlShadowTableConfiguration = new YamlShadowTableConfiguration();
        yamlShadowTableConfiguration.setShadowAlgorithmNames(Arrays.asList("user-id-match-algorithm", "note-algorithm"));
        result.getTables().put("t_order", yamlShadowTableConfiguration);
        YamlShardingSphereAlgorithmConfiguration yamlShardingSphereAlgorithmConfiguration = new YamlShardingSphereAlgorithmConfiguration();
        yamlShardingSphereAlgorithmConfiguration.setType("COLUMN-REGULAR-MATCH");
        result.getShadowAlgorithms().put("user-id-match-algorithm", yamlShardingSphereAlgorithmConfiguration);
        return result;
    }
    
    // fixme remove method when the api refactoring is complete
    private void buildBasicYamlShadowRule(final YamlShadowRuleConfiguration yamlShadowRuleConfiguration) {
        yamlShadowRuleConfiguration.setColumn("id");
        yamlShadowRuleConfiguration.setSourceDataSourceNames(Arrays.asList("ds"));
        yamlShadowRuleConfiguration.setShadowDataSourceNames(Arrays.asList("ds-shadow"));
    }
    
    // fixme remove method when the api refactoring is complete
    private void assertBasicAlgorithmShadowRule(final AlgorithmProvidedShadowRuleConfiguration actualConfiguration, final YamlShadowRuleConfiguration expectedConfiguration) {
        assertThat(actualConfiguration.getColumn(), is(expectedConfiguration.getColumn()));
        assertThat(actualConfiguration.getShadowDataSourceNames(), is(expectedConfiguration.getShadowDataSourceNames()));
        assertThat(actualConfiguration.getSourceDataSourceNames(), is(expectedConfiguration.getSourceDataSourceNames()));
    }
}
