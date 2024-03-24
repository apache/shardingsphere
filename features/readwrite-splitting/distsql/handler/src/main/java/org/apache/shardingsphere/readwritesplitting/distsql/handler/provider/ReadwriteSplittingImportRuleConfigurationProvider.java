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

import org.apache.shardingsphere.distsql.handler.engine.update.ral.rule.spi.database.ImportRuleConfigurationProvider;
import org.apache.shardingsphere.infra.algorithm.loadbalancer.core.LoadBalanceAlgorithm;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Readwrite-splitting import rule configuration provider.
 */
public final class ReadwriteSplittingImportRuleConfigurationProvider implements ImportRuleConfigurationProvider<ReadwriteSplittingRuleConfiguration> {
    
    @Override
    public void check(final String databaseName, final ReadwriteSplittingRuleConfiguration ruleConfig) {
        checkLoadBalancers(ruleConfig);
    }
    
    private void checkLoadBalancers(final ReadwriteSplittingRuleConfiguration ruleConfig) {
        ruleConfig.getLoadBalancers().values().forEach(each -> TypedSPILoader.checkService(LoadBalanceAlgorithm.class, each.getType(), each.getProps()));
    }
    
    @Override
    public Collection<String> getRequiredDataSourceNames(final ReadwriteSplittingRuleConfiguration ruleConfig) {
        Collection<String> result = new LinkedHashSet<>();
        for (ReadwriteSplittingDataSourceRuleConfiguration each : ruleConfig.getDataSources()) {
            if (null != each.getWriteDataSourceName()) {
                result.add(each.getWriteDataSourceName());
            }
            if (!each.getReadDataSourceNames().isEmpty()) {
                result.addAll(each.getReadDataSourceNames());
            }
        }
        return result;
    }
    
    @Override
    public Class<ReadwriteSplittingRuleConfiguration> getType() {
        return ReadwriteSplittingRuleConfiguration.class;
    }
}
