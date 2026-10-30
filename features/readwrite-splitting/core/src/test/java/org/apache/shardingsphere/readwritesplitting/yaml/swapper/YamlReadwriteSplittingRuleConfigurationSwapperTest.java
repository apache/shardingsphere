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

import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.transaction.TransactionalReadQueryStrategy;
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.rule.YamlReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class YamlReadwriteSplittingRuleConfigurationSwapperTest {
    
    private final YamlReadwriteSplittingRuleConfigurationSwapper swapper =
            (YamlReadwriteSplittingRuleConfigurationSwapper) OrderedSPILoader.getServicesByClass(YamlRuleConfigurationSwapper.class,
                    Collections.singleton(ReadwriteSplittingRuleConfiguration.class)).get(ReadwriteSplittingRuleConfiguration.class);
    
    @ParameterizedTest(name = "{index}: strategy={0}, expected={1}")
    @MethodSource("transactionalReadQueryStrategyArguments")
    void assertSwapToObjectWithName(final String strategy, final TransactionalReadQueryStrategy expectedStrategy) {
        ReadwriteSplittingDataSourceGroupRuleConfiguration actual = swapper.swapToObject("foo_group", createYamlDataSourceGroupRuleConfiguration(strategy));
        assertThat(actual.getName(), is("foo_group"));
        assertThat(actual.getWriteDataSourceName(), is("foo_write_ds"));
        assertThat(actual.getReadDataSourceNames(), is(Collections.singletonList("foo_read_ds")));
        assertThat(actual.getTransactionalReadQueryStrategy(), is(expectedStrategy));
        assertThat(actual.getLoadBalancerName(), is("foo_balancer"));
    }
    
    private YamlReadwriteSplittingDataSourceGroupRuleConfiguration createYamlDataSourceGroupRuleConfiguration(final String strategy) {
        YamlReadwriteSplittingDataSourceGroupRuleConfiguration result = new YamlReadwriteSplittingDataSourceGroupRuleConfiguration();
        result.setWriteDataSourceName("foo_write_ds");
        result.setReadDataSourceNames(Collections.singletonList("foo_read_ds"));
        result.setTransactionalReadQueryStrategy(strategy);
        result.setLoadBalancerName("foo_balancer");
        return result;
    }
    
    private static Stream<Arguments> transactionalReadQueryStrategyArguments() {
        return Stream.of(
                Arguments.of(null, TransactionalReadQueryStrategy.PRIMARY),
                Arguments.of("", TransactionalReadQueryStrategy.PRIMARY),
                Arguments.of("PRIMARY", TransactionalReadQueryStrategy.PRIMARY),
                Arguments.of("FIXED", TransactionalReadQueryStrategy.FIXED),
                Arguments.of("DYNAMIC", TransactionalReadQueryStrategy.DYNAMIC));
    }
    
    @Test
    void assertSwapToObjectWithInvalidStrategy() {
        assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject("foo_group", createYamlDataSourceGroupRuleConfiguration("INVALID")));
    }
    
    @Test
    void assertSwapToObject() {
        YamlReadwriteSplittingRuleConfiguration yamlConfig = new YamlReadwriteSplittingRuleConfiguration();
        yamlConfig.getDataSourceGroups().put("foo_group", createYamlDataSourceGroupRuleConfiguration("FIXED"));
        ReadwriteSplittingRuleConfiguration actual = swapper.swapToObject(yamlConfig);
        assertThat(actual.getDataSourceGroups().size(), is(1));
        assertThat(actual.getDataSourceGroups().iterator().next().getName(), is("foo_group"));
    }
}
