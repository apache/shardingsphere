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
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.database.DatabaseRulesBuilder;
import org.apache.shardingsphere.mode.event.config.RuleConfigurationChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Collection;
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
    
    @Subscribe
    private synchronized void renew(final RuleConfigurationChangedEvent event) {
        String databaseName = event.getDatabaseName();
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName);
        Collection<ShardingSphereRule> rules = new LinkedList<>(database.getRuleMetaData().getRules());
        rules.addAll(DatabaseRulesBuilder.build(databaseName, database.getResourceMetaData().getDataSources(), database.getRuleMetaData().getRules(),
                event.getRuleConfig(), contextManager.getInstanceContext()));
        database.getRuleMetaData().getRules().addAll(rules);
    }
}
