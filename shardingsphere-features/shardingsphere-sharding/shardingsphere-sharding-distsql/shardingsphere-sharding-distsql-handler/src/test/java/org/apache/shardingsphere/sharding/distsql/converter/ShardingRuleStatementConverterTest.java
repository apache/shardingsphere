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
import org.apache.shardingsphere.sharding.distsql.parser.segment.AuditStrategySegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.AutoTableRuleSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.KeyGenerateStrategySegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingAuditorSegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.ShardingStrategySegment;
import org.apache.shardingsphere.sharding.distsql.parser.segment.TableRuleSegment;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShardingRuleStatementConverterTest {
    
    @Test
    public void assertConvert() {
        ShardingRuleConfiguration config = ShardingTableRuleStatementConverter.convert(createTableRuleSegment1());
        assertThat(config.getTables().size(), is(1));
        ShardingTableRuleConfiguration tableRule = config.getTables().iterator().next();
        assertThat(tableRule.getLogicTable(), is("t_order"));
        assertThat(tableRule.getActualDataNodes(), is("ds0,ds1"));
        assertThat(tableRule.getDatabaseShardingStrategy().getShardingAlgorithmName(), is("t_order_database_inline"));
        assertThat(tableRule.getTableShardingStrategy().getShardingAlgorithmName(), is("order_id_algorithm"));
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
        assertThat(autoTableRule.getKeyGenerateStrategy().getKeyGeneratorName(), is("snowflake_algorithm"));
        assertThat(config.getShardingAlgorithms().size(), is(3));
        assertThat(config.getShardingAlgorithms().get("t_order_mod").getType(), is("mod"));
        assertThat(config.getShardingAlgorithms().get("t_order_mod").getProps().getProperty("sharding_count"), is("2"));
        assertThat(config.getKeyGenerators().size(), is(1));
        assertThat(config.getKeyGenerators().get("t_order_snowflake").getType(), is("snowflake"));
        assertThat(config.getKeyGenerators().get("t_order_snowflake").getProps().getProperty(""), is(""));
        assertThat(config.getAuditors().get("sharding_key_required_auditor").getType(), is("DML_SHARDING_CONDITIONS"));
    }
    
    private Collection<AbstractTableRuleSegment> createTableRuleSegment1() {
        Collection<AbstractTableRuleSegment> result = new LinkedList<>();
        AutoTableRuleSegment autoTableRuleSegment1 = new AutoTableRuleSegment("t_order", Arrays.asList("ds0", "ds1"), "order_id",
                new AlgorithmSegment("MOD", newProperties("sharding_count", "2")), null, null);
        AlgorithmSegment databaseAlgorithmSegment = getAutoCreativeAlgorithmSegment("inline", newProperties("algorithm-expression", "ds_${product_id% 2}"));
        AutoTableRuleSegment autoTableRuleSegment2 = new AutoTableRuleSegment("t_order_2", Arrays.asList("ds0", "ds1"), "order_id",
                new AlgorithmSegment("MOD", newProperties("sharding_count", "2")),
                new KeyGenerateStrategySegment("order_id", "snowflake_algorithm"),
                new AuditStrategySegment(Arrays.asList("sharding_key_required_auditor"), Arrays.asList(new ShardingAuditorSegment("sharding_key_required_auditor",
                        new AlgorithmSegment("DML_SHARDING_CONDITIONS", new Properties()))), true));
        TableRuleSegment tableRuleSegment = new TableRuleSegment("t_order", Arrays.asList("ds0", "ds1"),
                new ShardingStrategySegment("standard", "order_id", null, databaseAlgorithmSegment),
                new ShardingStrategySegment("standard", "order_id", "order_id_algorithm", null),
                new KeyGenerateStrategySegment("order_id", new AlgorithmSegment("snowflake", newProperties("", ""))),
                new AuditStrategySegment(Arrays.asList("sharding_key_required_auditor"), Arrays.asList(new ShardingAuditorSegment("sharding_key_required_auditor",
                        new AlgorithmSegment("DML_SHARDING_CONDITIONS", new Properties()))), true));
        result.add(autoTableRuleSegment1);
        result.add(autoTableRuleSegment2);
        result.add(tableRuleSegment);
        return result;
    }
    
    private AlgorithmSegment getAutoCreativeAlgorithmSegment(final String name, final Properties props) {
        return new AlgorithmSegment(name, props);
    }
    
    private Properties newProperties(final String key, final String value) {
        Properties result = new Properties();
        result.put(key, value);
        return result;
    }
}
