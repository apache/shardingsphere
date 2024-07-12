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

package org.apache.shardingsphere.readwritesplitting.rule;

import org.apache.shardingsphere.infra.algorithm.loadbalancer.random.RandomLoadBalanceAlgorithm;
import org.apache.shardingsphere.readwritesplitting.config.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.transaction.TransactionalReadQueryStrategy;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ReadwriteSplittingDataSourceGroupRuleTest {
    
    @Test
    void assertGetWriteDataSource() {
        ReadwriteSplittingDataSourceGroupRule dataSourceGroupRule = new ReadwriteSplittingDataSourceGroupRule(
                new ReadwriteSplittingDataSourceGroupRuleConfiguration("test_pr", "write_ds", Arrays.asList("read_ds_0", "read_ds_1"), TransactionalReadQueryStrategy.DYNAMIC, null),
                TransactionalReadQueryStrategy.DYNAMIC, new RandomLoadBalanceAlgorithm());
        String writeDataSourceName = dataSourceGroupRule.getWriteDataSource();
        assertThat(writeDataSourceName, is("write_ds"));
    }
}
