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
import org.apache.shardingsphere.infra.context.SchemaContexts;
import org.apache.shardingsphere.primaryreplica.algorithm.RandomPrimaryReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.primaryreplica.algorithm.RoundRobinPrimaryReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.primaryreplica.rule.PrimaryReplicaDataSourceRule;
import org.apache.shardingsphere.primaryreplica.rule.PrimaryReplicaRule;
import org.apache.shardingsphere.primaryreplica.spi.PrimaryReplicaLoadBalanceAlgorithm;
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

@ContextConfiguration(locations = "classpath:META-INF/rdb/primaryReplicaGovernance.xml")
public class GovernancePrimaryReplicaNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
    }
    
    @Test
    public void assertPrimaryReplicaDataSourceType() {
        assertNotNull(applicationContext.getBean("defaultPrimaryReplicaDataSourceGovernance", GovernanceShardingSphereDataSource.class));
    }
    
    @Test
    public void assertDefaultPrimaryReplicaDataSource() {
        PrimaryReplicaRule primaryReplicaRule = getPrimaryReplicaRule("defaultPrimaryReplicaDataSourceGovernance");
        Optional<PrimaryReplicaDataSourceRule> primaryReplicaDataSourceRule = primaryReplicaRule.findDataSourceRule("default_dbtbl_0");
        assertTrue(primaryReplicaDataSourceRule.isPresent());
        assertThat(primaryReplicaDataSourceRule.get().getPrimaryDataSourceName(), is("dbtbl_0_primary"));
        assertTrue(primaryReplicaDataSourceRule.get().getReplicaDataSourceNames().contains("dbtbl_0_replica_0"));
        assertTrue(primaryReplicaDataSourceRule.get().getReplicaDataSourceNames().contains("dbtbl_0_replica_1"));
    }
    
    @Test
    public void assertTypePrimaryReplicaDataSource() {
        PrimaryReplicaRule randomReplicaRule = getPrimaryReplicaRule("randomPrimaryReplicaDataSourceGovernance");
        Optional<PrimaryReplicaDataSourceRule> randomPrimaryReplicaDataSourceRule = randomReplicaRule.findDataSourceRule("random_dbtbl_0");
        assertTrue(randomPrimaryReplicaDataSourceRule.isPresent());
        assertTrue(randomPrimaryReplicaDataSourceRule.get().getLoadBalancer() instanceof RandomPrimaryReplicaLoadBalanceAlgorithm);
        PrimaryReplicaRule roundRobinReplicaRule = getPrimaryReplicaRule("roundRobinPrimaryReplicaDataSourceGovernance");
        Optional<PrimaryReplicaDataSourceRule> roundRobinPrimaryReplicaDataSourceRule = roundRobinReplicaRule.findDataSourceRule("roundRobin_dbtbl_0");
        assertTrue(roundRobinPrimaryReplicaDataSourceRule.isPresent());
        assertTrue(roundRobinPrimaryReplicaDataSourceRule.get().getLoadBalancer() instanceof RoundRobinPrimaryReplicaLoadBalanceAlgorithm);
    }
    
    @Test
    @Ignore
    // TODO load balance algorithm have been construct twice for SpringPrimaryDatasource extends PrimaryReplicaDatasource.
    public void assertRefPrimaryReplicaDataSource() {
        PrimaryReplicaLoadBalanceAlgorithm randomLoadBalanceAlgorithm = applicationContext.getBean("randomLoadBalanceAlgorithm", PrimaryReplicaLoadBalanceAlgorithm.class);
        PrimaryReplicaRule primaryReplicaRule = getPrimaryReplicaRule("refPrimaryReplicaDataSourceGovernance");
        Optional<PrimaryReplicaDataSourceRule> primaryReplicaDataSourceRule = primaryReplicaRule.findDataSourceRule("randomLoadBalanceAlgorithm");
        assertTrue(primaryReplicaDataSourceRule.isPresent());
        assertThat(primaryReplicaDataSourceRule.get().getLoadBalancer(), is(randomLoadBalanceAlgorithm));
    }
    
    private PrimaryReplicaRule getPrimaryReplicaRule(final String primaryReplicaDataSourceName) {
        GovernanceShardingSphereDataSource primaryReplicaDataSource = applicationContext.getBean(primaryReplicaDataSourceName, GovernanceShardingSphereDataSource.class);
        SchemaContexts schemaContexts = (SchemaContexts) FieldValueUtil.getFieldValue(primaryReplicaDataSource, "schemaContexts");
        return (PrimaryReplicaRule) schemaContexts.getDefaultSchemaContext().getSchema().getRules().iterator().next();
    }
    
    @Test
    public void assertProperties() {
        boolean showSQL = getProperties("defaultPrimaryReplicaDataSourceGovernance").getValue(ConfigurationPropertyKey.SQL_SHOW);
        assertTrue(showSQL);
    }
    
    private ConfigurationProperties getProperties(final String primaryReplicaDataSourceName) {
        GovernanceShardingSphereDataSource primaryReplicaDataSource = applicationContext.getBean(primaryReplicaDataSourceName, GovernanceShardingSphereDataSource.class);
        SchemaContexts schemaContexts = (SchemaContexts) FieldValueUtil.getFieldValue(primaryReplicaDataSource, "schemaContexts");
        return schemaContexts.getProps();
    }
}
