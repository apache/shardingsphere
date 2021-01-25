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

package org.apache.shardingsphere.spring.boot.governance.type;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.driver.governance.internal.datasource.GovernanceShardingSphereDataSource;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.replicaquery.rule.ReplicaQueryDataSourceRule;
import org.apache.shardingsphere.replicaquery.rule.ReplicaQueryRule;
import org.apache.shardingsphere.spring.boot.governance.util.EmbedTestingServer;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = GovernanceSpringBootReplicaQueryTest.class)
@SpringBootApplication
@ActiveProfiles("replica-query")
public class GovernanceSpringBootReplicaQueryTest {
    
    @Resource
    private DataSource dataSource;
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
    }
    
    @Test
    public void assertDataSource() throws NoSuchFieldException, IllegalAccessException {
        assertTrue(dataSource instanceof GovernanceShardingSphereDataSource);
        Field field = GovernanceShardingSphereDataSource.class.getDeclaredField("metaDataContexts");
        field.setAccessible(true);
        MetaDataContexts metaDataContexts = (MetaDataContexts) field.get(dataSource);
        for (DataSource each : metaDataContexts.getDefaultMetaData().getResource().getDataSources().values()) {
            assertThat(((BasicDataSource) each).getMaxTotal(), is(16));
            assertThat(((BasicDataSource) each).getUsername(), is("sa"));
        }
        Collection<ShardingSphereRule> rules = metaDataContexts.getDefaultMetaData().getRuleMetaData().getRules();
        assertThat(rules.size(), is(1));
        assertReplicaQueryRule((ReplicaQueryRule) rules.iterator().next());
    }
    
    private void assertReplicaQueryRule(final ReplicaQueryRule rule) {
        ReplicaQueryDataSourceRule dataSourceRule = rule.getSingleDataSourceRule();
        assertThat(dataSourceRule.getName(), is("pr_ds"));
        assertThat(dataSourceRule.getName(), is("pr_ds"));
        assertThat(dataSourceRule.getPrimaryDataSourceName(), is("primary_ds"));
        assertThat(dataSourceRule.getReplicaDataSourceNames().size(), is(2));
        assertThat(dataSourceRule.getReplicaDataSourceNames().get(0), is("replica_ds_0"));
        assertThat(dataSourceRule.getReplicaDataSourceNames().get(1), is("replica_ds_1"));
    }
}
