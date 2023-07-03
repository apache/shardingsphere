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

package org.apache.shardingsphere.single.subscriber;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.subsciber.RuleChangedSubscriber;
import org.apache.shardingsphere.mode.subsciber.unique.callback.UniqueRuleItemAlteredSubscribeCallback;
import org.apache.shardingsphere.mode.subsciber.unique.UniqueRuleItemChangedSubscribeEngine;
import org.apache.shardingsphere.mode.subsciber.unique.callback.UniqueRuleItemDroppedSubscribeCallback;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.event.config.AlterSingleTableEvent;
import org.apache.shardingsphere.single.event.config.DropSingleTableEvent;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.single.yaml.config.pojo.YamlSingleRuleConfiguration;
import org.apache.shardingsphere.single.yaml.config.swapper.YamlSingleRuleConfigurationSwapper;

/**
 * Single table subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
public final class SingleTableSubscriber implements RuleChangedSubscriber<AlterSingleTableEvent, DropSingleTableEvent> {
    
    private UniqueRuleItemChangedSubscribeEngine<SingleRuleConfiguration> engine;
    
    @Override
    public void setContextManager(final ContextManager contextManager) {
        engine = new UniqueRuleItemChangedSubscribeEngine<>(contextManager);
    }
    
    @Subscribe
    @Override
    public synchronized void renew(final AlterSingleTableEvent event) {
        UniqueRuleItemAlteredSubscribeCallback<SingleRuleConfiguration> callback = (yamlContent, database) -> {
            SingleRuleConfiguration result = database.getRuleMetaData().findSingleRule(SingleRule.class).map(SingleRule::getConfiguration).orElseGet(SingleRuleConfiguration::new);
            SingleRuleConfiguration configFromEvent = new YamlSingleRuleConfigurationSwapper().swapToObject(YamlEngine.unmarshal(yamlContent, YamlSingleRuleConfiguration.class));
            result.getTables().clear();
            result.getTables().addAll(configFromEvent.getTables());
            configFromEvent.getDefaultDataSource().ifPresent(optional -> result.setDefaultDataSource(configFromEvent.getDefaultDataSource().get()));
            return result;
        };
        engine.renew(event, callback);
    }
    
    @Subscribe
    @Override
    public synchronized void renew(final DropSingleTableEvent event) {
        UniqueRuleItemDroppedSubscribeCallback<SingleRuleConfiguration> callback = database -> {
            SingleRuleConfiguration result = database.getRuleMetaData().getSingleRule(SingleRule.class).getConfiguration();
            result.getTables().clear();
            result.setDefaultDataSource(null);
            return result;
        };
        engine.renew(event, callback);
    }
}
