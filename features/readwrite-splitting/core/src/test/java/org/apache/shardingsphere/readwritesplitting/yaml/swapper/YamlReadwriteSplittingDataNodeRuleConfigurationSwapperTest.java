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
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.transaction.TransactionalReadQueryStrategy;
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

class YamlReadwriteSplittingDataNodeRuleConfigurationSwapperTest {
    
    private final YamlReadwriteSplittingDataNodeRuleConfigurationSwapper swapper = new YamlReadwriteSplittingDataNodeRuleConfigurationSwapper();
    
    @Test
    void assertSwapToDataNodesLoadBalancersEmpty() {
        ReadwriteSplittingRuleConfiguration ruleConfig = new ReadwriteSplittingRuleConfiguration(Collections.singleton(new ReadwriteSplittingDataSourceGroupRuleConfiguration("group_0",
                "write_ds", Arrays.asList("read_ds_0", "read_ds_1"), null)), Collections.emptyMap());
        Collection<RepositoryTuple> actual = swapper.swapToRepositoryTuples(ruleConfig);
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getKey(), is("data_sources/group_0"));
    }
    
    @Test
    void assertSwapToDataNodesLoadBalancers() {
        ReadwriteSplittingRuleConfiguration ruleConfig = new ReadwriteSplittingRuleConfiguration(Collections.singleton(new ReadwriteSplittingDataSourceGroupRuleConfiguration("group_0",
                "write_ds", Arrays.asList("read_ds_0", "read_ds_1"), "random")), Collections.singletonMap("random", new AlgorithmConfiguration("random", new Properties())));
        Collection<RepositoryTuple> actual = swapper.swapToRepositoryTuples(ruleConfig);
        assertThat(actual.size(), is(2));
        Iterator<RepositoryTuple> iterator = actual.iterator();
        assertThat(iterator.next().getKey(), is("load_balancers/random"));
        assertThat(iterator.next().getKey(), is("data_sources/group_0"));
    }
    
    @Test
    void assertSwapToObjectEmpty() {
        assertFalse(swapper.swapToObject(Collections.emptyList()).isPresent());
    }
    
    @Test
    void assertSwapToObject() {
        Collection<RepositoryTuple> repositoryTuples = Arrays.asList(new RepositoryTuple("/metadata/foo_db/rules/readwrite_splitting/data_sources/group_0/versions/0", "loadBalancerName: random\n"
                + "readDataSourceNames:\n"
                + "- read_ds_0\n"
                + "- read_ds_1\n"
                + "transactionalReadQueryStrategy: DYNAMIC\n"
                + "writeDataSourceName: write_ds\n"),
                new RepositoryTuple("/metadata/foo_db/rules/readwrite_splitting/load_balancers/random/versions/0", "type: random\n"));
        Optional<ReadwriteSplittingRuleConfiguration> actual = swapper.swapToObject(repositoryTuples);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getDataSourceGroups().size(), is(1));
        assertThat(actual.get().getDataSourceGroups().iterator().next().getName(), is("group_0"));
        assertThat(actual.get().getDataSourceGroups().iterator().next().getWriteDataSourceName(), is("write_ds"));
        assertThat(actual.get().getDataSourceGroups().iterator().next().getReadDataSourceNames().size(), is(2));
        assertThat(actual.get().getDataSourceGroups().iterator().next().getLoadBalancerName(), is("random"));
        assertThat(actual.get().getDataSourceGroups().iterator().next().getTransactionalReadQueryStrategy(), is(TransactionalReadQueryStrategy.DYNAMIC));
        assertThat(actual.get().getLoadBalancers().size(), is(1));
        assertThat(actual.get().getLoadBalancers().get("random").getType(), is("random"));
        assertThat(actual.get().getLoadBalancers().get("random").getProps().size(), is(0));
    }
}
