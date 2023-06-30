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

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.transaction.TransactionalReadQueryStrategy;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class NewYamlReadwriteSplittingRuleConfigurationSwapperTest {
    
    private final NewYamlReadwriteSplittingRuleConfigurationSwapper swapper = new NewYamlReadwriteSplittingRuleConfigurationSwapper();
    
    @Test
    void assertSwapToDataNodesLoadBalancersEmpty() {
        ReadwriteSplittingRuleConfiguration config = new ReadwriteSplittingRuleConfiguration(Collections.singleton(new ReadwriteSplittingDataSourceRuleConfiguration("group_0",
                "write_ds", Arrays.asList("read_ds_0", "read_ds_1"), null)), Collections.emptyMap());
        Collection<YamlDataNode> result = swapper.swapToDataNodes(config);
        assertThat(result.size(), is(1));
        assertThat(result.iterator().next().getKey(), is("data_sources/group_0"));
    }
    
    @Test
    void assertSwapToDataNodesLoadBalancers() {
        ReadwriteSplittingRuleConfiguration config = new ReadwriteSplittingRuleConfiguration(Collections.singleton(new ReadwriteSplittingDataSourceRuleConfiguration("group_0",
                "write_ds", Arrays.asList("read_ds_0", "read_ds_1"), "random")), Collections.singletonMap("random", new AlgorithmConfiguration("random", new Properties())));
        Collection<YamlDataNode> result = swapper.swapToDataNodes(config);
        assertThat(result.size(), is(2));
        Iterator<YamlDataNode> iterator = result.iterator();
        assertThat(iterator.next().getKey(), is("load_balancers/random"));
        assertThat(iterator.next().getKey(), is("data_sources/group_0"));
    }
    
    @Test
    void assertSwapToObjectEmpty() {
        Collection<YamlDataNode> config = new LinkedList<>();
        assertFalse(swapper.swapToObject(config).isPresent());
    }
    
    @Test
    void assertSwapToObject() {
        Collection<YamlDataNode> config = new LinkedList<>();
        config.add(new YamlDataNode("/metadata/foo_db/rules/readwrite_splitting/data_sources/group_0/versions/0", "loadBalancerName: random\n"
                + "readDataSourceNames:\n"
                + "- read_ds_0\n"
                + "- read_ds_1\n"
                + "transactionalReadQueryStrategy: DYNAMIC\n"
                + "writeDataSourceName: write_ds\n"));
        config.add(new YamlDataNode("/metadata/foo_db/rules/readwrite_splitting/load_balancers/random/versions/0", "type: random\n"));
        ReadwriteSplittingRuleConfiguration result = swapper.swapToObject(config).get();
        assertThat(result.getDataSources().size(), is(1));
        assertThat(result.getDataSources().iterator().next().getName(), is("group_0"));
        assertThat(result.getDataSources().iterator().next().getWriteDataSourceName(), is("write_ds"));
        assertThat(result.getDataSources().iterator().next().getReadDataSourceNames().size(), is(2));
        assertThat(result.getDataSources().iterator().next().getLoadBalancerName(), is("random"));
        assertThat(result.getDataSources().iterator().next().getTransactionalReadQueryStrategy(), is(TransactionalReadQueryStrategy.DYNAMIC));
        assertThat(result.getLoadBalancers().size(), is(1));
        assertThat(result.getLoadBalancers().get("random").getType(), is("random"));
        assertThat(result.getLoadBalancers().get("random").getProps().size(), is(0));
    }
}
