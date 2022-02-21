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

import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.shadow.algorithm.config.AlgorithmProvidedShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.algorithm.shadow.column.ColumnRegexMatchShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.apache.shardingsphere.shadow.yaml.config.YamlShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.datasource.YamlShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.table.YamlShadowTableConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

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
        actualConfiguration.getDataSources().forEach((key, value) -> {
            ShadowDataSourceConfiguration dataSourceConfiguration = expectedConfiguration.getDataSources().get(key);
            assertNotNull(dataSourceConfiguration);
            assertThat(value.getShadowDataSourceName(), is(dataSourceConfiguration.getShadowDataSourceName()));
            assertThat(value.getSourceDataSourceName(), is(dataSourceConfiguration.getSourceDataSourceName()));
        });
        actualConfiguration.getTables().forEach((key, value) -> {
            ShadowTableConfiguration shadowTableConfiguration = expectedConfiguration.getTables().get(key);
            assertNotNull(shadowTableConfiguration);
            assertThat(value.getShadowAlgorithmNames(), is(shadowTableConfiguration.getShadowAlgorithmNames()));
        });
        actualConfiguration.getShadowAlgorithms().forEach((key, value) -> {
            ShadowAlgorithm shadowAlgorithm = expectedConfiguration.getShadowAlgorithms().get(key);
            assertNotNull(shadowAlgorithm);
            assertThat(value.getType(), is(shadowAlgorithm.getType()));
        });
    }
    
    private AlgorithmProvidedShadowRuleConfiguration buildAlgorithmProvidedShadowRuleConfiguration() {
        AlgorithmProvidedShadowRuleConfiguration result = new AlgorithmProvidedShadowRuleConfiguration();
        result.getDataSources().put("shadow-data-source", new ShadowDataSourceConfiguration("ds", "ds-shadow"));
        result.getTables().put("t_order", new ShadowTableConfiguration(Collections.singletonList("shadow-data-source"), Arrays.asList("user-id-match-algorithm", "note-algorithm")));
        result.getShadowAlgorithms().put("user-id-match-algorithm", new ColumnRegexMatchShadowAlgorithm());
        return result;
    }
    
    @Test
    public void assertSwapToObject() {
        YamlShadowRuleConfiguration expectedConfiguration = buildYamlShadowRuleConfiguration();
        AlgorithmProvidedShadowRuleConfiguration actualConfiguration = swapper.swapToObject(expectedConfiguration);
        actualConfiguration.getDataSources().forEach((key, value) -> {
            YamlShadowDataSourceConfiguration yamlShadowDataSourceConfiguration = expectedConfiguration.getDataSources().get(key);
            assertNotNull(yamlShadowDataSourceConfiguration);
            assertThat(value.getShadowDataSourceName(), is(yamlShadowDataSourceConfiguration.getShadowDataSourceName()));
            assertThat(value.getSourceDataSourceName(), is(yamlShadowDataSourceConfiguration.getSourceDataSourceName()));
        });
        actualConfiguration.getTables().forEach((key, value) -> {
            YamlShadowTableConfiguration yamlShadowTableConfiguration = expectedConfiguration.getTables().get(key);
            assertNotNull(yamlShadowTableConfiguration);
            assertThat(value.getShadowAlgorithmNames(), is(yamlShadowTableConfiguration.getShadowAlgorithmNames()));
        });
        actualConfiguration.getShadowAlgorithms().forEach((key, value) -> {
            YamlShardingSphereAlgorithmConfiguration yamlShardingSphereAlgorithmConfiguration = expectedConfiguration.getShadowAlgorithms().get(key);
            assertNotNull(yamlShardingSphereAlgorithmConfiguration);
            assertThat(value.getType(), is(yamlShardingSphereAlgorithmConfiguration.getType()));
        });
    }
    
    private YamlShadowRuleConfiguration buildYamlShadowRuleConfiguration() {
        YamlShadowRuleConfiguration result = new YamlShadowRuleConfiguration();
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
}
