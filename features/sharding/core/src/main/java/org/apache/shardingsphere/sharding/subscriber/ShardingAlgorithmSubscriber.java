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

package org.apache.shardingsphere.sharding.subscriber;

import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.RuleConfigurationSubscribeCoordinator;
import org.apache.shardingsphere.mode.event.config.DatabaseRuleConfigurationChangedEvent;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.event.algorithm.auditor.AddAuditorEvent;
import org.apache.shardingsphere.sharding.event.algorithm.auditor.AlterAuditorEvent;
import org.apache.shardingsphere.sharding.event.algorithm.auditor.DeleteAuditorEvent;
import org.apache.shardingsphere.sharding.event.algorithm.keygenerator.AddKeyGeneratorEvent;
import org.apache.shardingsphere.sharding.event.algorithm.keygenerator.AlterKeyGeneratorEvent;
import org.apache.shardingsphere.sharding.event.algorithm.keygenerator.DeleteKeyGeneratorEvent;
import org.apache.shardingsphere.sharding.event.algorithm.sharding.AddShardingAlgorithmEvent;
import org.apache.shardingsphere.sharding.event.algorithm.sharding.AlterShardingAlgorithmEvent;
import org.apache.shardingsphere.sharding.event.algorithm.sharding.DeleteShardingAlgorithmEvent;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Sharding algorithm subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
@RequiredArgsConstructor
public final class ShardingAlgorithmSubscriber implements RuleConfigurationSubscribeCoordinator {
    
    private Map<String, ShardingSphereDatabase> databases;
    
    private InstanceContext instanceContext;
    
    @Override
    public void registerRuleConfigurationSubscriber(final Map<String, ShardingSphereDatabase> databases, final InstanceContext instanceContext) {
        this.databases = databases;
        this.instanceContext = instanceContext;
        instanceContext.getEventBusContext().register(this);
    }
    
    /**
     * Renew with add sharding algorithm.
     *
     * @param event add sharding algorithm event
     */
    @Subscribe
    public synchronized void renew(final AddShardingAlgorithmEvent<AlgorithmConfiguration> event) {
        renewShardingAlgorithm(event.getDatabaseName(), event.getAlgorithmName(), event.getConfig());
    }
    
    /**
     * Renew with add sharding algorithm.
     *
     * @param event add sharding algorithm event
     */
    @Subscribe
    public synchronized void renew(final AddKeyGeneratorEvent<AlgorithmConfiguration> event) {
        renewKeyGenerator(event.getDatabaseName(), event.getKeyGeneratorName(), event.getConfig());
    }
    
    /**
     * Renew with add sharding algorithm.
     *
     * @param event add sharding algorithm event
     */
    @Subscribe
    public synchronized void renew(final AddAuditorEvent<AlgorithmConfiguration> event) {
        renewAuditor(event.getDatabaseName(), event.getAuditorName(), event.getConfig());
    }
    
    /**
     * Renew with alter sharding algorithm.
     *
     * @param event alter sharding algorithm event
     */
    @Subscribe
    public synchronized void renew(final AlterShardingAlgorithmEvent<AlgorithmConfiguration> event) {
        renewShardingAlgorithm(event.getDatabaseName(), event.getAlgorithmName(), event.getConfig());
    }
    
    /**
     * Renew with alter sharding algorithm.
     *
     * @param event alter sharding algorithm event
     */
    @Subscribe
    public synchronized void renew(final AlterKeyGeneratorEvent<AlgorithmConfiguration> event) {
        renewKeyGenerator(event.getDatabaseName(), event.getKeyGeneratorName(), event.getConfig());
    }
    
    /**
     * Renew with alter sharding algorithm.
     *
     * @param event alter sharding algorithm event
     */
    @Subscribe
    public synchronized void renew(final AlterAuditorEvent<AlgorithmConfiguration> event) {
        renewAuditor(event.getDatabaseName(), event.getAuditorName(), event.getConfig());
    }
    
    /**
     * Renew with delete sharding algorithm.
     *
     * @param event delete sharding algorithm event
     */
    @Subscribe
    public synchronized void renew(final DeleteShardingAlgorithmEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        Collection<RuleConfiguration> ruleConfigs = new LinkedList<>(database.getRuleMetaData().getConfigurations());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.getShardingAlgorithms().remove(event.getAlgorithmName());
        database.getRuleMetaData().getConfigurations().addAll(ruleConfigs);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete key generator.
     *
     * @param event delete key generator event
     */
    @Subscribe
    public synchronized void renew(final DeleteKeyGeneratorEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        Collection<RuleConfiguration> ruleConfigs = new LinkedList<>(database.getRuleMetaData().getConfigurations());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.getKeyGenerators().remove(event.getKeyGeneratorName());
        database.getRuleMetaData().getConfigurations().addAll(ruleConfigs);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete key generator.
     *
     * @param event delete key generator event
     */
    @Subscribe
    public synchronized void renew(final DeleteAuditorEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        Collection<RuleConfiguration> ruleConfigs = new LinkedList<>(database.getRuleMetaData().getConfigurations());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.getAuditors().remove(event.getAuditorName());
        database.getRuleMetaData().getConfigurations().addAll(ruleConfigs);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    private void renewShardingAlgorithm(final String databaseName, final String algorithmName, final AlgorithmConfiguration algorithmConfig) {
        ShardingSphereDatabase database = databases.get(databaseName);
        Collection<RuleConfiguration> ruleConfigs = new LinkedList<>(database.getRuleMetaData().getConfigurations());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.getShardingAlgorithms().put(algorithmName, algorithmConfig);
        database.getRuleMetaData().getConfigurations().addAll(ruleConfigs);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(databaseName, config));
    }
    
    private void renewKeyGenerator(final String databaseName, final String algorithmName, final AlgorithmConfiguration algorithmConfig) {
        ShardingSphereDatabase database = databases.get(databaseName);
        Collection<RuleConfiguration> ruleConfigs = new LinkedList<>(database.getRuleMetaData().getConfigurations());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.getKeyGenerators().put(algorithmName, algorithmConfig);
        database.getRuleMetaData().getConfigurations().addAll(ruleConfigs);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(databaseName, config));
    }
    
    private void renewAuditor(final String databaseName, final String algorithmName, final AlgorithmConfiguration algorithmConfig) {
        ShardingSphereDatabase database = databases.get(databaseName);
        Collection<RuleConfiguration> ruleConfigs = new LinkedList<>(database.getRuleMetaData().getConfigurations());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.getAuditors().put(algorithmName, algorithmConfig);
        database.getRuleMetaData().getConfigurations().addAll(ruleConfigs);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(databaseName, config));
    }
}
