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

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.RuleConfigurationSubscribeCoordinator;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.event.config.DatabaseRuleConfigurationChangedEvent;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.transaction.TransactionalReadQueryStrategy;
import org.apache.shardingsphere.readwritesplitting.event.datasource.AddReadwriteSplittingDataSourceEvent;
import org.apache.shardingsphere.readwritesplitting.event.datasource.AlterReadwriteSplittingDataSourceEvent;
import org.apache.shardingsphere.readwritesplitting.event.datasource.DeleteReadwriteSplittingDataSourceEvent;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.readwritesplitting.yaml.config.rule.YamlReadwriteSplittingDataSourceRuleConfiguration;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Readwrite-splitting configuration subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
@RequiredArgsConstructor
public final class ReadwriteSplittingDataSourceSubscriber implements RuleConfigurationSubscribeCoordinator {
    
    private Map<String, ShardingSphereDatabase> databases;
    
    private InstanceContext instanceContext;
    
    @Override
    public void registerRuleConfigurationSubscriber(final Map<String, ShardingSphereDatabase> databases, final InstanceContext instanceContext) {
        this.databases = databases;
        this.instanceContext = instanceContext;
        instanceContext.getEventBusContext().register(this);
    }
    
    /**
     * Renew with add readwrite-splitting configuration.
     *
     * @param event add readwrite-splitting configuration event
     */
    @Subscribe
    public synchronized void renew(final AddReadwriteSplittingDataSourceEvent event) {
        if (!event.getActiveVersion().equals(instanceContext.getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ReadwriteSplittingDataSourceRuleConfiguration needToAddedConfig = swapDataSource(event.getGroupName(),
                instanceContext.getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        Optional<ReadwriteSplittingRule> rule = database.getRuleMetaData().findSingleRule(ReadwriteSplittingRule.class);
        ReadwriteSplittingRuleConfiguration config;
        if (rule.isPresent()) {
            config = (ReadwriteSplittingRuleConfiguration) rule.get().getConfiguration();
            config.getDataSources().add(needToAddedConfig);
        } else {
            config = new ReadwriteSplittingRuleConfiguration(Collections.singletonList(needToAddedConfig), Collections.emptyMap());
        }
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with alter readwrite-splitting configuration.
     *
     * @param event alter readwrite-splitting configuration event
     */
    @Subscribe
    public synchronized void renew(final AlterReadwriteSplittingDataSourceEvent event) {
        if (!event.getActiveVersion().equals(instanceContext.getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ReadwriteSplittingDataSourceRuleConfiguration needToAlteredConfig = swapDataSource(event.getGroupName(),
                instanceContext.getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        ReadwriteSplittingRuleConfiguration config = (ReadwriteSplittingRuleConfiguration) database.getRuleMetaData().getSingleRule(ReadwriteSplittingRule.class).getConfiguration();
        config.getDataSources().removeIf(each -> each.getName().equals(needToAlteredConfig.getName()));
        config.getDataSources().add(needToAlteredConfig);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete readwrite-splitting configuration.
     *
     * @param event delete readwrite-splitting configuration event
     */
    @Subscribe
    public synchronized void renew(final DeleteReadwriteSplittingDataSourceEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        ReadwriteSplittingRuleConfiguration config = (ReadwriteSplittingRuleConfiguration) database.getRuleMetaData().getSingleRule(ReadwriteSplittingRule.class).getConfiguration();
        config.getDataSources().removeIf(each -> each.getName().equals(event.getGroupName()));
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
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
}
