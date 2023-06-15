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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.subscriber;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.infra.config.rule.global.event.AlterGlobalRuleConfigurationEvent;
import org.apache.shardingsphere.infra.config.rule.global.event.DeleteGlobalRuleConfigurationEvent;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.database.DatabaseRulesBuilder;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.mode.event.config.DatabaseRuleConfigurationChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * TODO Rename ConfigurationChangedSubscriber when metadata structure adjustment completed. #25485
 * New Configuration changed subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
public final class NewConfigurationChangedSubscriber {
    
    private final ContextManager contextManager;
    
    public NewConfigurationChangedSubscriber(final ContextManager contextManager) {
        this.contextManager = contextManager;
        contextManager.getInstanceContext().getEventBusContext().register(this);
    }
    
    /**
     * Renew for database rule configuration.
     *
     * @param event database rule changed event
     */
    @Subscribe
    public synchronized void renew(final DatabaseRuleConfigurationChangedEvent event) {
        String databaseName = event.getDatabaseName();
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName);
        Collection<ShardingSphereRule> rules = new LinkedList<>(database.getRuleMetaData().getRules());
        rules.addAll(DatabaseRulesBuilder.build(databaseName, database.getResourceMetaData().getDataSources(), database.getRuleMetaData().getRules(),
                event.getRuleConfig(), contextManager.getInstanceContext()));
        database.getRuleMetaData().getRules().clear();
        database.getRuleMetaData().getRules().addAll(rules);
    }
    
    /**
     * Renew for global rule configuration.
     *
     * @param event global rule alter event
     */
    @Subscribe
    public synchronized void renew(final AlterGlobalRuleConfigurationEvent event) {
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabase(event.getDatabaseName());
        Collection<ShardingSphereRule> rules = removeSingleGlobalRule(database, event.getRuleSimpleName());
        rules.addAll(GlobalRulesBuilder.buildRules(Collections.singletonList(event.getConfig()), contextManager.getMetaDataContexts().getMetaData().getDatabases(),
                contextManager.getMetaDataContexts().getMetaData().getProps()));
        database.getRuleMetaData().getRules().clear();
        database.getRuleMetaData().getRules().addAll(rules);
    }
    
    /**
     * Renew for global rule configuration.
     *
     * @param event global rule delete event
     */
    @Subscribe
    public synchronized void renew(final DeleteGlobalRuleConfigurationEvent event) {
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabase(event.getDatabaseName());
        Collection<ShardingSphereRule> rules = removeSingleGlobalRule(database, event.getRuleSimpleName());
        database.getRuleMetaData().getRules().clear();
        database.getRuleMetaData().getRules().addAll(rules);
    }
    
    private Collection<ShardingSphereRule> removeSingleGlobalRule(final ShardingSphereDatabase database, final String ruleSimpleName) {
        Collection<ShardingSphereRule> result = new LinkedList<>(database.getRuleMetaData().getRules());
        for (ShardingSphereRule each : result) {
            if (!each.getType().equals(ruleSimpleName)) {
                continue;
            }
            result.remove(each);
        }
        return result;
    }
}
