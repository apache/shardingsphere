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

package org.apache.shardingsphere.readwritesplitting.yaml.swapper;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.yaml.datanode.RepositoryTuple;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.transaction.TransactionalReadQueryStrategy;
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.rule.YamlReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReadwriteSplittingRuleConfigurationRepositoryTupleSwapperTest {
    
    private final ReadwriteSplittingRuleConfigurationRepositoryTupleSwapper swapper = new ReadwriteSplittingRuleConfigurationRepositoryTupleSwapper();
    
    @Test
    void assertSwapToRepositoryTuplesWithEmptyLoadBalancer() {
        YamlReadwriteSplittingRuleConfiguration yamlRuleConfig = new YamlReadwriteSplittingRuleConfiguration();
        yamlRuleConfig.setDataSourceGroups(Collections.singletonMap("foo_group", new YamlReadwriteSplittingDataSourceGroupRuleConfiguration()));
        Collection<RepositoryTuple> actual = swapper.swapToRepositoryTuples(yamlRuleConfig);
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getKey(), is("data_sources/foo_group"));
    }
    
    @Test
    void assertSwapToRepositoryTuples() {
        ReadwriteSplittingRuleConfiguration ruleConfig = new ReadwriteSplittingRuleConfiguration(Collections.singleton(new ReadwriteSplittingDataSourceGroupRuleConfiguration("foo_group",
                "write_ds", Arrays.asList("read_ds_0", "read_ds_1"), "random")), Collections.singletonMap("random", new AlgorithmConfiguration("random", new Properties())));
        YamlReadwriteSplittingRuleConfiguration yamlRuleConfig = (YamlReadwriteSplittingRuleConfiguration) new YamlRuleConfigurationSwapperEngine().swapToYamlRuleConfiguration(ruleConfig);
        Collection<RepositoryTuple> actual = swapper.swapToRepositoryTuples(yamlRuleConfig);
        assertThat(actual.size(), is(2));
        Iterator<RepositoryTuple> iterator = actual.iterator();
        assertThat(iterator.next().getKey(), is("load_balancers/random"));
        assertThat(iterator.next().getKey(), is("data_sources/foo_group"));
    }
    
    @Test
    void assertSwapToObjectWithEmptyTuple() {
        assertFalse(swapper.swapToObject0(Collections.emptyList()).isPresent());
    }
    
    @Test
    void assertSwapToObject() {
        Collection<RepositoryTuple> repositoryTuples = Arrays.asList(new RepositoryTuple("/metadata/foo_db/rules/readwrite_splitting/data_sources/foo_group/versions/0", "loadBalancerName: random\n"
                + "readDataSourceNames:\n"
                + "- read_ds_0\n"
                + "- read_ds_1\n"
                + "transactionalReadQueryStrategy: DYNAMIC\n"
                + "writeDataSourceName: write_ds\n"),
                new RepositoryTuple("/metadata/foo_db/rules/readwrite_splitting/load_balancers/random/versions/0", "type: random\n"));
        Optional<YamlReadwriteSplittingRuleConfiguration> actual = swapper.swapToObject0(repositoryTuples);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getDataSourceGroups().size(), is(1));
        assertThat(actual.get().getDataSourceGroups().get("foo_group").getWriteDataSourceName(), is("write_ds"));
        assertThat(actual.get().getDataSourceGroups().get("foo_group").getReadDataSourceNames().size(), is(2));
        assertThat(actual.get().getDataSourceGroups().get("foo_group").getLoadBalancerName(), is("random"));
        assertThat(actual.get().getDataSourceGroups().get("foo_group").getTransactionalReadQueryStrategy(), is(TransactionalReadQueryStrategy.DYNAMIC.name()));
        assertThat(actual.get().getLoadBalancers().size(), is(1));
        assertThat(actual.get().getLoadBalancers().get("random").getType(), is("random"));
        assertTrue(actual.get().getLoadBalancers().get("random").getProps().isEmpty());
    }
}
