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

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.mode.manager.ContextManager;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.readwritesplitting.algorithm.RandomReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.readwritesplitting.algorithm.RoundRobinReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceRule;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.readwritesplitting.spi.ReplicaLoadBalanceAlgorithm;
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

@ContextConfiguration(locations = "classpath:META-INF/rdb/readwrite-splitting-cluster.xml")
public class ClusterReadwriteSplittingNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
    }
    
    @Test
    public void assertReadwriteSplittingDataSourceType() {
        assertNotNull(applicationContext.getBean("defaultGovernanceDataSource", ShardingSphereDataSource.class));
    }
    
    @Test
    public void assertReadwriteSplittingDataSource() {
        Optional<ReadwriteSplittingRule> rule = getReadwriteSplittingRule("defaultGovernanceDataSource");
        assertTrue(rule.isPresent());
        Optional<ReadwriteSplittingDataSourceRule> dataSourceRule = rule.get().findDataSourceRule("default_dbtbl_0");
        assertTrue(dataSourceRule.isPresent());
        assertThat(dataSourceRule.get().getWriteDataSourceName(), is("dbtbl_write_0"));
        assertTrue(dataSourceRule.get().getReadDataSourceNames().contains("dbtbl_0_read_0"));
        assertTrue(dataSourceRule.get().getReadDataSourceNames().contains("dbtbl_0_read_1"));
    }
    
    @Test
    public void assertTypeReadwriteSplittingDataSource() {
        Optional<ReadwriteSplittingRule> randomRule = getReadwriteSplittingRule("randomGovernanceDataSource");
        assertTrue(randomRule.isPresent());
        Optional<ReadwriteSplittingDataSourceRule> randomDataSourceRule = randomRule.get().findDataSourceRule("random_dbtbl_0");
        assertTrue(randomDataSourceRule.isPresent());
        assertTrue(randomDataSourceRule.get().getLoadBalancer() instanceof RandomReplicaLoadBalanceAlgorithm);
        Optional<ReadwriteSplittingRule> roundRobinRule = getReadwriteSplittingRule("roundRobinGovernanceDataSource");
        assertTrue(roundRobinRule.isPresent());
        Optional<ReadwriteSplittingDataSourceRule> roundRobinDataSourceRule = roundRobinRule.get().findDataSourceRule("roundRobin_dbtbl_0");
        assertTrue(roundRobinDataSourceRule.isPresent());
        assertTrue(roundRobinDataSourceRule.get().getLoadBalancer() instanceof RoundRobinReplicaLoadBalanceAlgorithm);
    }
    
    @Test
    @Ignore
    // TODO load balance algorithm have been construct twice for SpringDatasource extends ReplicaQueryDatasource.
    public void assertRefReadwriteSplittingDataSource() {
        ReplicaLoadBalanceAlgorithm randomLoadBalanceAlgorithm = applicationContext.getBean("randomLoadBalanceAlgorithm", ReplicaLoadBalanceAlgorithm.class);
        Optional<ReadwriteSplittingRule> rule = getReadwriteSplittingRule("refGovernanceDataSource");
        assertTrue(rule.isPresent());
        Optional<ReadwriteSplittingDataSourceRule> dataSourceRule = rule.get().findDataSourceRule("randomLoadBalanceAlgorithm");
        assertTrue(dataSourceRule.isPresent());
        assertThat(dataSourceRule.get().getLoadBalancer(), is(randomLoadBalanceAlgorithm));
    }
    
    private Optional<ReadwriteSplittingRule> getReadwriteSplittingRule(final String dataSourceName) {
        ShardingSphereDataSource dataSource = applicationContext.getBean(dataSourceName, ShardingSphereDataSource.class);
        ContextManager contextManager = (ContextManager) FieldValueUtil.getFieldValue(dataSource, "contextManager");
        return contextManager.getMetaDataContexts().getMetaData(DefaultSchema.LOGIC_NAME).getRuleMetaData().getRules().stream().filter(each
            -> each instanceof ReadwriteSplittingRule).map(each -> (ReadwriteSplittingRule) each).findFirst();
    }
    
    @Test
    public void assertProperties() {
        boolean showSQL = getProperties("defaultGovernanceDataSource").getValue(ConfigurationPropertyKey.SQL_SHOW);
        assertTrue(showSQL);
    }
    
    private ConfigurationProperties getProperties(final String dataSourceName) {
        ShardingSphereDataSource dataSource = applicationContext.getBean(dataSourceName, ShardingSphereDataSource.class);
        ContextManager contextManager = (ContextManager) FieldValueUtil.getFieldValue(dataSource, "contextManager");
        return contextManager.getMetaDataContexts().getProps();
    }
}
