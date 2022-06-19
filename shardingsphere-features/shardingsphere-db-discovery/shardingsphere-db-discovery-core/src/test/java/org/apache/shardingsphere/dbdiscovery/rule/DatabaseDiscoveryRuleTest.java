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

import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryHeartBeatConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class DatabaseDiscoveryRuleTest {
    
    private final Map<String, DataSource> dataSourceMap = Collections.singletonMap("primary", new MockedDataSource());
    
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
        Map<String, Collection<String>> expected = getDataSourceMapper();
        assertThat(actual, is(expected));
    }
    
    private Map<String, Collection<String>> getDataSourceMapper() {
        Map<String, Collection<String>> result = new HashMap<>(2, 1);
        result.put("ds_0", Collections.singletonList("ds_0"));
        result.put("ds_1", Collections.singletonList("ds_1"));
        return result;
    }
    
    @Test
    public void assertGetExportedMethods() {
        DatabaseDiscoveryRule databaseDiscoveryRule = createRule();
        assertThat(databaseDiscoveryRule.getExportData().get(ExportableConstants.EXPORT_DB_DISCOVERY_PRIMARY_DATA_SOURCES), is(Collections.singletonMap("test_pr", "primary")));
    }
    
    private DatabaseDiscoveryRule createRule() {
        DatabaseDiscoveryDataSourceRuleConfiguration config = new DatabaseDiscoveryDataSourceRuleConfiguration("test_pr", Arrays.asList("ds_0", "ds_1"), "", "CORE.FIXTURE");
        return new DatabaseDiscoveryRule("db_discovery", dataSourceMap, new DatabaseDiscoveryRuleConfiguration(
                Collections.singleton(config), Collections.singletonMap("discovery_heartbeat", new DatabaseDiscoveryHeartBeatConfiguration(new Properties())),
                Collections.singletonMap("CORE.FIXTURE", new ShardingSphereAlgorithmConfiguration("CORE.FIXTURE", new Properties()))));
    }
}
