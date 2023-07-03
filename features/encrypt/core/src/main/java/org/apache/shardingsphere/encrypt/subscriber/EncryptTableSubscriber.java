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

package org.apache.shardingsphere.encrypt.subscriber;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.event.table.AlterEncryptTableEvent;
import org.apache.shardingsphere.encrypt.event.table.DropEncryptTableEvent;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.swapper.rule.YamlEncryptTableRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.subsciber.RuleChangedSubscriber;
import org.apache.shardingsphere.mode.subsciber.named.NamedRuleItemChangedSubscribeEngine;
import org.apache.shardingsphere.mode.subsciber.named.callback.NamedRuleItemAlteredSubscribeCallback;
import org.apache.shardingsphere.mode.subsciber.named.callback.NamedRuleItemDroppedSubscribeCallback;

import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * Encrypt table subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
public final class EncryptTableSubscriber implements RuleChangedSubscriber<AlterEncryptTableEvent, DropEncryptTableEvent> {
    
    private NamedRuleItemChangedSubscribeEngine<EncryptRuleConfiguration> engine;
    
    @Override
    public void setContextManager(final ContextManager contextManager) {
        engine = new NamedRuleItemChangedSubscribeEngine<>(contextManager);
    }
    
    @Subscribe
    @Override
    public synchronized void renew(final AlterEncryptTableEvent event) {
        NamedRuleItemAlteredSubscribeCallback<EncryptRuleConfiguration> callback = (yamlContent, database) -> {
            EncryptRuleConfiguration result = database.getRuleMetaData().findSingleRule(EncryptRule.class)
                    .map(optional -> getEncryptRuleConfiguration((EncryptRuleConfiguration) optional.getConfiguration()))
                    .orElseGet(() -> new EncryptRuleConfiguration(new LinkedList<>(), new LinkedHashMap<>()));
            EncryptTableRuleConfiguration configFromEvent = new YamlEncryptTableRuleConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContent, YamlEncryptTableRuleConfiguration.class));
            // TODO refactor DistSQL to only persist config
            result.getTables().removeIf(each -> each.getName().equals(configFromEvent.getName()));
            result.getTables().add(configFromEvent);
            return result;
        };
        engine.renew(event, callback);
    }
    
    @Subscribe
    @Override
    public synchronized void renew(final DropEncryptTableEvent event) {
        NamedRuleItemDroppedSubscribeCallback<EncryptRuleConfiguration> callback = (itemName, database) -> {
            EncryptRuleConfiguration result = (EncryptRuleConfiguration) database.getRuleMetaData().getSingleRule(EncryptRule.class).getConfiguration();
            result.getTables().removeIf(each -> each.getName().equals(itemName));
            return result;
        };
        engine.renew(event, callback);
    }
    
    private EncryptRuleConfiguration getEncryptRuleConfiguration(final EncryptRuleConfiguration config) {
        return null == config.getTables() ? new EncryptRuleConfiguration(new LinkedList<>(), config.getEncryptors()) : config;
    }
}
