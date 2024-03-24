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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.provider;

import org.apache.shardingsphere.distsql.handler.engine.update.ral.rule.spi.database.ImportRuleConfigurationChecker;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.exception.ServiceProviderNotFoundException;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReadwriteSplittingImportRuleConfigurationProviderTest {
    
    private final ReadwriteSplittingImportRuleConfigurationProvider importRuleConfigProvider = new ReadwriteSplittingImportRuleConfigurationProvider();
    
    @Test
    void assertCheckDataSources() {
        ShardingSphereDatabase database = mockDatabaseWithDataSource();
        ReadwriteSplittingRuleConfiguration currentRuleConfig = getRuleConfigWithNotExistedDataSources();
        assertThrows(MissingRequiredStorageUnitsException.class, () -> ImportRuleConfigurationChecker.checkRule(currentRuleConfig, database));
    }
    
    @Test
    void assertCheckLoadBalancers() {
        ReadwriteSplittingRuleConfiguration currentRuleConfig = createInvalidLoadBalancerRuleConfig();
        assertThrows(ServiceProviderNotFoundException.class, () -> importRuleConfigProvider.check("foo_db", currentRuleConfig));
    }
    
    private ShardingSphereDatabase mockDatabaseWithDataSource() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        Collection<String> dataSources = new LinkedList<>();
        dataSources.add("su_1");
        when(result.getRuleMetaData().getAttributes(DataSourceMapperRuleAttribute.class)).thenReturn(Collections.emptyList());
        when(result.getResourceMetaData().getNotExistedDataSources(any())).thenReturn(dataSources);
        return result;
    }
    
    private ReadwriteSplittingRuleConfiguration getRuleConfigWithNotExistedDataSources() {
        ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig =
                new ReadwriteSplittingDataSourceRuleConfiguration("data_source", "write_ds", Collections.emptyList(), "load_balancer");
        Collection<ReadwriteSplittingDataSourceRuleConfiguration> dataSources = new LinkedList<>();
        dataSources.add(dataSourceRuleConfig);
        return new ReadwriteSplittingRuleConfiguration(dataSources, Collections.emptyMap());
    }
    
    private ReadwriteSplittingRuleConfiguration createInvalidLoadBalancerRuleConfig() {
        Map<String, AlgorithmConfiguration> loadBalancer = new HashMap<>();
        loadBalancer.put("invalid_load_balancer", mock(AlgorithmConfiguration.class));
        return new ReadwriteSplittingRuleConfiguration(Collections.emptyList(), loadBalancer);
    }
}
