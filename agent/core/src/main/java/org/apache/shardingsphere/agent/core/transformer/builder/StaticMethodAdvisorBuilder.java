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

import net.bytebuddy.description.method.MethodDescription.InDefinedShape;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.config.advisor.StaticMethodAdvisorConfiguration;
import org.apache.shardingsphere.agent.config.plugin.PluginConfiguration;
import org.apache.shardingsphere.agent.core.logging.LoggerFactory;
import org.apache.shardingsphere.agent.core.plugin.OverrideArgsInvoker;
import org.apache.shardingsphere.agent.core.plugin.advice.StaticMethodAroundAdvice;
import org.apache.shardingsphere.agent.core.plugin.interceptor.StaticMethodAroundInterceptor;
import org.apache.shardingsphere.agent.core.plugin.interceptor.StaticMethodInterceptorArgsOverride;
import org.apache.shardingsphere.agent.core.plugin.interceptor.composed.ComposedStaticMethodAroundInterceptor;
import org.apache.shardingsphere.agent.core.plugin.interceptor.composed.ComposedStaticMethodInterceptorArgsOverride;
import org.apache.shardingsphere.agent.core.transformer.MethodAdvisor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Static method advisor builder.
 */
public final class StaticMethodAdvisorBuilder {
    
    private static final LoggerFactory.Logger LOGGER = LoggerFactory.getLogger(StaticMethodAdvisorBuilder.class);
    
    private final Collection<StaticMethodAdvisorConfiguration> staticMethodAdvisorConfigs;
    
    private final TypeDescription typePointcut;
    
    private final AdviceFactory adviceFactory;
    
    public StaticMethodAdvisorBuilder(final Map<String, PluginConfiguration> pluginConfigs, final Collection<StaticMethodAdvisorConfiguration> staticMethodAdvisorConfigs,
                                      final boolean isEnhancedForProxy, final TypeDescription typePointcut, final ClassLoader classLoader) {
        this.staticMethodAdvisorConfigs = staticMethodAdvisorConfigs;
        this.typePointcut = typePointcut;
        adviceFactory = new AdviceFactory(classLoader, pluginConfigs, isEnhancedForProxy);
    }
    
    /**
     * Create static method advisor builder.
     * 
     * @param builder original builder
     * @return created builder
     */
    public Builder<?> create(final Builder<?> builder) {
        Collection<MethodAdvisor<?>> staticMethodAdvicePoints = typePointcut.getDeclaredMethods().stream()
                .filter(each -> each.isStatic() && !(each.isAbstract() || each.isSynthetic()))
                .map(this::getMatchedStaticMethodPoint)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Builder<?> result = builder;
        for (MethodAdvisor<?> each : staticMethodAdvicePoints) {
            try {
                if (each.getAdvice() instanceof StaticMethodInterceptorArgsOverride) {
                    result = result.method(ElementMatchers.is(each.getPointcut()))
                            .intercept(MethodDelegation.withDefaultConfiguration().withBinders(Morph.Binder.install(OverrideArgsInvoker.class)).to(each.getAdvice()));
                } else {
                    result = result.method(ElementMatchers.is(each.getPointcut())).intercept(MethodDelegation.withDefaultConfiguration().to(each.getAdvice()));
                }
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                LOGGER.error("Failed to load advice class: {}.", typePointcut.getTypeName(), ex);
            }
        }
        return result;
    }
    
    private MethodAdvisor<?> getMatchedStaticMethodPoint(final InDefinedShape methodPointcut) {
        List<StaticMethodAdvisorConfiguration> matchedAdvisors = staticMethodAdvisorConfigs.stream().filter(each -> each.getPointcut().matches(methodPointcut)).collect(Collectors.toList());
        if (matchedAdvisors.isEmpty()) {
            return null;
        }
        if (1 == matchedAdvisors.size()) {
            return getSingleStaticMethodPoint(methodPointcut, matchedAdvisors.get(0));
        }
        return getComposedStaticMethodPoint(methodPointcut);
    }
    
    private MethodAdvisor<?> getSingleStaticMethodPoint(final InDefinedShape methodPointcut, final StaticMethodAdvisorConfiguration staticMethodAdvisorConfig) {
        StaticMethodAroundAdvice staticMethodAroundAdvice = adviceFactory.getAdvice(staticMethodAdvisorConfig.getAdviceClassName());
        return staticMethodAdvisorConfig.isOverrideArgs()
                ? new MethodAdvisor<>(methodPointcut, new StaticMethodInterceptorArgsOverride(staticMethodAroundAdvice))
                : new MethodAdvisor<>(methodPointcut, new StaticMethodAroundInterceptor(staticMethodAroundAdvice));
    }
    
    private MethodAdvisor<?> getComposedStaticMethodPoint(final InDefinedShape methodPointcut) {
        Collection<StaticMethodAroundAdvice> staticMethodAroundAdvices = new LinkedList<>();
        boolean isArgsOverride = false;
        for (StaticMethodAdvisorConfiguration each : staticMethodAdvisorConfigs) {
            if (each.isOverrideArgs()) {
                isArgsOverride = true;
            }
            if (null != each.getAdviceClassName()) {
                staticMethodAroundAdvices.add(adviceFactory.getAdvice(each.getAdviceClassName()));
            }
        }
        return isArgsOverride ? new MethodAdvisor<>(methodPointcut, new ComposedStaticMethodInterceptorArgsOverride(staticMethodAroundAdvices))
                : new MethodAdvisor<>(methodPointcut, new ComposedStaticMethodAroundInterceptor(staticMethodAroundAdvices));
    }
}
