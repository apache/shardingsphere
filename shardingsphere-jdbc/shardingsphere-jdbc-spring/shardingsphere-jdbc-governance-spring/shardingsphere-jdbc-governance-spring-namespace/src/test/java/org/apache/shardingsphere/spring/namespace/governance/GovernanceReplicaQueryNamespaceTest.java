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
import org.apache.shardingsphere.infra.context.schema.MetaDataContexts;
import org.apache.shardingsphere.replicaquery.algorithm.RandomReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.replicaquery.algorithm.RoundRobinReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.replicaquery.rule.ReplicaQueryDataSourceRule;
import org.apache.shardingsphere.replicaquery.rule.ReplicaQueryRule;
import org.apache.shardingsphere.replicaquery.spi.ReplicaLoadBalanceAlgorithm;
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

@ContextConfiguration(locations = "classpath:META-INF/rdb/replica-query-governance.xml")
public class GovernanceReplicaQueryNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
    }
    
    @Test
    public void assertReplicaQueryDataSourceType() {
        assertNotNull(applicationContext.getBean("defaultGovernanceDataSource", GovernanceShardingSphereDataSource.class));
    }
    
    @Test
    public void assertDefaultReplicaQueryDataSource() {
        ReplicaQueryRule rule = getReplicaQueryRule("defaultGovernanceDataSource");
        Optional<ReplicaQueryDataSourceRule> dataSourceRule = rule.findDataSourceRule("default_dbtbl_0");
        assertTrue(dataSourceRule.isPresent());
        assertThat(dataSourceRule.get().getPrimaryDataSourceName(), is("dbtbl_primary_0"));
        assertTrue(dataSourceRule.get().getReplicaDataSourceNames().contains("dbtbl_0_replica_0"));
        assertTrue(dataSourceRule.get().getReplicaDataSourceNames().contains("dbtbl_0_replica_1"));
    }
    
    @Test
    public void assertTypeReplicaQueryDataSource() {
        ReplicaQueryRule randomRule = getReplicaQueryRule("randomGovernanceDataSource");
        Optional<ReplicaQueryDataSourceRule> randomDataSourceRule = randomRule.findDataSourceRule("random_dbtbl_0");
        assertTrue(randomDataSourceRule.isPresent());
        assertTrue(randomDataSourceRule.get().getLoadBalancer() instanceof RandomReplicaLoadBalanceAlgorithm);
        ReplicaQueryRule roundRobinRule = getReplicaQueryRule("roundRobinGovernanceDataSource");
        Optional<ReplicaQueryDataSourceRule> roundRobinDataSourceRule = roundRobinRule.findDataSourceRule("roundRobin_dbtbl_0");
        assertTrue(roundRobinDataSourceRule.isPresent());
        assertTrue(roundRobinDataSourceRule.get().getLoadBalancer() instanceof RoundRobinReplicaLoadBalanceAlgorithm);
    }
    
    @Test
    @Ignore
    // TODO load balance algorithm have been construct twice for SpringDatasource extends ReplicaQueryDatasource.
    public void assertRefReplicaQueryDataSource() {
        ReplicaLoadBalanceAlgorithm randomLoadBalanceAlgorithm = applicationContext.getBean("randomLoadBalanceAlgorithm", ReplicaLoadBalanceAlgorithm.class);
        ReplicaQueryRule rule = getReplicaQueryRule("refGovernanceDataSource");
        Optional<ReplicaQueryDataSourceRule> dataSourceRule = rule.findDataSourceRule("randomLoadBalanceAlgorithm");
        assertTrue(dataSourceRule.isPresent());
        assertThat(dataSourceRule.get().getLoadBalancer(), is(randomLoadBalanceAlgorithm));
    }
    
    private ReplicaQueryRule getReplicaQueryRule(final String dataSourceName) {
        GovernanceShardingSphereDataSource dataSource = applicationContext.getBean(dataSourceName, GovernanceShardingSphereDataSource.class);
        MetaDataContexts metaDataContexts = (MetaDataContexts) FieldValueUtil.getFieldValue(dataSource, "metaDataContexts");
        return (ReplicaQueryRule) metaDataContexts.getDefaultMetaData().getRuleMetaData().getRules().iterator().next();
    }
    
    @Test
    public void assertProperties() {
        boolean showSQL = getProperties("defaultGovernanceDataSource").getValue(ConfigurationPropertyKey.SQL_SHOW);
        assertTrue(showSQL);
    }
    
    private ConfigurationProperties getProperties(final String dataSourceName) {
        GovernanceShardingSphereDataSource dataSource = applicationContext.getBean(dataSourceName, GovernanceShardingSphereDataSource.class);
        MetaDataContexts metaDataContexts = (MetaDataContexts) FieldValueUtil.getFieldValue(dataSource, "metaDataContexts");
        return metaDataContexts.getProps();
    }
}
