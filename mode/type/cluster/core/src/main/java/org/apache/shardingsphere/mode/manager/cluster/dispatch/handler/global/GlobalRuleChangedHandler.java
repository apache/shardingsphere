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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.metadata.persist.node.GlobalNodePath;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.DataChangedEventHandler;
import org.apache.shardingsphere.mode.spi.RuleConfigurationPersistDecorator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * Global rule changed handler.
 */
public final class GlobalRuleChangedHandler implements DataChangedEventHandler {
    
    @Override
    public String getSubscribedKey() {
        return GlobalNodePath.getRuleRootPath();
    }
    
    @Override
    public Collection<Type> getSubscribedTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void handle(final ContextManager contextManager, final DataChangedEvent event) {
        if (!org.apache.shardingsphere.mode.path.GlobalNodePath.isRuleActiveVersionPath(event.getKey())) {
            return;
        }
        Optional<String> ruleName = org.apache.shardingsphere.mode.path.GlobalNodePath.getRuleName(event.getKey());
        if (!ruleName.isPresent()) {
            return;
        }
        Preconditions.checkArgument(event.getValue().equals(
                contextManager.getPersistServiceFacade().getMetaDataPersistService().getMetaDataVersionPersistService().getActiveVersionByFullPath(event.getKey())),
                "Invalid active version: %s of key: %s", event.getValue(), event.getKey());
        Optional<RuleConfiguration> ruleConfig = contextManager.getPersistServiceFacade().getMetaDataPersistService().getGlobalRuleService().load(ruleName.get());
        Preconditions.checkArgument(ruleConfig.isPresent(), "Can not find rule configuration with name: %s", ruleName.get());
        contextManager.getMetaDataContextManager().getGlobalConfigurationManager().alterGlobalRuleConfiguration(
                TypedSPILoader.findService(RuleConfigurationPersistDecorator.class, ruleConfig.get().getClass()).map(optional -> optional.restore(ruleConfig.get())).orElse(ruleConfig.get()));
    }
}
