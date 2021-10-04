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

package org.apache.shardingsphere.sharding.distsql.converter;

import org.apache.shardingsphere.distsql.parser.segment.AlgorithmSegment;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ShardingRuleStatementConverter;
import org.apache.shardingsphere.sharding.distsql.parser.segment.TableRuleSegment;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ShardingRuleStatementConverterTest {
    
    @Test
    public void assertConvert() {
        ShardingRuleConfiguration config = ShardingRuleStatementConverter.convert(Collections.singleton(createTableRuleSegment()));
        assertTrue(config.getTables().isEmpty());
        assertThat(config.getAutoTables().size(), is(1));
        assertThat(config.getAutoTables().iterator().next().getActualDataSources(), is("ds0,ds1"));
        assertThat(((StandardShardingStrategyConfiguration) config.getAutoTables().iterator().next().getShardingStrategy()).getShardingColumn(), is("order_id"));
        assertThat(config.getAutoTables().iterator().next().getShardingStrategy().getShardingAlgorithmName(), is("t_order_MOD"));
        assertTrue(config.getShardingAlgorithms().containsKey("t_order_MOD"));
        assertThat(config.getShardingAlgorithms().get("t_order_MOD").getType(), is("MOD"));
    }
    
    private TableRuleSegment createTableRuleSegment() {
        ShardingSphereServiceLoader.register(ShardingAlgorithm.class);
        Properties props = new Properties();
        props.setProperty("sharding_count", "2");
        return new TableRuleSegment("t_order", Arrays.asList("ds0", "ds1"), "order_id", new AlgorithmSegment("MOD", props), null, null);
    }
}
