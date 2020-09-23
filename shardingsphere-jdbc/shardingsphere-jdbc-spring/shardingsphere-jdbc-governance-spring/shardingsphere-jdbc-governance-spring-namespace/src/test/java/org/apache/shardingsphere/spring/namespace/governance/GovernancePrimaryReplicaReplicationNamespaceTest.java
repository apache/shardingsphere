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

package org.apache.shardingsphere.spring.namespace.governance;

import org.apache.shardingsphere.driver.governance.internal.datasource.GovernanceShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.schema.SchemaContexts;
import org.apache.shardingsphere.replication.primaryreplica.algorithm.RandomReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.replication.primaryreplica.algorithm.RoundRobinReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.replication.primaryreplica.rule.PrimaryReplicaReplicationDataSourceRule;
import org.apache.shardingsphere.replication.primaryreplica.rule.PrimaryReplicaReplicationRule;
import org.apache.shardingsphere.replication.primaryreplica.spi.ReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.spring.namespace.governance.util.EmbedTestingServer;
import org.apache.shardingsphere.spring.namespace.governance.util.FieldValueUtil;
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

@ContextConfiguration(locations = "classpath:META-INF/rdb/primary-replica-replication-governance.xml")
public class GovernancePrimaryReplicaReplicationNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
    }
    
    @Test
    public void assertPrimaryReplicaReplicationDataSourceType() {
        assertNotNull(applicationContext.getBean("defaultMasterSlaveDataSourceGovernance", GovernanceShardingSphereDataSource.class));
    }
    
    @Test
    public void assertDefaultPrimaryReplicaReplicationDataSource() {
        PrimaryReplicaReplicationRule masterSlaveRule = getPrimaryReplicaReplicationRule("defaultMasterSlaveDataSourceGovernance");
        Optional<PrimaryReplicaReplicationDataSourceRule> masterSlaveDataSourceRule = masterSlaveRule.findDataSourceRule("default_dbtbl_0");
        assertTrue(masterSlaveDataSourceRule.isPresent());
        assertThat(masterSlaveDataSourceRule.get().getPrimaryDataSourceName(), is("dbtbl_primary_0"));
        assertTrue(masterSlaveDataSourceRule.get().getReplicaDataSourceNames().contains("dbtbl_0_replica_0"));
        assertTrue(masterSlaveDataSourceRule.get().getReplicaDataSourceNames().contains("dbtbl_0_replica_1"));
    }
    
    @Test
    public void assertTypePrimaryReplicaReplicationDataSource() {
        PrimaryReplicaReplicationRule randomSlaveRule = getPrimaryReplicaReplicationRule("randomMasterSlaveDataSourceGovernance");
        Optional<PrimaryReplicaReplicationDataSourceRule> randomMasterSlaveDataSourceRule = randomSlaveRule.findDataSourceRule("random_dbtbl_0");
        assertTrue(randomMasterSlaveDataSourceRule.isPresent());
        assertTrue(randomMasterSlaveDataSourceRule.get().getLoadBalancer() instanceof RandomReplicaLoadBalanceAlgorithm);
        PrimaryReplicaReplicationRule roundRobinSlaveRule = getPrimaryReplicaReplicationRule("roundRobinMasterSlaveDataSourceGovernance");
        Optional<PrimaryReplicaReplicationDataSourceRule> roundRobinMasterSlaveDataSourceRule = roundRobinSlaveRule.findDataSourceRule("roundRobin_dbtbl_0");
        assertTrue(roundRobinMasterSlaveDataSourceRule.isPresent());
        assertTrue(roundRobinMasterSlaveDataSourceRule.get().getLoadBalancer() instanceof RoundRobinReplicaLoadBalanceAlgorithm);
    }
    
    @Test
    @Ignore
    // TODO load balance algorithm have been construct twice for SpringMasterDatasource extends PrimaryReplicaReplicationDatasource.
    public void assertRefPrimaryReplicaReplicationDataSource() {
        ReplicaLoadBalanceAlgorithm randomLoadBalanceAlgorithm = applicationContext.getBean("randomLoadBalanceAlgorithm", ReplicaLoadBalanceAlgorithm.class);
        PrimaryReplicaReplicationRule masterSlaveRule = getPrimaryReplicaReplicationRule("refMasterSlaveDataSourceGovernance");
        Optional<PrimaryReplicaReplicationDataSourceRule> masterSlaveDataSourceRule = masterSlaveRule.findDataSourceRule("randomLoadBalanceAlgorithm");
        assertTrue(masterSlaveDataSourceRule.isPresent());
        assertThat(masterSlaveDataSourceRule.get().getLoadBalancer(), is(randomLoadBalanceAlgorithm));
    }
    
    private PrimaryReplicaReplicationRule getPrimaryReplicaReplicationRule(final String dataSourceName) {
        GovernanceShardingSphereDataSource masterSlaveDataSource = applicationContext.getBean(dataSourceName, GovernanceShardingSphereDataSource.class);
        SchemaContexts schemaContexts = (SchemaContexts) FieldValueUtil.getFieldValue(masterSlaveDataSource, "schemaContexts");
        return (PrimaryReplicaReplicationRule) schemaContexts.getDefaultSchemaContext().getSchema().getRules().iterator().next();
    }
    
    @Test
    public void assertProperties() {
        boolean showSQL = getProperties("defaultMasterSlaveDataSourceGovernance").getValue(ConfigurationPropertyKey.SQL_SHOW);
        assertTrue(showSQL);
    }
    
    private ConfigurationProperties getProperties(final String dataSourceName) {
        GovernanceShardingSphereDataSource masterSlaveDataSource = applicationContext.getBean(dataSourceName, GovernanceShardingSphereDataSource.class);
        SchemaContexts schemaContexts = (SchemaContexts) FieldValueUtil.getFieldValue(masterSlaveDataSource, "schemaContexts");
        return schemaContexts.getProps();
    }
}
