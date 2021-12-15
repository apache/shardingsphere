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

package org.apache.shardingsphere.spring.namespace;

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.spring.transaction.TransactionTypeScanner;
import org.apache.shardingsphere.sql.parser.api.CacheOption;
import org.junit.Test;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public abstract class AbstractSpringNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @Resource
    private ShardingSphereDataSource dataSource;
    
    @Test
    public void assertShardingSphereDataSource() {
        assertDataSources();
        Collection<ShardingSphereRule> rules = dataSource.getContextManager().getMetaDataContexts().getMetaData(DefaultSchema.LOGIC_NAME).getRuleMetaData().getRules();
        Collection<ShardingSphereRule> globalRules = dataSource.getContextManager().getMetaDataContexts().getGlobalRuleMetaData().getRules();
        assertThat(rules.size(), is(4));
        for (ShardingSphereRule each : rules) {
            if (each instanceof ShardingRule) {
                assertShardingRule((ShardingRule) each);
            } else if (each instanceof ReadwriteSplittingRule) {
                assertReadwriteSplittingRule((ReadwriteSplittingRule) each);
            } else if (each instanceof EncryptRule) {
                assertEncryptRule((EncryptRule) each);
            }
        }
        assertThat(globalRules.size(), is(3));
        for (ShardingSphereRule each : globalRules) {
            if (each instanceof SQLParserRule) {
                assertSQLParserRule((SQLParserRule) each);
            }
        }
    }
    
    private void assertDataSources() {
        Map<String, DataSource> dataSources = dataSource.getContextManager().getMetaDataContexts().getMetaData(DefaultSchema.LOGIC_NAME).getResource().getDataSources();
        assertThat(dataSources.size(), is(6));
        assertTrue(dataSources.containsKey("ds_0_write"));
        assertTrue(dataSources.containsKey("ds_0_read_0"));
        assertTrue(dataSources.containsKey("ds_0_read_1"));
        assertTrue(dataSources.containsKey("ds_1_write"));
        assertTrue(dataSources.containsKey("ds_1_read_0"));
        assertTrue(dataSources.containsKey("ds_1_read_1"));
    }
    
    private void assertShardingRule(final ShardingRule rule) {
        assertThat(rule.getTableRules().size(), is(1));
        assertThat(rule.getTableRule("t_order").getActualDataNodes(), is(Arrays.asList(
                new DataNode("ds_0.t_order_0"), new DataNode("ds_0.t_order_1"), new DataNode("ds_0.t_order_2"), new DataNode("ds_0.t_order_3"),
                new DataNode("ds_1.t_order_0"), new DataNode("ds_1.t_order_1"), new DataNode("ds_1.t_order_2"), new DataNode("ds_1.t_order_3"))));
    }
    
    private void assertReadwriteSplittingRule(final ReadwriteSplittingRule rule) {
        assertTrue(rule.findDataSourceRule("ds_0").isPresent());
        assertThat(rule.findDataSourceRule("ds_0").get().getWriteDataSourceName(), is("ds_0_write"));
        assertThat(rule.findDataSourceRule("ds_0").get().getReadDataSourceNames(), is(Arrays.asList("ds_0_read_0", "ds_0_read_1")));
        assertTrue(rule.findDataSourceRule("ds_1").isPresent());
        assertThat(rule.findDataSourceRule("ds_1").get().getWriteDataSourceName(), is("ds_1_write"));
        assertThat(rule.findDataSourceRule("ds_1").get().getReadDataSourceNames(), is(Arrays.asList("ds_1_read_0", "ds_1_read_1")));
    }
    
    private void assertEncryptRule(final EncryptRule rule) {
        assertThat(rule.getCipherColumn("t_order", "pwd"), is("pwd_cipher"));
        assertTrue(rule.findEncryptor(DefaultSchema.LOGIC_NAME, "t_order", "pwd").isPresent());
        assertThat(rule.findEncryptor(DefaultSchema.LOGIC_NAME, "t_order", "pwd").get().getProps().getProperty("aes-key-value"), is("123456"));
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
    
    @Test
    public void assertTransactionTypeScanner() {
        assertNotNull(applicationContext.getBean(TransactionTypeScanner.class));
    }
}
