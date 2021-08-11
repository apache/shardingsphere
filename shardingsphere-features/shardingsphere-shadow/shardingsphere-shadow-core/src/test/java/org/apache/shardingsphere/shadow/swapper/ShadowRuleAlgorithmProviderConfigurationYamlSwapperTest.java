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
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public final class ShadowRuleAlgorithmProviderConfigurationYamlSwapperTest {

    private AlgorithmProvidedShadowRuleConfiguration algorithmProvidedShadowRuleConfiguration;

    private YamlShadowRuleConfiguration yamlShadowRuleConfiguration;

    @Test
    public void assertSwapToYamlConfiguration() {
        buildAlgorithmProvidedShadowRuleConfiguration();
        ShadowRuleAlgorithmProviderConfigurationYamlSwapper swapper = new ShadowRuleAlgorithmProviderConfigurationYamlSwapper();
        YamlShadowRuleConfiguration yamlShadowRuleConfiguration = swapper.swapToYamlConfiguration(algorithmProvidedShadowRuleConfiguration);
        assertThat(yamlShadowRuleConfiguration.getColumn(), is(algorithmProvidedShadowRuleConfiguration.getColumn()));
        assertThat(yamlShadowRuleConfiguration.getShadowDataSourceNames(), is(algorithmProvidedShadowRuleConfiguration.getShadowDataSourceNames()));
        assertThat(yamlShadowRuleConfiguration.getSourceDataSourceNames(), is(algorithmProvidedShadowRuleConfiguration.getSourceDataSourceNames()));
        yamlShadowRuleConfiguration.getDataSources().entrySet().forEach(each -> {
            ShadowDataSourceConfiguration dataSourceConfiguration = algorithmProvidedShadowRuleConfiguration.getDataSources().get(each.getKey());
            assertNotNull(dataSourceConfiguration);
            assertThat(each.getValue().getShadowDataSourceName(), is(dataSourceConfiguration.getShadowDataSourceName()));
            assertThat(each.getValue().getSourceDataSourceName(), is(dataSourceConfiguration.getSourceDataSourceName()));
        });
        yamlShadowRuleConfiguration.getShadowTables().entrySet().forEach(each -> {
            ShadowTableConfiguration shadowTableConfiguration = algorithmProvidedShadowRuleConfiguration.getShadowTables().get(each.getKey());
            assertNotNull(shadowTableConfiguration);
            assertThat(each.getValue().getShadowAlgorithmNames(), is(shadowTableConfiguration.getShadowAlgorithmNames()));
        });
        yamlShadowRuleConfiguration.getShadowAlgorithms().entrySet().forEach(each -> {
            ShadowAlgorithm shadowAlgorithm = algorithmProvidedShadowRuleConfiguration.getShadowAlgorithms().get(each.getKey());
            assertNotNull(shadowAlgorithm);
            assertThat(each.getValue().getType(), is(shadowAlgorithm.getType()));
        });
    }

    @Test
    public void assertSwapToObject() {
        buildYamlShadowRuleConfiguration();
        ShadowRuleAlgorithmProviderConfigurationYamlSwapper swapper = new ShadowRuleAlgorithmProviderConfigurationYamlSwapper();
        AlgorithmProvidedShadowRuleConfiguration targetConfiguration = swapper.swapToObject(yamlShadowRuleConfiguration);
        assertThat(targetConfiguration.getColumn(), is(yamlShadowRuleConfiguration.getColumn()));
        assertThat(targetConfiguration.getShadowDataSourceNames(), is(yamlShadowRuleConfiguration.getShadowDataSourceNames()));
        assertThat(targetConfiguration.getSourceDataSourceNames(), is(yamlShadowRuleConfiguration.getSourceDataSourceNames()));
        targetConfiguration.getDataSources().entrySet().forEach(each -> {
            YamlShadowDataSourceConfiguration yamlShadowDataSourceConfiguration = yamlShadowRuleConfiguration.getDataSources().get(each.getKey());
            assertNotNull(yamlShadowDataSourceConfiguration);
            assertThat(each.getValue().getShadowDataSourceName(), is(yamlShadowDataSourceConfiguration.getShadowDataSourceName()));
            assertThat(each.getValue().getSourceDataSourceName(), is(yamlShadowDataSourceConfiguration.getSourceDataSourceName()));
        });
        targetConfiguration.getShadowTables().entrySet().forEach(each -> {
            YamlShadowTableConfiguration yamlShadowTableConfiguration = yamlShadowRuleConfiguration.getShadowTables().get(each.getKey());
            assertNotNull(yamlShadowTableConfiguration);
            assertThat(each.getValue().getShadowAlgorithmNames(), is(yamlShadowTableConfiguration.getShadowAlgorithmNames()));
        });
        targetConfiguration.getShadowAlgorithms().entrySet().forEach(each -> {
            YamlShardingSphereAlgorithmConfiguration yamlShardingSphereAlgorithmConfiguration = yamlShadowRuleConfiguration.getShadowAlgorithms().get(each.getKey());
            assertNotNull(yamlShardingSphereAlgorithmConfiguration);
            assertThat(each.getValue().getType(), is(yamlShardingSphereAlgorithmConfiguration.getType()));
        });
    }

    private void buildAlgorithmProvidedShadowRuleConfiguration() {
        algorithmProvidedShadowRuleConfiguration = new AlgorithmProvidedShadowRuleConfiguration("id", Arrays.asList("ds"), Arrays.asList("ds-shadow"));
        algorithmProvidedShadowRuleConfiguration.getDataSources().put("shadow-data-source", new ShadowDataSourceConfiguration("ds", "ds-shadow"));
        algorithmProvidedShadowRuleConfiguration.getShadowTables().put("t_order", new ShadowTableConfiguration(Arrays.asList("user-id-match-algorithm", "note-algorithm")));
        algorithmProvidedShadowRuleConfiguration.getShadowAlgorithms().put("user-id-match-algorithm", new ColumnRegularMatchShadowAlgorithm());
    }

    private void buildYamlShadowRuleConfiguration() {
        yamlShadowRuleConfiguration = new YamlShadowRuleConfiguration();
        yamlShadowRuleConfiguration.setColumn("id");
        yamlShadowRuleConfiguration.setSourceDataSourceNames(Arrays.asList("ds"));
        yamlShadowRuleConfiguration.setShadowDataSourceNames(Arrays.asList("ds-shadow"));
        YamlShadowDataSourceConfiguration yamlShadowDataSourceConfiguration = new YamlShadowDataSourceConfiguration();
        yamlShadowDataSourceConfiguration.setSourceDataSourceName("ds");
        yamlShadowDataSourceConfiguration.setShadowDataSourceName("ds-shadow");
        yamlShadowRuleConfiguration.getDataSources().put("shadow-data-source", yamlShadowDataSourceConfiguration);
        YamlShadowTableConfiguration yamlShadowTableConfiguration = new YamlShadowTableConfiguration();
        yamlShadowTableConfiguration.setShadowAlgorithmNames(Arrays.asList("user-id-match-algorithm", "note-algorithm"));
        yamlShadowRuleConfiguration.getShadowTables().put("t_order", yamlShadowTableConfiguration);
        YamlShardingSphereAlgorithmConfiguration yamlShardingSphereAlgorithmConfiguration = new YamlShardingSphereAlgorithmConfiguration();
        yamlShardingSphereAlgorithmConfiguration.setType("COLUMN-REGULAR-MATCH");
        yamlShadowRuleConfiguration.getShadowAlgorithms().put("user-id-match-algorithm", yamlShardingSphereAlgorithmConfiguration);
    }
}
