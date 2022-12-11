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
import org.apache.shardingsphere.agent.api.advice.AdviceTargetObject;
import org.apache.shardingsphere.agent.api.advice.StaticMethodAroundAdvice;
import org.apache.shardingsphere.agent.api.advice.ConstructorAdvice;
import org.apache.shardingsphere.agent.api.advice.InstanceMethodAroundAdvice;
import org.apache.shardingsphere.agent.api.advice.OverrideArgsInvoker;
import org.apache.shardingsphere.agent.api.pointcut.StaticMethodPointcut;
import org.apache.shardingsphere.agent.api.pointcut.ConstructorPointcut;
import org.apache.shardingsphere.agent.api.pointcut.InstanceMethodPointcut;
import org.apache.shardingsphere.agent.api.pointcut.PluginPointcuts;
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
        Builder<?> result = builder.defineField(EXTRA_DATA, Object.class, Opcodes.ACC_PRIVATE | Opcodes.ACC_VOLATILE).implement(AdviceTargetObject.class).intercept(FieldAccessor.ofField(EXTRA_DATA));
        PluginPointcuts pluginPointcuts = pluginLoader.loadPluginInterceptorPoint(typeDescription);
        result = interceptConstructor(typeDescription, pluginPointcuts.getConstructorPointcuts(), result, classLoader);
        result = interceptStaticMethod(typeDescription, pluginPointcuts.getStaticMethodPointcuts(), result, classLoader);
        result = interceptInstanceMethod(typeDescription, pluginPointcuts.getInstanceMethodPointcuts(), result, classLoader);
        return result;
    }
    
    private Builder<?> interceptConstructor(final TypeDescription description,
                                            final Collection<ConstructorPointcut> constructorPointcuts, final Builder<?> builder, final ClassLoader classLoader) {
        Collection<ShardingSphereTransformationPoint<? extends ConstructorInterceptor>> constructorAdviceComposePoints = description.getDeclaredMethods().stream()
                .filter(MethodDescription::isConstructor)
                .map(each -> getMatchedTransformationPoint(constructorPointcuts, each, classLoader))
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
    
    private ShardingSphereTransformationPoint<? extends ConstructorInterceptor> getMatchedTransformationPoint(final Collection<ConstructorPointcut> constructorPointcuts,
                                                                                                              final InDefinedShape methodDescription, final ClassLoader classLoader) {
        List<ConstructorPointcut> matchedConstructorPointcuts = constructorPointcuts
                .stream().filter(each -> each.getMatcher().matches(methodDescription)).collect(Collectors.toList());
        if (matchedConstructorPointcuts.isEmpty()) {
            return null;
        }
        if (1 == matchedConstructorPointcuts.size()) {
            return new ShardingSphereTransformationPoint<>(
                    methodDescription, new ConstructorInterceptor(pluginLoader.getOrCreateInstance(matchedConstructorPointcuts.get(0).getAdviceClassName(), classLoader)));
        }
        Collection<ConstructorAdvice> constructorAdvices = matchedConstructorPointcuts.stream()
                .map(ConstructorPointcut::getAdviceClassName)
                .map(each -> (ConstructorAdvice) pluginLoader.getOrCreateInstance(each, classLoader))
                .collect(Collectors.toList());
        return new ShardingSphereTransformationPoint<>(methodDescription, new ComposedConstructorInterceptor(constructorAdvices));
    }
    
    private Builder<?> interceptStaticMethod(final TypeDescription description, final Collection<StaticMethodPointcut> staticMethodPointcuts,
                                             final Builder<?> builder, final ClassLoader classLoader) {
        Collection<ShardingSphereTransformationPoint<?>> staticMethodAdvicePoints = description.getDeclaredMethods().stream()
                .filter(each -> each.isStatic() && !(each.isAbstract() || each.isSynthetic()))
                .map(each -> getMatchedStaticMethodPoint(staticMethodPointcuts, each, classLoader))
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
    
    private ShardingSphereTransformationPoint<?> getMatchedStaticMethodPoint(final Collection<StaticMethodPointcut> staticMethodAroundPoints,
                                                                             final InDefinedShape methodDescription, final ClassLoader classLoader) {
        List<StaticMethodPointcut> staticMethodPointcuts = staticMethodAroundPoints.stream().filter(each -> each.getMatcher().matches(methodDescription)).collect(Collectors.toList());
        if (staticMethodPointcuts.isEmpty()) {
            return null;
        }
        if (1 == staticMethodPointcuts.size()) {
            return getSingleStaticMethodPoint(methodDescription, staticMethodPointcuts.get(0), classLoader);
        }
        return getComposedStaticMethodPoint(methodDescription, staticMethodPointcuts, classLoader);
    }
    
    private ShardingSphereTransformationPoint<?> getSingleStaticMethodPoint(final InDefinedShape methodDescription,
                                                                            final StaticMethodPointcut staticMethodPointcut, final ClassLoader classLoader) {
        StaticMethodAroundAdvice staticMethodAroundAdvice = pluginLoader.getOrCreateInstance(staticMethodPointcut.getAdviceClassName(), classLoader);
        return staticMethodPointcut.isOverrideArgs()
                ? new ShardingSphereTransformationPoint<>(methodDescription, new StaticMethodInterceptorArgsOverride(staticMethodAroundAdvice))
                : new ShardingSphereTransformationPoint<>(methodDescription, new StaticMethodAroundInterceptor(staticMethodAroundAdvice));
    }
    
    private ShardingSphereTransformationPoint<?> getComposedStaticMethodPoint(final InDefinedShape methodDescription,
                                                                              final Collection<StaticMethodPointcut> staticMethodPointcuts, final ClassLoader classLoader) {
        Collection<StaticMethodAroundAdvice> staticMethodAroundAdvices = new LinkedList<>();
        boolean isArgsOverride = false;
        for (StaticMethodPointcut each : staticMethodPointcuts) {
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
    
    private Builder<?> interceptInstanceMethod(final TypeDescription description, final Collection<InstanceMethodPointcut> instanceMethodPointcuts,
                                               final Builder<?> builder, final ClassLoader classLoader) {
        Collection<ShardingSphereTransformationPoint<?>> instanceMethodAdviceComposePoints = description.getDeclaredMethods().stream()
                .filter(each -> !(each.isAbstract() || each.isSynthetic()))
                .map(each -> getMatchedInstanceMethodPoint(instanceMethodPointcuts, each, classLoader))
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
    
    private ShardingSphereTransformationPoint<?> getMatchedInstanceMethodPoint(final Collection<InstanceMethodPointcut> instanceMethodAroundPoints,
                                                                               final InDefinedShape methodDescription, final ClassLoader classLoader) {
        List<InstanceMethodPointcut> instanceMethodPointcuts = instanceMethodAroundPoints
                .stream().filter(each -> each.getMatcher().matches(methodDescription)).collect(Collectors.toList());
        if (instanceMethodPointcuts.isEmpty()) {
            return null;
        }
        if (1 == instanceMethodPointcuts.size()) {
            return getSingleInstanceMethodPoint(methodDescription, instanceMethodPointcuts.get(0), classLoader);
        }
        return getComposeInstanceMethodPoint(methodDescription, instanceMethodPointcuts, classLoader);
    }
    
    private ShardingSphereTransformationPoint<?> getSingleInstanceMethodPoint(final InDefinedShape methodDescription,
                                                                              final InstanceMethodPointcut instanceMethodPointcut, final ClassLoader classLoader) {
        InstanceMethodAroundAdvice instanceMethodAroundAdvice = pluginLoader.getOrCreateInstance(instanceMethodPointcut.getAdviceClassName(), classLoader);
        return instanceMethodPointcut.isOverrideArgs()
                ? new ShardingSphereTransformationPoint<>(methodDescription, new InstanceMethodInterceptorArgsOverride(instanceMethodAroundAdvice))
                : new ShardingSphereTransformationPoint<>(methodDescription, new InstanceMethodAroundInterceptor(instanceMethodAroundAdvice));
    }
    
    private ShardingSphereTransformationPoint<?> getComposeInstanceMethodPoint(final InDefinedShape methodDescription,
                                                                               final Collection<InstanceMethodPointcut> instanceMethodPointcuts, final ClassLoader classLoader) {
        Collection<InstanceMethodAroundAdvice> instanceMethodAroundAdvices = new LinkedList<>();
        boolean isArgsOverride = false;
        for (InstanceMethodPointcut each : instanceMethodPointcuts) {
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
