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

package org.apache.shardingsphere.spring.boot.type;

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.masterslave.rule.MasterSlaveDataSourceRule;
import org.apache.shardingsphere.masterslave.rule.MasterSlaveRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = SpringBootMasterSlaveTest.class)
@SpringBootApplication
@ActiveProfiles("masterslave")
public class SpringBootMasterSlaveTest {
    
    @Resource
    private DataSource dataSource;
    
    @Test
    public void assertDataSource() {
        assertTrue(dataSource instanceof ShardingSphereDataSource);
        assertThat(((ShardingSphereDataSource) dataSource).getDataSourceMap().size(), is(3));
        Collection<ShardingSphereRule> rules = ((ShardingSphereDataSource) dataSource).getSchemaContexts().getDefaultSchemaContext().getSchema().getRules();
        assertThat(rules.size(), is(1));
        assertMasterSlaveRule((MasterSlaveRule) rules.iterator().next());
    }
    
    private void assertMasterSlaveRule(final MasterSlaveRule rule) {
        MasterSlaveDataSourceRule dataSourceRule = rule.getSingleDataSourceRule();
        assertThat(dataSourceRule.getName(), is("ds_ms"));
        assertThat(dataSourceRule.getMasterDataSourceName(), is("ds_master"));
        assertThat(dataSourceRule.getSlaveDataSourceNames().size(), is(2));
        assertThat(dataSourceRule.getSlaveDataSourceNames().get(0), is("ds_slave_0"));
        assertThat(dataSourceRule.getSlaveDataSourceNames().get(1), is("ds_slave_1"));
        assertThat(dataSourceRule.getLoadBalancer().getType(), is("RANDOM"));
    }
}
