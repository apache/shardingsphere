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

package org.apache.shardingsphere.dbdiscovery.spring.namespace;

import org.apache.shardingsphere.dbdiscovery.algorithm.config.AlgorithmProvidedDatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryHeartBeatConfiguration;
import org.apache.shardingsphere.dbdiscovery.mysql.type.MGRMySQLDatabaseDiscoveryProviderAlgorithm;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryProviderAlgorithm;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ContextConfiguration(locations = "classpath:META-INF/spring/database-discovery-application-context.xml")
public final class DatabaseDiscoverySpringNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @Resource
    private AlgorithmProvidedDatabaseDiscoveryRuleConfiguration mgrDatabaseDiscoveryRule;
    
    @Test
    public void assertDefaultDataSource() {
        assertDiscoveryTypes(mgrDatabaseDiscoveryRule.getDiscoveryTypes());
        assertHeartbeats(mgrDatabaseDiscoveryRule.getDiscoveryHeartbeats());
        assertThat(mgrDatabaseDiscoveryRule.getDataSources().size(), is(1));
        assertDefaultDataSourceRule(mgrDatabaseDiscoveryRule.getDataSources().iterator().next());
    }
    
    private void assertDiscoveryTypes(final Map<String, DatabaseDiscoveryProviderAlgorithm> discoveryTypes) {
        assertThat(discoveryTypes.size(), is(1));
        assertThat(discoveryTypes.get("mgr"), instanceOf(MGRMySQLDatabaseDiscoveryProviderAlgorithm.class));
        assertThat(discoveryTypes.get("mgr").getProps().getProperty("group-name"), is("92504d5b-6dec-11e8-91ea-246e9612aaf1"));
    }
    
    private void assertHeartbeats(final Map<String, DatabaseDiscoveryHeartBeatConfiguration> heartbeats) {
        assertThat(heartbeats.size(), is(1));
        assertThat(heartbeats.get("mgr-heartbeat"), instanceOf(DatabaseDiscoveryHeartBeatConfiguration.class));
        assertThat(heartbeats.get("mgr-heartbeat").getProps().getProperty("keep-alive-cron"), is("0/5 * * * * ?"));
    }
    
    private void assertDefaultDataSourceRule(final DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig) {
        assertThat(dataSourceRuleConfig.getDataSourceNames(), is(Arrays.asList("ds_0", "ds_1", "ds_2")));
        assertThat(dataSourceRuleConfig.getDiscoveryHeartbeatName(), is("mgr-heartbeat"));
        assertThat(dataSourceRuleConfig.getDiscoveryTypeName(), is("mgr"));
        assertThat(dataSourceRuleConfig.getGroupName(), is("readwrite_ds"));
    }
}
