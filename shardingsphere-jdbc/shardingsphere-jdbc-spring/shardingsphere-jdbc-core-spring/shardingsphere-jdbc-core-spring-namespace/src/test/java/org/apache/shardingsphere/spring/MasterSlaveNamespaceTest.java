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

package org.apache.shardingsphere.spring;

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.masterslave.api.config.LoadBalanceStrategyConfiguration;
import org.apache.shardingsphere.masterslave.rule.MasterSlaveDataSourceRule;
import org.apache.shardingsphere.masterslave.rule.MasterSlaveRule;
import org.apache.shardingsphere.masterslave.spi.MasterSlaveLoadBalanceAlgorithm;
import org.apache.shardingsphere.masterslave.strategy.RandomMasterSlaveLoadBalanceAlgorithm;
import org.apache.shardingsphere.masterslave.strategy.RoundRobinMasterSlaveLoadBalanceAlgorithm;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = "classpath:META-INF/rdb/masterSlaveNamespace.xml")
public class MasterSlaveNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @Test
    public void assertDefaultMaserSlaveDataSource() {
        MasterSlaveRule masterSlaveRule = getMasterSlaveRule("defaultMasterSlaveDataSource");
        Optional<MasterSlaveDataSourceRule> masterSlaveDataSourceRule = masterSlaveRule.findDataSourceRule("default_dbtbl_0");
        assertTrue(masterSlaveDataSourceRule.isPresent());
        assertThat(masterSlaveDataSourceRule.get().getMasterDataSourceName(), is("dbtbl_0_master"));
        assertTrue(masterSlaveDataSourceRule.get().getSlaveDataSourceNames().contains("dbtbl_0_slave_0"));
        assertTrue(masterSlaveDataSourceRule.get().getSlaveDataSourceNames().contains("dbtbl_0_slave_1"));
    }
    
    @Test
    public void assertTypeMasterSlaveDataSource() {
        MasterSlaveRule randomSlaveRule = getMasterSlaveRule("randomMasterSlaveDataSource");
        Optional<MasterSlaveDataSourceRule> randomMasterSlaveDataSourceRule = randomSlaveRule.findDataSourceRule("random_dbtbl_0");
        assertTrue(randomMasterSlaveDataSourceRule.isPresent());
        assertTrue(randomMasterSlaveDataSourceRule.get().getLoadBalanceAlgorithm() instanceof RandomMasterSlaveLoadBalanceAlgorithm);
        MasterSlaveRule roundRobinSlaveRule = getMasterSlaveRule("roundRobinMasterSlaveDataSource");
        Optional<MasterSlaveDataSourceRule> roundRobinMasterSlaveDataSourceRule = roundRobinSlaveRule.findDataSourceRule("roundRobin_dbtbl_0");
        assertTrue(roundRobinMasterSlaveDataSourceRule.isPresent());
        assertTrue(roundRobinMasterSlaveDataSourceRule.get().getLoadBalanceAlgorithm() instanceof RoundRobinMasterSlaveLoadBalanceAlgorithm);
    }
    
    @Test
    public void assertRefMasterSlaveDataSource() {
        assertThat(applicationContext.getBean("randomLoadBalanceAlgorithm"), instanceOf(LoadBalanceStrategyConfiguration.class));
        MasterSlaveRule masterSlaveRule = getMasterSlaveRule("refMasterSlaveDataSource");
        Optional<MasterSlaveDataSourceRule> masterSlaveDataSourceRule = masterSlaveRule.findDataSourceRule("random_dbtbl_1");
        assertTrue(masterSlaveDataSourceRule.isPresent());
        assertThat(masterSlaveDataSourceRule.get().getLoadBalanceAlgorithm(), instanceOf(MasterSlaveLoadBalanceAlgorithm.class));
    }
    
    private MasterSlaveRule getMasterSlaveRule(final String masterSlaveDataSourceName) {
        ShardingSphereDataSource dataSource = applicationContext.getBean(masterSlaveDataSourceName, ShardingSphereDataSource.class);
        return (MasterSlaveRule) dataSource.getSchemaContexts().getDefaultSchemaContext().getSchema().getRules().iterator().next();
    }
}
