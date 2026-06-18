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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.update;

import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnusedAlgorithmFinderTest {
    
    @Test
    void assertFindUnusedLoadBalancers() {
        ReadwriteSplittingRuleConfiguration ruleConfig = createRuleConfiguration(Collections.singletonList("lb_0"), Arrays.asList("lb_0", "lb_1"));
        assertThat(UnusedAlgorithmFinder.findUnusedLoadBalancers(ruleConfig), is(Collections.singleton("lb_1")));
    }
    
    @Test
    void assertFindUnusedLoadBalancersWithoutUnused() {
        ReadwriteSplittingRuleConfiguration ruleConfig = createRuleConfiguration(Arrays.asList("lb_0", "lb_1"), Arrays.asList("lb_0", "lb_1"));
        assertTrue(UnusedAlgorithmFinder.findUnusedLoadBalancers(ruleConfig).isEmpty());
    }
    
    private ReadwriteSplittingRuleConfiguration createRuleConfiguration(final Collection<String> usedLoadBalancers, final Collection<String> allLoadBalancers) {
        Collection<ReadwriteSplittingDataSourceGroupRuleConfiguration> dataSourceGroups = usedLoadBalancers.stream()
                .map(each -> new ReadwriteSplittingDataSourceGroupRuleConfiguration("group_" + each, "write_ds", Collections.singletonList("read_ds"), each)).collect(Collectors.toList());
        Map<String, AlgorithmConfiguration> loadBalancers = allLoadBalancers.stream().collect(Collectors.toMap(each -> each, each -> new AlgorithmConfiguration("RANDOM", new Properties())));
        return new ReadwriteSplittingRuleConfiguration(dataSourceGroups, loadBalancers);
    }
}
