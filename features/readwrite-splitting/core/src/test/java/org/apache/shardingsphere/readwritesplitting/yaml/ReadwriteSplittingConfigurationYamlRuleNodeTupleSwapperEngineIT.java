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

package org.apache.shardingsphere.readwritesplitting.yaml;

import org.apache.shardingsphere.mode.node.rule.tuple.RuleNodeTuple;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.test.it.yaml.YamlRuleNodeTupleSwapperEngineIT;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ReadwriteSplittingConfigurationYamlRuleNodeTupleSwapperEngineIT extends YamlRuleNodeTupleSwapperEngineIT {
    
    ReadwriteSplittingConfigurationYamlRuleNodeTupleSwapperEngineIT() {
        super("yaml/readwrite-splitting-rule.yaml");
    }
    
    @Override
    protected void assertRuleNodeTuples(final List<RuleNodeTuple> actualTuples, final YamlRuleConfiguration expectedYamlRuleConfig) {
        assertThat(actualTuples.size(), is(4));
        assertRuleNodeTuple(actualTuples.get(0), "load_balancers/random", ((YamlReadwriteSplittingRuleConfiguration) expectedYamlRuleConfig).getLoadBalancers().get("random"));
        assertRuleNodeTuple(actualTuples.get(1), "load_balancers/roundRobin", ((YamlReadwriteSplittingRuleConfiguration) expectedYamlRuleConfig).getLoadBalancers().get("roundRobin"));
        assertRuleNodeTuple(actualTuples.get(2), "data_source_groups/ds_0", ((YamlReadwriteSplittingRuleConfiguration) expectedYamlRuleConfig).getDataSourceGroups().get("ds_0"));
        assertRuleNodeTuple(actualTuples.get(3), "data_source_groups/ds_1", ((YamlReadwriteSplittingRuleConfiguration) expectedYamlRuleConfig).getDataSourceGroups().get("ds_1"));
    }
}
