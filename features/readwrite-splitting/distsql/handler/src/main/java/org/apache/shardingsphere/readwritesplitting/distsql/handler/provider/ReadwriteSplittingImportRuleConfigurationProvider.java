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
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.infra.algorithm.load.balancer.core.LoadBalanceAlgorithm;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.identifier.scope.DatabaseRule;
import org.apache.shardingsphere.infra.rule.identifier.type.datasource.DataSourceMapperRule;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

/**
 * Readwrite-splitting import rule configuration provider.
 */
public final class ReadwriteSplittingImportRuleConfigurationProvider implements ImportRuleConfigurationProvider {
    
    @Override
    public void check(final ShardingSphereDatabase database, final RuleConfiguration ruleConfig) {
        if (null == database || null == ruleConfig) {
            return;
        }
        ReadwriteSplittingRuleConfiguration readwriteSplittingRuleConfig = (ReadwriteSplittingRuleConfiguration) ruleConfig;
        checkDataSources(database, readwriteSplittingRuleConfig);
        checkLoadBalancers(readwriteSplittingRuleConfig);
    }
    
    @Override
    public DatabaseRule build(final ShardingSphereDatabase database, final RuleConfiguration ruleConfig, final InstanceContext instanceContext) {
        return new ReadwriteSplittingRule(database.getName(), (ReadwriteSplittingRuleConfiguration) ruleConfig, instanceContext);
    }
    
    private void checkDataSources(final ShardingSphereDatabase database, final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        Collection<String> requiredDataSources = new LinkedHashSet<>();
        for (ReadwriteSplittingDataSourceRuleConfiguration each : currentRuleConfig.getDataSources()) {
            if (null != each.getWriteDataSourceName()) {
                requiredDataSources.add(each.getWriteDataSourceName());
            }
            if (!each.getReadDataSourceNames().isEmpty()) {
                requiredDataSources.addAll(each.getReadDataSourceNames());
            }
        }
        Collection<String> notExistedDataSources = database.getResourceMetaData().getNotExistedDataSources(requiredDataSources);
        Collection<String> logicalDataSources = getLogicDataSources(database);
        notExistedDataSources.removeIf(logicalDataSources::contains);
        ShardingSpherePreconditions.checkState(notExistedDataSources.isEmpty(), () -> new MissingRequiredStorageUnitsException(database.getName(), notExistedDataSources));
    }
    
    private Collection<String> getLogicDataSources(final ShardingSphereDatabase database) {
        Collection<String> result = new HashSet<>();
        for (DataSourceMapperRule each : database.getRuleMetaData().getRuleIdentifiers(DataSourceMapperRule.class)) {
            result.addAll(each.getDataSourceMapper().keySet());
        }
        return result;
    }
    
    private void checkLoadBalancers(final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getLoadBalancers().values().forEach(each -> TypedSPILoader.checkService(LoadBalanceAlgorithm.class, each.getType(), each.getProps()));
    }
    
    @Override
    public Class<? extends RuleConfiguration> getType() {
        return ReadwriteSplittingRuleConfiguration.class;
    }
}
