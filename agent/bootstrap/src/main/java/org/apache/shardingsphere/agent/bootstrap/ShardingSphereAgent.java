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

package org.apache.shardingsphere.agent.bootstrap;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.config.AgentConfiguration;
import org.apache.shardingsphere.agent.config.PluginConfiguration;
import org.apache.shardingsphere.agent.core.bytebuddy.listener.LoggingListener;
import org.apache.shardingsphere.agent.core.bytebuddy.transformer.ShardingSphereTransformer;
import org.apache.shardingsphere.agent.core.config.registry.AgentConfigurationRegistry;
import org.apache.shardingsphere.agent.core.config.loader.AgentConfigurationLoader;
import org.apache.shardingsphere.agent.core.plugin.PluginBootServiceManager;
import org.apache.shardingsphere.agent.core.plugin.AgentPluginLoader;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Map;

/**
 * ShardingSphere agent.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSphereAgent {
    
    /**
     * Premain for instrumentation.
     *
     * @param args arguments
     * @param instrumentation instrumentation
     * @throws IOException IO exception
     */
    public static void premain(final String args, final Instrumentation instrumentation) throws IOException {
        AgentConfiguration agentConfig = AgentConfigurationLoader.load();
        AgentConfigurationRegistry.INSTANCE.put(agentConfig);
        AgentPluginLoader loader = createPluginLoader();
        setUpAgentBuilder(instrumentation, loader);
        setupPluginBootService(agentConfig.getPlugins());
    }
    
    private static AgentPluginLoader createPluginLoader() throws IOException {
        AgentPluginLoader result = AgentPluginLoader.getInstance();
        result.loadAllPlugins();
        return result;
    }
    
    private static void setupPluginBootService(final Map<String, PluginConfiguration> pluginConfigs) {
        PluginBootServiceManager.startAllServices(pluginConfigs);
        Runtime.getRuntime().addShutdownHook(new Thread(PluginBootServiceManager::closeAllServices));
    }
    
    private static void setUpAgentBuilder(final Instrumentation instrumentation, final AgentPluginLoader loader) {
        AgentBuilder agentBuilder = new AgentBuilder.Default().with(new ByteBuddy().with(TypeValidation.ENABLED))
                .ignore(ElementMatchers.isSynthetic()).or(ElementMatchers.nameStartsWith("org.apache.shardingsphere.agent."));
        agentBuilder.type(loader.typeMatcher())
                .transform(new ShardingSphereTransformer(loader)).with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION).with(new LoggingListener()).installOn(instrumentation);
    }
}
