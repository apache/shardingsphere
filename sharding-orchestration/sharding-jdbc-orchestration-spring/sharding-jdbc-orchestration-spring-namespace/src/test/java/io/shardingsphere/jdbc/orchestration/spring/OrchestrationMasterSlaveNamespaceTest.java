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

package io.shardingsphere.jdbc.orchestration.spring;

import io.shardingsphere.core.api.ConfigMapContext;
import io.shardingsphere.core.api.algorithm.masterslave.MasterSlaveLoadBalanceAlgorithm;
import io.shardingsphere.core.api.algorithm.masterslave.RandomMasterSlaveLoadBalanceAlgorithm;
import io.shardingsphere.core.api.algorithm.masterslave.RoundRobinMasterSlaveLoadBalanceAlgorithm;
import io.shardingsphere.core.constant.properties.ShardingProperties;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.core.jdbc.core.datasource.MasterSlaveDataSource;
import io.shardingsphere.core.rule.MasterSlaveRule;
import io.shardingsphere.jdbc.orchestration.internal.datasource.OrchestrationMasterSlaveDataSource;
import io.shardingsphere.jdbc.orchestration.spring.datasource.OrchestrationSpringMasterSlaveDataSource;
import io.shardingsphere.jdbc.orchestration.spring.util.EmbedTestingServer;
import io.shardingsphere.jdbc.orchestration.spring.util.FieldValueUtil;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = "classpath:META-INF/rdb/masterSlaveOrchestration.xml")
public class OrchestrationMasterSlaveNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
    }
    
    @Test
    public void assertMasterSlaveDataSourceType() {
        assertTrue(null != applicationContext.getBean("defaultMasterSlaveDataSourceOrchestration", OrchestrationSpringMasterSlaveDataSource.class));
    }
    
    @Test
    public void assertDefaultMaserSlaveDataSource() {
        MasterSlaveRule masterSlaveRule = getMasterSlaveRule("defaultMasterSlaveDataSourceOrchestration");
        assertThat(masterSlaveRule.getMasterDataSourceName(), is("dbtbl_0_master"));
        assertTrue(masterSlaveRule.getSlaveDataSourceNames().contains("dbtbl_0_slave_0"));
        assertTrue(masterSlaveRule.getSlaveDataSourceNames().contains("dbtbl_0_slave_1"));
    }
    
    @Test
    public void assertTypeMasterSlaveDataSource() {
        MasterSlaveRule randomSlaveRule = getMasterSlaveRule("randomMasterSlaveDataSourceOrchestration");
        MasterSlaveRule roundRobinSlaveRule = getMasterSlaveRule("roundRobinMasterSlaveDataSourceOrchestration");
        assertTrue(randomSlaveRule.getLoadBalanceAlgorithm() instanceof RandomMasterSlaveLoadBalanceAlgorithm);
        assertTrue(roundRobinSlaveRule.getLoadBalanceAlgorithm() instanceof RoundRobinMasterSlaveLoadBalanceAlgorithm);
    }
    
    @Test
    public void assertRefMasterSlaveDataSource() {
        MasterSlaveLoadBalanceAlgorithm randomStrategy = this.applicationContext.getBean("randomStrategy", MasterSlaveLoadBalanceAlgorithm.class);
        MasterSlaveRule masterSlaveRule = getMasterSlaveRule("refMasterSlaveDataSourceOrchestration");
        assertTrue(EqualsBuilder.reflectionEquals(masterSlaveRule.getLoadBalanceAlgorithm(), randomStrategy));
    }
    
    private MasterSlaveRule getMasterSlaveRule(final String masterSlaveDataSourceName) {
        OrchestrationMasterSlaveDataSource masterSlaveDataSource = this.applicationContext.getBean(masterSlaveDataSourceName, OrchestrationMasterSlaveDataSource.class);
        MasterSlaveDataSource dataSource = (MasterSlaveDataSource) FieldValueUtil.getFieldValue(masterSlaveDataSource, "dataSource", true);
        return dataSource.getMasterSlaveRule();
    }
    
    @Test
    public void assertConfigMapDataSource() {
        Object masterSlaveDataSource = this.applicationContext.getBean("configMapDataSourceOrchestration");
        Map<String, Object> configMap = new HashMap<>();
        configMap.put("key1", "value1");
        assertThat(ConfigMapContext.getInstance().getMasterSlaveConfig(), is(configMap));
        assertThat(masterSlaveDataSource, instanceOf(OrchestrationMasterSlaveDataSource.class));
    }
    
    @Test
    public void assertProperties() {
        boolean showSQL = getShardingProperties("defaultMasterSlaveDataSourceOrchestration").getValue(ShardingPropertiesConstant.SQL_SHOW);
        assertTrue(showSQL);
    }
    
    private ShardingProperties getShardingProperties(final String masterSlaveDataSourceName) {
        OrchestrationSpringMasterSlaveDataSource masterSlaveDataSource = this.applicationContext.getBean(masterSlaveDataSourceName, OrchestrationSpringMasterSlaveDataSource.class);
        MasterSlaveDataSource dataSource = (MasterSlaveDataSource) FieldValueUtil.getFieldValue(masterSlaveDataSource, "dataSource", true);
        return dataSource.getShardingProperties();
    }
}
