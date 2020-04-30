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

package org.apache.shardingsphere.shardingjdbc.orchestration.spring;

import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.spring.datasource.OrchestrationSpringShardingDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.spring.util.EmbedTestingServer;
import org.apache.shardingsphere.shardingjdbc.orchestration.spring.util.FieldValueUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.sql.DataSource;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "classpath:META-INF/rdb/shardingMasterSlaveOrchestration.xml")
public class OrchestrationShardingMasterSlaveNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
    }
    
    @Test
    public void assertMasterSlaveShardingDataSourceByUserStrategy() {
        Map<String, DataSource> dataSourceMap = getDataSourceMap("masterSlaveShardingDataSourceByUserStrategyOrchestration");
        assertNotNull(dataSourceMap.get("dbtbl_0_master"));
        assertNotNull(dataSourceMap.get("dbtbl_0_slave_0"));
        assertNotNull(dataSourceMap.get("dbtbl_0_slave_1"));
        assertNotNull(dataSourceMap.get("dbtbl_1_master"));
        assertNotNull(dataSourceMap.get("dbtbl_1_slave_0"));
        assertNotNull(dataSourceMap.get("dbtbl_1_slave_1"));
        ShardingRule shardingRule = getShardingRule("masterSlaveShardingDataSourceByUserStrategyOrchestration");
        assertThat(shardingRule.getTableRules().size(), is(1));
        assertThat(shardingRule.getTableRules().iterator().next().getLogicTable(), is("t_order"));
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, DataSource> getDataSourceMap(final String shardingDataSourceName) {
        OrchestrationSpringShardingDataSource shardingDataSource = applicationContext.getBean(shardingDataSourceName, OrchestrationSpringShardingDataSource.class);
        ShardingDataSource dataSource = (ShardingDataSource) FieldValueUtil.getFieldValue(shardingDataSource, "dataSource", true);
        return dataSource.getDataSourceMap();
    }
    
    private ShardingRule getShardingRule(final String shardingDataSourceName) {
        OrchestrationSpringShardingDataSource shardingDataSource = applicationContext.getBean(shardingDataSourceName, OrchestrationSpringShardingDataSource.class);
        ShardingDataSource dataSource = (ShardingDataSource) FieldValueUtil.getFieldValue(shardingDataSource, "dataSource", true);
        return (ShardingRule) dataSource.getRuntimeContext().getRules().iterator().next();
    }
}
