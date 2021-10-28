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
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ShardingTableRuleStatementConverter;
import org.apache.shardingsphere.sharding.distsql.parser.segment.AbstractTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.AutoTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.KeyGenerateSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingStrategySegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.TableRuleSegment;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public final class ShardingRuleStatementConverterTest {
    
    @Before
    public void before() {
        ShardingSphereServiceLoader.register(ShardingAlgorithm.class);
    }
    
    @Test
    public void assertConvert() {
        ShardingRuleConfiguration config = ShardingTableRuleStatementConverter.convert(createTableRuleSegment());
        assertEquals(1, config.getTables().size());
        ShardingTableRuleConfiguration tableRule = config.getTables().iterator().next();
        assertEquals("t_order", tableRule.getLogicTable());
        assertEquals("ds0,ds1", tableRule.getActualDataNodes());
        assertEquals("order_id_algorithm", tableRule.getDatabaseShardingStrategy().getShardingAlgorithmName());
        assertEquals("order_id_algorithm", tableRule.getTableShardingStrategy().getShardingAlgorithmName());
        assertEquals("t_order_snowflake", tableRule.getKeyGenerateStrategy().getKeyGeneratorName());
        assertEquals("order_id", tableRule.getKeyGenerateStrategy().getColumn());
        assertEquals(1, config.getAutoTables().size());
        ShardingAutoTableRuleConfiguration autoTableRule = config.getAutoTables().iterator().next();
        assertEquals("t_order", autoTableRule.getLogicTable());
        assertEquals("ds0,ds1", autoTableRule.getActualDataSources());
        assertEquals("t_order_MOD", autoTableRule.getShardingStrategy().getShardingAlgorithmName());
        assertEquals(1, config.getShardingAlgorithms().size());
        assertEquals("MOD", config.getShardingAlgorithms().get("t_order_MOD").getType());
        assertEquals("2", config.getShardingAlgorithms().get("t_order_MOD").getProps().get("sharding_count"));
        assertEquals(1, config.getKeyGenerators().size());
        assertEquals("snowflake", config.getKeyGenerators().get("t_order_snowflake").getType());
        assertEquals("", config.getKeyGenerators().get("t_order_snowflake").getProps().get(""));
    }
    
    private Collection<AbstractTableRuleSegment> createTableRuleSegment() {
        Collection<AbstractTableRuleSegment> result = new LinkedList<>();
        AutoTableRuleSegment autoTableRuleSegment = new AutoTableRuleSegment("t_order", Arrays.asList("ds0", "ds1"), "order_id",
                new AlgorithmSegment("MOD", newProperties("sharding_count", "2")), null);
        TableRuleSegment tableRuleSegment = new TableRuleSegment("t_order", Arrays.asList("ds0", "ds1"),
                new ShardingStrategySegment("standard", "order_id", "order_id_algorithm"),
                new ShardingStrategySegment("standard", "order_id", "order_id_algorithm"),
                new KeyGenerateSegment("order_id", new AlgorithmSegment("snowflake", newProperties("", ""))));
        result.add(autoTableRuleSegment);
        result.add(tableRuleSegment);
        return result;
    }
    
    private static Properties newProperties(final String key, final String value) {
        Properties properties = new Properties();
        properties.put(key, value);
        return properties;
    }
}
