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

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceNameDisabledEvent;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class DatabaseDiscoveryRuleTest {
    
    private final Map<String, DataSource> dataSourceMap = Collections.singletonMap("ds", mock(DataSource.class));
    
    @Test(expected = IllegalArgumentException.class)
    public void assertNewWithEmptyDataSourceRule() {
        new DatabaseDiscoveryRule(new DatabaseDiscoveryRuleConfiguration(Collections.emptyList(), Collections.emptyMap()), mock(DatabaseType.class), dataSourceMap, "ha_db");
    }
    
    @Test
    public void assertFindDataSourceRule() {
        Optional<DatabaseDiscoveryDataSourceRule> actual = createHARule().findDataSourceRule("test_pr");
        assertTrue(actual.isPresent());
        assertDataSourceRule(actual.get());
    }
    
    @Test
    public void assertGetSingleDataSourceRule() {
        assertDataSourceRule(createHARule().getSingleDataSourceRule());
    }
    
    private DatabaseDiscoveryRule createHARule() {
        DatabaseDiscoveryDataSourceRuleConfiguration config =
                new DatabaseDiscoveryDataSourceRuleConfiguration("test_pr", Arrays.asList("ds_0", "ds_1"), "discoveryTypeName");
        return new DatabaseDiscoveryRule(new DatabaseDiscoveryRuleConfiguration(
                Collections.singleton(config), ImmutableMap.of("mgr", new ShardingSphereAlgorithmConfiguration("MGR", new Properties()))),
                mock(DatabaseType.class), dataSourceMap, "ha_db");
    }
    
    private void assertDataSourceRule(final DatabaseDiscoveryDataSourceRule actual) {
        assertThat(actual.getName(), is("test_pr"));
        assertThat(actual.getDataSourceNames(), is(Arrays.asList("ds_0", "ds_1")));
    }
    
    @Test
    public void assertUpdateRuleStatusWithNotExistDataSource() {
        DatabaseDiscoveryRule databaseDiscoveryRule = createHARule();
        databaseDiscoveryRule.updateRuleStatus(new DataSourceNameDisabledEvent("db", true));
        assertThat(databaseDiscoveryRule.getSingleDataSourceRule().getDataSourceNames(), is(Arrays.asList("ds_0", "ds_1")));
    }
    
    @Test
    public void assertUpdateRuleStatus() {
        DatabaseDiscoveryRule databaseDiscoveryRule = createHARule();
        databaseDiscoveryRule.updateRuleStatus(new DataSourceNameDisabledEvent("ds_0", true));
        assertThat(databaseDiscoveryRule.getSingleDataSourceRule().getDataSourceNames(), is(Collections.singletonList("ds_1")));
    }
    
    @Test
    public void assertUpdateRuleStatusWithEnable() {
        DatabaseDiscoveryRule databaseDiscoveryRule = createHARule();
        databaseDiscoveryRule.updateRuleStatus(new DataSourceNameDisabledEvent("ds_0", true));
        assertThat(databaseDiscoveryRule.getSingleDataSourceRule().getDataSourceNames(), is(Collections.singletonList("ds_1")));
        databaseDiscoveryRule.updateRuleStatus(new DataSourceNameDisabledEvent("ds_0", false));
        assertThat(databaseDiscoveryRule.getSingleDataSourceRule().getDataSourceNames(), is(Arrays.asList("ds_0", "ds_1")));
    }
    
    @Test
    public void assertGetDataSourceMapper() {
        DatabaseDiscoveryRule databaseDiscoveryRule = createHARule();
        Map<String, Collection<String>> actual = databaseDiscoveryRule.getDataSourceMapper();
        Map<String, Collection<String>> expected = ImmutableMap.of("test_pr", Arrays.asList("ds_0", "ds_1"));
        assertThat(actual, is(expected));
    }
}
