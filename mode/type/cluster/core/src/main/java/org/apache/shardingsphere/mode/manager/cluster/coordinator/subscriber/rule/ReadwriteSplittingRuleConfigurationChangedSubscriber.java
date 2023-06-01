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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.subscriber.rule;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.database.DatabaseRulesBuilder;
import org.apache.shardingsphere.infra.rule.identifier.type.StaticDataSourceContainedRule;
import org.apache.shardingsphere.mode.event.config.readwritesplitting.configuration.AddReadwriteSplittingConfigurationEvent;
import org.apache.shardingsphere.mode.event.config.readwritesplitting.configuration.AlterReadwriteSplittingConfigurationEvent;
import org.apache.shardingsphere.mode.event.config.readwritesplitting.configuration.DeleteReadwriteSplittingConfigurationEvent;
import org.apache.shardingsphere.mode.event.config.readwritesplitting.loadbalance.AddLoadBalanceEvent;
import org.apache.shardingsphere.mode.event.config.readwritesplitting.loadbalance.AlterLoadBalanceEvent;
import org.apache.shardingsphere.mode.event.config.readwritesplitting.loadbalance.DeleteLoadBalanceEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.readwritesplitting.LoadBalanceConfigurationChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.readwritesplitting.ReadwriteSplittingRuleConfigurationChangedEvent;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Readwrite-splitting rule configuration changed subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
public final class ReadwriteSplittingRuleConfigurationChangedSubscriber {
    
    private final ContextManager contextManager;
    
    private final InstanceContext instanceContext;
    
    public ReadwriteSplittingRuleConfigurationChangedSubscriber(final ContextManager contextManager) {
        this.contextManager = contextManager;
        this.instanceContext = contextManager.getInstanceContext();
        instanceContext.getEventBusContext().register(this);
    }
    
    @Subscribe
    public synchronized void renew(ReadwriteSplittingRuleConfigurationChangedEvent event) {
        switch (event.getEventType()) {
            case ADDED:
                instanceContext.getEventBusContext().post(new AddReadwriteSplittingConfigurationEvent(event.getDatabaseName(), event.getData()));
                break;
            case UPDATED:
                instanceContext.getEventBusContext().post(new AlterReadwriteSplittingConfigurationEvent(event.getDatabaseName(), event.getGroupName(), event.getData()));
                break;
            default:
                instanceContext.getEventBusContext().post(new DeleteReadwriteSplittingConfigurationEvent(event.getDatabaseName(), event.getGroupName()));
        }
        refreshRule(event.getDatabaseName());
    }
    
    @Subscribe
    public synchronized void renew(LoadBalanceConfigurationChangedEvent event) {
        switch (event.getEventType()) {
            case ADDED:
                instanceContext.getEventBusContext().post(new AddLoadBalanceEvent(event.getDatabaseName(), event.getLoadBalanceName(), event.getData()));
                break;
            case UPDATED:
                instanceContext.getEventBusContext().post(new AlterLoadBalanceEvent(event.getDatabaseName(), event.getLoadBalanceName(), event.getData()));
                break;
            default:
                instanceContext.getEventBusContext().post(new DeleteLoadBalanceEvent(event.getDatabaseName(), event.getLoadBalanceName()));
        }
        refreshRule(event.getDatabaseName());
    }
    
    private void refreshRule(final String databaseName) {
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName);
        Collection<ShardingSphereRule> rules = new LinkedList<>(database.getRuleMetaData().getRules());
        rules.addAll(DatabaseRulesBuilder.build(databaseName, database.getResourceMetaData().getDataSources(), database.getRuleMetaData().getRules(),
                database.getRuleMetaData().getSingleRule(StaticDataSourceContainedRule.class).getConfiguration(), contextManager.getInstanceContext()));
        database.getRuleMetaData().getRules().addAll(rules);
    }
}
