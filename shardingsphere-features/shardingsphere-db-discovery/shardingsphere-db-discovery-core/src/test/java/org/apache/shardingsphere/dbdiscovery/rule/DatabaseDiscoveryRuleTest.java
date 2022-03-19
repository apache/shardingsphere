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
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryHeartBeatConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class DatabaseDiscoveryRuleTest {
    
    private final Map<String, DataSource> dataSourceMap = Collections.singletonMap("ds", mock(DataSource.class));
    
    @Test
    public void assertNewWithEmptyDataSourceRule() {
        new DatabaseDiscoveryRule("db_discovery", dataSourceMap, new DatabaseDiscoveryRuleConfiguration(Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap()));
    }
    
    @Test
    public void assertFindDataSourceRule() {
        Optional<DatabaseDiscoveryDataSourceRule> actual = createRule().findDataSourceRule("test_pr");
        assertTrue(actual.isPresent());
        assertDataSourceRule(actual.get());
    }
    
    @Test
    public void assertGetSingleDataSourceRule() {
        assertDataSourceRule(createRule().getSingleDataSourceRule());
    }
    
    private void assertDataSourceRule(final DatabaseDiscoveryDataSourceRule actual) {
        assertThat(actual.getGroupName(), is("test_pr"));
        assertThat(actual.getDataSourceNames(), is(Arrays.asList("ds_0", "ds_1")));
    }
    
    @Test
    public void assertGetDataSourceMapper() {
        DatabaseDiscoveryRule databaseDiscoveryRule = createRule();
        Map<String, Collection<String>> actual = databaseDiscoveryRule.getDataSourceMapper();
        Map<String, Collection<String>> expected = ImmutableMap.of("ds_0", Collections.singletonList("ds_0"), "ds_1", Collections.singletonList("ds_1"));
        assertThat(actual, is(expected));
    }
    
    @Test
    public void assertGetRuleType() {
        DatabaseDiscoveryRule databaseDiscoveryRule = createRule();
        assertThat(databaseDiscoveryRule.getType(), is(DatabaseDiscoveryRule.class.getSimpleName()));
    }
    
    @Test
    public void assertGetExportedMethods() {
        DatabaseDiscoveryRule databaseDiscoveryRule = createRule();
        Map<String, String> singleDataSourceRuleMap = new HashMap<>(1, 1);
        singleDataSourceRuleMap.put("test_pr", "primary");
        assertThat(databaseDiscoveryRule.getExportedMethods().get(ExportableConstants.EXPORTABLE_KEY_PRIMARY_DATA_SOURCE).get(), is(singleDataSourceRuleMap));
    }
    
    private DatabaseDiscoveryRule createRule() {
        DatabaseDiscoveryDataSourceRuleConfiguration config = new DatabaseDiscoveryDataSourceRuleConfiguration("test_pr", Arrays.asList("ds_0", "ds_1"), "", "TEST");
        return new DatabaseDiscoveryRule("db_discovery", dataSourceMap, new DatabaseDiscoveryRuleConfiguration(
                Collections.singleton(config), Collections.singletonMap("discovery_heartbeat", new DatabaseDiscoveryHeartBeatConfiguration(new Properties())),
                ImmutableMap.of("TEST", new ShardingSphereAlgorithmConfiguration("TEST", new Properties()))));
    }
}
