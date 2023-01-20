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
import net.bytebuddy.utility.JavaModule;
import org.apache.shardingsphere.agent.api.PluginConfiguration;
import org.apache.shardingsphere.agent.core.advisor.config.AdvisorConfiguration;
import org.apache.shardingsphere.agent.core.builder.interceptor.AgentBuilderInterceptChainEngine;
import org.apache.shardingsphere.agent.core.builder.interceptor.impl.MethodAdvisorBuilderInterceptor;
import org.apache.shardingsphere.agent.core.builder.interceptor.impl.TargetAdviceObjectBuilderInterceptor;
import org.apache.shardingsphere.agent.core.classloader.ClassLoaderContext;
import org.apache.shardingsphere.agent.core.plugin.PluginLifecycleServiceManager;

import java.util.Collection;
import java.util.Map;
import java.util.jar.JarFile;

/**
 * Agent transformer.
 */
@RequiredArgsConstructor
public final class AgentTransformer implements Transformer {
    
    private final Map<String, PluginConfiguration> pluginConfigs;
    
    private final Collection<JarFile> pluginJars;
    
    private final Map<String, AdvisorConfiguration> advisorConfigs;
    
    private final boolean isEnhancedForProxy;
    
    @SuppressWarnings("NullableProblems")
    @Override
    public Builder<?> transform(final Builder<?> builder, final TypeDescription typeDescription, final ClassLoader classLoader, final JavaModule module) {
        if (!advisorConfigs.containsKey(typeDescription.getTypeName())) {
            return builder;
        }
        ClassLoaderContext classLoaderContext = new ClassLoaderContext(classLoader, pluginJars);
        PluginLifecycleServiceManager.init(pluginConfigs, pluginJars, classLoaderContext.getPluginClassLoader(), isEnhancedForProxy);
        return AgentBuilderInterceptChainEngine.intercept(builder,
                new TargetAdviceObjectBuilderInterceptor(), new MethodAdvisorBuilderInterceptor(typeDescription, classLoaderContext, advisorConfigs.get(typeDescription.getTypeName())));
    }
}
