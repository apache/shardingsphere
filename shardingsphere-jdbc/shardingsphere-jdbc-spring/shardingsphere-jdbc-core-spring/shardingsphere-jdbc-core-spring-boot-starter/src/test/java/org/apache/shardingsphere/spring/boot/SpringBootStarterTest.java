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
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodeUtil;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.readwritesplitting.algorithm.RandomReplicaLoadBalanceAlgorithm;
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
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
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
    public void assertDataSources() {
        Map<String, DataSource> dataSources = dataSource.getContextManager().getMetaDataContexts().getMetaData(DefaultSchema.LOGIC_NAME).getResource().getDataSources();
        assertThat(dataSources.size(), is(2));
        assertTrue(dataSources.containsKey("ds_0"));
        assertTrue(dataSources.containsKey("ds_1"));
    }
    
    @Test
    public void assertRules() {
        Collection<ShardingSphereRule> rules = dataSource.getContextManager().getMetaDataContexts().getMetaData(DefaultSchema.LOGIC_NAME).getRuleMetaData().getRules();
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
    
    private void assertShardingRule(final ShardingRule rule) {
        assertThat(rule.getDataSourceNames(), is(Sets.newHashSet("ds_0", "ds_1")));
        Map<String, ShardingAlgorithm> shardingAlgorithmMap = rule.getShardingAlgorithms();
        assertNotNull(shardingAlgorithmMap);
        InlineShardingAlgorithm databaseShardingAlgorithm = (InlineShardingAlgorithm) shardingAlgorithmMap.get("databaseShardingAlgorithm");
        assertThat(databaseShardingAlgorithm.getProps().getProperty("algorithm-expression"), is("ds_$->{user_id % 2}"));
        InlineShardingAlgorithm orderTableShardingAlgorithm = (InlineShardingAlgorithm) shardingAlgorithmMap.get("orderTableShardingAlgorithm");
        assertThat(orderTableShardingAlgorithm.getProps().getProperty("algorithm-expression"), is("t_order_$->{order_id % 2}"));
        Collection<TableRule> tableRules = rule.getTableRules().values();
        assertNotNull(tableRules);
        assertThat(tableRules.size(), is(1));
        TableRule tableRule = tableRules.iterator().next();
        assertThat(tableRule.getLogicTable(), is("t_order"));
        List<DataNode> dataNodes = Arrays.asList(new DataNode("ds_0.t_order_0"), new DataNode("ds_0.t_order_1"), new DataNode("ds_1.t_order_0"), new DataNode("ds_1.t_order_1"));
        assertThat(tableRule.getActualDataNodes(), is(dataNodes));
        assertThat(tableRule.getActualDatasourceNames(), is(Sets.newHashSet("ds_0", "ds_1")));
        assertThat(tableRule.getDataNodeGroups(), is(DataNodeUtil.getDataNodeGroups(dataNodes)));
        assertThat(tableRule.getDatasourceToTablesMap(), is(ImmutableMap.of("ds_1", Sets.newHashSet("t_order_0", "t_order_1"), "ds_0", Sets.newHashSet("t_order_0", "t_order_1"))));
        assertThat(rule.getDefaultShardingColumn(), is("user_id"));
    }
    
    private void assertReadwriteSplittingRule(final ReadwriteSplittingRule rule) {
        assertThat(rule.getDataSourceMapper(), is(Collections.singletonMap("pr_ds", Arrays.asList("write_ds", "read_ds_0", "read_ds_1"))));
        ReadwriteSplittingDataSourceRule dataSourceRule = rule.getSingleDataSourceRule();
        assertNotNull(dataSourceRule);
        assertThat(dataSourceRule.getName(), is("pr_ds"));
        assertThat(dataSourceRule.getWriteDataSourceName(), is("write_ds"));
        assertThat(dataSourceRule.getReadDataSourceNames(), is(Arrays.asList("read_ds_0", "read_ds_1")));
        assertThat(dataSourceRule.getLoadBalancer(), instanceOf(RandomReplicaLoadBalanceAlgorithm.class));
        assertThat(dataSourceRule.getDataSourceMapper(), is(Collections.singletonMap("pr_ds", Arrays.asList("write_ds", "read_ds_0", "read_ds_1"))));
    }
    
    private void assertEncryptRule(final EncryptRule rule) {
        assertTrue(rule.findEncryptTable("t_order").isPresent());
        EncryptTable table = rule.findEncryptTable("t_order").get();
        assertThat(table.getLogicColumn("pwd_cipher"), is("pwd"));
        assertThat(table.getPlainColumns(), is(Collections.singletonList("pwd_plain")));
        assertThat(table.getAssistedQueryColumns(), is(Collections.singletonList("pwd_assisted_query_cipher")));
        assertThat(rule.getCipherColumn("t_order", "pwd"), is("pwd_cipher"));
        assertThat(rule.getAssistedQueryColumns("t_order"), is(Collections.singletonList("pwd_assisted_query_cipher")));
        assertThat(rule.getLogicAndCipherColumns("t_order"), is(Collections.singletonMap("pwd", "pwd_cipher")));
        assertThat(rule.getEncryptValues(DefaultSchema.LOGIC_NAME, "t_order", "pwd", Collections.singletonList("pwd_plain")), 
                is(Collections.singletonList("V/RkV1+dVv80Y3csT3cR4g==")));
    }
    
    private void assertShadowRule(final ShadowRule rule) {
        assertThat(rule.isEnable(), is(true));
        assertShadowDataSourceMappings(rule.getShadowDataSourceMappings());
        assertShadowAlgorithms(rule.getShadowAlgorithms());
        assertShadowTableRules(rule.getShadowTableRules());
    }
    
    private void assertShadowTableRules(final Map<String, ShadowTableRule> shadowTableRules) {
        assertThat(shadowTableRules.size(), is(2));
        assertThat(shadowTableRules.get("t_order").getTableName(), is("t_order"));
        assertThat(shadowTableRules.get("t_order").getShadowDataSources().size(), is(1));
        assertThat(shadowTableRules.get("t_order").getHintShadowAlgorithmNames().size(), is(1));
        assertThat(shadowTableRules.get("t_order").getColumnShadowAlgorithmNames().size(), is(2));
        assertThat(shadowTableRules.get("t_user").getTableName(), is("t_user"));
        assertThat(shadowTableRules.get("t_user").getShadowDataSources().size(), is(1));
        assertThat(shadowTableRules.get("t_user").getHintShadowAlgorithmNames().size(), is(1));
        assertThat(shadowTableRules.get("t_user").getColumnShadowAlgorithmNames().size(), is(0));
    }
    
    private void assertSQLParserRule(final SQLParserRule sqlParserRule) {
        assertThat(sqlParserRule.isSqlCommentParseEnabled(), is(true));
        assertCacheOption(sqlParserRule.getSqlStatementCache());
        assertCacheOption(sqlParserRule.getParserTreeCache());
    }

    private void assertCacheOption(final CacheOption cacheOption) {
        assertThat(cacheOption.getInitialCapacity(), is(1024));
        assertThat(cacheOption.getMaximumSize(), is(1024L));
        assertThat(cacheOption.getConcurrencyLevel(), is(4));
    }
    
    private void assertShadowAlgorithms(final Map<String, ShadowAlgorithm> shadowAlgorithms) {
        assertThat(shadowAlgorithms.size(), is(3));
        assertThat(shadowAlgorithms.get("user-id-match-algorithm") instanceof ColumnShadowAlgorithm, is(true));
        assertThat(shadowAlgorithms.get("order-id-match-algorithm") instanceof ColumnShadowAlgorithm, is(true));
        assertThat(shadowAlgorithms.get("simple-hint-algorithm") instanceof HintShadowAlgorithm, is(true));
    }
    
    private void assertShadowDataSourceMappings(final Map<String, ShadowDataSourceRule> shadowDataSourceMappings) {
        assertThat(shadowDataSourceMappings.size(), is(1));
        assertThat(shadowDataSourceMappings.get("shadow-data-source").getSourceDataSource(), is("ds"));
        assertThat(shadowDataSourceMappings.get("shadow-data-source").getShadowDataSource(), is("ds-shadow"));
    }
    
    @Test
    public void assertProperties() {
        assertTrue(dataSource.getContextManager().getMetaDataContexts().getProps().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW));
        assertThat(dataSource.getContextManager().getMetaDataContexts().getProps().getValue(ConfigurationPropertyKey.KERNEL_EXECUTOR_SIZE), is(10));
    }
}
