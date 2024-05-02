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

package org.apache.shardingsphere.shadow.it;

import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.yaml.datanode.RepositoryTuple;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.YamlShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.datasource.YamlShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.table.YamlShadowTableConfiguration;
import org.apache.shardingsphere.shadow.yaml.swapper.ShadowRuleConfigurationRepositoryTupleSwapper;
import org.apache.shardingsphere.test.it.yaml.RepositoryTupleSwapperIT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ShadowRuleConfigurationRepositoryTupleSwapperIT extends RepositoryTupleSwapperIT {
    
    ShadowRuleConfigurationRepositoryTupleSwapperIT() {
        super("yaml/shadow-rule.yaml", new ShadowRuleConfigurationRepositoryTupleSwapper(), false);
    }
    
    @Override
    protected void assertRepositoryTuples(final Collection<RepositoryTuple> actualRepositoryTuples, final YamlRuleConfiguration expectedYamlRuleConfig) {
        assertThat(actualRepositoryTuples.size(), is(9));
        List<RepositoryTuple> actual = new ArrayList<>(actualRepositoryTuples);
        assertAlgorithms(actual.subList(0, 4), ((YamlShadowRuleConfiguration) expectedYamlRuleConfig).getShadowAlgorithms());
        assertDefaultAlgorithm(actual.get(4), ((YamlShadowRuleConfiguration) expectedYamlRuleConfig).getDefaultShadowAlgorithmName());
        assertDataSources(actual.get(5), ((YamlShadowRuleConfiguration) expectedYamlRuleConfig).getDataSources());
        assertTables(actual.subList(6, 9), ((YamlShadowRuleConfiguration) expectedYamlRuleConfig).getTables());
    }
    
    private void assertAlgorithms(final List<RepositoryTuple> actual, final Map<String, YamlAlgorithmConfiguration> expectedAlgorithms) {
        assertRepositoryTuple(actual.get(0), "algorithms/user-id-insert-match-algorithm", expectedAlgorithms.get("user-id-insert-match-algorithm"));
        assertRepositoryTuple(actual.get(1), "algorithms/user-id-update-match-algorithm", expectedAlgorithms.get("user-id-update-match-algorithm"));
        assertRepositoryTuple(actual.get(2), "algorithms/user-id-select-match-algorithm", expectedAlgorithms.get("user-id-select-match-algorithm"));
        assertRepositoryTuple(actual.get(3), "algorithms/sql-hint-algorithm", expectedAlgorithms.get("sql-hint-algorithm"));
    }
    
    private void assertDefaultAlgorithm(final RepositoryTuple actual, final String expectedDefaultShadowAlgorithmName) {
        assertRepositoryTuple(actual, "default_algorithm_name", expectedDefaultShadowAlgorithmName);
    }
    
    private void assertDataSources(final RepositoryTuple actual, final Map<String, YamlShadowDataSourceConfiguration> expectedDataSources) {
        assertRepositoryTuple(actual, "data_sources/shadowDataSource", expectedDataSources.get("shadowDataSource"));
    }
    
    private void assertTables(final List<RepositoryTuple> actual, final Map<String, YamlShadowTableConfiguration> expectedTables) {
        assertRepositoryTuple(actual.get(0), "tables/t_order", expectedTables.get("t_order"));
        assertRepositoryTuple(actual.get(1), "tables/t_order_item", expectedTables.get("t_order_item"));
        assertRepositoryTuple(actual.get(2), "tables/t_address", expectedTables.get("t_address"));
    }
}
