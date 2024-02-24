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

package org.apache.shardingsphere.agent.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.bytebuddy.pool.TypePool;
import org.apache.shardingsphere.agent.api.PluginConfiguration;
import org.apache.shardingsphere.agent.core.advisor.config.AdvisorConfiguration;
import org.apache.shardingsphere.agent.core.advisor.config.AdvisorConfigurationLoader;
import org.apache.shardingsphere.agent.core.builder.AgentBuilderFactory;
import org.apache.shardingsphere.agent.core.path.AgentPath;
import org.apache.shardingsphere.agent.core.plugin.config.PluginConfigurationLoader;
import org.apache.shardingsphere.agent.core.plugin.jar.PluginJarLoader;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Collection;
import java.util.Map;
import java.util.jar.JarFile;

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
        File rootPath = AgentPath.getRootPath();
        Map<String, PluginConfiguration> pluginConfigs = PluginConfigurationLoader.load(rootPath);
        Collection<JarFile> pluginJars = PluginJarLoader.load(rootPath);
        Map<String, AdvisorConfiguration> advisorConfigs = AdvisorConfigurationLoader.load(pluginJars, pluginConfigs.keySet());
        AgentBuilderFactory.create(pluginConfigs, pluginJars, advisorConfigs, isEnhancedForProxy()).installOn(instrumentation);
    }
    
    private static boolean isEnhancedForProxy() {
        return TypePool.Default.ofSystemLoader().describe("org.apache.shardingsphere.proxy.Bootstrap").isResolved();
    }
}
