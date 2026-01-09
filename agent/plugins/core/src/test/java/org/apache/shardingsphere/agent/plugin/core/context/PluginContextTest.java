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
    
    private ContextManager originalPluginContextManager;
    
    private boolean originalIsEnhancedForProxy;
    
    private ContextManager originalProxyContextManager;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        ShardingSphereDataSourceContextHolder.getShardingSphereDataSourceContexts().clear();
        PluginContext pluginContext = PluginContext.getInstance();
        Field contextManagerField = PluginContext.class.getDeclaredField("contextManager");
        originalPluginContextManager = (ContextManager) Plugins.getMemberAccessor().get(contextManagerField, pluginContext);
        Plugins.getMemberAccessor().set(contextManagerField, pluginContext, null);
        Field isEnhancedForProxyField = PluginContext.class.getDeclaredField("isEnhancedForProxy");
        originalIsEnhancedForProxy = (boolean) Plugins.getMemberAccessor().get(isEnhancedForProxyField, pluginContext);
        Plugins.getMemberAccessor().set(isEnhancedForProxyField, pluginContext, false);
        Field proxyContextManagerField = ProxyContext.class.getDeclaredField("contextManager");
        originalProxyContextManager = (ContextManager) Plugins.getMemberAccessor().get(proxyContextManagerField, ProxyContext.getInstance());
        Plugins.getMemberAccessor().set(proxyContextManagerField, ProxyContext.getInstance(), null);
    }
    
    @AfterEach
    void tearDown() throws ReflectiveOperationException {
        ShardingSphereDataSourceContextHolder.getShardingSphereDataSourceContexts().clear();
        PluginContext pluginContext = PluginContext.getInstance();
        Plugins.getMemberAccessor().set(PluginContext.class.getDeclaredField("contextManager"), pluginContext, originalPluginContextManager);
        Plugins.getMemberAccessor().set(PluginContext.class.getDeclaredField("isEnhancedForProxy"), pluginContext, originalIsEnhancedForProxy);
        Plugins.getMemberAccessor().set(ProxyContext.class.getDeclaredField("contextManager"), ProxyContext.getInstance(), originalProxyContextManager);
    }
    
    @Test
    void assertIsPluginEnabledWithEmptyDataSourceContextHolder() {
        assertTrue(PluginContext.getInstance().isPluginEnabled());
    }
    
    @Test
    void assertIsPluginEnabledWithExistingContextManager() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getProps().<Boolean>getValue(ConfigurationPropertyKey.AGENT_PLUGINS_ENABLED)).thenReturn(true);
        PluginContext.getInstance().setContextManager(contextManager);
        assertTrue(PluginContext.getInstance().isPluginEnabled());
    }
    
    @Test
    void assertIsPluginEnabledWithShardingSphereDataSourceContextDisabled() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getProps().<Boolean>getValue(ConfigurationPropertyKey.AGENT_PLUGINS_ENABLED)).thenReturn(false);
        ShardingSphereDataSourceContextHolder.put("instance_1", new ShardingSphereDataSourceContext("logic_db", contextManager));
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
