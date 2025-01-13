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

import org.apache.shardingsphere.mode.node.tuple.RepositoryTuple;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.YamlShadowRuleConfiguration;
import org.apache.shardingsphere.test.it.yaml.RepositoryTupleSwapperEngineIT;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ShadowRuleConfigurationRepositoryTupleSwapperEngineIT extends RepositoryTupleSwapperEngineIT {
    
    ShadowRuleConfigurationRepositoryTupleSwapperEngineIT() {
        super("yaml/shadow-rule.yaml");
    }
    
    @Override
    protected void assertRepositoryTuples(final List<RepositoryTuple> actualRepositoryTuples, final YamlRuleConfiguration expectedYamlRuleConfig) {
        assertThat(actualRepositoryTuples.size(), is(9));
        List<RepositoryTuple> actual = new ArrayList<>(actualRepositoryTuples);
        assertRepositoryTuple(actual.get(0),
                "shadow_algorithms/user-id-insert-match-algorithm", ((YamlShadowRuleConfiguration) expectedYamlRuleConfig).getShadowAlgorithms().get("user-id-insert-match-algorithm"));
        assertRepositoryTuple(actual.get(1),
                "shadow_algorithms/user-id-update-match-algorithm", ((YamlShadowRuleConfiguration) expectedYamlRuleConfig).getShadowAlgorithms().get("user-id-update-match-algorithm"));
        assertRepositoryTuple(actual.get(2),
                "shadow_algorithms/user-id-select-match-algorithm", ((YamlShadowRuleConfiguration) expectedYamlRuleConfig).getShadowAlgorithms().get("user-id-select-match-algorithm"));
        assertRepositoryTuple(actual.get(3), "shadow_algorithms/sql-hint-algorithm", ((YamlShadowRuleConfiguration) expectedYamlRuleConfig).getShadowAlgorithms().get("sql-hint-algorithm"));
        assertRepositoryTuple(actual.get(4), "default_shadow_algorithm_name", ((YamlShadowRuleConfiguration) expectedYamlRuleConfig).getDefaultShadowAlgorithmName());
        assertRepositoryTuple(actual.get(5), "data_sources/shadowDataSource", ((YamlShadowRuleConfiguration) expectedYamlRuleConfig).getDataSources().get("shadowDataSource"));
        assertRepositoryTuple(actual.get(6), "tables/t_order", ((YamlShadowRuleConfiguration) expectedYamlRuleConfig).getTables().get("t_order"));
        assertRepositoryTuple(actual.get(7), "tables/t_order_item", ((YamlShadowRuleConfiguration) expectedYamlRuleConfig).getTables().get("t_order_item"));
        assertRepositoryTuple(actual.get(8), "tables/t_address", ((YamlShadowRuleConfiguration) expectedYamlRuleConfig).getTables().get("t_address"));
    }
}
