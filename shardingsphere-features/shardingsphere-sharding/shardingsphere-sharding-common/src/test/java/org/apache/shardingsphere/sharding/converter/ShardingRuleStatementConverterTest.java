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

package org.apache.shardingsphere.sharding.converter;

import org.apache.shardingsphere.distsql.parser.segment.FunctionSegment;
import org.apache.shardingsphere.distsql.parser.segment.TableRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateShardingTableRuleStatement;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ShardingRuleStatementConverterTest {
    
    private TableRuleSegment segment;
    
    private CreateShardingTableRuleStatement sqlStatement;
    
    @Before
    public void setUp() {
        ShardingSphereServiceLoader.register(ShardingAlgorithm.class);
        segment = new TableRuleSegment();
        segment.setLogicTable("t_order");
        segment.setDataSources(Arrays.asList("ds0", "ds1"));
        segment.setTableStrategyColumn("order_id");
        FunctionSegment functionSegment = new FunctionSegment();
        functionSegment.setAlgorithmName("MOD");
        Properties props = new Properties();
        props.setProperty("sharding_count", "2");
        functionSegment.setAlgorithmProps(props);
        segment.setTableStrategy(functionSegment);
        sqlStatement = new CreateShardingTableRuleStatement(Collections.singleton(segment));
    }
    
    @Test
    public void assertConvert() {
        YamlShardingRuleConfiguration config = ShardingRuleStatementConverter.convert(sqlStatement);
        assertTrue(config.getTables().isEmpty());
        assertThat(config.getAutoTables().size(), is(1));
        assertThat(config.getAutoTables().get(segment.getLogicTable()).getActualDataSources(), is("ds0,ds1"));
        assertThat(config.getAutoTables().get(segment.getLogicTable()).getShardingStrategy().getStandard().getShardingColumn(), is("order_id"));
        assertThat(config.getAutoTables().get(segment.getLogicTable()).getShardingStrategy().getStandard().getShardingAlgorithmName(), is("t_order_MOD"));
        assertTrue(config.getShardingAlgorithms().containsKey("t_order_MOD"));
        assertThat(config.getShardingAlgorithms().get("t_order_MOD").getType(), is("MOD"));
    }
}
