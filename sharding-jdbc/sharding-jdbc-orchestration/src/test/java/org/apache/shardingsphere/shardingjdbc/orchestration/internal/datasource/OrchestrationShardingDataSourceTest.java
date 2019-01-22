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

package org.apache.shardingsphere.shardingjdbc.orchestration.internal.datasource;

import lombok.SneakyThrows;
import org.apache.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.rule.TableRuleConfiguration;
import org.apache.shardingsphere.core.constant.ShardingConstant;
import org.apache.shardingsphere.orchestration.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.internal.registry.config.event.ShardingRuleChangedEvent;
import org.apache.shardingsphere.orchestration.reg.api.RegistryCenterConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.yaml.YamlShardingDataSourceFactory;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.user.YamlUserTest;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class OrchestrationShardingDataSourceTest {
    
    private OrchestrationShardingDataSource shardingDataSource;
    
    @Before
    @SneakyThrows
    public void setUp() {
        shardingDataSource = new OrchestrationShardingDataSource(getShardingDataSource(), getOrchestrationConfiguration());
    }
    
    @SneakyThrows
    private ShardingDataSource getShardingDataSource() {
        File yamlFile = new File(YamlUserTest.class.getResource("/yaml/unit/sharding.yaml").toURI());
        return (ShardingDataSource) YamlShardingDataSourceFactory.createDataSource(yamlFile);
    }
    
    private OrchestrationConfiguration getOrchestrationConfiguration() {
        RegistryCenterConfiguration registryCenterConfiguration = new RegistryCenterConfiguration();
        registryCenterConfiguration.setNamespace("test_sharding");
        registryCenterConfiguration.setServerLists("localhost:2181");
        return new OrchestrationConfiguration("test", registryCenterConfiguration, true);
    }
    
    @Test
    public void assertRenewRule() {
        shardingDataSource.renew(new ShardingRuleChangedEvent(ShardingConstant.LOGIC_SCHEMA_NAME, getShardingRuleConfig()));
        assertThat(shardingDataSource.getDataSource().getShardingContext().getShardingRule().getTableRules().size(), is(1));
    }
    
    private ShardingRuleConfiguration getShardingRuleConfig() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration();
        tableRuleConfig.setLogicTable("logic_table");
        tableRuleConfig.setActualDataNodes("ds_0.table_${0..1}");
        result.getTableRuleConfigs().add(tableRuleConfig);
        return result;
    }
    
    @Test
    public void testRenew1() {
    }
    
    @Test
    public void testRenew2() {
    }
    
    @Test
    public void testRenew3() {
    }
    
    @Test
    public void testRenew4() {
    }
    
    @Test
    public void testGetDataSource() {
    }
}
