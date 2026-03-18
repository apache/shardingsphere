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

package org.apache.shardingsphere.agent.core.builder;

import lombok.RequiredArgsConstructor;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.pool.TypePool;
import net.bytebuddy.pool.TypePool.Default;
import net.bytebuddy.utility.JavaModule;
import org.apache.shardingsphere.agent.api.PluginConfiguration;
import org.apache.shardingsphere.agent.core.advisor.config.AdvisorConfiguration;
import org.apache.shardingsphere.agent.core.advisor.config.MethodAdvisorConfiguration;
import org.apache.shardingsphere.agent.core.builder.interceptor.AgentBuilderInterceptChainEngine;
import org.apache.shardingsphere.agent.core.builder.interceptor.impl.MethodAdvisorBuilderInterceptor;
import org.apache.shardingsphere.agent.core.builder.interceptor.impl.TargetAdviceObjectBuilderInterceptor;
import org.apache.shardingsphere.agent.core.plugin.PluginLifecycleServiceManager;
import org.apache.shardingsphere.agent.core.plugin.classloader.AgentPluginClassLoader;
import org.apache.shardingsphere.agent.core.plugin.classloader.ClassLoaderContext;

import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Agent transformer.
 */
@RequiredArgsConstructor
public final class AgentTransformer implements Transformer {
    
    private static final Logger LOGGER = Logger.getLogger(AgentTransformer.class.getName());
    
    private static final Map<AgentPluginClassLoader, TypePool> TYPE_POOL_MAP = new ConcurrentHashMap<>();
    
    private final Map<String, PluginConfiguration> pluginConfigs;
    
    private final Collection<JarFile> pluginJars;
    
    private final Map<String, AdvisorConfiguration> advisorConfigs;
    
    private final boolean isEnhancedForProxy;
    
    @SuppressWarnings("NullableProblems")
    @Override
    public Builder<?> transform(final Builder<?> builder, final TypeDescription typeDescription, final ClassLoader classLoader, final JavaModule module, final ProtectionDomain protectionDomain) {
        ClassLoaderContext classLoaderContext = new ClassLoaderContext(classLoader, pluginJars);
        PluginLifecycleServiceManager.init(pluginConfigs, pluginJars, classLoaderContext.getPluginClassLoader(), isEnhancedForProxy);
        return AgentBuilderInterceptChainEngine.intercept(builder, new TargetAdviceObjectBuilderInterceptor(),
                new MethodAdvisorBuilderInterceptor(typeDescription, classLoaderContext, filterInvalidAdviceClass(advisorConfigs.get(typeDescription.getTypeName()), classLoaderContext)));
    }
    
    private AdvisorConfiguration filterInvalidAdviceClass(final AdvisorConfiguration advisorConfig, final ClassLoaderContext classLoaderContext) {
        AdvisorConfiguration result = new AdvisorConfiguration(advisorConfig.getTargetClassName());
        for (MethodAdvisorConfiguration each : advisorConfig.getAdvisors()) {
            if (isExist(each.getAdviceClassName(), classLoaderContext.getPluginClassLoader())) {
                result.getAdvisors().add(each);
                continue;
            }
            LOGGER.log(Level.SEVERE, "The advice class `{0}` does not exist", new String[]{each.getAdviceClassName()});
        }
        return result;
    }
    
    private boolean isExist(final String adviceClassName, final AgentPluginClassLoader pluginClassLoader) {
        TypePool typePool = TYPE_POOL_MAP.get(pluginClassLoader);
        return null == typePool ? TYPE_POOL_MAP.computeIfAbsent(pluginClassLoader, Default::of).describe(adviceClassName).isResolved() : typePool.describe(adviceClassName).isResolved();
    }
}
