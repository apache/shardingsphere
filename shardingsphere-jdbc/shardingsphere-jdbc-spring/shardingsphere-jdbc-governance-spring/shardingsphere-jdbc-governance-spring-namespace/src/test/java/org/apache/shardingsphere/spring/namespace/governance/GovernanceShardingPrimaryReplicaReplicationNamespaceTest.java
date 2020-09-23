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
import org.apache.shardingsphere.infra.context.schema.SchemaContexts;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.spring.namespace.governance.util.EmbedTestingServer;
import org.apache.shardingsphere.spring.namespace.governance.util.FieldValueUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.sql.DataSource;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "classpath:META-INF/rdb/sharding-primary-replica-replication-governance.xml")
public class GovernanceShardingPrimaryReplicaReplicationNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
    }
    
    @Test
    public void assertPrimaryReplicaReplicationShardingDataSourceByUserStrategy() {
        Map<String, DataSource> dataSourceMap = getDataSourceMap("masterSlaveShardingDataSourceByUserStrategyGovernance");
        assertNotNull(dataSourceMap.get("dbtbl_primary_0"));
        assertNotNull(dataSourceMap.get("dbtbl_0_replica_0"));
        assertNotNull(dataSourceMap.get("dbtbl_0_replica_1"));
        assertNotNull(dataSourceMap.get("dbtbl_primary_1"));
        assertNotNull(dataSourceMap.get("dbtbl_1_replica_0"));
        assertNotNull(dataSourceMap.get("dbtbl_1_replica_1"));
        ShardingRule shardingRule = getShardingRule("masterSlaveShardingDataSourceByUserStrategyGovernance");
        assertThat(shardingRule.getTableRules().size(), is(1));
        assertThat(shardingRule.getTableRules().iterator().next().getLogicTable(), is("t_order"));
    }
    
    private Map<String, DataSource> getDataSourceMap(final String dataSourceName) {
        GovernanceShardingSphereDataSource shardingSphereDataSource = applicationContext.getBean(dataSourceName, GovernanceShardingSphereDataSource.class);
        SchemaContexts schemaContexts = (SchemaContexts) FieldValueUtil.getFieldValue(shardingSphereDataSource, "schemaContexts");
        return schemaContexts.getDefaultSchemaContext().getSchema().getDataSources();
    }
    
    private ShardingRule getShardingRule(final String dataSourceName) {
        GovernanceShardingSphereDataSource shardingSphereDataSource = applicationContext.getBean(dataSourceName, GovernanceShardingSphereDataSource.class);
        SchemaContexts schemaContexts = (SchemaContexts) FieldValueUtil.getFieldValue(shardingSphereDataSource, "schemaContexts");
        return (ShardingRule) schemaContexts.getDefaultSchemaContext().getSchema().getRules().iterator().next();
    }
}
