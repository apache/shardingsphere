/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.spring;

import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import io.shardingjdbc.core.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithm;
import io.shardingjdbc.core.api.algorithm.masterslave.RandomMasterSlaveLoadBalanceAlgorithm;
import io.shardingjdbc.core.api.algorithm.masterslave.RoundRobinMasterSlaveLoadBalanceAlgorithm;
import io.shardingjdbc.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import io.shardingjdbc.core.rule.MasterSlaveRule;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.spring.datasource.SpringMasterSlaveDataSource;
import io.shardingjdbc.spring.util.FieldValueUtil;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = "classpath:META-INF/rdb/masterSlaveNamespace.xml")
public class MasterSlaveNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @Test
    public void testDefaultMaserSlaveDataSource() {
        MasterSlaveRule masterSlaveRule = getMasterSlaveRule("defaultMasterSlaveDataSource");
        assertThat(masterSlaveRule.getMasterDataSourceName(), is("dbtbl_0_master"));
        assertNotNull(masterSlaveRule.getSlaveDataSourceMap().get("dbtbl_0_slave_0"));
        assertNotNull(masterSlaveRule.getSlaveDataSourceMap().get("dbtbl_0_slave_1"));
    }
    
    @Test
    public void testTypeMasterSlaveDataSource() {
        MasterSlaveRule randomSlaveRule = getMasterSlaveRule("randomMasterSlaveDataSource");
        MasterSlaveRule roundRobinSlaveRule = getMasterSlaveRule("roundRobinMasterSlaveDataSource");
        assertTrue(randomSlaveRule.getStrategy() instanceof RandomMasterSlaveLoadBalanceAlgorithm);
        assertTrue(roundRobinSlaveRule.getStrategy() instanceof RoundRobinMasterSlaveLoadBalanceAlgorithm);
    }
    
    @Test
    public void testRefMasterSlaveDataSource() {
        MasterSlaveLoadBalanceAlgorithm randomStrategy = this.applicationContext.getBean("randomStrategy", MasterSlaveLoadBalanceAlgorithm.class);
        MasterSlaveRule masterSlaveRule = getMasterSlaveRule("refMasterSlaveDataSource");
        assertTrue(masterSlaveRule.getStrategy() == randomStrategy);
    }
    
    @Test
    public void testDefaultShardingDataSource() {
        ShardingRule shardingRule = getShardingRule("defaultShardingDataSource");
        assertNotNull(shardingRule.getDataSourceMap().get("randomMasterSlaveDataSource"));
        assertNotNull(shardingRule.getDataSourceMap().get("refMasterSlaveDataSource"));
        assertThat(shardingRule.getDefaultDataSourceName(), is("randomMasterSlaveDataSource"));
        assertThat(shardingRule.getTableRules().size(), is(1));
        assertThat(shardingRule.getTableRules().iterator().next().getLogicTable(), is("t_order"));
    }
    
    @Test
    public void testShardingDataSourceType() {
        assertTrue(this.applicationContext.getBean("defaultMasterSlaveDataSource", MasterSlaveDataSource.class) instanceof SpringMasterSlaveDataSource);
    }
    
    private ShardingRule getShardingRule(final String shardingDataSourceName) {
        ShardingDataSource shardingDataSource = this.applicationContext.getBean(shardingDataSourceName, ShardingDataSource.class);
        Object shardingContext = FieldValueUtil.getFieldValue(shardingDataSource, "shardingContext", true);
        return (ShardingRule) FieldValueUtil.getFieldValue(shardingContext, "shardingRule");
    }
    
    private MasterSlaveRule getMasterSlaveRule(final String masterSlaveDataSourceName) {
        MasterSlaveDataSource masterSlaveDataSource = this.applicationContext.getBean(masterSlaveDataSourceName, MasterSlaveDataSource.class);
        return (MasterSlaveRule) FieldValueUtil.getFieldValue(masterSlaveDataSource, "masterSlaveRule", true);
    }
}
