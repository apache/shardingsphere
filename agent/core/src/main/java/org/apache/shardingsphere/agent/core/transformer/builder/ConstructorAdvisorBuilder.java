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

package org.apache.shardingsphere.agent.core.transformer.builder;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodDescription.InDefinedShape;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.config.advisor.ConstructorAdvisorConfiguration;
import org.apache.shardingsphere.agent.config.plugin.PluginConfiguration;
import org.apache.shardingsphere.agent.core.logging.LoggerFactory;
import org.apache.shardingsphere.agent.core.plugin.advice.ConstructorAdvice;
import org.apache.shardingsphere.agent.core.plugin.interceptor.ConstructorInterceptor;
import org.apache.shardingsphere.agent.core.plugin.interceptor.composed.ComposedConstructorInterceptor;
import org.apache.shardingsphere.agent.core.transformer.MethodAdvisor;
import org.apache.shardingsphere.agent.core.transformer.builder.advise.AdviceFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Constructor advisor builder.
 */
public final class ConstructorAdvisorBuilder {
    
    private static final LoggerFactory.Logger LOGGER = LoggerFactory.getLogger(ConstructorAdvisorBuilder.class);
    
    private final Collection<ConstructorAdvisorConfiguration> constructorAdvisorConfigs;
    
    private final TypeDescription typePointcut;
    
    private final AdviceFactory adviceFactory;
    
    public ConstructorAdvisorBuilder(final Map<String, PluginConfiguration> pluginConfigs, final Collection<ConstructorAdvisorConfiguration> constructorAdvisorConfigs,
                                     final boolean isEnhancedForProxy, final TypeDescription typePointcut, final ClassLoader classLoader) {
        this.constructorAdvisorConfigs = constructorAdvisorConfigs;
        this.typePointcut = typePointcut;
        adviceFactory = new AdviceFactory(classLoader, pluginConfigs, isEnhancedForProxy);
    }
    
    /**
     * Create constructor advisor builder.
     * 
     * @param builder original builder
     * @return created builder
     */
    public Builder<?> create(final Builder<?> builder) {
        Builder<?> result = builder;
        Collection<MethodAdvisor<? extends ConstructorInterceptor>> constructorAdvisors = typePointcut.getDeclaredMethods().stream()
                .filter(MethodDescription::isConstructor)
                .map(this::getMatchedConstructorAdvisor)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        for (MethodAdvisor<? extends ConstructorInterceptor> each : constructorAdvisors) {
            try {
                result = result.constructor(ElementMatchers.is(each.getPointcut()))
                        .intercept(SuperMethodCall.INSTANCE.andThen(MethodDelegation.withDefaultConfiguration().to(each.getAdvice())));
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                LOGGER.error("Failed to load advice class: {}.", typePointcut.getTypeName(), ex);
            }
        }
        return result;
    }
    
    private MethodAdvisor<? extends ConstructorInterceptor> getMatchedConstructorAdvisor(final InDefinedShape methodPointcut) {
        List<ConstructorAdvisorConfiguration> matchedConstructorAdvisorConfigs = constructorAdvisorConfigs
                .stream().filter(each -> each.getPointcut().matches(methodPointcut)).collect(Collectors.toList());
        if (matchedConstructorAdvisorConfigs.isEmpty()) {
            return null;
        }
        if (1 == matchedConstructorAdvisorConfigs.size()) {
            return new MethodAdvisor<>(
                    methodPointcut, new ConstructorInterceptor(adviceFactory.getAdvice(matchedConstructorAdvisorConfigs.get(0).getAdviceClassName())));
        }
        Collection<ConstructorAdvice> constructorAdvices = matchedConstructorAdvisorConfigs.stream()
                .map(ConstructorAdvisorConfiguration::getAdviceClassName)
                .map(each -> (ConstructorAdvice) adviceFactory.getAdvice(each))
                .collect(Collectors.toList());
        return new MethodAdvisor<>(methodPointcut, new ComposedConstructorInterceptor(constructorAdvices));
    }
}
