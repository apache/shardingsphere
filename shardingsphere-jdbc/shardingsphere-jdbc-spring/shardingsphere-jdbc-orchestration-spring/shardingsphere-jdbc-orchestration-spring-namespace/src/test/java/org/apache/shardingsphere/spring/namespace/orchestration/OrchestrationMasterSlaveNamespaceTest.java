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

package org.apache.shardingsphere.spring.namespace.orchestration;

import org.apache.shardingsphere.driver.orchestration.internal.datasource.OrchestrationShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.kernel.context.SchemaContexts;
import org.apache.shardingsphere.masterslave.algorithm.RandomMasterSlaveLoadBalanceAlgorithm;
import org.apache.shardingsphere.masterslave.algorithm.RoundRobinMasterSlaveLoadBalanceAlgorithm;
import org.apache.shardingsphere.masterslave.rule.MasterSlaveDataSourceRule;
import org.apache.shardingsphere.masterslave.rule.MasterSlaveRule;
import org.apache.shardingsphere.masterslave.spi.MasterSlaveLoadBalanceAlgorithm;
import org.apache.shardingsphere.spring.namespace.orchestration.util.EmbedTestingServer;
import org.apache.shardingsphere.spring.namespace.orchestration.util.FieldValueUtil;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
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
        assertNotNull(applicationContext.getBean("defaultMasterSlaveDataSourceOrchestration", OrchestrationShardingSphereDataSource.class));
    }
    
    @Test
    public void assertDefaultMaserSlaveDataSource() {
        MasterSlaveRule masterSlaveRule = getMasterSlaveRule("defaultMasterSlaveDataSourceOrchestration");
        Optional<MasterSlaveDataSourceRule> masterSlaveDataSourceRule = masterSlaveRule.findDataSourceRule("default_dbtbl_0");
        assertTrue(masterSlaveDataSourceRule.isPresent());
        assertThat(masterSlaveDataSourceRule.get().getMasterDataSourceName(), is("dbtbl_0_master"));
        assertTrue(masterSlaveDataSourceRule.get().getSlaveDataSourceNames().contains("dbtbl_0_slave_0"));
        assertTrue(masterSlaveDataSourceRule.get().getSlaveDataSourceNames().contains("dbtbl_0_slave_1"));
    }
    
    @Test
    public void assertTypeMasterSlaveDataSource() {
        MasterSlaveRule randomSlaveRule = getMasterSlaveRule("randomMasterSlaveDataSourceOrchestration");
        Optional<MasterSlaveDataSourceRule> randomMasterSlaveDataSourceRule = randomSlaveRule.findDataSourceRule("random_dbtbl_0");
        assertTrue(randomMasterSlaveDataSourceRule.isPresent());
        assertTrue(randomMasterSlaveDataSourceRule.get().getLoadBalancer() instanceof RandomMasterSlaveLoadBalanceAlgorithm);
        MasterSlaveRule roundRobinSlaveRule = getMasterSlaveRule("roundRobinMasterSlaveDataSourceOrchestration");
        Optional<MasterSlaveDataSourceRule> roundRobinMasterSlaveDataSourceRule = roundRobinSlaveRule.findDataSourceRule("roundRobin_dbtbl_0");
        assertTrue(roundRobinMasterSlaveDataSourceRule.isPresent());
        assertTrue(roundRobinMasterSlaveDataSourceRule.get().getLoadBalancer() instanceof RoundRobinMasterSlaveLoadBalanceAlgorithm);
    }
    
    @Test
    @Ignore
    // TODO load balance algorithm have been construct twice for SpringMasterDatasource extends MasterSlaveDatasource.
    public void assertRefMasterSlaveDataSource() {
        MasterSlaveLoadBalanceAlgorithm randomLoadBalanceAlgorithm = applicationContext.getBean("randomLoadBalanceAlgorithm", MasterSlaveLoadBalanceAlgorithm.class);
        MasterSlaveRule masterSlaveRule = getMasterSlaveRule("refMasterSlaveDataSourceOrchestration");
        Optional<MasterSlaveDataSourceRule> masterSlaveDataSourceRule = masterSlaveRule.findDataSourceRule("randomLoadBalanceAlgorithm");
        assertTrue(masterSlaveDataSourceRule.isPresent());
        assertThat(masterSlaveDataSourceRule.get().getLoadBalancer(), is(randomLoadBalanceAlgorithm));
    }
    
    private MasterSlaveRule getMasterSlaveRule(final String masterSlaveDataSourceName) {
        OrchestrationShardingSphereDataSource masterSlaveDataSource = applicationContext.getBean(masterSlaveDataSourceName, OrchestrationShardingSphereDataSource.class);
        SchemaContexts schemaContexts = (SchemaContexts) FieldValueUtil.getFieldValue(masterSlaveDataSource, "schemaContexts");
        return (MasterSlaveRule) schemaContexts.getDefaultSchemaContext().getSchema().getRules().iterator().next();
    }
    
    @Test
    public void assertProperties() {
        boolean showSQL = getProperties("defaultMasterSlaveDataSourceOrchestration").getValue(ConfigurationPropertyKey.SQL_SHOW);
        assertTrue(showSQL);
    }
    
    private ConfigurationProperties getProperties(final String masterSlaveDataSourceName) {
        OrchestrationShardingSphereDataSource masterSlaveDataSource = applicationContext.getBean(masterSlaveDataSourceName, OrchestrationShardingSphereDataSource.class);
        SchemaContexts schemaContexts = (SchemaContexts) FieldValueUtil.getFieldValue(masterSlaveDataSource, "schemaContexts");
        return schemaContexts.getProps();
    }
}
