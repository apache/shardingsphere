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
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.spring.namespace.util.EmbedTestingServer;
import org.apache.shardingsphere.spring.namespace.util.FieldValueUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@ContextConfiguration(locations = "classpath:META-INF/rdb/cluster-application-context-sharding-readwrite-splitting.xml")
public class SpringNamespaceWithShardingAndReadwriteSplittingForClusterTest extends AbstractJUnit4SpringContextTests {
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
    }
    
    @Test
    public void assertReadwriteSplittingShardingDataSourceByUserStrategy() {
        Map<String, DataSource> dataSourceMap = getDataSourceMap("dataSourceByUserStrategyGovernance");
        assertNotNull(dataSourceMap.get("dbtbl_write_0"));
        assertNotNull(dataSourceMap.get("dbtbl_0_read_0"));
        assertNotNull(dataSourceMap.get("dbtbl_0_read_1"));
        assertNotNull(dataSourceMap.get("dbtbl_write_1"));
        assertNotNull(dataSourceMap.get("dbtbl_1_read_0"));
        assertNotNull(dataSourceMap.get("dbtbl_1_read_1"));
        Optional<ShardingRule> shardingRule = getShardingRule("dataSourceByUserStrategyGovernance");
        assertTrue(shardingRule.isPresent());
        assertThat(shardingRule.get().getTableRules().size(), is(1));
        assertThat(shardingRule.get().getTableRules().values().iterator().next().getLogicTable(), is("t_order"));
    }
    
    private Map<String, DataSource> getDataSourceMap(final String dataSourceName) {
        ShardingSphereDataSource shardingSphereDataSource = applicationContext.getBean(dataSourceName, ShardingSphereDataSource.class);
        ContextManager contextManager = (ContextManager) FieldValueUtil.getFieldValue(shardingSphereDataSource, "contextManager");
        return contextManager.getMetaDataContexts().getMetaData(DefaultSchema.LOGIC_NAME).getResource().getDataSources();
    }
    
    private Optional<ShardingRule> getShardingRule(final String dataSourceName) {
        ShardingSphereDataSource shardingSphereDataSource = applicationContext.getBean(dataSourceName, ShardingSphereDataSource.class);
        ContextManager contextManager = (ContextManager) FieldValueUtil.getFieldValue(shardingSphereDataSource, "contextManager");
        return contextManager.getMetaDataContexts().getMetaData(DefaultSchema.LOGIC_NAME).getRuleMetaData().getRules().stream().filter(
            each -> each instanceof ShardingRule).map(each -> (ShardingRule) each).findFirst();
    }
}
