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
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryHeartBeatConfiguration;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.DropDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.distsql.handler.exception.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.distsql.handler.exception.rule.RuleInUsedException;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.ExportableRule;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.constant.ExportableConstants;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.constant.ExportableItemConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public final class DropDatabaseDiscoveryRuleStatementUpdaterTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @BeforeEach
    public void init() {
        when(database.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.emptyList()));
    }
    
    @Test
    public void assertCheckSQLStatementWithoutCurrentRule() {
        assertThrows(MissingRequiredRuleException.class, () -> new DropDatabaseDiscoveryRuleStatementUpdater().checkSQLStatement(database, createSQLStatement(), null));
    }
    
    @Test
    public void assertCheckSQLStatementWithoutToBeDroppedRules() {
        assertThrows(MissingRequiredRuleException.class, () -> new DropDatabaseDiscoveryRuleStatementUpdater().checkSQLStatement(
                database, createSQLStatement(), new DatabaseDiscoveryRuleConfiguration(Collections.emptyList(), Collections.emptyMap(), Collections.emptyMap())));
    }
    
    @Test
    public void assertCheckSQLStatementWithRuleInUsed() {
        when(database.getRuleMetaData()).thenReturn(mock(ShardingSphereRuleMetaData.class, RETURNS_DEEP_STUBS));
        ExportableRule exportableRule = mock(ExportableRule.class);
        when(exportableRule.getExportData()).thenReturn(getExportData());
        when(database.getRuleMetaData().findRules(ExportableRule.class)).thenReturn(Collections.singleton(exportableRule));
        DatabaseDiscoveryDataSourceRuleConfiguration config = new DatabaseDiscoveryDataSourceRuleConfiguration("ha_group", null, null, null);
        assertThrows(RuleInUsedException.class, () -> new DropDatabaseDiscoveryRuleStatementUpdater().checkSQLStatement(
                database, createSQLStatement(), new DatabaseDiscoveryRuleConfiguration(Collections.singletonList(config), Collections.emptyMap(), Collections.emptyMap())));
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
        assertTrue(new DropDatabaseDiscoveryRuleStatementUpdater().updateCurrentRuleConfiguration(createSQLStatement(), databaseDiscoveryRuleConfig));
        assertTrue(databaseDiscoveryRuleConfig.getDataSources().isEmpty());
        assertTrue(databaseDiscoveryRuleConfig.getDiscoveryTypes().isEmpty());
        assertTrue(databaseDiscoveryRuleConfig.getDiscoveryHeartbeats().isEmpty());
    }
    
    @Test
    public void assertUpdateCurrentRuleConfigurationWithIfExists() {
        DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfig = createCurrentRuleConfiguration();
        DropDatabaseDiscoveryRuleStatement dropDatabaseDiscoveryRuleStatement = createSQLStatementWithIfExists();
        DropDatabaseDiscoveryRuleStatementUpdater updater = new DropDatabaseDiscoveryRuleStatementUpdater();
        updater.checkSQLStatement(database, dropDatabaseDiscoveryRuleStatement, databaseDiscoveryRuleConfig);
        assertFalse(updater.updateCurrentRuleConfiguration(dropDatabaseDiscoveryRuleStatement, databaseDiscoveryRuleConfig));
        assertThat(databaseDiscoveryRuleConfig.getDataSources().size(), is(1));
        assertThat(databaseDiscoveryRuleConfig.getDiscoveryTypes().size(), is(1));
    }
    
    @Test
    public void assertUpdateCurrentRuleConfigurationWithInUsedDiscoveryType() {
        DatabaseDiscoveryRuleConfiguration databaseDiscoveryRuleConfig = createMultipleCurrentRuleConfigurations();
        assertFalse(new DropDatabaseDiscoveryRuleStatementUpdater().updateCurrentRuleConfiguration(createSQLStatement(), databaseDiscoveryRuleConfig));
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
        Map<String, AlgorithmConfiguration> discoveryTypes = new HashMap<>(Collections.singletonMap("readwrite_ds_MGR", new AlgorithmConfiguration("readwrite_ds_MGR", new Properties())));
        Map<String, DatabaseDiscoveryHeartBeatConfiguration> discoveryHeartbeats = new HashMap<>(
                Collections.singletonMap("unused_heartbeat", new DatabaseDiscoveryHeartBeatConfiguration(new Properties())));
        return new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(Collections.singleton(dataSourceRuleConfig)), discoveryHeartbeats, discoveryTypes);
    }
    
    private DatabaseDiscoveryRuleConfiguration createMultipleCurrentRuleConfigurations() {
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("ha_group", Collections.emptyList(), "ha_heartbeat", "readwrite_ds_MGR");
        Map<String, AlgorithmConfiguration> discoveryTypes = Collections.singletonMap(
                "readwrite_ds_MGR", new AlgorithmConfiguration("readwrite_ds_MGR", new Properties()));
        return new DatabaseDiscoveryRuleConfiguration(new LinkedList<>(Arrays.asList(dataSourceRuleConfig,
                new DatabaseDiscoveryDataSourceRuleConfiguration("ha_group_another", Collections.emptyList(), "ha_heartbeat", "readwrite_ds_MGR"))), Collections.emptyMap(), discoveryTypes);
    }
}
