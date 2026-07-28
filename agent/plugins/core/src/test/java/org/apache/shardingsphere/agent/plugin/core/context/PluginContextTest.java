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

package org.apache.shardingsphere.agent.plugin.core.context;

import org.apache.shardingsphere.agent.plugin.core.holder.ShardingSphereDataSourceContextHolder;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PluginContextTest {
    
    private boolean originalIsEnhancedForProxy;
    
    private ContextManager originalProxyContextManager;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        ShardingSphereDataSourceContextHolder.getShardingSphereDataSourceContexts().clear();
        PluginContext pluginContext = PluginContext.getInstance();
        originalIsEnhancedForProxy = pluginContext.isEnhancedForProxy();
        pluginContext.setEnhancedForProxy(false);
        Field contextManagerField = ProxyContext.class.getDeclaredField("contextManager");
        originalProxyContextManager = (ContextManager) Plugins.getMemberAccessor().get(contextManagerField, ProxyContext.getInstance());
        Plugins.getMemberAccessor().set(contextManagerField, ProxyContext.getInstance(), null);
    }
    
    @AfterEach
    void tearDown() throws ReflectiveOperationException {
        ShardingSphereDataSourceContextHolder.getShardingSphereDataSourceContexts().clear();
        PluginContext.getInstance().setEnhancedForProxy(originalIsEnhancedForProxy);
        Plugins.getMemberAccessor().set(ProxyContext.class.getDeclaredField("contextManager"), ProxyContext.getInstance(), originalProxyContextManager);
    }
    
    @Test
    void assertIsPluginEnabledWithEmptyDataSourceContextHolder() {
        assertTrue(PluginContext.getInstance().isPluginEnabled());
    }
    
    @Test
    void assertIsPluginEnabledWithShardingSphereDataSourceContextEnabled() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getProps().<Boolean>getValue(ConfigurationPropertyKey.AGENT_PLUGINS_ENABLED)).thenReturn(true);
        ShardingSphereDataSourceContextHolder.put("foo_instance", new ShardingSphereDataSourceContext("foo_db", contextManager));
        assertTrue(PluginContext.getInstance().isPluginEnabled());
    }
    
    @Test
    void assertIsPluginEnabledWithShardingSphereDataSourceContextDisabled() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getProps().<Boolean>getValue(ConfigurationPropertyKey.AGENT_PLUGINS_ENABLED)).thenReturn(false);
        ShardingSphereDataSourceContextHolder.put("foo_instance", new ShardingSphereDataSourceContext("foo_db", contextManager));
        assertFalse(PluginContext.getInstance().isPluginEnabled());
    }
    
    @Test
    void assertIsPluginEnabledWhenEnhancedForProxy() {
        PluginContext pluginContext = PluginContext.getInstance();
        pluginContext.setEnhancedForProxy(true);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getProps().<Boolean>getValue(ConfigurationPropertyKey.AGENT_PLUGINS_ENABLED)).thenReturn(false);
        ProxyContext.init(contextManager);
        assertFalse(pluginContext.isPluginEnabled());
    }
}
