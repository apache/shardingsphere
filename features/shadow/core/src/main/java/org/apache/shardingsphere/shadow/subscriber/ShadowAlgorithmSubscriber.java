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

package org.apache.shardingsphere.shadow.subscriber;

import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.RuleConfigurationSubscribeCoordinator;
import org.apache.shardingsphere.mode.event.config.DatabaseRuleConfigurationChangedEvent;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.event.algorithm.AddShadowAlgorithmEvent;
import org.apache.shardingsphere.shadow.event.algorithm.AlterShadowAlgorithmEvent;
import org.apache.shardingsphere.shadow.event.algorithm.DeleteShadowAlgorithmEvent;
import org.apache.shardingsphere.shadow.rule.ShadowRule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Shadow algorithm subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
@RequiredArgsConstructor
public final class ShadowAlgorithmSubscriber implements RuleConfigurationSubscribeCoordinator {
    
    private Map<String, ShardingSphereDatabase> databases;
    
    private InstanceContext instanceContext;
    
    @Override
    public void registerRuleConfigurationSubscriber(final Map<String, ShardingSphereDatabase> databases, final InstanceContext instanceContext) {
        this.databases = databases;
        this.instanceContext = instanceContext;
        instanceContext.getEventBusContext().register(this);
    }
    
    /**
     * Renew with add algorithm.
     *
     * @param event add algorithm event
     */
    @Subscribe
    public synchronized void renew(final AddShadowAlgorithmEvent<AlgorithmConfiguration> event) {
        renew(event.getDatabaseName(), event.getAlgorithmName(), event.getConfig());
    }
    
    /**
     * Renew with alter algorithm.
     *
     * @param event alter algorithm event
     */
    @Subscribe
    public synchronized void renew(final AlterShadowAlgorithmEvent<AlgorithmConfiguration> event) {
        renew(event.getDatabaseName(), event.getAlgorithmName(), event.getConfig());
    }
    
    private void renew(final String databaseName, final String algorithmName, final AlgorithmConfiguration algorithmConfig) {
        ShardingSphereDatabase database = databases.get(databaseName);
        Collection<RuleConfiguration> ruleConfigs = new LinkedList<>(database.getRuleMetaData().getConfigurations());
        ShadowRuleConfiguration config = (ShadowRuleConfiguration) database.getRuleMetaData().getSingleRule(ShadowRule.class).getConfiguration();
        config.getShadowAlgorithms().put(algorithmName, algorithmConfig);
        database.getRuleMetaData().getConfigurations().addAll(ruleConfigs);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(databaseName, config));
    }
    
    /**
     * Renew with delete algorithm.
     *
     * @param event delete algorithm event
     */
    @Subscribe
    public synchronized void renew(final DeleteShadowAlgorithmEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        Collection<RuleConfiguration> ruleConfigs = new LinkedList<>(database.getRuleMetaData().getConfigurations());
        ShadowRuleConfiguration config = (ShadowRuleConfiguration) database.getRuleMetaData().getSingleRule(ShadowRule.class).getConfiguration();
        config.getShadowAlgorithms().remove(event.getAlgorithmName());
        database.getRuleMetaData().getConfigurations().addAll(ruleConfigs);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
}
