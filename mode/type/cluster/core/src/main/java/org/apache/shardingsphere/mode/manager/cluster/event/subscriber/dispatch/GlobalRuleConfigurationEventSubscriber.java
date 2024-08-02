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

package org.apache.shardingsphere.mode.manager.cluster.event.subscriber.dispatch;

import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.eventbus.EventSubscriber;
import org.apache.shardingsphere.mode.event.dispatch.config.AlterGlobalRuleConfigurationEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.spi.RuleConfigurationPersistDecorator;

import java.util.Optional;

/**
 * Global rule configuration event subscriber.
 */
@RequiredArgsConstructor
public final class GlobalRuleConfigurationEventSubscriber implements EventSubscriber {
    
    private final ContextManager contextManager;
    
    /**
     * Renew for global rule configuration.
     *
     * @param event global rule alter event
     */
    @SuppressWarnings("unchecked")
    @Subscribe
    public synchronized void renew(final AlterGlobalRuleConfigurationEvent event) {
        if (!event.getActiveVersion().equals(
                contextManager.getPersistServiceFacade().getMetaDataPersistService().getMetaDataVersionPersistService().getActiveVersionByFullPath(event.getActiveVersionKey()))) {
            return;
        }
        Optional<RuleConfiguration> ruleConfig = contextManager.getPersistServiceFacade().getMetaDataPersistService().getGlobalRuleService().load(event.getRuleSimpleName());
        if (!ruleConfig.isPresent()) {
            return;
        }
        contextManager.getMetaDataContextManager().getGlobalConfigurationManager().alterGlobalRuleConfiguration(
                TypedSPILoader.findService(RuleConfigurationPersistDecorator.class, ruleConfig.getClass()).map(optional -> optional.restore(ruleConfig.get())).orElse(ruleConfig.get()));
    }
}
