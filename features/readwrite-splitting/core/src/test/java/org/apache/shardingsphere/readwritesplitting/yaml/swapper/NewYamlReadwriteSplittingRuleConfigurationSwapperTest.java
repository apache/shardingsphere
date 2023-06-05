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
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

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
        assertThat(iterator.next().getKey(), is("data_sources/group_0"));
        assertThat(iterator.next().getKey(), is("load_balancers/random"));
    }
}
