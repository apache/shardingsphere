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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.api.PluginConfiguration;
import org.apache.shardingsphere.agent.core.advisor.config.AdvisorConfiguration;

import java.util.Collection;
import java.util.Map;
import java.util.jar.JarFile;

/**
 * Agent builder factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AgentBuilderFactory {
    
    /**
     * Create agent builder.
     * 
     * @param pluginConfigs plugin configurations
     * @param pluginJars plugin jars
     * @param advisorConfigs advisor configurations
     * @param isEnhancedForProxy is enhanced for proxy
     * @return created agent builder
     */
    public static AgentBuilder create(final Map<String, PluginConfiguration> pluginConfigs,
                                      final Collection<JarFile> pluginJars, final Map<String, AdvisorConfiguration> advisorConfigs, final boolean isEnhancedForProxy) {
        return new AgentBuilder.Default()
                .with(new ByteBuddy().with(TypeValidation.ENABLED))
                .ignore(ElementMatchers.isSynthetic())
                .or(ElementMatchers.nameStartsWith("org.apache.shardingsphere.agent."))
                .type(new AgentJunction(advisorConfigs))
                .transform(new AgentTransformer(pluginConfigs, pluginJars, advisorConfigs, isEnhancedForProxy))
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION);
    }
}
