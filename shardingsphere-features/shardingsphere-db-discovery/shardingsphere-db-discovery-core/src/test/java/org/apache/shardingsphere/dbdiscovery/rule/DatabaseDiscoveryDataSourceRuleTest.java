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

package org.apache.shardingsphere.dbdiscovery.rule;

import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.mysql.type.MGRMySQLDatabaseDiscoveryProviderAlgorithm;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class DatabaseDiscoveryDataSourceRuleTest {
    
    private final DatabaseDiscoveryDataSourceRule databaseDiscoveryDataSourceRule = new DatabaseDiscoveryDataSourceRule(
            new DatabaseDiscoveryDataSourceRuleConfiguration("test_pr", Arrays.asList("ds_0", "ds_1"), "ha_heartbeat", "discoveryTypeName"), new Properties(),
            new MGRMySQLDatabaseDiscoveryProviderAlgorithm());
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewHADataSourceRuleWithoutName() {
        new DatabaseDiscoveryDataSourceRule(new DatabaseDiscoveryDataSourceRuleConfiguration("", Arrays.asList("ds_0", "ds_1"), "ha_heartbeat", "discoveryTypeName"),
                new Properties(), new MGRMySQLDatabaseDiscoveryProviderAlgorithm());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewHADataSourceRuleWithNullDataSourceName() {
        new DatabaseDiscoveryDataSourceRule(new DatabaseDiscoveryDataSourceRuleConfiguration("ds", null, "ha_heartbeat", "discoveryTypeName"),
                new Properties(), new MGRMySQLDatabaseDiscoveryProviderAlgorithm());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewHADataSourceRuleWithEmptyDataSourceName() {
        new DatabaseDiscoveryDataSourceRule(new DatabaseDiscoveryDataSourceRuleConfiguration("ds", Collections.emptyList(), "ha_heartbeat", "discoveryTypeName"),
                new Properties(), new MGRMySQLDatabaseDiscoveryProviderAlgorithm());
    }
    
    @Test
    public void assertGetDataSourceNamesWithoutDisabledDataSourceNames() {
        assertThat(databaseDiscoveryDataSourceRule.getDataSourceNames(), is(Arrays.asList("ds_0", "ds_1")));
    }
    
    @Test
    public void assertGetDataSourceMapper() {
        assertThat(databaseDiscoveryDataSourceRule.getDataSourceMapper(), is(getExpectedDataSourceMapper()));
    }
    
    private Map<String, Collection<String>> getExpectedDataSourceMapper() {
        Map<String, Collection<String>> result = new LinkedHashMap<>(2, 1);
        result.put("test_pr", Collections.singletonList("ds_1"));
        return result;
    }
}
