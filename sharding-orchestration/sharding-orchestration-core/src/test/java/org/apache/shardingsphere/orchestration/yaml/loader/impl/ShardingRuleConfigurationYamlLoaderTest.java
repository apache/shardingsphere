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

package org.apache.shardingsphere.orchestration.yaml.loader.impl;

import org.apache.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShardingRuleConfigurationYamlLoaderTest {
    
    private static final String SHARDING_RULE_YAML = "  tables:\n" + "    t_order:\n" + "      actualDataNodes: ds_${0..1}.t_order_${0..1}\n" + "      tableStrategy:\n" 
            + "        inline:\n" + "          shardingColumn: order_id\n" + "          algorithmExpression: t_order_${order_id % 2}\n" + "      keyGenerator:\n" 
            + "        column: order_id\n" + "    t_order_item:\n" + "      actualDataNodes: ds_${0..1}.t_order_item_${0..1}\n" + "      tableStrategy:\n" 
            + "        inline:\n" + "          shardingColumn: order_id\n" + "          algorithmExpression: t_order_item_${order_id % 2}\n" + "      keyGenerator:\n" 
            + "        column: order_item_id\n" + "  bindingTables:\n" + "    - t_order,t_order_item\n" + "  defaultDataSourceName: ds_1\n" 
            + "  defaultDatabaseStrategy:\n" + "    inline:\n" + "      shardingColumn: user_id\n" + "      algorithmExpression: ds_${user_id % 2}";
    
    @Test
    public void assertLoadShardingRuleConfiguration() {
        ShardingRuleConfiguration actual = new ShardingRuleConfigurationYamlLoader().load(SHARDING_RULE_YAML);
        assertThat(actual.getTableRuleConfigs().size(), is(2));
        assertThat(actual.getBindingTableGroups().size(), is(1));
        assertThat(actual.getDefaultDataSourceName(), is("ds_1"));
    }
}
