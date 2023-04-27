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

package org.apache.shardingsphere.agent.core.plugin;

import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class PluginContextTest {
    
    @AfterEach
    void reset() {
        PluginContext.getInstance().setContextManager(null);
    }
    
    @Test
    void assertPluginEnabledIsTrue() {
        PluginContext.getInstance().setEnhancedForProxy(true);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.AGENT_PLUGINS_ENABLED)).thenReturn(true);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        assertTrue(PluginContext.getInstance().isPluginEnabled());
    }
    
    @Test
    void assertPluginEnabledIsFalse() {
        PluginContext.getInstance().setEnhancedForProxy(true);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.AGENT_PLUGINS_ENABLED)).thenReturn(false);
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        assertFalse(PluginContext.getInstance().isPluginEnabled());
    }
}
