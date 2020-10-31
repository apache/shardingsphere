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

import org.apache.shardingsphere.infra.binder.statement.rdl.CreateShardingRuleStatementContext;
import org.apache.shardingsphere.distsql.parser.statement.rdl.CreateShardingRuleStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.TableRuleSegment;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class CreateShardingRuleStatementContextConverterTest {
    
    private TableRuleSegment segment;
    
    private CreateShardingRuleStatementContext context;
    
    @Before
    public void setUp() {
        ShardingSphereServiceLoader.register(ShardingAlgorithm.class);
        segment = new TableRuleSegment();
        segment.setLogicTable("t_order");
        segment.setDataSources(Arrays.asList("ds0", "ds1"));
        segment.setShardingColumn("order_id");
        segment.setAlgorithmType("MOD");
        segment.setProperties(Collections.singleton("2"));
        context = new CreateShardingRuleStatementContext(new CreateShardingRuleStatement(Collections.singleton(segment)));
    }
    
    @Test
    public void generate() {
        YamlShardingRuleConfiguration rule = new CreateShardingRuleStatementContextConverter().convert(context);
        assertTrue(rule.getTables().isEmpty());
        assertThat(rule.getAutoTables().size(), is(1));
        assertThat(rule.getAutoTables().get(segment.getLogicTable()).getActualDataSources(), is("ds0,ds1"));
        assertThat(rule.getAutoTables().get(segment.getLogicTable()).getShardingStrategy().getStandard().getShardingColumn(), is("order_id"));
        assertThat(rule.getAutoTables().get(segment.getLogicTable()).getShardingStrategy().getStandard().getShardingAlgorithmName(), is("t_order_MOD"));
        assertTrue(rule.getShardingAlgorithms().containsKey("t_order_MOD"));
        assertThat(rule.getShardingAlgorithms().get("t_order_MOD").getType(), is("MOD"));
    }
}
