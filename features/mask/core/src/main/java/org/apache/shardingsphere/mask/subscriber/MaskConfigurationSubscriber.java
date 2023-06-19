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

package org.apache.shardingsphere.mask.subscriber;

import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.RuleConfigurationSubscribeCoordinator;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.event.config.AddMaskConfigurationEvent;
import org.apache.shardingsphere.mask.event.config.AlterMaskConfigurationEvent;
import org.apache.shardingsphere.mask.event.config.DeleteMaskConfigurationEvent;
import org.apache.shardingsphere.mask.rule.MaskRule;
import org.apache.shardingsphere.mode.event.config.DatabaseRuleConfigurationChangedEvent;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Mask configuration subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
@RequiredArgsConstructor
public final class MaskConfigurationSubscriber implements RuleConfigurationSubscribeCoordinator {
    
    private Map<String, ShardingSphereDatabase> databases;
    
    private InstanceContext instanceContext;
    
    @Override
    public void registerRuleConfigurationSubscriber(final Map<String, ShardingSphereDatabase> databases, final InstanceContext instanceContext) {
        this.databases = databases;
        this.instanceContext = instanceContext;
        instanceContext.getEventBusContext().register(this);
    }
    
    /**
     * Renew with add mask configuration.
     *
     * @param event add mask configuration event
     */
    @Subscribe
    public synchronized void renew(final AddMaskConfigurationEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        MaskTableRuleConfiguration needToAddedConfig = event.getConfig();
        Optional<MaskRule> rule = database.getRuleMetaData().findSingleRule(MaskRule.class);
        MaskRuleConfiguration config;
        if (rule.isPresent()) {
            config = (MaskRuleConfiguration) rule.get().getConfiguration();
            config.getTables().removeIf(each -> each.getName().equals(needToAddedConfig.getName()));
            config.getTables().add(needToAddedConfig);
        } else {
            config = new MaskRuleConfiguration(Collections.singletonList(needToAddedConfig), Collections.emptyMap());
        }
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with alter mask configuration.
     *
     * @param event alter mask configuration event
     */
    @Subscribe
    public synchronized void renew(final AlterMaskConfigurationEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        MaskTableRuleConfiguration needToAlteredConfig = event.getConfig();
        MaskRuleConfiguration config = (MaskRuleConfiguration) database.getRuleMetaData().getSingleRule(MaskRule.class).getConfiguration();
        config.getTables().removeIf(each -> each.getName().equals(event.getTableName()));
        config.getTables().add(needToAlteredConfig);
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    /**
     * Renew with delete mask configuration.
     *
     * @param event delete mask configuration event
     */
    @Subscribe
    public synchronized void renew(final DeleteMaskConfigurationEvent event) {
        ShardingSphereDatabase database = databases.get(event.getDatabaseName());
        MaskRuleConfiguration config = (MaskRuleConfiguration) database.getRuleMetaData().getSingleRule(MaskRule.class).getConfiguration();
        config.getTables().removeIf(each -> each.getName().equals(event.getTableName()));
        instanceContext.getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
}
