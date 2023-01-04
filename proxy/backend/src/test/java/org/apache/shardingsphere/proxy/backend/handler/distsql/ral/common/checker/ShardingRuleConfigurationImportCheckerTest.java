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

import org.apache.shardingsphere.distsql.handler.exception.algorithm.InvalidAlgorithmConfigurationException;
import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingRuleConfigurationImportCheckerTest {
    
    private final ShardingRuleConfigurationImportChecker shardingRuleConfigurationImportChecker = new ShardingRuleConfigurationImportChecker();
    
    @Test(expected = DuplicateRuleException.class)
    public void assertCheckLogicTables() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        ShardingRuleConfiguration currentRuleConfig = createDuplicatedTablesRuleConfig();
        shardingRuleConfigurationImportChecker.check(database, currentRuleConfig);
    }
    
    @Test(expected = MissingRequiredStorageUnitsException.class)
    public void assertCheckDataSources() {
        ShardingSphereDatabase database = mockDatabaseWithDataSource();
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.getTables().add(new ShardingTableRuleConfiguration("t_order", "ds_${0..2}.t_order_${0..2}"));
        shardingRuleConfigurationImportChecker.check(database, currentRuleConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckKeyGenerators() {
        ShardingSphereDatabase database = mockDatabase();
        ShardingRuleConfiguration currentRuleConfig = createInvalidKeyGeneratorRuleConfig();
        shardingRuleConfigurationImportChecker.check(database, currentRuleConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckShardingAlgorithms() {
        ShardingSphereDatabase database = mockDatabase();
        ShardingRuleConfiguration currentRuleConfig = createInvalidShardingAlgorithmRuleConfig();
        shardingRuleConfigurationImportChecker.check(database, currentRuleConfig);
    }
    
    private ShardingRuleConfiguration createDuplicatedTablesRuleConfig() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.getTables().add(new ShardingTableRuleConfiguration("t_order"));
        ruleConfig.getTables().add(new ShardingTableRuleConfiguration("t_order"));
        return ruleConfig;
    }
    
    private ShardingSphereDatabase mockDatabaseWithDataSource() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        Collection<String> dataSources = new LinkedList<>();
        dataSources.add("su_1");
        when(database.getResourceMetaData().getNotExistedResources(any())).thenReturn(dataSources);
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        return database;
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResourceMetaData().getNotExistedResources(any())).thenReturn(Collections.emptyList());
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        return database;
    }
    
    private ShardingRuleConfiguration createInvalidKeyGeneratorRuleConfig() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.getKeyGenerators().put("Invalid_key_generator", mock(AlgorithmConfiguration.class));
        return ruleConfig;
    }
    
    private ShardingRuleConfiguration createInvalidShardingAlgorithmRuleConfig() {
        ShardingRuleConfiguration ruleConfig = new ShardingRuleConfiguration();
        ruleConfig.getShardingAlgorithms().put("Invalid_algorithm", mock(AlgorithmConfiguration.class));
        return ruleConfig;
    }
}
