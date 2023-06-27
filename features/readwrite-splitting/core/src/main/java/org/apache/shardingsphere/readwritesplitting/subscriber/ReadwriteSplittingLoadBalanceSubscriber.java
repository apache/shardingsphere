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
import lombok.Setter;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.YamlAlgorithmConfigurationSwapper;
import org.apache.shardingsphere.mode.event.config.DatabaseRuleConfigurationChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.subsciber.RuleChangedSubscriber;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.event.loadbalance.AlterLoadBalanceEvent;
import org.apache.shardingsphere.readwritesplitting.event.loadbalance.DeleteLoadBalanceEvent;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Readwrite-splitting load-balance subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
@Setter
public final class ReadwriteSplittingLoadBalanceSubscriber implements RuleChangedSubscriber {
    
    private ContextManager contextManager;
    
    private InstanceContext instanceContext;
    
    /**
     * Renew with alter load-balance.
     *
     * @param event alter load-balance event
     */
    @Subscribe
    public synchronized void renew(final AlterLoadBalanceEvent event) {
        if (!event.getActiveVersion().equals(instanceContext.getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        AlgorithmConfiguration needToAltered =
                swapToAlgorithmConfig(instanceContext.getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(),
                getConfig(contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName()), event.getLoadBalanceName(), needToAltered)));
    }
    
    /**
     * Renew with delete load-balance.
     *
     * @param event delete load-balance event
     */
    @Subscribe
    public synchronized void renew(final DeleteLoadBalanceEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ReadwriteSplittingRuleConfiguration config = (ReadwriteSplittingRuleConfiguration) database.getRuleMetaData().getSingleRule(ReadwriteSplittingRule.class).getConfiguration();
        config.getLoadBalancers().remove(event.getLoadBalanceName());
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    private ReadwriteSplittingRuleConfiguration getConfig(final ShardingSphereDatabase database, final String loadBalanceName, final AlgorithmConfiguration needToAltered) {
        Optional<ReadwriteSplittingRule> rule = database.getRuleMetaData().findSingleRule(ReadwriteSplittingRule.class);
        if (rule.isPresent()) {
            return getConfig((ReadwriteSplittingRuleConfiguration) rule.get().getConfiguration(), loadBalanceName, needToAltered);
        }
        Map<String, AlgorithmConfiguration> loadBalancers = new LinkedHashMap<>();
        loadBalancers.put(loadBalanceName, needToAltered);
        return new ReadwriteSplittingRuleConfiguration(Collections.emptyList(), loadBalancers);
    }
    
    private ReadwriteSplittingRuleConfiguration getConfig(final ReadwriteSplittingRuleConfiguration result, final String loadBalanceName, final AlgorithmConfiguration needToAltered) {
        if (null == result.getLoadBalancers()) {
            Map<String, AlgorithmConfiguration> loadBalancers = new LinkedHashMap<>();
            loadBalancers.put(loadBalanceName, needToAltered);
            return new ReadwriteSplittingRuleConfiguration(result.getDataSources(), loadBalancers);
        }
        result.getLoadBalancers().put(loadBalanceName, needToAltered);
        return result;
    }
    
    private AlgorithmConfiguration swapToAlgorithmConfig(final String yamlContext) {
        return new YamlAlgorithmConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContext, YamlAlgorithmConfiguration.class));
    }
}
