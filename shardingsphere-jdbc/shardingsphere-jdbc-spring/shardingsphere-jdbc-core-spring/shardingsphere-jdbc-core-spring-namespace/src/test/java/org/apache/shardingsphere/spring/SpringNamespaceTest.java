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
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.replication.primaryreplica.rule.PrimaryReplicaReplicationRule;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.spring.transaction.ShardingTransactionTypeScanner;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = "classpath:META-INF/spring/application-context.xml")
public final class SpringNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @Resource
    private ShardingSphereDataSource dataSource;
    
    @Test
    public void assertShardingSphereDataSource() {
        assertDataSourceMap();
        Collection<ShardingSphereRule> rules = dataSource.getSchemaContexts().getDefaultSchemaContext().getSchema().getRules();
        assertThat(rules.size(), is(3));
        for (ShardingSphereRule each : rules) {
            if (each instanceof ShardingRule) {
                assertShardingRule((ShardingRule) each);
            } else if (each instanceof PrimaryReplicaReplicationRule) {
                assertPrimaryReplicaReplicationRule((PrimaryReplicaReplicationRule) each);
            } else if (each instanceof EncryptRule) {
                assertEncryptRule((EncryptRule) each);
            }
        }
    }
    
    private void assertDataSourceMap() {
        assertThat(dataSource.getDataSourceMap().size(), is(6));
        assertTrue(dataSource.getDataSourceMap().containsKey("ds_0_primary"));
        assertTrue(dataSource.getDataSourceMap().containsKey("ds_0_replica_0"));
        assertTrue(dataSource.getDataSourceMap().containsKey("ds_0_replica_1"));
        assertTrue(dataSource.getDataSourceMap().containsKey("ds_1_primary"));
        assertTrue(dataSource.getDataSourceMap().containsKey("ds_1_replica_0"));
        assertTrue(dataSource.getDataSourceMap().containsKey("ds_1_replica_1"));
    }
    
    private void assertShardingRule(final ShardingRule rule) {
        assertThat(rule.getTableRules().size(), is(1));
        assertThat(rule.getTableRule("t_order").getActualDataNodes(), is(Arrays.asList(
                new DataNode("ds_0.t_order_0"), new DataNode("ds_0.t_order_1"), new DataNode("ds_0.t_order_2"), new DataNode("ds_0.t_order_3"),
                new DataNode("ds_1.t_order_0"), new DataNode("ds_1.t_order_1"), new DataNode("ds_1.t_order_2"), new DataNode("ds_1.t_order_3"))));
        assertThat(rule.getTableRule("t_order").getDatabaseShardingStrategy().getShardingAlgorithm().getProps().getProperty("algorithm-expression"), is("ds_$->{user_id % 2}"));
        assertThat(rule.getTableRule("t_order").getTableShardingStrategy().getShardingAlgorithm().getProps().getProperty("algorithm-expression"), is("t_order_$->{order_id % 4}"));
    }
    
    private void assertPrimaryReplicaReplicationRule(final PrimaryReplicaReplicationRule rule) {
        assertTrue(rule.findDataSourceRule("ds_0").isPresent());
        assertThat(rule.findDataSourceRule("ds_0").get().getPrimaryDataSourceName(), is("ds_0_primary"));
        assertThat(rule.findDataSourceRule("ds_0").get().getReplicaDataSourceNames(), is(Arrays.asList("ds_0_replica_0", "ds_0_replica_1")));
        assertTrue(rule.findDataSourceRule("ds_1").isPresent());
        assertThat(rule.findDataSourceRule("ds_1").get().getPrimaryDataSourceName(), is("ds_1_primary"));
        assertThat(rule.findDataSourceRule("ds_1").get().getReplicaDataSourceNames(), is(Arrays.asList("ds_1_replica_0", "ds_1_replica_1")));
    }
    
    private void assertEncryptRule(final EncryptRule rule) {
        assertThat(rule.getCipherColumn("t_order", "pwd"), is("pwd_cipher"));
        assertTrue(rule.findEncryptor("t_order", "pwd").isPresent());
        assertThat(rule.findEncryptor("t_order", "pwd").get().getProps().getProperty("aes-key-value"), is("123456"));
    }
    
    @Test
    public void assertShardingTransactionTypeScanner() {
        assertNotNull(applicationContext.getBean(ShardingTransactionTypeScanner.class));
    }
}
