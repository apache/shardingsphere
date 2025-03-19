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

import org.apache.shardingsphere.mode.node.rule.tuple.RuleNodeTuple;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.YamlShadowRuleConfiguration;
import org.apache.shardingsphere.test.it.yaml.YamlRuleNodeTupleSwapperEngineIT;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ShadowRuleConfigurationYamlRuleNodeTupleSwapperEngineIT extends YamlRuleNodeTupleSwapperEngineIT {
    
    ShadowRuleConfigurationYamlRuleNodeTupleSwapperEngineIT() {
        super("yaml/shadow-rule.yaml");
    }
    
    @Override
    protected void assertRuleNodeTuples(final List<RuleNodeTuple> actualTuples, final YamlRuleConfiguration expectedYamlRuleConfig) {
        assertThat(actualTuples.size(), is(9));
        List<RuleNodeTuple> actual = new ArrayList<>(actualTuples);
        assertRuleNodeTuple(actual.get(0),
                "shadow_algorithms/user-id-insert-match-algorithm", ((YamlShadowRuleConfiguration) expectedYamlRuleConfig).getShadowAlgorithms().get("user-id-insert-match-algorithm"));
        assertRuleNodeTuple(actual.get(1),
                "shadow_algorithms/user-id-update-match-algorithm", ((YamlShadowRuleConfiguration) expectedYamlRuleConfig).getShadowAlgorithms().get("user-id-update-match-algorithm"));
        assertRuleNodeTuple(actual.get(2),
                "shadow_algorithms/user-id-select-match-algorithm", ((YamlShadowRuleConfiguration) expectedYamlRuleConfig).getShadowAlgorithms().get("user-id-select-match-algorithm"));
        assertRuleNodeTuple(actual.get(3), "shadow_algorithms/sql-hint-algorithm", ((YamlShadowRuleConfiguration) expectedYamlRuleConfig).getShadowAlgorithms().get("sql-hint-algorithm"));
        assertRuleNodeTuple(actual.get(4), "default_shadow_algorithm_name", ((YamlShadowRuleConfiguration) expectedYamlRuleConfig).getDefaultShadowAlgorithmName());
        assertRuleNodeTuple(actual.get(5), "data_sources/shadowDataSource", ((YamlShadowRuleConfiguration) expectedYamlRuleConfig).getDataSources().get("shadowDataSource"));
        assertRuleNodeTuple(actual.get(6), "tables/t_order", ((YamlShadowRuleConfiguration) expectedYamlRuleConfig).getTables().get("t_order"));
        assertRuleNodeTuple(actual.get(7), "tables/t_order_item", ((YamlShadowRuleConfiguration) expectedYamlRuleConfig).getTables().get("t_order_item"));
        assertRuleNodeTuple(actual.get(8), "tables/t_address", ((YamlShadowRuleConfiguration) expectedYamlRuleConfig).getTables().get("t_address"));
    }
}
