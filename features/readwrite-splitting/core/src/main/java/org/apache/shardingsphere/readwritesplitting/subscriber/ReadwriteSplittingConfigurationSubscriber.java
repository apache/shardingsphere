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

package org.apache.shardingsphere.readwritesplitting.subscriber;

import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.RuleConfigurationSubscribeCoordinator;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.event.rule.readwritesplitting.configuration.AddReadwriteSplittingConfigurationEvent;
import org.apache.shardingsphere.mode.event.rule.readwritesplitting.configuration.AlterReadwriteSplittingConfigurationEvent;
import org.apache.shardingsphere.mode.event.rule.readwritesplitting.configuration.DeleteReadwriteSplittingConfigurationEvent;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Map;
import java.util.LinkedList;

/**
 * Readwrite-splitting configuration subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
@RequiredArgsConstructor
public final class ReadwriteSplittingConfigurationSubscriber implements RuleConfigurationSubscribeCoordinator {
    
    private Map<String, ShardingSphereDatabase> databases;
    
    @Override
    public void registerRuleConfigurationSubscriber(final Map<String, ShardingSphereDatabase> databases, final InstanceContext instanceContext) {
        this.databases = databases;
        instanceContext.getEventBusContext().register(this);
    }
    
    /**
     * Renew with add readwrite-splitting configuration.
     *
     * @param event add readwrite-splitting configuration event
     */
    @Subscribe
    public synchronized void renew(final AddReadwriteSplittingConfigurationEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ReadwriteSplittingDataSourceRuleConfiguration needToAddedConfig = YamlEngine.unmarshal(event.getData(), ReadwriteSplittingDataSourceRuleConfiguration.class);
        Collection<RuleConfiguration> ruleConfigs = new LinkedList<>(database.getRuleMetaData().getConfigurations());
        Optional<ReadwriteSplittingRule> rule = database.getRuleMetaData().findSingleRule(ReadwriteSplittingRule.class);
        if (rule.isPresent()) {
            ReadwriteSplittingRuleConfiguration config = (ReadwriteSplittingRuleConfiguration) rule.get().getConfiguration();
            config.getDataSources().add(needToAddedConfig);
            ruleConfigs.add(config);
        } else {
            ruleConfigs.add(new ReadwriteSplittingRuleConfiguration(Collections.singletonList(needToAddedConfig), Collections.emptyMap()));
        }
        database.getRuleMetaData().getConfigurations().addAll(ruleConfigs);
    }
    
    /**
     * Renew with alter readwrite-splitting configuration.
     *
     * @param event alter readwrite-splitting configuration event
     */
    @Subscribe
    public synchronized void renew(final AlterReadwriteSplittingConfigurationEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ReadwriteSplittingDataSourceRuleConfiguration needToAlteredConfig = YamlEngine.unmarshal(event.getData(), ReadwriteSplittingDataSourceRuleConfiguration.class);
        Collection<RuleConfiguration> ruleConfigs = new LinkedList<>(database.getRuleMetaData().getConfigurations());
        ReadwriteSplittingRuleConfiguration config = (ReadwriteSplittingRuleConfiguration) database.getRuleMetaData().getSingleRule(ReadwriteSplittingRule.class).getConfiguration();
        config.getDataSources().removeIf(each -> each.getName().equals(event.getGroupName()));
        ruleConfigs.add(new ReadwriteSplittingRuleConfiguration(Collections.singletonList(needToAlteredConfig), Collections.emptyMap()));
        database.getRuleMetaData().getConfigurations().addAll(ruleConfigs);
    }
    
    /**
     * Renew with delete readwrite-splitting configuration.
     *
     * @param event delete readwrite-splitting configuration event
     */
    @Subscribe
    public synchronized void renew(final DeleteReadwriteSplittingConfigurationEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        Collection<RuleConfiguration> ruleConfigs = new LinkedList<>(database.getRuleMetaData().getConfigurations());
        ReadwriteSplittingRuleConfiguration config = (ReadwriteSplittingRuleConfiguration) database.getRuleMetaData().getSingleRule(ReadwriteSplittingRule.class).getConfiguration();
        config.getDataSources().removeIf(each -> each.getName().equals(event.getGroupName()));
        ruleConfigs.add(config);
        database.getRuleMetaData().getConfigurations().addAll(ruleConfigs);
    }
}
