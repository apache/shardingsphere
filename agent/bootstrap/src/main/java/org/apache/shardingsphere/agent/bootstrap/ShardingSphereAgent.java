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
import org.apache.shardingsphere.agent.bootstrap.config.loader.PluginConfigurationLoader;
import org.apache.shardingsphere.agent.bootstrap.logging.LoggingListener;
import org.apache.shardingsphere.agent.bootstrap.plugin.PluginJar;
import org.apache.shardingsphere.agent.bootstrap.plugin.loader.AdvisorConfigurationLoader;
import org.apache.shardingsphere.agent.bootstrap.plugin.loader.AgentPluginLoader;
import org.apache.shardingsphere.agent.bootstrap.transformer.AgentJunction;
import org.apache.shardingsphere.agent.bootstrap.transformer.AgentTransformer;
import org.apache.shardingsphere.agent.config.advisor.AdvisorConfiguration;
import org.apache.shardingsphere.agent.config.plugin.PluginConfiguration;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Collection;
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
        Map<String, PluginConfiguration> pluginConfigs = PluginConfigurationLoader.load();
        boolean isEnhancedForProxy = isEnhancedForProxy();
        Collection<PluginJar> pluginJars = AgentPluginLoader.load();
        Map<String, AdvisorConfiguration> advisorConfigs = AdvisorConfigurationLoader.load(pluginJars, pluginConfigs.keySet(), isEnhancedForProxy);
        setUpAgentBuilder(instrumentation, pluginConfigs, pluginJars, advisorConfigs, isEnhancedForProxy);
    }
    
    private static boolean isEnhancedForProxy() {
        try {
            Class.forName("org.apache.shardingsphere.proxy.Bootstrap");
        } catch (final ClassNotFoundException ignored) {
            return false;
        }
        return true;
    }
    
    private static void setUpAgentBuilder(final Instrumentation instrumentation, final Map<String, PluginConfiguration> pluginConfigs,
                                          final Collection<PluginJar> pluginJars, final Map<String, AdvisorConfiguration> advisorConfigs, final boolean isEnhancedForProxy) {
        AgentBuilder agentBuilder = new AgentBuilder.Default().with(new ByteBuddy().with(TypeValidation.ENABLED))
                .ignore(ElementMatchers.isSynthetic())
                .or(ElementMatchers.nameStartsWith("org.apache.shardingsphere.agent."));
        agentBuilder.type(new AgentJunction(advisorConfigs))
                .transform(new AgentTransformer(pluginConfigs, pluginJars, advisorConfigs, isEnhancedForProxy))
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .with(new LoggingListener()).installOn(instrumentation);
    }
}
