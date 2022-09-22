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
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.mode.PersistRepositoryConfiguration;
import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.schedule.core.ScheduleContextFactory;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.BeforeClass;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DatabaseDiscoveryRuleTest {
    
    private final Map<String, DataSource> dataSourceMap = Collections.singletonMap("primary_ds", new MockedDataSource());
    
    @BeforeClass
    public static void setUp() {
        ScheduleContextFactory.newInstance(new ModeConfiguration("Cluster", mock(PersistRepositoryConfiguration.class), false));
    }
    
    @Test
    public void assertFindDataSourceRule() {
        Optional<DatabaseDiscoveryDataSourceRule> actual = createRule().findDataSourceRule("replica_ds");
        assertTrue(actual.isPresent());
        assertDataSourceRule(actual.get());
    }
    
    @Test
    public void assertGetSingleDataSourceRule() {
        assertDataSourceRule(createRule().getSingleDataSourceRule());
    }
    
    private void assertDataSourceRule(final DatabaseDiscoveryDataSourceRule actual) {
        assertThat(actual.getGroupName(), is("replica_ds"));
        assertThat(actual.getDataSourceNames(), is(Arrays.asList("primary_ds", "replica_ds_0", "replica_ds_1")));
    }
    
    @Test
    public void assertGetDataSourceMapper() {
        DatabaseDiscoveryRule databaseDiscoveryRule = createRule();
        Map<String, Collection<String>> actual = databaseDiscoveryRule.getDataSourceMapper();
        Map<String, Collection<String>> expected = getDataSourceMapper();
        assertThat(actual, is(expected));
    }
    
    private Map<String, Collection<String>> getDataSourceMapper() {
        Map<String, Collection<String>> result = new HashMap<>(1, 1);
        result.put("replica_ds", Collections.singletonList("replica_ds_1"));
        return result;
    }
    
    @Test
    public void assertGetExportedMethods() {
        DatabaseDiscoveryRule databaseDiscoveryRule = createRule();
        assertThat(databaseDiscoveryRule.getExportData().get(ExportableConstants.EXPORT_DB_DISCOVERY_PRIMARY_DATA_SOURCES), is(Collections.singletonMap("replica_ds", "primary_ds")));
    }
    
    private DatabaseDiscoveryRule createRule() {
        DatabaseDiscoveryDataSourceRuleConfiguration config =
                new DatabaseDiscoveryDataSourceRuleConfiguration("replica_ds", Arrays.asList("primary_ds", "replica_ds_0", "replica_ds_1"), "", "CORE.FIXTURE");
        InstanceContext instanceContext = mock(InstanceContext.class, RETURNS_DEEP_STUBS);
        when(instanceContext.getInstance().getCurrentInstanceId()).thenReturn("foo_id");
        return new DatabaseDiscoveryRule("db_discovery", dataSourceMap, new DatabaseDiscoveryRuleConfiguration(
                Collections.singleton(config), Collections.singletonMap("discovery_heartbeat", new DatabaseDiscoveryHeartBeatConfiguration(new Properties())),
                Collections.singletonMap("CORE.FIXTURE", new AlgorithmConfiguration("CORE.FIXTURE", new Properties()))), instanceContext);
    }
}
