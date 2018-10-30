/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.metadata.datasource;

import com.google.common.collect.Lists;
import io.shardingsphere.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithmType;
import io.shardingsphere.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.api.config.ShardingRuleConfiguration;
import io.shardingsphere.api.config.TableRuleConfiguration;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ShardingDataSourceMetaDataTest {
    
    private ShardingDataSourceMetaData masterSlaveShardingDataSourceMetaData;
    
    private ShardingDataSourceMetaData shardingDataSourceMetaData;
    
    @Before
    public void setUp() {
        Map<String, String> masterSlaveShardingDataSourceURLs = new LinkedHashMap<>();
        masterSlaveShardingDataSourceURLs.put("single", "jdbc:mysql://127.0.0.1:3306/single");
        masterSlaveShardingDataSourceURLs.put("master_0", "jdbc:mysql://127.0.0.1:3306/master_0");
        masterSlaveShardingDataSourceURLs.put("master_1", "jdbc:mysql://127.0.0.1:3306/master_1");
        masterSlaveShardingDataSourceURLs.put("master_2", "jdbc:mysql://127.0.0.1:3307/master_2");
        masterSlaveShardingDataSourceURLs.put("slave_0", "jdbc:mysql://127.0.0.2:3306/slave_0");
        masterSlaveShardingDataSourceURLs.put("slave_1", "jdbc:mysql://127.0.0.2:3306/slave_1");
        masterSlaveShardingDataSourceURLs.put("slave_2", "jdbc:mysql://127.0.0.2:3307/slave_2");
        masterSlaveShardingDataSourceMetaData = new ShardingDataSourceMetaData(masterSlaveShardingDataSourceURLs, getMasterSlaveShardingRule(), DatabaseType.MySQL);
        Map<String, String> shardingDataSourceURLs = new LinkedHashMap<>();
        shardingDataSourceURLs.put("ds_0", "jdbc:mysql://127.0.0.1:3306/db_0");
        shardingDataSourceURLs.put("ds_1", "jdbc:mysql://127.0.0.1:3306/db_1");
        shardingDataSourceMetaData = new ShardingDataSourceMetaData(shardingDataSourceURLs, getShardingRule(), DatabaseType.MySQL);
    }
    
    private ShardingRule getMasterSlaveShardingRule() {
        TableRuleConfiguration tableRuleConfig0 = new TableRuleConfiguration();
        tableRuleConfig0.setLogicTable("t_order");
        tableRuleConfig0.setActualDataNodes("ms_${0..2}.t_order_${0..1}");
        TableRuleConfiguration tableRuleConfig1 = new TableRuleConfiguration();
        tableRuleConfig1.setLogicTable("t_order_item");
        tableRuleConfig1.setActualDataNodes("single.t_order_item");
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        MasterSlaveRuleConfiguration masterSlaveConfig0 = new MasterSlaveRuleConfiguration(
                "ms_0", "master_0", Collections.singleton("slave_0"), MasterSlaveLoadBalanceAlgorithmType.ROUND_ROBIN.getAlgorithm());
        MasterSlaveRuleConfiguration masterSlaveConfig1 = new MasterSlaveRuleConfiguration(
                "ms_1", "master_1", Collections.singleton("slave_1"), MasterSlaveLoadBalanceAlgorithmType.ROUND_ROBIN.getAlgorithm());
        MasterSlaveRuleConfiguration masterSlaveConfig2 = new MasterSlaveRuleConfiguration(
                "ms_2", "master_2", Collections.singleton("slave_2"), MasterSlaveLoadBalanceAlgorithmType.ROUND_ROBIN.getAlgorithm());
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig0);
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig1);
        shardingRuleConfig.getMasterSlaveRuleConfigs().addAll(Lists.newArrayList(masterSlaveConfig0, masterSlaveConfig1, masterSlaveConfig2));
        return new ShardingRule(shardingRuleConfig, Lists.newArrayList("single", "master_0", "master_1", "master_2", "slave_0", "slave_1", "slave_2"));
    }
    
    private ShardingRule getShardingRule() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("t_order");
        tableRuleConfig.setActualDataNodes("ds_${0..1}.t_order_${0..1}");
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        return new ShardingRule(shardingRuleConfig, Lists.newArrayList("ds_0", "ds_1"));
    }
    
    @Test
    public void assertGetAllInstanceDataSourceNamesForMasterSlaveShardingRule() {
        assertEquals(masterSlaveShardingDataSourceMetaData.getAllInstanceDataSourceNames(), Lists.newArrayList("single", "ms_2"));
    }
    
    @Test
    public void assertGetAllInstanceDataSourceNamesForShardingRule() {
        assertEquals(shardingDataSourceMetaData.getAllInstanceDataSourceNames(), Lists.newArrayList("ds_0"));
    }
    
    @Test
    public void assertGetActualSchemaNameForMasterSlaveShardingRule() {
        assertEquals(masterSlaveShardingDataSourceMetaData.getActualDataSourceMetaData("ms_0").getSchemeName(), "master_0");
    }
    
    @Test
    public void assertGetActualSchemaNameForShardingRule() {
        assertEquals(shardingDataSourceMetaData.getActualDataSourceMetaData("ds_0").getSchemeName(), "db_0");
    }
}
