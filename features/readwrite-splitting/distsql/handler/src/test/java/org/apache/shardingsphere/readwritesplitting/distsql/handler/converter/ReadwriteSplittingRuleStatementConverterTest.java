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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.converter;

import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.readwritesplitting.transaction.TransactionalReadQueryStrategy;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.apache.shardingsphere.test.infra.framework.matcher.ShardingSphereAssertionMatchers.deepEqual;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReadwriteSplittingRuleStatementConverterTest {
    
    @Test
    void assertConvert() {
        ReadwriteSplittingRuleSegment ruleSegment = new ReadwriteSplittingRuleSegment(
                "foo_name", "write_ds", Arrays.asList("read_ds0", "read_ds1"), TransactionalReadQueryStrategy.FIXED.name(), new AlgorithmSegment("foo_algo", new Properties()));
        ReadwriteSplittingRuleConfiguration actual = ReadwriteSplittingRuleStatementConverter.convert(Collections.singleton(ruleSegment));
        ReadwriteSplittingDataSourceGroupRuleConfiguration expectedDataSourceGroupRuleConfig = new ReadwriteSplittingDataSourceGroupRuleConfiguration(
                "foo_name", "write_ds", Arrays.asList("read_ds0", "read_ds1"), TransactionalReadQueryStrategy.FIXED, "foo_name_foo_algo");
        assertThat(actual.getDataSourceGroups(), deepEqual(Collections.singletonList(expectedDataSourceGroupRuleConfig)));
        assertThat(actual.getLoadBalancers(), deepEqual(Collections.singletonMap("foo_name_foo_algo", new AlgorithmConfiguration("foo_algo", new Properties()))));
    }
    
    @Test
    void assertConvertWithoutTransactionalReadQueryStrategyAndLoadBalancer() {
        ReadwriteSplittingRuleConfiguration actual = ReadwriteSplittingRuleStatementConverter.convert(
                Collections.singleton(new ReadwriteSplittingRuleSegment("foo_name", "write_ds", Arrays.asList("read_ds0", "read_ds1"), null)));
        assertThat(actual.getDataSourceGroups(), deepEqual(Collections.singletonList(
                new ReadwriteSplittingDataSourceGroupRuleConfiguration("foo_name", "write_ds", Arrays.asList("read_ds0", "read_ds1"), null))));
        assertTrue(actual.getLoadBalancers().isEmpty());
    }
}
