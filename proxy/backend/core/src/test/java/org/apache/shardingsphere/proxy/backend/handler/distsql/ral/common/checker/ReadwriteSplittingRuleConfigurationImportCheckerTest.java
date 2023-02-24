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
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.DynamicReadwriteSplittingStrategyConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.strategy.StaticReadwriteSplittingStrategyConfiguration;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ReadwriteSplittingRuleConfigurationImportCheckerTest {
    
    private final ReadwriteSplittingRuleConfigurationImportChecker readwriteRuleConfigurationImportChecker = new ReadwriteSplittingRuleConfigurationImportChecker();
    
    @Test(expected = MissingRequiredStorageUnitsException.class)
    public void assertCheckDataSources() {
        ShardingSphereDatabase database = mockDatabaseWithDataSource();
        ReadwriteSplittingRuleConfiguration currentRuleConfig = getRuleConfigWithNotExistedDataSources();
        readwriteRuleConfigurationImportChecker.check(database, currentRuleConfig);
    }
    
    @Test(expected = InvalidAlgorithmConfigurationException.class)
    public void assertCheckLoadBalancers() {
        ShardingSphereDatabase database = mockDatabase();
        ReadwriteSplittingRuleConfiguration currentRuleConfig = createInvalidLoadBalancerRuleConfig();
        readwriteRuleConfigurationImportChecker.check(database, currentRuleConfig);
    }
    
    private ShardingSphereDatabase mockDatabaseWithDataSource() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        Collection<String> dataSources = new LinkedList<>();
        dataSources.add("su_1");
        when(database.getResourceMetaData().getNotExistedDataSources(any())).thenReturn(dataSources);
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        return database;
    }
    
    private ReadwriteSplittingRuleConfiguration getRuleConfigWithNotExistedDataSources() {
        StaticReadwriteSplittingStrategyConfiguration staticStrategy = new StaticReadwriteSplittingStrategyConfiguration("write_ds", Collections.emptyList());
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig = new ReadwriteSplittingDataSourceRuleConfiguration("data_source", staticStrategy,
                mock(DynamicReadwriteSplittingStrategyConfiguration.class), "load_balancer");
        Collection<ReadwriteSplittingDataSourceRuleConfiguration> dataSources = new LinkedList<>();
        dataSources.add(dataSourceRuleConfig);
        return new ReadwriteSplittingRuleConfiguration(dataSources, Collections.emptyMap());
    }
    
    private ShardingSphereDatabase mockDatabase() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResourceMetaData().getNotExistedDataSources(any())).thenReturn(Collections.emptyList());
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        return database;
    }
    
    private ReadwriteSplittingRuleConfiguration createInvalidLoadBalancerRuleConfig() {
        Map<String, AlgorithmConfiguration> loadBalancer = new HashMap<>();
        loadBalancer.put("invalid_load_balancer", mock(AlgorithmConfiguration.class));
        return new ReadwriteSplittingRuleConfiguration(mock(Collection.class), loadBalancer);
    }
}
