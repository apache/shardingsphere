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

import org.apache.shardingsphere.distsql.handler.exception.rule.DuplicateRuleException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingRuleConfigurationImportCheckerTest {
    
    @Test
    void assertCheckLogicTables() {
        assertThrows(DuplicateRuleException.class, () -> new ShardingRuleConfigurationImportChecker().check(mock(ShardingSphereDatabase.class), createDuplicatedTablesRuleConfiguration()));
    }
    
    @Test
    void assertCheckDataSources() {
        ShardingRuleConfiguration currentRuleConfig = new ShardingRuleConfiguration();
        currentRuleConfig.getTables().add(new ShardingTableRuleConfiguration("t_order", "ds_${0..2}.t_order_${0..2}"));
        assertThrows(MissingRequiredStorageUnitsException.class, () -> new ShardingRuleConfigurationImportChecker().check(mockDatabaseWithDataSource(), currentRuleConfig));
    }
    
    @Test
    void assertCheckKeyGenerators() {
        assertThrows(ServiceProviderNotFoundException.class, () -> new ShardingRuleConfigurationImportChecker().check(mockDatabase(), createInvalidKeyGeneratorRuleConfiguration()));
    }
    
    @Test
    void assertCheckShardingAlgorithms() {
        assertThrows(ServiceProviderNotFoundException.class, () -> new ShardingRuleConfigurationImportChecker().check(mockDatabase(), createInvalidShardingAlgorithmRuleConfiguration()));
    }
    
    private ShardingRuleConfiguration createDuplicatedTablesRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(new ShardingTableRuleConfiguration("t_order", null));
        result.getTables().add(new ShardingTableRuleConfiguration("t_order", null));
        return result;
    }
    
    private ShardingSphereDatabase mockDatabaseWithDataSource() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        Collection<String> dataSources = new LinkedList<>();
        dataSources.add("su_1");
        when(result.getResourceMetaData().getNotExistedDataSources(any())).thenReturn(dataSources);
        when(result.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.emptyList()));
        return result;
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getResourceMetaData().getNotExistedDataSources(any())).thenReturn(Collections.emptyList());
        when(result.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.emptyList()));
        return result;
    }
    
    private ShardingRuleConfiguration createInvalidKeyGeneratorRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getKeyGenerators().put("Invalid_key_generator", mock(AlgorithmConfiguration.class));
        return result;
    }
    
    private ShardingRuleConfiguration createInvalidShardingAlgorithmRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getShardingAlgorithms().put("Invalid_algorithm", mock(AlgorithmConfiguration.class));
        return result;
    }
}
