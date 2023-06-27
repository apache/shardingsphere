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
import lombok.Setter;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.mode.event.config.DatabaseRuleConfigurationChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.subsciber.RuleChangedSubscriber;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.event.algorithm.AlterDefaultShadowAlgorithmNameEvent;
import org.apache.shardingsphere.shadow.event.algorithm.DeleteDefaultShadowAlgorithmNameEvent;
import org.apache.shardingsphere.shadow.rule.ShadowRule;

import java.util.Optional;

/**
 * Default shadow algorithm name subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
@Setter
public final class DefaultShadowAlgorithmNameSubscriber implements RuleChangedSubscriber {
    
    private ContextManager contextManager;
    
    private InstanceContext instanceContext;
    
    /**
     * Renew with alter default algorithm name.
     *
     * @param event alter default algorithm name event
     */
    @Subscribe
    public synchronized void renew(final AlterDefaultShadowAlgorithmNameEvent event) {
        Optional<ShadowRule> rule = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName()).getRuleMetaData().findSingleRule(ShadowRule.class);
        ShadowRuleConfiguration config;
        if (rule.isPresent()) {
            config = (ShadowRuleConfiguration) rule.get().getConfiguration();
        } else {
            config = new ShadowRuleConfiguration();
        }
        config.setDefaultShadowAlgorithmName(instanceContext.getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion()));
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete default algorithm name.
     *
     * @param event delete default algorithm name event
     */
    @Subscribe
    public synchronized void renew(final DeleteDefaultShadowAlgorithmNameEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShadowRuleConfiguration config = (ShadowRuleConfiguration) database.getRuleMetaData().getSingleRule(ShadowRule.class).getConfiguration();
        config.setDefaultShadowAlgorithmName(null);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
}
