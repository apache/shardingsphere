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

package org.apache.shardingsphere.agent.core.bytebuddy.transformer;

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
import org.apache.shardingsphere.agent.advisor.StaticMethodAdvisor;
import org.apache.shardingsphere.agent.core.plugin.TargetAdviceObject;
import org.apache.shardingsphere.agent.core.plugin.advice.StaticMethodAroundAdvice;
import org.apache.shardingsphere.agent.core.plugin.advice.ConstructorAdvice;
import org.apache.shardingsphere.agent.core.plugin.advice.InstanceMethodAroundAdvice;
import org.apache.shardingsphere.agent.core.plugin.OverrideArgsInvoker;
import org.apache.shardingsphere.agent.advisor.ConstructorAdvisor;
import org.apache.shardingsphere.agent.advisor.InstanceMethodAdvisor;
import org.apache.shardingsphere.agent.advisor.ClassAdvisor;
import org.apache.shardingsphere.agent.core.logging.LoggerFactory;
import org.apache.shardingsphere.agent.core.plugin.PluginLoader;
import org.apache.shardingsphere.agent.core.plugin.interceptor.StaticMethodAroundInterceptor;
import org.apache.shardingsphere.agent.core.plugin.interceptor.StaticMethodInterceptorArgsOverride;
import org.apache.shardingsphere.agent.core.plugin.interceptor.ConstructorInterceptor;
import org.apache.shardingsphere.agent.core.plugin.interceptor.InstanceMethodAroundInterceptor;
import org.apache.shardingsphere.agent.core.plugin.interceptor.InstanceMethodInterceptorArgsOverride;
import org.apache.shardingsphere.agent.core.plugin.interceptor.compose.ComposedStaticMethodAroundInterceptor;
import org.apache.shardingsphere.agent.core.plugin.interceptor.compose.ComposedStaticMethodInterceptorArgsOverride;
import org.apache.shardingsphere.agent.core.plugin.interceptor.compose.ComposedConstructorInterceptor;
import org.apache.shardingsphere.agent.core.plugin.interceptor.compose.ComposedInstanceMethodAroundInterceptor;
import org.apache.shardingsphere.agent.core.plugin.interceptor.compose.ComposedInstanceMethodInterceptorArgsOverride;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * ShardingSphere transformer.
 */
@RequiredArgsConstructor
public final class ShardingSphereTransformer implements Transformer {
    
    private static final LoggerFactory.Logger LOGGER = LoggerFactory.getLogger(ShardingSphereTransformer.class);
    
    private static final String EXTRA_DATA = "_$EXTRA_DATA$_";
    
    private final PluginLoader pluginLoader;
    
    @Override
    public Builder<?> transform(final Builder<?> builder, final TypeDescription typeDescription, final ClassLoader classLoader, final JavaModule module) {
        if (!pluginLoader.containsType(typeDescription)) {
            return builder;
        }
        Builder<?> result = builder.defineField(EXTRA_DATA, Object.class, Opcodes.ACC_PRIVATE | Opcodes.ACC_VOLATILE).implement(TargetAdviceObject.class).intercept(FieldAccessor.ofField(EXTRA_DATA));
        ClassAdvisor classAdvisor = pluginLoader.loadPluginAdvisor(typeDescription);
        result = interceptConstructor(typeDescription, classAdvisor.getConstructorAdvisors(), result, classLoader);
        result = interceptStaticMethod(typeDescription, classAdvisor.getStaticMethodAdvisors(), result, classLoader);
        result = interceptInstanceMethod(typeDescription, classAdvisor.getInstanceMethodAdvisors(), result, classLoader);
        return result;
    }
    
    private Builder<?> interceptConstructor(final TypeDescription description,
                                            final Collection<ConstructorAdvisor> constructorAdvisors, final Builder<?> builder, final ClassLoader classLoader) {
        Collection<ShardingSphereTransformationPoint<? extends ConstructorInterceptor>> constructorAdviceComposePoints = description.getDeclaredMethods().stream()
                .filter(MethodDescription::isConstructor)
                .map(each -> getMatchedTransformationPoint(constructorAdvisors, each, classLoader))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Builder<?> result = builder;
        for (ShardingSphereTransformationPoint<? extends ConstructorInterceptor> each : constructorAdviceComposePoints) {
            try {
                result = result.constructor(ElementMatchers.is(each.getDescription()))
                        .intercept(SuperMethodCall.INSTANCE.andThen(MethodDelegation.withDefaultConfiguration().to(each.getInterceptor())));
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                LOGGER.error("Failed to load advice class: {}", description.getTypeName(), ex);
            }
        }
        return result;
    }
    
    private ShardingSphereTransformationPoint<? extends ConstructorInterceptor> getMatchedTransformationPoint(final Collection<ConstructorAdvisor> constructorAdvisors,
                                                                                                              final InDefinedShape methodDescription, final ClassLoader classLoader) {
        List<ConstructorAdvisor> matchedConstructorAdvisors = constructorAdvisors
                .stream().filter(each -> each.getMatcher().matches(methodDescription)).collect(Collectors.toList());
        if (matchedConstructorAdvisors.isEmpty()) {
            return null;
        }
        if (1 == matchedConstructorAdvisors.size()) {
            return new ShardingSphereTransformationPoint<>(
                    methodDescription, new ConstructorInterceptor(pluginLoader.getOrCreateInstance(matchedConstructorAdvisors.get(0).getAdviceClassName(), classLoader)));
        }
        Collection<ConstructorAdvice> constructorAdvices = matchedConstructorAdvisors.stream()
                .map(ConstructorAdvisor::getAdviceClassName)
                .map(each -> (ConstructorAdvice) pluginLoader.getOrCreateInstance(each, classLoader))
                .collect(Collectors.toList());
        return new ShardingSphereTransformationPoint<>(methodDescription, new ComposedConstructorInterceptor(constructorAdvices));
    }
    
    private Builder<?> interceptStaticMethod(final TypeDescription description, final Collection<StaticMethodAdvisor> staticMethodAdvisors,
                                             final Builder<?> builder, final ClassLoader classLoader) {
        Collection<ShardingSphereTransformationPoint<?>> staticMethodAdvicePoints = description.getDeclaredMethods().stream()
                .filter(each -> each.isStatic() && !(each.isAbstract() || each.isSynthetic()))
                .map(each -> getMatchedStaticMethodPoint(staticMethodAdvisors, each, classLoader))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Builder<?> result = builder;
        for (ShardingSphereTransformationPoint<?> each : staticMethodAdvicePoints) {
            try {
                if (each.getInterceptor() instanceof StaticMethodInterceptorArgsOverride || each.getInterceptor() instanceof ComposedStaticMethodInterceptorArgsOverride) {
                    result = result.method(ElementMatchers.is(each.getDescription()))
                            .intercept(MethodDelegation.withDefaultConfiguration().withBinders(Morph.Binder.install(OverrideArgsInvoker.class)).to(each.getInterceptor()));
                } else {
                    result = result.method(ElementMatchers.is(each.getDescription()))
                            .intercept(MethodDelegation.withDefaultConfiguration().to(each.getInterceptor()));
                }
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                LOGGER.error("Failed to load advice class: {}", description.getTypeName(), ex);
            }
        }
        return result;
    }
    
    private ShardingSphereTransformationPoint<?> getMatchedStaticMethodPoint(final Collection<StaticMethodAdvisor> staticMethodAroundPoints,
                                                                             final InDefinedShape methodDescription, final ClassLoader classLoader) {
        List<StaticMethodAdvisor> staticMethodAdvisors = staticMethodAroundPoints.stream().filter(each -> each.getMatcher().matches(methodDescription)).collect(Collectors.toList());
        if (staticMethodAdvisors.isEmpty()) {
            return null;
        }
        if (1 == staticMethodAdvisors.size()) {
            return getSingleStaticMethodPoint(methodDescription, staticMethodAdvisors.get(0), classLoader);
        }
        return getComposedStaticMethodPoint(methodDescription, staticMethodAdvisors, classLoader);
    }
    
    private ShardingSphereTransformationPoint<?> getSingleStaticMethodPoint(final InDefinedShape methodDescription,
                                                                            final StaticMethodAdvisor staticMethodAdvisor, final ClassLoader classLoader) {
        StaticMethodAroundAdvice staticMethodAroundAdvice = pluginLoader.getOrCreateInstance(staticMethodAdvisor.getAdviceClassName(), classLoader);
        return staticMethodAdvisor.isOverrideArgs()
                ? new ShardingSphereTransformationPoint<>(methodDescription, new StaticMethodInterceptorArgsOverride(staticMethodAroundAdvice))
                : new ShardingSphereTransformationPoint<>(methodDescription, new StaticMethodAroundInterceptor(staticMethodAroundAdvice));
    }
    
    private ShardingSphereTransformationPoint<?> getComposedStaticMethodPoint(final InDefinedShape methodDescription,
                                                                              final Collection<StaticMethodAdvisor> staticMethodAdvisors, final ClassLoader classLoader) {
        Collection<StaticMethodAroundAdvice> staticMethodAroundAdvices = new LinkedList<>();
        boolean isArgsOverride = false;
        for (StaticMethodAdvisor each : staticMethodAdvisors) {
            if (each.isOverrideArgs()) {
                isArgsOverride = true;
            }
            if (null != each.getAdviceClassName()) {
                staticMethodAroundAdvices.add(pluginLoader.getOrCreateInstance(each.getAdviceClassName(), classLoader));
            }
        }
        return isArgsOverride ? new ShardingSphereTransformationPoint<>(methodDescription, new ComposedStaticMethodInterceptorArgsOverride(staticMethodAroundAdvices))
                : new ShardingSphereTransformationPoint<>(methodDescription, new ComposedStaticMethodAroundInterceptor(staticMethodAroundAdvices));
    }
    
    private Builder<?> interceptInstanceMethod(final TypeDescription description, final Collection<InstanceMethodAdvisor> instanceMethodAdvisors,
                                               final Builder<?> builder, final ClassLoader classLoader) {
        Collection<ShardingSphereTransformationPoint<?>> instanceMethodAdviceComposePoints = description.getDeclaredMethods().stream()
                .filter(each -> !(each.isAbstract() || each.isSynthetic()))
                .map(each -> getMatchedInstanceMethodPoint(instanceMethodAdvisors, each, classLoader))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Builder<?> result = builder;
        for (ShardingSphereTransformationPoint<?> each : instanceMethodAdviceComposePoints) {
            try {
                if (each.getInterceptor() instanceof InstanceMethodInterceptorArgsOverride || each.getInterceptor() instanceof ComposedInstanceMethodInterceptorArgsOverride) {
                    result = result.method(ElementMatchers.is(each.getDescription()))
                            .intercept(MethodDelegation.withDefaultConfiguration().withBinders(Morph.Binder.install(OverrideArgsInvoker.class)).to(each.getInterceptor()));
                } else {
                    result = result.method(ElementMatchers.is(each.getDescription()))
                            .intercept(MethodDelegation.withDefaultConfiguration().to(each.getInterceptor()));
                }
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                LOGGER.error("Failed to load advice class `{}`", description.getTypeName(), ex);
            }
        }
        return result;
    }
    
    private ShardingSphereTransformationPoint<?> getMatchedInstanceMethodPoint(final Collection<InstanceMethodAdvisor> instanceMethodAroundPoints,
                                                                               final InDefinedShape methodDescription, final ClassLoader classLoader) {
        List<InstanceMethodAdvisor> instanceMethodAdvisors = instanceMethodAroundPoints
                .stream().filter(each -> each.getMatcher().matches(methodDescription)).collect(Collectors.toList());
        if (instanceMethodAdvisors.isEmpty()) {
            return null;
        }
        if (1 == instanceMethodAdvisors.size()) {
            return getSingleInstanceMethodPoint(methodDescription, instanceMethodAdvisors.get(0), classLoader);
        }
        return getComposeInstanceMethodPoint(methodDescription, instanceMethodAdvisors, classLoader);
    }
    
    private ShardingSphereTransformationPoint<?> getSingleInstanceMethodPoint(final InDefinedShape methodDescription,
                                                                              final InstanceMethodAdvisor instanceMethodAdvisor, final ClassLoader classLoader) {
        InstanceMethodAroundAdvice instanceMethodAroundAdvice = pluginLoader.getOrCreateInstance(instanceMethodAdvisor.getAdviceClassName(), classLoader);
        return instanceMethodAdvisor.isOverrideArgs()
                ? new ShardingSphereTransformationPoint<>(methodDescription, new InstanceMethodInterceptorArgsOverride(instanceMethodAroundAdvice))
                : new ShardingSphereTransformationPoint<>(methodDescription, new InstanceMethodAroundInterceptor(instanceMethodAroundAdvice));
    }
    
    private ShardingSphereTransformationPoint<?> getComposeInstanceMethodPoint(final InDefinedShape methodDescription,
                                                                               final Collection<InstanceMethodAdvisor> instanceMethodAdvisors, final ClassLoader classLoader) {
        Collection<InstanceMethodAroundAdvice> instanceMethodAroundAdvices = new LinkedList<>();
        boolean isArgsOverride = false;
        for (InstanceMethodAdvisor each : instanceMethodAdvisors) {
            if (each.isOverrideArgs()) {
                isArgsOverride = true;
            }
            if (null != each.getAdviceClassName()) {
                instanceMethodAroundAdvices.add(pluginLoader.getOrCreateInstance(each.getAdviceClassName(), classLoader));
            }
        }
        return isArgsOverride
                ? new ShardingSphereTransformationPoint<>(methodDescription, new ComposedInstanceMethodInterceptorArgsOverride(instanceMethodAroundAdvices))
                : new ShardingSphereTransformationPoint<>(methodDescription, new ComposedInstanceMethodAroundInterceptor(instanceMethodAroundAdvices));
    }
}
