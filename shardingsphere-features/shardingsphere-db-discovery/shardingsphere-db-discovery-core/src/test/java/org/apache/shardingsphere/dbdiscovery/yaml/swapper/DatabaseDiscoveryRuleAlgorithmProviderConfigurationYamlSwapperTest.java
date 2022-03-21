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

package org.apache.shardingsphere.dbdiscovery.yaml.swapper;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.dbdiscovery.algorithm.config.AlgorithmProvidedDatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryHeartBeatConfiguration;
import org.apache.shardingsphere.dbdiscovery.constant.DatabaseDiscoveryOrder;
import org.apache.shardingsphere.dbdiscovery.mysql.type.MGRDatabaseDiscoveryType;
import org.apache.shardingsphere.dbdiscovery.yaml.config.YamlDatabaseDiscoveryRuleConfiguration;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class DatabaseDiscoveryRuleAlgorithmProviderConfigurationYamlSwapperTest {
    
    private final DatabaseDiscoveryRuleAlgorithmProviderConfigurationYamlSwapper swapper = new DatabaseDiscoveryRuleAlgorithmProviderConfigurationYamlSwapper();
    
    @Test
    public void assertSwapToYamlConfiguration() {
        YamlDatabaseDiscoveryRuleConfiguration actual = createYamlHARuleConfiguration();
        assertNotNull(actual);
        assertNotNull(actual.getDataSources());
        assertThat(actual.getDataSources().keySet(), is(Collections.singleton("name")));
        assertNotNull(actual.getDiscoveryHeartbeats());
        assertThat(actual.getDiscoveryHeartbeats().keySet(), is(Collections.singleton("mgr_heartbeat")));
    }
    
    @Test
    public void assertSwapToObject() {
        AlgorithmProvidedDatabaseDiscoveryRuleConfiguration actual = swapper.swapToObject(createYamlHARuleConfiguration());
        assertNotNull(actual);
        assertNotNull(actual.getDataSources());
        assertTrue(actual.getDataSources().iterator().hasNext());
        DatabaseDiscoveryDataSourceRuleConfiguration ruleConfig = actual.getDataSources().iterator().next();
        assertNotNull(ruleConfig);
        assertThat(ruleConfig.getGroupName(), is("name"));
        assertNotNull(actual.getDiscoveryHeartbeats());
        assertThat(actual.getDiscoveryHeartbeats().keySet(), is(Collections.singleton("mgr_heartbeat")));
    }
    
    @Test
    public void assertGetTypeClass() {
        assertThat(swapper.getTypeClass(), equalTo(AlgorithmProvidedDatabaseDiscoveryRuleConfiguration.class));
    }
    
    @Test
    public void assertGetRuleTagName() {
        assertThat(swapper.getRuleTagName(), is("DB_DISCOVERY"));
    }
    
    @Test
    public void assertGetOrder() {
        assertThat(swapper.getOrder(), is(DatabaseDiscoveryOrder.ALGORITHM_PROVIDER_ORDER));
    }
    
    private YamlDatabaseDiscoveryRuleConfiguration createYamlHARuleConfiguration() {
        DatabaseDiscoveryDataSourceRuleConfiguration ruleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("name",
                Collections.singletonList("dataSourceNames"), "mgr_heartbeat", "discoveryTypeName");
        Map<String, DatabaseDiscoveryHeartBeatConfiguration> heartBeatConfig = new LinkedHashMap<>();
        heartBeatConfig.put("mgr_heartbeat", new DatabaseDiscoveryHeartBeatConfiguration(new Properties()));
        return swapper.swapToYamlConfiguration(
                new AlgorithmProvidedDatabaseDiscoveryRuleConfiguration(Collections.singletonList(ruleConfig), heartBeatConfig,
                        ImmutableMap.of("mgr", new MGRDatabaseDiscoveryType())));
    }
}
