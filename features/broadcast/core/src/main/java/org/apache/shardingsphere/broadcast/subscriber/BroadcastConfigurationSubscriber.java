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

package org.apache.shardingsphere.broadcast.subscriber;

import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.broadcast.api.config.BroadcastRuleConfiguration;
import org.apache.shardingsphere.broadcast.event.config.AddBroadcastConfigurationEvent;
import org.apache.shardingsphere.broadcast.event.config.AlterBroadcastConfigurationEvent;
import org.apache.shardingsphere.broadcast.event.config.DeleteBroadcastConfigurationEvent;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.RuleConfigurationSubscribeCoordinator;
import org.apache.shardingsphere.mode.event.config.DatabaseRuleConfigurationChangedEvent;

import java.util.Map;
import java.util.Optional;

/**
 * Broadcast configuration subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
@RequiredArgsConstructor
public final class BroadcastConfigurationSubscriber implements RuleConfigurationSubscribeCoordinator {
    
    private Map<String, ShardingSphereDatabase> databases;
    
    private InstanceContext instanceContext;
    
    @Override
    public void registerRuleConfigurationSubscriber(final Map<String, ShardingSphereDatabase> databases, final InstanceContext instanceContext) {
        this.databases = databases;
        this.instanceContext = instanceContext;
        instanceContext.getEventBusContext().register(this);
    }
    
    /**
     * Renew with add broadcast configuration.
     *
     * @param event add broadcast configuration event
     */
    @Subscribe
    public synchronized void renew(final AddBroadcastConfigurationEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        BroadcastRuleConfiguration needToAddedConfig = event.getConfig();
        Optional<BroadcastRule> rule = database.getRuleMetaData().findSingleRule(BroadcastRule.class);
        BroadcastRuleConfiguration config;
        if (rule.isPresent()) {
            config = rule.get().getConfiguration();
            config.setTables(needToAddedConfig.getTables());
        } else {
            config = new BroadcastRuleConfiguration();
            config.setTables(needToAddedConfig.getTables());
        }
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with alter broadcast configuration.
     *
     * @param event alter broadcast configuration event
     */
    @Subscribe
    public synchronized void renew(final AlterBroadcastConfigurationEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        BroadcastRuleConfiguration needToAlteredConfig = event.getConfig();
        BroadcastRuleConfiguration config = database.getRuleMetaData().getSingleRule(BroadcastRule.class).getConfiguration();
        config.setTables(needToAlteredConfig.getTables());
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete broadcast configuration.
     *
     * @param event delete broadcast configuration event
     */
    @Subscribe
    public synchronized void renew(final DeleteBroadcastConfigurationEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        BroadcastRuleConfiguration config = database.getRuleMetaData().getSingleRule(BroadcastRule.class).getConfiguration();
        config.setTables(null);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
}
