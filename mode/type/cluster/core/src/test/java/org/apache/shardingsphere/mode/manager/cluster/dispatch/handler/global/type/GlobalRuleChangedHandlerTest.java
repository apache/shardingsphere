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

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.GlobalDataChangedEventHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalRuleChangedHandlerTest {
    
    private GlobalDataChangedEventHandler handler;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @BeforeEach
    void setUp() {
        handler = ShardingSphereServiceLoader.getServiceInstances(GlobalDataChangedEventHandler.class).stream()
                .filter(each -> each.getSubscribedKey().equals("/rules")).findFirst().orElse(null);
    }
    
    @Test
    void assertHandleWithInvalidEventKey() {
        handler.handle(contextManager, new DataChangedEvent("/rules/foo_rule/xxx", "rule_value", Type.ADDED));
        verify(contextManager.getPersistServiceFacade().getMetaDataPersistFacade().getGlobalRuleService(), times(0)).load(any());
    }
    
    @Test
    void assertHandleWithEmptyRuleName() {
        handler.handle(contextManager, new DataChangedEvent("/rules/foo_rule/active_version/foo", "rule_value", Type.ADDED));
        verify(contextManager.getPersistServiceFacade().getMetaDataPersistFacade().getGlobalRuleService(), times(0)).load(any());
    }
    
    @Test
    void assertHandle() {
        when(contextManager.getPersistServiceFacade().getRepository().query("/rules/foo_rule/active_version")).thenReturn("rule_value");
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class);
        when(contextManager.getPersistServiceFacade().getMetaDataPersistFacade().getGlobalRuleService().load("foo_rule")).thenReturn(Optional.of(ruleConfig));
        handler.handle(contextManager, new DataChangedEvent("/rules/foo_rule/active_version", "rule_value", Type.ADDED));
        verify(contextManager.getMetaDataContextManager().getGlobalConfigurationManager()).alterGlobalRuleConfiguration(ruleConfig);
    }
}
