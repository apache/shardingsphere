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
import org.apache.shardingsphere.encrypt.rule.EncryptTable;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodeUtil;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.readwritesplitting.algorithm.StaticReadwriteSplittingType;
import org.apache.shardingsphere.readwritesplitting.algorithm.loadbalance.RandomReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingDataSourceRule;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.shadow.api.shadow.column.ColumnShadowAlgorithm;
import org.apache.shardingsphere.shadow.api.shadow.hint.HintShadowAlgorithm;
import org.apache.shardingsphere.shadow.rule.ShadowDataSourceRule;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.shadow.rule.ShadowTableRule;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;
import org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineShardingAlgorithm;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringBootStarterTest.class)
@SpringBootApplication
public class SpringBootStarterTest {
    
    @Resource
    private ShardingSphereDataSource dataSource;
    
    @Test
    public void assertDataSources() {
        Map<String, DataSource> dataSources = dataSource.getContextManager().getMetaDataContexts().getMetaData("foo_db").getResource().getDataSources();
        assertThat(dataSources.size(), is(2));
        assertTrue(dataSources.containsKey("ds0"));
        assertTrue(dataSources.containsKey("ds1"));
    }
    
    @Test
    public void assertRules() {
        Collection<ShardingSphereRule> rules = dataSource.getContextManager().getMetaDataContexts().getMetaData("foo_db").getRuleMetaData().getRules();
        assertThat(rules.size(), is(5));
        for (ShardingSphereRule each : rules) {
            if (each instanceof ShardingRule) {
                assertShardingRule((ShardingRule) each);
            } else if (each instanceof ReadwriteSplittingRule) {
                assertReadwriteSplittingRule((ReadwriteSplittingRule) each);
            } else if (each instanceof EncryptRule) {
                assertEncryptRule((EncryptRule) each);
            } else if (each instanceof ShadowRule) {
                assertShadowRule((ShadowRule) each);
            } else if (each instanceof SQLParserRule) {
                assertSQLParserRule((SQLParserRule) each);
            }
        }
    }
    
    private void assertShardingRule(final ShardingRule actual) {
        assertThat(actual.getDataSourceNames(), is(Sets.newHashSet("ds0", "ds1")));
        InlineShardingAlgorithm databaseShardingAlgorithm = (InlineShardingAlgorithm) actual.getShardingAlgorithms().get("databaseShardingAlgorithm");
        assertThat(databaseShardingAlgorithm.getProps().getProperty("algorithm-expression"), is("ds$->{user_id % 2}"));
        InlineShardingAlgorithm orderTableShardingAlgorithm = (InlineShardingAlgorithm) actual.getShardingAlgorithms().get("orderTableShardingAlgorithm");
        assertThat(orderTableShardingAlgorithm.getProps().getProperty("algorithm-expression"), is("t_order_$->{order_id % 2}"));
        assertShardingTableRules(actual.getTableRules());
        assertThat(actual.getDefaultShardingColumn(), is("user_id"));
    }
    
    private void assertShardingTableRules(final Map<String, TableRule> actual) {
        assertThat(actual.size(), is(1));
        assertThat(actual.keySet().iterator().next(), is("t_order"));
        assertShardingTableRule(actual.values().iterator().next());
    }
    
    private void assertShardingTableRule(final TableRule actual) {
        assertThat(actual.getLogicTable(), is("t_order"));
        Collection<DataNode> dataNodes = Arrays.asList(new DataNode("ds0.t_order_0"), new DataNode("ds0.t_order_1"), new DataNode("ds1.t_order_0"), new DataNode("ds1.t_order_1"));
        assertThat(actual.getActualDataNodes(), is(dataNodes));
        assertThat(actual.getActualDatasourceNames(), is(Sets.newHashSet("ds0", "ds1")));
        assertThat(actual.getDataNodeGroups(), is(DataNodeUtil.getDataNodeGroups(dataNodes)));
        assertThat(actual.getDatasourceToTablesMap(), is(ImmutableMap.of("ds1", Sets.newHashSet("t_order_0", "t_order_1"), "ds0", Sets.newHashSet("t_order_0", "t_order_1"))));
    }
    
    private void assertReadwriteSplittingRule(final ReadwriteSplittingRule actual) {
        assertThat(actual.getDataSourceMapper(), is(Collections.singletonMap("readwrite_ds", Arrays.asList("write_ds", "read_ds_0", "read_ds_1"))));
        ReadwriteSplittingDataSourceRule dataSourceRule = actual.getSingleDataSourceRule();
        assertThat(dataSourceRule.getName(), is("readwrite_ds"));
        StaticReadwriteSplittingType staticReadwriteSplittingType = (StaticReadwriteSplittingType) dataSourceRule.getReadwriteSplittingType();
        assertThat(staticReadwriteSplittingType.getWriteDataSource(), is("write_ds"));
        assertThat(staticReadwriteSplittingType.getReadDataSources(), is(Arrays.asList("read_ds_0", "read_ds_1")));
        assertThat(dataSourceRule.getLoadBalancer(), instanceOf(RandomReplicaLoadBalanceAlgorithm.class));
        assertThat(dataSourceRule.getDataSourceMapper(), is(Collections.singletonMap("readwrite_ds", Arrays.asList("write_ds", "read_ds_0", "read_ds_1"))));
    }
    
    private void assertEncryptRule(final EncryptRule actual) {
        assertTrue(actual.findEncryptTable("t_order").isPresent());
        assertEncryptTable(actual.findEncryptTable("t_order").get());
        assertThat(actual.getCipherColumn("t_order", "pwd"), is("pwd_cipher"));
        assertThat(actual.getAssistedQueryColumns("t_order"), is(Collections.singletonList("pwd_assisted_query_cipher")));
        assertThat(actual.getLogicAndCipherColumns("t_order"), is(Collections.singletonMap("pwd", "pwd_cipher")));
        assertThat(actual.getEncryptValues("foo_db", "t_order", "pwd", Collections.singletonList("pwd_plain")), is(Collections.singletonList("V/RkV1+dVv80Y3csT3cR4g==")));
    }
    
    private void assertEncryptTable(final EncryptTable actual) {
        assertThat(actual.getLogicColumn("pwd_cipher"), is("pwd"));
        assertThat(actual.getPlainColumns(), is(Collections.singletonList("pwd_plain")));
        assertThat(actual.getAssistedQueryColumns(), is(Collections.singletonList("pwd_assisted_query_cipher")));
    }
    
    private void assertShadowRule(final ShadowRule actual) {
        assertShadowDataSourceMappings(actual.getShadowDataSourceMappings());
        assertShadowAlgorithms(actual.getShadowAlgorithms());
        assertShadowTableRules(actual.getShadowTableRules());
    }
    
    private void assertShadowDataSourceMappings(final Map<String, ShadowDataSourceRule> actual) {
        assertThat(actual.size(), is(1));
        assertThat(actual.get("shadow-data-source").getSourceDataSource(), is("ds"));
        assertThat(actual.get("shadow-data-source").getShadowDataSource(), is("ds-shadow"));
    }
    
    private void assertShadowAlgorithms(final Map<String, ShadowAlgorithm> actual) {
        assertThat(actual.size(), is(3));
        assertThat(actual.get("user-id-match-algorithm"), instanceOf(ColumnShadowAlgorithm.class));
        assertThat(actual.get("order-id-match-algorithm"), instanceOf(ColumnShadowAlgorithm.class)); 
        assertThat(actual.get("simple-hint-algorithm"), instanceOf(HintShadowAlgorithm.class));
    }
    
    private void assertShadowTableRules(final Map<String, ShadowTableRule> actual) {
        assertThat(actual.size(), is(2));
        assertThat(actual.get("t_order").getTableName(), is("t_order"));
        assertThat(actual.get("t_order").getShadowDataSources().size(), is(1));
        assertThat(actual.get("t_order").getHintShadowAlgorithmNames().size(), is(1));
        assertThat(actual.get("t_order").getColumnShadowAlgorithmNames().size(), is(2));
        assertThat(actual.get("t_user").getTableName(), is("t_user"));
        assertThat(actual.get("t_user").getShadowDataSources().size(), is(1));
        assertThat(actual.get("t_user").getHintShadowAlgorithmNames().size(), is(1));
        assertThat(actual.get("t_user").getColumnShadowAlgorithmNames().size(), is(0));
    }
    
    private void assertSQLParserRule(final SQLParserRule actual) {
        assertTrue(actual.isSqlCommentParseEnabled());
        assertCacheOption(actual.getSqlStatementCache());
        assertCacheOption(actual.getParseTreeCache());
    }

    private void assertCacheOption(final CacheOption actual) {
        assertThat(actual.getInitialCapacity(), is(1024));
        assertThat(actual.getMaximumSize(), is(1024L));
        assertThat(actual.getConcurrencyLevel(), is(4));
    }
    
    @Test
    public void assertProperties() {
        assertTrue(dataSource.getContextManager().getMetaDataContexts().getProps().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW));
        assertThat(dataSource.getContextManager().getMetaDataContexts().getProps().getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE), is(10));
    }
}
