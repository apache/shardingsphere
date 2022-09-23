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

package org.apache.shardingsphere.dbdiscovery.distsql.handler.update;

import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.DropDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.infra.distsql.constant.ExportableItemConstants;
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RuleInUsedException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.ExportableRule;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DropDatabaseDiscoveryRuleStatementUpdaterTest {
    
    private final DropDatabaseDiscoveryRuleStatementUpdater updater = new DropDatabaseDiscoveryRuleStatementUpdater();
    
    private ShardingSphereDatabase database;
    
    @Before
    public void init() {
        database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.emptyList()));
    }
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertCheckSQLStatementWithoutCurrentRule() throws DistSQLException {
        updater.checkSQLStatement(database, createSQLStatement(), null);
    }
    
    @Test(expected = MissingRequiredRuleException.class)
    public void assertCheckSQLStatementWithoutToBeDroppedRules() throws DistSQLException {
        updater.checkSQLStatement(database, createSQLStatement(), new DatabaseDiscoveryRuleConfiguration(Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap()));
    }
    
    @Test(expected = RuleInUsedException.class)
    public void assertCheckSQLStatementWithRuleInUsed() throws DistSQLException {
        when(database.getRuleMetaData()).thenReturn(mock(ShardingSphereRuleMetaData.class, RETURNS_DEEP_STUBS));
        ExportableRule exportableRule = mock(ExportableRule.class);
        when(exportableRule.getExportData()).thenReturn(getExportData());
        when(database.getRuleMetaData().findRules(ExportableRule.class)).thenReturn(Collections.singleton(exportableRule));
        DatabaseDiscoveryDataSourceRuleConfiguration configuration = new DatabaseDiscoveryDataSourceRuleConfiguration("ha_group", null, null, null);
        updater.checkSQLStatement(database, createSQLStatement(),
                new DatabaseDiscoveryRuleConfiguration(Collections.singletonList(configuration), Collections.emptyMap(), Collections.emptyMap()));
    }
    
    private Map<String, Object> getExportData() {
        Map<String, Object> result = new HashMap<>(2, 1);
        result.put(ExportableConstants.EXPORT_DYNAMIC_READWRITE_SPLITTING_RULE, exportDynamicDataSources());
        result.put(ExportableConstants.EXPORT_STATIC_READWRITE_SPLITTING_RULE, exportStaticDataSources());
        return result;
    }
    
    private Map<String, Map<String, String>> exportDynamicDataSources() {
        Map<String, String> result = new LinkedHashMap<>(3, 1);
        result.put(ExportableItemConstants.AUTO_AWARE_DATA_SOURCE_NAME, "ha_group");
        result.put(ExportableItemConstants.PRIMARY_DATA_SOURCE_NAME, "write_ds");
        result.put(ExportableItemConstants.REPLICA_DATA_SOURCE_NAMES, "read_ds_0, read_ds_1");
        return Collections.singletonMap("dynamic_rule", result);
    }
    
    private Map<String, Map<String, String>> exportStaticDataSources() {
        Map<String, String> result = new LinkedHashMap<>(2, 1);
        result.put(ExportableItemConstants.PRIMARY_DATA_SOURCE_NAME, "write_ds");
        result.put(ExportableItemConstants.REPLICA_DATA_SOURCE_NAMES, "read_ds");
        return Collections.singletonMap("static_rule", result);
    }
    
    @Test
    public void assertUpdateCurrentRuleConfiguration() {
        DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfig = createCurrentRuleConfiguration();
        updater.updateCurrentRuleConfiguration(createSQLStatement(), databaseDiscoveryRuleConfig);
        assertTrue(databaseDiscoveryRuleConfig.getDataSources().isEmpty());
        assertThat(databaseDiscoveryRuleConfig.getDiscoveryTypes().size(), is(1));
    }
    
    @Test
    public void assertUpdateCurrentRuleConfigurationWithIfExists() throws DistSQLException {
        DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfig = createCurrentRuleConfiguration();
        DropDatabaseDiscoveryRuleStatement dropDatabaseDiscoveryRuleStatement = createSQLStatementWithIfExists();
        updater.checkSQLStatement(database, dropDatabaseDiscoveryRuleStatement, databaseDiscoveryRuleConfig);
        assertFalse(updater.updateCurrentRuleConfiguration(dropDatabaseDiscoveryRuleStatement, databaseDiscoveryRuleConfig));
        assertThat(databaseDiscoveryRuleConfig.getDataSources().size(), is(1));
        assertThat(databaseDiscoveryRuleConfig.getDiscoveryTypes().size(), is(1));
    }
    
    @Test
    public void assertUpdateCurrentRuleConfigurationWithInUsedDiscoveryType() {
        DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfig = createMultipleCurrentRuleConfigurations();
        assertFalse(updater.updateCurrentRuleConfiguration(createSQLStatement(), databaseDiscoveryRuleConfig));
        assertThat(databaseDiscoveryRuleConfig.getDiscoveryTypes().size(), is(1));
    }
    
    private DropDatabaseDiscoveryRuleStatement createSQLStatement() {
        return new DropDatabaseDiscoveryRuleStatement(Collections.singleton("ha_group"));
    }
    
    private DropDatabaseDiscoveryRuleStatement createSQLStatementWithIfExists() {
        return new DropDatabaseDiscoveryRuleStatement(true, Collections.singleton("ha_group_0"));
    }
    
    private DatabaseDiscoveryRuleConfiguration createCurrentRuleConfiguration() {
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("ha_group", Collections.emptyList(), "ha_heartbeat", "readwrite_ds_MGR");
        Map<String, AlgorithmConfiguration> discoveryTypes = Collections.singletonMap(
                "readwrite_ds_MGR", new AlgorithmConfiguration("readwrite_ds_MGR", new Properties()));
        return new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(Collections.singleton(dataSourceRuleConfig)), Collections.emptyMap(), discoveryTypes);
    }
    
    private DatabaseDiscoveryRuleConfiguration createMultipleCurrentRuleConfigurations() {
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("ha_group", Collections.emptyList(), "ha_heartbeat", "readwrite_ds_MGR");
        Map<String, AlgorithmConfiguration> discoveryTypes = Collections.singletonMap(
                "readwrite_ds_MGR", new AlgorithmConfiguration("readwrite_ds_MGR", new Properties()));
        return new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(Arrays.asList(dataSourceRuleConfig,
                new DatabaseDiscoveryDataSourceRuleConfiguration("ha_group_another", Collections.emptyList(), "ha_heartbeat", "readwrite_ds_MGR"))), Collections.emptyMap(), discoveryTypes);
    }
}
