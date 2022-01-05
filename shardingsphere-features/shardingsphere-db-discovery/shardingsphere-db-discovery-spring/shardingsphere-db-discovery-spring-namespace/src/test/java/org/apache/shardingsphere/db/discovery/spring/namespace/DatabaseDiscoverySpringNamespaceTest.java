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

package org.apache.shardingsphere.db.discovery.spring.namespace;

import org.apache.shardingsphere.dbdiscovery.algorithm.config.AlgorithmProvidedDatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryHeartBeatConfiguration;
import org.apache.shardingsphere.dbdiscovery.mgr.MGRDatabaseDiscoveryType;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@ContextConfiguration(locations = "classpath:META-INF/spring/database-discovery-application-context.xml")
public final class DatabaseDiscoverySpringNamespaceTest extends AbstractJUnit4SpringContextTests {
    
    @Resource
    private DatabaseDiscoveryType mGRDatabaseDiscoveryType;
    
    @Resource
    private AlgorithmProvidedDatabaseDiscoveryRuleConfiguration defaultRule;
    
    @Test
    public void assertMGRDatabaseDiscoveryType() {
        assertThat(mGRDatabaseDiscoveryType.getType(), is("MGR"));
    }
    
    @Test
    public void assertDefaultDataSource() {
        assertDiscoveryTypes(defaultRule.getDiscoveryTypes());
        assertHeartbeats(defaultRule.getDiscoveryHeartbeats());
        assertThat(defaultRule.getDataSources().size(), is(1));
        assertDefaultDataSourceRule(defaultRule.getDataSources().iterator().next());
    }
    
    private void assertDiscoveryTypes(final Map<String, DatabaseDiscoveryType> discoveryTypes) {
        assertThat(discoveryTypes.size(), is(1));
        assertThat(discoveryTypes.get("mGRDatabaseDiscoveryType"), instanceOf(MGRDatabaseDiscoveryType.class));
    }
    
    private void assertHeartbeats(final Map<String, DatabaseDiscoveryHeartBeatConfiguration> heartbeats) {
        assertThat(heartbeats.size(), is(1));
        assertThat(heartbeats.get("defaultDiscoveryHeartbeat"), instanceOf(DatabaseDiscoveryHeartBeatConfiguration.class));
    }
    
    private void assertDefaultDataSourceRule(final DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig) {
        assertThat(dataSourceRuleConfig.getDataSourceNames(), is(Collections.singletonList("defaultDs")));
        assertThat(dataSourceRuleConfig.getDiscoveryHeartbeatName(), is("defaultHeartbeat"));
        assertThat(dataSourceRuleConfig.getDiscoveryTypeName(), is("defaultDiscoveryType"));
        assertThat(dataSourceRuleConfig.getGroupName(), is("defaultDsRule"));
    }
}
