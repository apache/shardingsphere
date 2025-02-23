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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.type;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.checker.ActiveVersionChecker;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.GlobalDataChangedEventHandler;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathPattern;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.config.global.GlobalRuleNodePath;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * Global rule changed handler.
 */
public final class GlobalRuleChangedHandler implements GlobalDataChangedEventHandler {
    
    @Override
    public String getSubscribedKey() {
        return NodePathGenerator.toPath(new GlobalRuleNodePath(null), false);
    }
    
    @Override
    public Collection<Type> getSubscribedTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED);
    }
    
    @Override
    public void handle(final ContextManager contextManager, final DataChangedEvent event) {
        Optional<String> ruleType = NodePathSearcher.getVersion(new GlobalRuleNodePath(NodePathPattern.IDENTIFIER)).findIdentifierByActiveVersionPath(event.getKey(), 1);
        if (!ruleType.isPresent()) {
            return;
        }
        ActiveVersionChecker.checkActiveVersion(contextManager, event);
        Optional<RuleConfiguration> ruleConfig = contextManager.getPersistServiceFacade().getMetaDataPersistFacade().getGlobalRuleService().load(ruleType.get());
        Preconditions.checkArgument(ruleConfig.isPresent(), "Can not find rule configuration with name: %s", ruleType.get());
        contextManager.getMetaDataContextManager().getGlobalConfigurationManager().alterGlobalRuleConfiguration(ruleConfig.get());
    }
}
