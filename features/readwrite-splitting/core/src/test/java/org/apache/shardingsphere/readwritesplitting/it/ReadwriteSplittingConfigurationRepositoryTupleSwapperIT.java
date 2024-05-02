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

package org.apache.shardingsphere.readwritesplitting.it;

import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.yaml.datanode.RepositoryTuple;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.rule.YamlReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.swapper.ReadwriteSplittingRuleConfigurationRepositoryTupleSwapper;
import org.apache.shardingsphere.test.it.yaml.RepositoryTupleSwapperIT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ReadwriteSplittingConfigurationRepositoryTupleSwapperIT extends RepositoryTupleSwapperIT {
    
    ReadwriteSplittingConfigurationRepositoryTupleSwapperIT() {
        super("yaml/readwrite-splitting-rule.yaml", new ReadwriteSplittingRuleConfigurationRepositoryTupleSwapper(), false);
    }
    
    @Override
    protected void assertRepositoryTuples(final Collection<RepositoryTuple> actualRepositoryTuples, final YamlRuleConfiguration expectedYamlRuleConfig) {
        assertThat(actualRepositoryTuples.size(), is(4));
        List<RepositoryTuple> actual = new ArrayList<>(actualRepositoryTuples);
        assertLoadBalancers(actual.subList(0, 2), ((YamlReadwriteSplittingRuleConfiguration) expectedYamlRuleConfig).getLoadBalancers());
        assertDataSourceGroups(actual.subList(2, 4), ((YamlReadwriteSplittingRuleConfiguration) expectedYamlRuleConfig).getDataSourceGroups());
    }
    
    private void assertLoadBalancers(final List<RepositoryTuple> actual, final Map<String, YamlAlgorithmConfiguration> expectedLoadBalancers) {
        assertRepositoryTuple(actual.get(0), "load_balancers/random", expectedLoadBalancers.get("random"));
        assertRepositoryTuple(actual.get(1), "load_balancers/roundRobin", expectedLoadBalancers.get("roundRobin"));
    }
    
    private void assertDataSourceGroups(final List<RepositoryTuple> actual, final Map<String, YamlReadwriteSplittingDataSourceGroupRuleConfiguration> expectedDataSourceGroups) {
        assertRepositoryTuple(actual.get(0), "data_sources/ds_0", expectedDataSourceGroups.get("ds_0"));
        assertRepositoryTuple(actual.get(1), "data_sources/ds_1", expectedDataSourceGroups.get("ds_1"));
    }
}
