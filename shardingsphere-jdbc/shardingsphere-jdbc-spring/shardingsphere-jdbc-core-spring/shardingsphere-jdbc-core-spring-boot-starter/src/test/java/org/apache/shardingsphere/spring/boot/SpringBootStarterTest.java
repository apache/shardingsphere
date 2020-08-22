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

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.masterslave.rule.MasterSlaveRule;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.is;
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
        Collection<ShardingSphereRule> rules = dataSource.getSchemaContexts().getDefaultSchemaContext().getSchema().getRules();
        assertThat(rules.size(), is(2));
        for (ShardingSphereRule each : rules) {
            if (each instanceof ShardingRule) {
                assertShardingRule((ShardingRule) each);
            } else if (each instanceof MasterSlaveRule) {
                assertMasterSlaveRule((MasterSlaveRule) each);
            } else if (each instanceof EncryptRule) {
                assertEncryptRule((EncryptRule) each);
            } else if (each instanceof ShadowRule) {
                assertShadowRule((ShadowRule) each);
            }
        }
    }
    
    private void assertShardingRule(final ShardingRule rule) {
        // TODO
    }
    
    private void assertMasterSlaveRule(final MasterSlaveRule rule) {
        // TODO
    }
    
    private void assertEncryptRule(final EncryptRule rule) {
        // TODO
    }
    
    private void assertShadowRule(final ShadowRule rule) {
        // TODO
    }
    
    @Test
    public void assertProperties() {
        assertTrue(dataSource.getSchemaContexts().getProps().<Boolean>getValue(ConfigurationPropertyKey.SQL_SHOW));
        assertThat(dataSource.getSchemaContexts().getProps().getValue(ConfigurationPropertyKey.EXECUTOR_SIZE), is(10));
    }
}
