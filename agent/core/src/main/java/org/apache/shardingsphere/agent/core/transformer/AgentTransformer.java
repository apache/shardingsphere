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

package org.apache.shardingsphere.agent.core.transformer;

import lombok.RequiredArgsConstructor;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodDescription.InDefinedShape;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import org.apache.shardingsphere.agent.config.advisor.AdvisorConfiguration;
import org.apache.shardingsphere.agent.config.advisor.ConstructorAdvisorConfiguration;
import org.apache.shardingsphere.agent.config.advisor.InstanceMethodAdvisorConfiguration;
import org.apache.shardingsphere.agent.config.advisor.StaticMethodAdvisorConfiguration;
import org.apache.shardingsphere.agent.config.plugin.PluginConfiguration;
import org.apache.shardingsphere.agent.core.logging.LoggerFactory;
import org.apache.shardingsphere.agent.core.plugin.OverrideArgsInvoker;
import org.apache.shardingsphere.agent.core.plugin.TargetAdviceObject;
import org.apache.shardingsphere.agent.core.plugin.advice.ConstructorAdvice;
import org.apache.shardingsphere.agent.core.plugin.advice.InstanceMethodAroundAdvice;
import org.apache.shardingsphere.agent.core.plugin.advice.StaticMethodAroundAdvice;
import org.apache.shardingsphere.agent.core.plugin.interceptor.ConstructorInterceptor;
import org.apache.shardingsphere.agent.core.plugin.interceptor.InstanceMethodAroundInterceptor;
import org.apache.shardingsphere.agent.core.plugin.interceptor.InstanceMethodInterceptorArgsOverride;
import org.apache.shardingsphere.agent.core.plugin.interceptor.StaticMethodAroundInterceptor;
import org.apache.shardingsphere.agent.core.plugin.interceptor.StaticMethodInterceptorArgsOverride;
import org.apache.shardingsphere.agent.core.plugin.interceptor.composed.ComposedConstructorInterceptor;
import org.apache.shardingsphere.agent.core.plugin.interceptor.composed.ComposedInstanceMethodAroundInterceptor;
import org.apache.shardingsphere.agent.core.plugin.interceptor.composed.ComposedInstanceMethodInterceptorArgsOverride;
import org.apache.shardingsphere.agent.core.plugin.interceptor.composed.ComposedStaticMethodAroundInterceptor;
import org.apache.shardingsphere.agent.core.plugin.interceptor.composed.ComposedStaticMethodInterceptorArgsOverride;
import org.apache.shardingsphere.agent.core.plugin.loader.AdviceInstanceLoader;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Agent transformer.
 */
@RequiredArgsConstructor
public final class AgentTransformer implements Transformer {
    
    private static final LoggerFactory.Logger LOGGER = LoggerFactory.getLogger(AgentTransformer.class);
    
    private static final String EXTRA_DATA = "_$EXTRA_DATA$_";
    
    private final Map<String, PluginConfiguration> pluginConfigs;
    
    private final Map<String, AdvisorConfiguration> advisorConfigs;
    
    private final boolean isEnhancedForProxy;
    
    @SuppressWarnings("NullableProblems")
    @Override
    public Builder<?> transform(final Builder<?> builder, final TypeDescription typeDescription, final ClassLoader classLoader, final JavaModule module) {
        if (!advisorConfigs.containsKey(typeDescription.getTypeName())) {
            return builder;
        }
        Builder<?> result = builder.defineField(EXTRA_DATA, Object.class, Opcodes.ACC_PRIVATE | Opcodes.ACC_VOLATILE).implement(TargetAdviceObject.class).intercept(FieldAccessor.ofField(EXTRA_DATA));
        AdvisorConfiguration advisorConfig = advisorConfigs.getOrDefault(typeDescription.getTypeName(), new AdvisorConfiguration(""));
        result = interceptConstructor(typeDescription, advisorConfig.getConstructorAdvisors(), result, classLoader);
        result = interceptStaticMethod(typeDescription, advisorConfig.getStaticMethodAdvisors(), result, classLoader);
        result = interceptInstanceMethod(typeDescription, advisorConfig.getInstanceMethodAdvisors(), result, classLoader);
        return result;
    }
    
    private Builder<?> interceptConstructor(final TypeDescription description,
                                            final Collection<ConstructorAdvisorConfiguration> constructorAdvisorConfigs, final Builder<?> builder, final ClassLoader classLoader) {
        Collection<AgentTransformationPoint<? extends ConstructorInterceptor>> constructorAdviceComposePoints = description.getDeclaredMethods().stream()
                .filter(MethodDescription::isConstructor)
                .map(each -> getMatchedTransformationPoint(constructorAdvisorConfigs, each, classLoader))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Builder<?> result = builder;
        for (AgentTransformationPoint<? extends ConstructorInterceptor> each : constructorAdviceComposePoints) {
            try {
                result = result.constructor(ElementMatchers.is(each.getDescription()))
                        .intercept(SuperMethodCall.INSTANCE.andThen(MethodDelegation.withDefaultConfiguration().to(each.getInterceptor())));
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                LOGGER.error("Failed to load advice class: {}.", description.getTypeName(), ex);
            }
        }
        return result;
    }
    
    private AgentTransformationPoint<? extends ConstructorInterceptor> getMatchedTransformationPoint(final Collection<ConstructorAdvisorConfiguration> constructorAdvisorConfigs,
                                                                                                     final InDefinedShape methodDescription, final ClassLoader classLoader) {
        List<ConstructorAdvisorConfiguration> matchedConstructorAdvisorConfigs = constructorAdvisorConfigs
                .stream().filter(each -> each.getPointcut().matches(methodDescription)).collect(Collectors.toList());
        if (matchedConstructorAdvisorConfigs.isEmpty()) {
            return null;
        }
        if (1 == matchedConstructorAdvisorConfigs.size()) {
            return new AgentTransformationPoint<>(
                    methodDescription, new ConstructorInterceptor(loadAdviceInstance(matchedConstructorAdvisorConfigs.get(0).getAdviceClassName(), classLoader)));
        }
        Collection<ConstructorAdvice> constructorAdvices = matchedConstructorAdvisorConfigs.stream()
                .map(ConstructorAdvisorConfiguration::getAdviceClassName)
                .map(each -> (ConstructorAdvice) loadAdviceInstance(each, classLoader))
                .collect(Collectors.toList());
        return new AgentTransformationPoint<>(methodDescription, new ComposedConstructorInterceptor(constructorAdvices));
    }
    
    private Builder<?> interceptStaticMethod(final TypeDescription description, final Collection<StaticMethodAdvisorConfiguration> staticMethodAdvisorConfigs,
                                             final Builder<?> builder, final ClassLoader classLoader) {
        Collection<AgentTransformationPoint<?>> staticMethodAdvicePoints = description.getDeclaredMethods().stream()
                .filter(each -> each.isStatic() && !(each.isAbstract() || each.isSynthetic()))
                .map(each -> getMatchedStaticMethodPoint(staticMethodAdvisorConfigs, each, classLoader))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Builder<?> result = builder;
        for (AgentTransformationPoint<?> each : staticMethodAdvicePoints) {
            try {
                if (each.getInterceptor() instanceof StaticMethodInterceptorArgsOverride) {
                    result = result.method(ElementMatchers.is(each.getDescription()))
                            .intercept(MethodDelegation.withDefaultConfiguration().withBinders(Morph.Binder.install(OverrideArgsInvoker.class)).to(each.getInterceptor()));
                } else {
                    result = result.method(ElementMatchers.is(each.getDescription())).intercept(MethodDelegation.withDefaultConfiguration().to(each.getInterceptor()));
                }
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                LOGGER.error("Failed to load advice class: {}.", description.getTypeName(), ex);
            }
        }
        return result;
    }
    
    private AgentTransformationPoint<?> getMatchedStaticMethodPoint(final Collection<StaticMethodAdvisorConfiguration> staticMethodAdvisorConfigs,
                                                                    final InDefinedShape methodDescription, final ClassLoader classLoader) {
        List<StaticMethodAdvisorConfiguration> matchedAdvisors = staticMethodAdvisorConfigs.stream().filter(each -> each.getPointcut().matches(methodDescription)).collect(Collectors.toList());
        if (matchedAdvisors.isEmpty()) {
            return null;
        }
        if (1 == matchedAdvisors.size()) {
            return getSingleStaticMethodPoint(methodDescription, matchedAdvisors.get(0), classLoader);
        }
        return getComposedStaticMethodPoint(methodDescription, matchedAdvisors, classLoader);
    }
    
    private AgentTransformationPoint<?> getSingleStaticMethodPoint(final InDefinedShape methodDescription,
                                                                   final StaticMethodAdvisorConfiguration staticMethodAdvisorConfig, final ClassLoader classLoader) {
        StaticMethodAroundAdvice staticMethodAroundAdvice = loadAdviceInstance(staticMethodAdvisorConfig.getAdviceClassName(), classLoader);
        return staticMethodAdvisorConfig.isOverrideArgs()
                ? new AgentTransformationPoint<>(methodDescription, new StaticMethodInterceptorArgsOverride(staticMethodAroundAdvice))
                : new AgentTransformationPoint<>(methodDescription, new StaticMethodAroundInterceptor(staticMethodAroundAdvice));
    }
    
    private AgentTransformationPoint<?> getComposedStaticMethodPoint(final InDefinedShape methodDescription,
                                                                     final Collection<StaticMethodAdvisorConfiguration> staticMethodAdvisorConfigs, final ClassLoader classLoader) {
        Collection<StaticMethodAroundAdvice> staticMethodAroundAdvices = new LinkedList<>();
        boolean isArgsOverride = false;
        for (StaticMethodAdvisorConfiguration each : staticMethodAdvisorConfigs) {
            if (each.isOverrideArgs()) {
                isArgsOverride = true;
            }
            if (null != each.getAdviceClassName()) {
                staticMethodAroundAdvices.add(loadAdviceInstance(each.getAdviceClassName(), classLoader));
            }
        }
        return isArgsOverride ? new AgentTransformationPoint<>(methodDescription, new ComposedStaticMethodInterceptorArgsOverride(staticMethodAroundAdvices))
                : new AgentTransformationPoint<>(methodDescription, new ComposedStaticMethodAroundInterceptor(staticMethodAroundAdvices));
    }
    
    private Builder<?> interceptInstanceMethod(final TypeDescription description,
                                               final Collection<InstanceMethodAdvisorConfiguration> instanceMethodAdvisorConfigs, final Builder<?> builder, final ClassLoader classLoader) {
        Collection<AgentTransformationPoint<?>> instanceMethodAdviceComposePoints = description.getDeclaredMethods().stream()
                .filter(each -> !(each.isAbstract() || each.isSynthetic()))
                .map(each -> getMatchedInstanceMethodPoint(instanceMethodAdvisorConfigs, each, classLoader))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Builder<?> result = builder;
        for (AgentTransformationPoint<?> each : instanceMethodAdviceComposePoints) {
            try {
                if (each.getInterceptor() instanceof InstanceMethodInterceptorArgsOverride) {
                    result = result.method(ElementMatchers.is(each.getDescription()))
                            .intercept(MethodDelegation.withDefaultConfiguration().withBinders(Morph.Binder.install(OverrideArgsInvoker.class)).to(each.getInterceptor()));
                } else {
                    result = result.method(ElementMatchers.is(each.getDescription())).intercept(MethodDelegation.withDefaultConfiguration().to(each.getInterceptor()));
                }
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                LOGGER.error("Failed to load advice class: {}.", description.getTypeName(), ex);
            }
        }
        return result;
    }
    
    private AgentTransformationPoint<?> getMatchedInstanceMethodPoint(final Collection<InstanceMethodAdvisorConfiguration> instanceMethodAroundPoints,
                                                                      final InDefinedShape methodDescription, final ClassLoader classLoader) {
        List<InstanceMethodAdvisorConfiguration> instanceMethodAdvisorConfigs = instanceMethodAroundPoints
                .stream().filter(each -> each.getPointcut().matches(methodDescription)).collect(Collectors.toList());
        if (instanceMethodAdvisorConfigs.isEmpty()) {
            return null;
        }
        if (1 == instanceMethodAdvisorConfigs.size()) {
            return getSingleInstanceMethodPoint(methodDescription, instanceMethodAdvisorConfigs.get(0), classLoader);
        }
        return getComposeInstanceMethodPoint(methodDescription, instanceMethodAdvisorConfigs, classLoader);
    }
    
    private AgentTransformationPoint<?> getSingleInstanceMethodPoint(final InDefinedShape methodDescription,
                                                                     final InstanceMethodAdvisorConfiguration instanceMethodAdvisorConfig, final ClassLoader classLoader) {
        InstanceMethodAroundAdvice instanceMethodAroundAdvice = loadAdviceInstance(instanceMethodAdvisorConfig.getAdviceClassName(), classLoader);
        return instanceMethodAdvisorConfig.isOverrideArgs()
                ? new AgentTransformationPoint<>(methodDescription, new InstanceMethodInterceptorArgsOverride(instanceMethodAroundAdvice))
                : new AgentTransformationPoint<>(methodDescription, new InstanceMethodAroundInterceptor(instanceMethodAroundAdvice));
    }
    
    private AgentTransformationPoint<?> getComposeInstanceMethodPoint(final InDefinedShape methodDescription,
                                                                      final Collection<InstanceMethodAdvisorConfiguration> instanceMethodAdvisorConfigs, final ClassLoader classLoader) {
        Collection<InstanceMethodAroundAdvice> instanceMethodAroundAdvices = new LinkedList<>();
        boolean isArgsOverride = false;
        for (InstanceMethodAdvisorConfiguration each : instanceMethodAdvisorConfigs) {
            if (each.isOverrideArgs()) {
                isArgsOverride = true;
            }
            if (null != each.getAdviceClassName()) {
                instanceMethodAroundAdvices.add(loadAdviceInstance(each.getAdviceClassName(), classLoader));
            }
        }
        return isArgsOverride
                ? new AgentTransformationPoint<>(methodDescription, new ComposedInstanceMethodInterceptorArgsOverride(instanceMethodAroundAdvices))
                : new AgentTransformationPoint<>(methodDescription, new ComposedInstanceMethodAroundInterceptor(instanceMethodAroundAdvices));
    }
    
    private <T> T loadAdviceInstance(final String adviceClassName, final ClassLoader classLoader) {
        return AdviceInstanceLoader.loadAdviceInstance(adviceClassName, classLoader, pluginConfigs, isEnhancedForProxy);
    }
}
