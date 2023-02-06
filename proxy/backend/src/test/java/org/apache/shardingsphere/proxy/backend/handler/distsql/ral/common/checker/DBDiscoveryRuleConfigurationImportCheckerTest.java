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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.checker;

import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryHeartBeatConfiguration;
import org.apache.shardingsphere.distsql.handler.exception.algorithm.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.distsql.handler.exception.algorithm.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DBDiscoveryRuleConfigurationImportCheckerTest {
    
    private final DatabaseDiscoveryRuleConfigurationImportChecker databaseDiscoveryRuleConfigurationImportChecker = new DatabaseDiscoveryRuleConfigurationImportChecker();
    
    @Test(expected = MissingRequiredStorageUnitsException.class)
    public void assertCheckDataSources() {
        ShardingSphereDatabase database = mockDatabaseWithDataSource();
        DatabaseDiscoveryRuleConfiguration currentRuleConfig = getRuleConfigWithNotExistedDataSources();
        databaseDiscoveryRuleConfigurationImportChecker.check(database, currentRuleConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckDiscoveryTypes() {
        ShardingSphereDatabase database = mockDatabase();
        DatabaseDiscoveryRuleConfiguration currentRuleConfig = createRuleConfigWithInvalidDiscoveryType();
        databaseDiscoveryRuleConfigurationImportChecker.check(database, currentRuleConfig);
    }
    
    @Test(expected = MissingRequiredAlgorithmException.class)
    public void assertCheckDiscoveryHeartBeats() {
        ShardingSphereDatabase database = mockDatabase();
        DatabaseDiscoveryRuleConfiguration currentRuleConfig = createRuleConfigWithNotExistsHeartBeats();
        databaseDiscoveryRuleConfigurationImportChecker.check(database, currentRuleConfig);
    }
    
    private ShardingSphereDatabase mockDatabaseWithDataSource() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        Collection<String> dataSources = new LinkedList<>();
        dataSources.add("su_1");
        when(database.getResourceMetaData().getNotExistedDataSources(any())).thenReturn(dataSources);
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        return database;
    }
    
    private DatabaseDiscoveryRuleConfiguration getRuleConfigWithNotExistedDataSources() {
        List<String> dataSourcesNames = new LinkedList<>();
        dataSourcesNames.add("ds_1");
        dataSourcesNames.add("ds_2");
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("groups", dataSourcesNames, "heart_beat", "type");
        Collection<DatabaseDiscoveryDataSourceRuleConfiguration> dataSources = new LinkedList<>();
        dataSources.add(dataSourceRuleConfig);
        return new DatabaseDiscoveryRuleConfiguration(dataSources, Collections.emptyMap(), Collections.emptyMap());
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResourceMetaData().getNotExistedDataSources(any())).thenReturn(Collections.emptyList());
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        return database;
    }
    
    private DatabaseDiscoveryRuleConfiguration createRuleConfigWithInvalidDiscoveryType() {
        Map<String, AlgorithmConfiguration> discoveryType = new HashMap<>();
        discoveryType.put("invalid_discovery_type", mock(AlgorithmConfiguration.class));
        return new DatabaseDiscoveryRuleConfiguration(mock(Collection.class), mock(Map.class), discoveryType);
    }
    
    private DatabaseDiscoveryRuleConfiguration createRuleConfigWithNotExistsHeartBeats() {
        Map<String, DatabaseDiscoveryHeartBeatConfiguration> heartBeats = new HashMap<>();
        heartBeats.put("heart_beat", mock(DatabaseDiscoveryHeartBeatConfiguration.class));
        List<String> dataSourcesNames = new LinkedList<>();
        dataSourcesNames.add("ds_1");
        dataSourcesNames.add("ds_2");
        DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig = new DatabaseDiscoveryDataSourceRuleConfiguration("groups", dataSourcesNames, "heart_beat", "type");
        Collection<DatabaseDiscoveryDataSourceRuleConfiguration> dataSources = new LinkedList<>();
        dataSources.add(dataSourceRuleConfig);
        return new DatabaseDiscoveryRuleConfiguration(dataSources, heartBeats, mock(Map.class));
    }
}
