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

package org.apache.shardingsphere.spring.boot;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodeUtil;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.replicaquery.algorithm.RandomReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.replicaquery.rule.ReplicaQueryDataSourceRule;
import org.apache.shardingsphere.replicaquery.rule.ReplicaQueryRule;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineShardingAlgorithm;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringBootStarterTest.class)
@SpringBootApplication
@ActiveProfiles("common")
public class SpringBootStarterTest {
    
    @Resource
    private ShardingSphereDataSource dataSource;
    
    @Test
    public void assertDataSourceMap() {
        assertThat(dataSource.getDataSourceMap().size(), is(2));
        assertTrue(dataSource.getDataSourceMap().containsKey("ds_0"));
        assertTrue(dataSource.getDataSourceMap().containsKey("ds_1"));
    }
    
    @Test
    public void assertRules() {
        Collection<ShardingSphereRule> rules = dataSource.getSchemaContexts().getDefaultMetaData().getRuleMetaData().getRules();
        assertThat(rules.size(), is(4));
        for (ShardingSphereRule each : rules) {
            if (each instanceof ShardingRule) {
                assertShardingRule((ShardingRule) each);
            } else if (each instanceof ReplicaQueryRule) {
                assertReplicaQueryRule((ReplicaQueryRule) each);
            } else if (each instanceof EncryptRule) {
                assertEncryptRule((EncryptRule) each);
            } else if (each instanceof ShadowRule) {
                assertShadowRule((ShadowRule) each);
            }
        }
    }
    
    private void assertShardingRule(final ShardingRule rule) {
        assertThat(rule.getDataSourceNames(), is(Sets.newHashSet("ds_0", "ds_1")));
        Map<String, ShardingAlgorithm> shardingAlgorithmMap = rule.getShardingAlgorithms();
        assertNotNull(shardingAlgorithmMap);
        InlineShardingAlgorithm databaseShardingAlgorithm = (InlineShardingAlgorithm) shardingAlgorithmMap.get("databaseShardingAlgorithm");
        assertThat(databaseShardingAlgorithm.getProps().getProperty("algorithm-expression"), is("ds_$->{user_id % 2}"));
        InlineShardingAlgorithm orderTableShardingAlgorithm = (InlineShardingAlgorithm) shardingAlgorithmMap.get("orderTableShardingAlgorithm");
        assertThat(orderTableShardingAlgorithm.getProps().getProperty("algorithm-expression"), is("t_order_$->{order_id % 2}"));
        Collection<TableRule> tableRules = rule.getTableRules();
        assertNotNull(tableRules);
        assertThat(tableRules.size(), is(1));
        TableRule tableRule = tableRules.iterator().next();
        assertThat(tableRule.getLogicTable(), is("t_order"));
        List<DataNode> dataNodes = Arrays.asList(new DataNode("ds_0.t_order_0"), new DataNode("ds_0.t_order_1"), new DataNode("ds_1.t_order_0"), new DataNode("ds_1.t_order_1"));
        assertThat(tableRule.getActualDataNodes(), is(dataNodes));
        assertThat(tableRule.getActualDatasourceNames(), is(Sets.newHashSet("ds_0", "ds_1")));
        assertThat(tableRule.getDataNodeGroups(), is(DataNodeUtil.getDataNodeGroups(dataNodes)));
        assertThat(tableRule.getDatasourceToTablesMap(), is(ImmutableMap.of("ds_1", Sets.newHashSet("t_order_0", "t_order_1"), "ds_0", Sets.newHashSet("t_order_0", "t_order_1"))));
    }
    
    private void assertReplicaQueryRule(final ReplicaQueryRule rule) {
        assertThat(rule.getDataSourceMapper(), is(Collections.singletonMap("pr_ds", Arrays.asList("primary_ds", "replica_ds_0", "replica_ds_1"))));
        ReplicaQueryDataSourceRule dataSourceRule = rule.getSingleDataSourceRule();
        assertNotNull(dataSourceRule);
        assertThat(dataSourceRule.getName(), is("pr_ds"));
        assertThat(dataSourceRule.getPrimaryDataSourceName(), is("primary_ds"));
        assertThat(dataSourceRule.getReplicaDataSourceNames(), is(Arrays.asList("replica_ds_0", "replica_ds_1")));
        assertThat(dataSourceRule.getLoadBalancer(), instanceOf(RandomReplicaLoadBalanceAlgorithm.class));
        assertThat(dataSourceRule.getDataSourceMapper(), is(Collections.singletonMap("pr_ds", Arrays.asList("primary_ds", "replica_ds_0", "replica_ds_1"))));
    }
    
    private void assertEncryptRule(final EncryptRule rule) {
        assertThat(rule.getEncryptTableNames(), is(Sets.newLinkedHashSet(Collections.singletonList("t_order"))));
        assertThat(rule.getCipherColumn("t_order", "pwd"), is("pwd_cipher"));
        assertThat(rule.getAssistedQueryColumns("t_order"), is(Collections.singletonList("pwd_assisted_query_cipher")));
        assertThat(rule.getLogicAndCipherColumns("t_order"), is(Collections.singletonMap("pwd", "pwd_cipher")));
        assertThat(rule.getLogicColumnOfCipher("t_order", "pwd_cipher"), is("pwd"));
        assertThat(rule.getEncryptValues("t_order", "pwd", Collections.singletonList("pwd_plain")), is(Collections.singletonList("V/RkV1+dVv80Y3csT3cR4g==")));
        assertThat(rule.getAssistedQueryAndPlainColumns("t_order"), is(Arrays.asList("pwd_assisted_query_cipher", "pwd_plain")));
    }
    
    private void assertShadowRule(final ShadowRule rule) {
        assertThat(rule.getColumn(), is("shadow"));
        assertThat(rule.getShadowMappings(), is(Collections.singletonMap("ds", "shadow_ds")));
    }
    
    @Test
    public void assertProperties() {
        assertTrue(dataSource.getSchemaContexts().getProps().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW));
        assertThat(dataSource.getSchemaContexts().getProps().getValue(ConfigurationPropertyKey.EXECUTOR_SIZE), is(10));
    }
}
