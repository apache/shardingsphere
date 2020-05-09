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

package org.apache.shardingsphere.shardingjdbc.spring;

import org.apache.shardingsphere.api.config.masterslave.LoadBalanceStrategyConfiguration;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.core.strategy.algorithm.masterslave.RandomMasterSlaveLoadBalanceAlgorithm;
import org.apache.shardingsphere.core.strategy.algorithm.masterslave.RoundRobinMasterSlaveLoadBalanceAlgorithm;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.apache.shardingsphere.spi.masterslave.MasterSlaveLoadBalanceAlgorithm;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = "classpath:META-INF/rdb/masterSlaveNamespace.xml")
public class MasterSlaveNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @Test
    public void assertDefaultMaserSlaveDataSource() {
        MasterSlaveRule masterSlaveRule = getMasterSlaveRule("defaultMasterSlaveDataSource");
        assertThat(masterSlaveRule.getMasterDataSourceName(), is("dbtbl_0_master"));
        assertTrue(masterSlaveRule.getSlaveDataSourceNames().contains("dbtbl_0_slave_0"));
        assertTrue(masterSlaveRule.getSlaveDataSourceNames().contains("dbtbl_0_slave_1"));
    }
    
    @Test
    public void assertTypeMasterSlaveDataSource() {
        MasterSlaveRule randomSlaveRule = getMasterSlaveRule("randomMasterSlaveDataSource");
        MasterSlaveRule roundRobinSlaveRule = getMasterSlaveRule("roundRobinMasterSlaveDataSource");
        assertTrue(randomSlaveRule.getLoadBalanceAlgorithm() instanceof RandomMasterSlaveLoadBalanceAlgorithm);
        assertTrue(roundRobinSlaveRule.getLoadBalanceAlgorithm() instanceof RoundRobinMasterSlaveLoadBalanceAlgorithm);
    }
    
    @Test
    public void assertRefMasterSlaveDataSource() {
        assertThat(applicationContext.getBean("randomLoadBalanceAlgorithm"), instanceOf(LoadBalanceStrategyConfiguration.class));
        MasterSlaveRule masterSlaveRule = getMasterSlaveRule("refMasterSlaveDataSource");
        assertThat(masterSlaveRule.getLoadBalanceAlgorithm(), instanceOf(MasterSlaveLoadBalanceAlgorithm.class));
    }
    
    private MasterSlaveRule getMasterSlaveRule(final String masterSlaveDataSourceName) {
        ShardingDataSource dataSource = applicationContext.getBean(masterSlaveDataSourceName, ShardingDataSource.class);
        return (MasterSlaveRule) dataSource.getRuntimeContext().getRules().iterator().next();
    }
}
