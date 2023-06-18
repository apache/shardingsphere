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

package org.apache.shardingsphere.readwritesplitting.event;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.rule.event.GovernanceEvent;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.YamlAlgorithmConfigurationSwapper;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.spi.RuleConfigurationEventBuilder;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.transaction.TransactionalReadQueryStrategy;
import org.apache.shardingsphere.readwritesplitting.event.config.AddReadwriteSplittingConfigurationEvent;
import org.apache.shardingsphere.readwritesplitting.event.config.AlterReadwriteSplittingConfigurationEvent;
import org.apache.shardingsphere.readwritesplitting.event.config.DeleteReadwriteSplittingConfigurationEvent;
import org.apache.shardingsphere.readwritesplitting.event.loadbalance.AlterLoadBalanceEvent;
import org.apache.shardingsphere.readwritesplitting.event.loadbalance.DeleteLoadBalanceEvent;
import org.apache.shardingsphere.readwritesplitting.metadata.converter.ReadwriteSplittingNodeConverter;
import org.apache.shardingsphere.readwritesplitting.yaml.config.rule.YamlReadwriteSplittingDataSourceRuleConfiguration;

import java.util.Optional;

/**
 * Readwrite-splitting rule configuration event builder.
 */
public final class ReadwriteSplittingRuleConfigurationEventBuilder implements RuleConfigurationEventBuilder {
    
    @Override
    public Optional<GovernanceEvent> build(final String databaseName, final DataChangedEvent event) {
        if (!ReadwriteSplittingNodeConverter.isReadwriteSplittingPath(event.getKey()) || Strings.isNullOrEmpty(event.getValue())) {
            return Optional.empty();
        }
        if (ReadwriteSplittingNodeConverter.isDataSourcePath(event.getKey())) {
            Optional<String> groupNameVersion = ReadwriteSplittingNodeConverter.getGroupNameVersion(event.getKey());
            if (groupNameVersion.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
                return createReadwriteSplittingConfigEvent(databaseName, Integer.parseInt(groupNameVersion.get()), event);
            }
        }
        Optional<String> loadBalancerNameVersion = ReadwriteSplittingNodeConverter.getLoadBalancerNameVersion(event.getKey());
        if (loadBalancerNameVersion.isPresent() && !Strings.isNullOrEmpty(event.getValue())) {
            return createLoadBalanceEvent(databaseName, Integer.parseInt(loadBalancerNameVersion.get()), event);
        }
        return Optional.empty();
    }
    
    private Optional<GovernanceEvent> createReadwriteSplittingConfigEvent(final String databaseName, final int version, final DataChangedEvent event) {
        String groupName = ReadwriteSplittingNodeConverter.getGroupName(event.getKey()).orElse("");
        if (Type.ADDED == event.getType()) {
            return Optional.of(new AddReadwriteSplittingConfigurationEvent(databaseName, swapDataSource(groupName, event.getValue()), event.getKey(), version));
        }
        if (Type.UPDATED == event.getType()) {
            return Optional.of(new AlterReadwriteSplittingConfigurationEvent(databaseName, swapDataSource(groupName, event.getValue()), event.getKey(), version));
        }
        return Optional.of(new DeleteReadwriteSplittingConfigurationEvent(databaseName, groupName, event.getKey(), version));
    }
    
    private ReadwriteSplittingDataSourceRuleConfiguration swapDataSource(final String name, final String yamlContext) {
        YamlReadwriteSplittingDataSourceRuleConfiguration yamlDataSourceRuleConfig = YamlEngine.unmarshal(yamlContext, YamlReadwriteSplittingDataSourceRuleConfiguration.class);
        return new ReadwriteSplittingDataSourceRuleConfiguration(name, yamlDataSourceRuleConfig.getWriteDataSourceName(), yamlDataSourceRuleConfig.getReadDataSourceNames(),
                getTransactionalReadQueryStrategy(yamlDataSourceRuleConfig), yamlDataSourceRuleConfig.getLoadBalancerName());
    }
    
    private TransactionalReadQueryStrategy getTransactionalReadQueryStrategy(final YamlReadwriteSplittingDataSourceRuleConfiguration yamlDataSourceRuleConfig) {
        return Strings.isNullOrEmpty(yamlDataSourceRuleConfig.getTransactionalReadQueryStrategy())
                ? TransactionalReadQueryStrategy.DYNAMIC
                : TransactionalReadQueryStrategy.valueOf(yamlDataSourceRuleConfig.getTransactionalReadQueryStrategy());
    }
    
    private Optional<GovernanceEvent> createLoadBalanceEvent(final String databaseName, final int version, final DataChangedEvent event) {
        String loadBalanceName = ReadwriteSplittingNodeConverter.getLoadBalancerName(event.getKey()).orElse("");
        if (Type.ADDED == event.getType() || Type.UPDATED == event.getType()) {
            return Optional.of(new AlterLoadBalanceEvent(databaseName, loadBalanceName, swapToAlgorithmConfig(event.getValue()), event.getKey(), version));
        }
        return Optional.of(new DeleteLoadBalanceEvent(databaseName, loadBalanceName, event.getKey(), version));
    }
    
    private AlgorithmConfiguration swapToAlgorithmConfig(final String yamlContext) {
        return new YamlAlgorithmConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlAlgorithmConfiguration.class));
    }
}
