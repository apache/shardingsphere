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

package org.apache.shardingsphere.sharding.distsql.handler.converter;

import org.apache.shardingsphere.distsql.segment.AlgorithmSegment;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingAutoTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.AuditStrategySegment;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.KeyGenerateStrategySegment;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.ShardingAuditorSegment;
import org.apache.shardingsphere.sharding.distsql.segment.strategy.ShardingStrategySegment;
import org.apache.shardingsphere.sharding.distsql.segment.table.AbstractTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.segment.table.AutoTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.segment.table.TableRuleSegment;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ShardingTableRuleStatementConverterTest {
    
    @Test
    void assertConvert() {
        ShardingRuleConfiguration config = ShardingTableRuleStatementConverter.convert(createTableRuleSegment1());
        assertThat(config.getTables().size(), is(1));
        ShardingTableRuleConfiguration tableRule = config.getTables().iterator().next();
        assertThat(tableRule.getLogicTable(), is("t_order"));
        assertThat(tableRule.getActualDataNodes(), is("ds0,ds1"));
        assertThat(tableRule.getDatabaseShardingStrategy().getShardingAlgorithmName(), is("t_order_database_inline"));
        assertThat(tableRule.getTableShardingStrategy().getShardingAlgorithmName(), is("t_order_table_order_id_algorithm"));
        assertThat(tableRule.getKeyGenerateStrategy().getKeyGeneratorName(), is("t_order_snowflake"));
        assertThat(tableRule.getKeyGenerateStrategy().getColumn(), is("order_id"));
        assertThat(config.getAutoTables().size(), is(2));
        Iterator<ShardingAutoTableRuleConfiguration> autoTableConfigs = config.getAutoTables().iterator();
        ShardingAutoTableRuleConfiguration autoTableRule = autoTableConfigs.next();
        assertThat(autoTableRule.getLogicTable(), is("t_order"));
        assertThat(autoTableRule.getActualDataSources(), is("ds0,ds1"));
        assertThat(autoTableRule.getShardingStrategy().getShardingAlgorithmName(), is("t_order_mod"));
        assertThat(tableRule.getKeyGenerateStrategy().getKeyGeneratorName(), is("t_order_snowflake"));
        assertThat(tableRule.getKeyGenerateStrategy().getColumn(), is("order_id"));
        autoTableRule = autoTableConfigs.next();
        assertThat(autoTableRule.getKeyGenerateStrategy().getKeyGeneratorName(), is("t_order_2_snowflake"));
        assertThat(config.getShardingAlgorithms().size(), is(4));
        assertThat(config.getShardingAlgorithms().get("t_order_mod").getType(), is("mod"));
        assertThat(config.getShardingAlgorithms().get("t_order_mod").getProps().getProperty("sharding_count"), is("2"));
        assertThat(config.getKeyGenerators().size(), is(2));
        assertThat(config.getKeyGenerators().get("t_order_snowflake").getType(), is("snowflake"));
        assertThat(config.getKeyGenerators().get("t_order_snowflake").getProps().getProperty(""), is(""));
        assertThat(config.getAuditors().get("sharding_key_required_auditor").getType(), is("DML_SHARDING_CONDITIONS"));
    }
    
    @Test
    void assertConvertWithNoneStrategyType() {
        ShardingRuleConfiguration config = ShardingTableRuleStatementConverter.convert(createNoneStrategyTypeTableRuleSegment());
        assertThat(config.getTables().size(), is(1));
        ShardingTableRuleConfiguration tableRule = config.getTables().iterator().next();
        assertThat(tableRule.getLogicTable(), is("t_order"));
        assertThat(tableRule.getActualDataNodes(), is("ds0,ds1"));
        assertThat(tableRule.getDatabaseShardingStrategy().getType(), is(""));
        assertThat(tableRule.getKeyGenerateStrategy().getKeyGeneratorName(), is("t_order_snowflake"));
        assertThat(tableRule.getKeyGenerateStrategy().getColumn(), is("order_id"));
    }
    
    private Collection<AbstractTableRuleSegment> createNoneStrategyTypeTableRuleSegment() {
        TableRuleSegment tableRuleSegment = new TableRuleSegment("t_order", Arrays.asList("ds0", "ds1"),
                new KeyGenerateStrategySegment("order_id", new AlgorithmSegment("snowflake", PropertiesBuilder.build(new Property("", "")))),
                new AuditStrategySegment(Collections.singleton(new ShardingAuditorSegment("sharding_key_required_auditor",
                        new AlgorithmSegment("DML_SHARDING_CONDITIONS", new Properties()))), true));
        tableRuleSegment.setDatabaseStrategySegment(new ShardingStrategySegment("none", null, null));
        return Collections.singleton(tableRuleSegment);
    }
    
    private Collection<AbstractTableRuleSegment> createTableRuleSegment1() {
        AutoTableRuleSegment autoTableRuleSegment1 = new AutoTableRuleSegment("t_order", Arrays.asList("ds0", "ds1"));
        autoTableRuleSegment1.setShardingColumn("order_id");
        autoTableRuleSegment1.setShardingAlgorithmSegment(new AlgorithmSegment("MOD", PropertiesBuilder.build(new Property("sharding_count", "2"))));
        AutoTableRuleSegment autoTableRuleSegment2 = new AutoTableRuleSegment("t_order_2", Arrays.asList("ds0", "ds1"));
        autoTableRuleSegment2.setShardingColumn("order_id");
        autoTableRuleSegment2.setShardingAlgorithmSegment(new AlgorithmSegment("MOD", PropertiesBuilder.build(new Property("sharding_count", "2"))));
        autoTableRuleSegment2.setKeyGenerateStrategySegment(new KeyGenerateStrategySegment("order_id", new AlgorithmSegment("snowflake", new Properties())));
        autoTableRuleSegment2.setAuditStrategySegment(new AuditStrategySegment(Collections.singleton(new ShardingAuditorSegment("sharding_key_required_auditor",
                new AlgorithmSegment("DML_SHARDING_CONDITIONS", new Properties()))), true));
        TableRuleSegment tableRuleSegment = new TableRuleSegment("t_order", Arrays.asList("ds0", "ds1"),
                new KeyGenerateStrategySegment("order_id", new AlgorithmSegment("snowflake", PropertiesBuilder.build(new Property("", "")))),
                new AuditStrategySegment(Collections.singleton(new ShardingAuditorSegment("sharding_key_required_auditor",
                        new AlgorithmSegment("DML_SHARDING_CONDITIONS", new Properties()))), true));
        AlgorithmSegment databaseAlgorithmSegment = new AlgorithmSegment("inline", PropertiesBuilder.build(new Property("algorithm-expression", "ds_${product_id % 2}")));
        tableRuleSegment.setDatabaseStrategySegment(new ShardingStrategySegment("standard", "order_id", databaseAlgorithmSegment));
        tableRuleSegment.setTableStrategySegment(new ShardingStrategySegment("standard", "order_id", new AlgorithmSegment("order_id_algorithm", new Properties())));
        Collection<AbstractTableRuleSegment> result = new LinkedList<>();
        result.add(autoTableRuleSegment1);
        result.add(autoTableRuleSegment2);
        result.add(tableRuleSegment);
        return result;
    }
}
