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
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.RuleConfigurationSubscribeCoordinator;
import org.apache.shardingsphere.mode.event.config.RuleConfigurationChangedEvent;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.event.loadbalance.AddLoadBalanceEvent;
import org.apache.shardingsphere.readwritesplitting.event.loadbalance.AlterLoadBalanceEvent;
import org.apache.shardingsphere.readwritesplitting.event.loadbalance.DeleteLoadBalanceEvent;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;

import java.util.Map;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Readwrite-splitting load-balance subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
@RequiredArgsConstructor
public final class ReadwriteSplittingLoadBalanceSubscriber implements RuleConfigurationSubscribeCoordinator {
    
    private Map<String, ShardingSphereDatabase> databases;
    
    private InstanceContext instanceContext;
    
    @Override
    public void registerRuleConfigurationSubscriber(final Map<String, ShardingSphereDatabase> databases, final InstanceContext instanceContext) {
        this.databases = databases;
        this.instanceContext = instanceContext;
        instanceContext.getEventBusContext().register(this);
    }
    
    /**
     * Renew with add load-balance.
     *
     * @param event add load-balance event
     */
    @Subscribe
    public synchronized void renew(final AddLoadBalanceEvent<AlgorithmConfiguration> event) {
        renew(event.getDatabaseName(), event.getLoadBalanceName(), event.getConfig());
    }
    
    /**
     * Renew with alter load-balance.
     *
     * @param event alter load-balance event
     */
    @Subscribe
    public synchronized void renew(final AlterLoadBalanceEvent<AlgorithmConfiguration> event) {
        renew(event.getDatabaseName(), event.getLoadBalanceName(), event.getConfig());
    }
    
    private void renew(final String databaseName, final String loadBalanceName, final AlgorithmConfiguration algorithmConfig) {
        ShardingSphereDatabase database = databases.get(databaseName);
        Collection<RuleConfiguration> ruleConfigs = new LinkedList<>(database.getRuleMetaData().getConfigurations());
        ReadwriteSplittingRuleConfiguration config = (ReadwriteSplittingRuleConfiguration) database.getRuleMetaData().getSingleRule(ReadwriteSplittingRule.class).getConfiguration();
        config.getLoadBalancers().put(loadBalanceName, algorithmConfig);
        ruleConfigs.add(config);
        database.getRuleMetaData().getConfigurations().addAll(ruleConfigs);
        instanceContext.getEventBusContext().post(new RuleConfigurationChangedEvent(databaseName, config));
    }
    
    /**
     * Renew with delete load-balance.
     *
     * @param event delete load-balance event
     */
    @Subscribe
    public synchronized void renew(final DeleteLoadBalanceEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        Collection<RuleConfiguration> ruleConfigs = new LinkedList<>(database.getRuleMetaData().getConfigurations());
        ReadwriteSplittingRuleConfiguration config = (ReadwriteSplittingRuleConfiguration) database.getRuleMetaData().getSingleRule(ReadwriteSplittingRule.class).getConfiguration();
        config.getLoadBalancers().remove(event.getLoadBalanceName());
        ruleConfigs.add(config);
        database.getRuleMetaData().getConfigurations().addAll(ruleConfigs);
        instanceContext.getEventBusContext().post(new RuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
}
