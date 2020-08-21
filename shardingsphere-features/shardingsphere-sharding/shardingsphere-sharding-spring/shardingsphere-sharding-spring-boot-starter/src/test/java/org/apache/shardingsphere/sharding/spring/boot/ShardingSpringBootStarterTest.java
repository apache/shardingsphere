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

package org.apache.shardingsphere.sharding.spring.boot;

import org.apache.shardingsphere.sharding.algorithm.config.AlgorithmProvidedShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.algorithm.keygen.SnowflakeKeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineShardingAlgorithm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ShardingSpringBootStarterTest.class)
@SpringBootApplication
@ActiveProfiles("sharding")
public class ShardingSpringBootStarterTest {
    
    @Resource
    private InlineShardingAlgorithm databaseShardingAlgorithm;
    
    @Resource
    private InlineShardingAlgorithm orderTableShardingAlgorithm;
    
    @Resource
    private InlineShardingAlgorithm orderItemTableShardingAlgorithm;
    
    @Resource
    private SnowflakeKeyGenerateAlgorithm keyGenerator;
    
    @Resource
    private AlgorithmProvidedShardingRuleConfiguration shardingRuleConfiguration;
    
    @Test
    public void assertShardingAlgorithm() {
        assertThat(databaseShardingAlgorithm.getProps().getProperty("algorithm.expression"), is("ds_$->{user_id % 2}"));
        assertThat(orderTableShardingAlgorithm.getProps().getProperty("algorithm.expression"), is("t_order_$->{order_id % 2}"));
        assertThat(orderItemTableShardingAlgorithm.getProps().getProperty("algorithm.expression"), is("t_order_item_$->{order_id % 2}"));
    }
    
    @Test
    public void assertKeyGenerateAlgorithm() {
        assertThat(keyGenerator.getProps().getProperty("worker.id"), is("123"));
    }
    
    @Test
    public void assertShardingConfiguration() {
        assertThat(shardingRuleConfiguration.getTables().size(), is(2));
        // TODO assert sharding configuration
    }
}
