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

package org.apache.shardingsphere.shardingjdbc.orchestration.spring.boot.type;

import lombok.SneakyThrows;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.core.rule.MasterSlaveRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.datasource.OrchestrationShardingDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.spring.boot.util.EmbedTestingServer;
import org.apache.shardingsphere.underlying.common.rule.ShardingSphereRule;
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
@SpringBootTest(classes = OrchestrationSpringBootMasterSlaveTest.class)
@SpringBootApplication
@ActiveProfiles("masterslave")
public class OrchestrationSpringBootMasterSlaveTest {
    
    @Resource
    private DataSource dataSource;
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
    }
    
    @Test
    @SneakyThrows
    public void assertDataSource() {
        assertTrue(dataSource instanceof OrchestrationShardingDataSource);
        Field field = OrchestrationShardingDataSource.class.getDeclaredField("dataSource");
        field.setAccessible(true);
        ShardingDataSource shardingDataSource = (ShardingDataSource) field.get(dataSource);
        for (DataSource each : shardingDataSource.getDataSourceMap().values()) {
            assertThat(((BasicDataSource) each).getMaxTotal(), is(16));
            assertThat(((BasicDataSource) each).getUsername(), is("root"));
        }
        Collection<ShardingSphereRule> rules = shardingDataSource.getRuntimeContext().getRules();
        assertThat(rules.size(), is(1));
        assertMasterSlaveRule((MasterSlaveRule) rules.iterator().next());
    }
    
    private void assertMasterSlaveRule(final MasterSlaveRule rule) {
        assertThat(rule.getName(), is("ds_ms"));
        assertThat(rule.getMasterDataSourceName(), is("ds_master"));
        assertThat(rule.getSlaveDataSourceNames().size(), is(2));
        assertThat(rule.getSlaveDataSourceNames().get(0), is("ds_slave_0"));
        assertThat(rule.getSlaveDataSourceNames().get(1), is("ds_slave_1"));
    }
}
