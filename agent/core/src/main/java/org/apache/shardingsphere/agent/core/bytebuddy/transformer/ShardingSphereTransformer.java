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
import org.apache.shardingsphere.agent.api.advice.ClassStaticMethodAroundAdvice;
import org.apache.shardingsphere.agent.api.advice.ConstructorAdvice;
import org.apache.shardingsphere.agent.api.advice.InstanceMethodAroundAdvice;
import org.apache.shardingsphere.agent.api.advice.OverrideArgsInvoker;
import org.apache.shardingsphere.agent.api.point.ClassStaticMethodPoint;
import org.apache.shardingsphere.agent.api.point.ConstructorPoint;
import org.apache.shardingsphere.agent.api.point.InstanceMethodPoint;
import org.apache.shardingsphere.agent.api.point.PluginInterceptorPoint;
import org.apache.shardingsphere.agent.core.logging.LoggerFactory;
import org.apache.shardingsphere.agent.core.plugin.PluginLoader;
import org.apache.shardingsphere.agent.core.plugin.interceptor.ClassStaticMethodAroundInterceptor;
import org.apache.shardingsphere.agent.core.plugin.interceptor.ClassStaticMethodInterceptorArgsOverride;
import org.apache.shardingsphere.agent.core.plugin.interceptor.ConstructorInterceptor;
import org.apache.shardingsphere.agent.core.plugin.interceptor.InstanceMethodAroundInterceptor;
import org.apache.shardingsphere.agent.core.plugin.interceptor.InstanceMethodInterceptorArgsOverride;
import org.apache.shardingsphere.agent.core.plugin.interceptor.compose.ComposeClassStaticMethodAroundInterceptor;
import org.apache.shardingsphere.agent.core.plugin.interceptor.compose.ComposeClassStaticMethodInterceptorArgsOverride;
import org.apache.shardingsphere.agent.core.plugin.interceptor.compose.ComposeConstructorInterceptor;
import org.apache.shardingsphere.agent.core.plugin.interceptor.compose.ComposeInstanceMethodAroundInterceptor;
import org.apache.shardingsphere.agent.core.plugin.interceptor.compose.ComposeInstanceMethodInterceptorArgsOverride;

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
        PluginInterceptorPoint pluginInterceptorPoint = pluginLoader.loadPluginInterceptorPoint(typeDescription);
        result = interceptorConstructorPoint(typeDescription, pluginInterceptorPoint.getConstructorPoints(), result, classLoader);
        result = interceptorClassStaticMethodPoint(typeDescription, pluginInterceptorPoint.getClassStaticMethodPoints(), result, classLoader);
        result = interceptorInstanceMethodPoint(typeDescription, pluginInterceptorPoint.getInstanceMethodPoints(), result, classLoader);
        return result;
    }
    
    private Builder<?> interceptorConstructorPoint(final TypeDescription description, final Collection<ConstructorPoint> constructorPoints, final Builder<?> builder, final ClassLoader classLoader) {
        Collection<ShardingSphereTransformationPoint<? extends ConstructorInterceptor>> constructorAdviceComposePoints = description.getDeclaredMethods().stream()
                .filter(MethodDescription::isConstructor)
                .map(each -> getMatchedTransformationPoint(constructorPoints, each, classLoader))
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
    
    private ShardingSphereTransformationPoint<? extends ConstructorInterceptor> getMatchedTransformationPoint(final Collection<ConstructorPoint> constructorPoints,
                                                                                                              final InDefinedShape methodDescription,
                                                                                                              final ClassLoader classLoader) {
        List<ConstructorPoint> matchedConstructorPoints = constructorPoints.stream().filter(each -> each.getMatcher().matches(methodDescription)).collect(Collectors.toList());
        if (matchedConstructorPoints.isEmpty()) {
            return null;
        }
        if (1 == matchedConstructorPoints.size()) {
            return new ShardingSphereTransformationPoint<>(methodDescription, new ConstructorInterceptor(pluginLoader.getOrCreateInstance(matchedConstructorPoints.get(0).getAdvice(), classLoader)));
        }
        Collection<ConstructorAdvice> constructorAdvices = matchedConstructorPoints.stream()
                .map(ConstructorPoint::getAdvice)
                .map(each -> (ConstructorAdvice) pluginLoader.getOrCreateInstance(each, classLoader))
                .collect(Collectors.toList());
        return new ShardingSphereTransformationPoint<>(methodDescription, new ComposeConstructorInterceptor(constructorAdvices));
    }
    
    private Builder<?> interceptorClassStaticMethodPoint(final TypeDescription description, final Collection<ClassStaticMethodPoint> staticMethodAroundPoints,
                                                         final Builder<?> builder, final ClassLoader classLoader) {
        Collection<ShardingSphereTransformationPoint<?>> classStaticMethodAdvicePoints = description.getDeclaredMethods().stream()
                .filter(each -> each.isStatic() && !(each.isAbstract() || each.isSynthetic()))
                .map(each -> getMatchedStaticMethodPoint(staticMethodAroundPoints, each, classLoader))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Builder<?> result = builder;
        for (ShardingSphereTransformationPoint<?> each : classStaticMethodAdvicePoints) {
            try {
                if (each.getInterceptor() instanceof ClassStaticMethodInterceptorArgsOverride || each.getInterceptor() instanceof ComposeClassStaticMethodInterceptorArgsOverride) {
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
    
    private ShardingSphereTransformationPoint<?> getMatchedStaticMethodPoint(final Collection<ClassStaticMethodPoint> staticMethodAroundPoints,
                                                                             final InDefinedShape methodDescription, final ClassLoader classLoader) {
        List<ClassStaticMethodPoint> staticMethodPoints = staticMethodAroundPoints.stream().filter(each -> each.getMatcher().matches(methodDescription)).collect(Collectors.toList());
        if (staticMethodPoints.isEmpty()) {
            return null;
        }
        if (1 == staticMethodPoints.size()) {
            return getSingleStaticMethodPoint(methodDescription, staticMethodPoints.get(0), classLoader);
        }
        return getComposeStaticMethodPoint(methodDescription, staticMethodPoints, classLoader);
    }
    
    private ShardingSphereTransformationPoint<?> getSingleStaticMethodPoint(final InDefinedShape methodDescription, final ClassStaticMethodPoint staticMethodPoint, final ClassLoader classLoader) {
        ClassStaticMethodAroundAdvice staticMethodAroundAdvice = pluginLoader.getOrCreateInstance(staticMethodPoint.getAdvice(), classLoader);
        return staticMethodPoint.isOverrideArgs()
                ? new ShardingSphereTransformationPoint<>(methodDescription, new ClassStaticMethodInterceptorArgsOverride(staticMethodAroundAdvice))
                : new ShardingSphereTransformationPoint<>(methodDescription, new ClassStaticMethodAroundInterceptor(staticMethodAroundAdvice));
    }
    
    private ShardingSphereTransformationPoint<?> getComposeStaticMethodPoint(final InDefinedShape methodDescription,
                                                                             final Collection<ClassStaticMethodPoint> staticMethodPoints, final ClassLoader classLoader) {
        Collection<ClassStaticMethodAroundAdvice> classStaticMethodAroundAdvices = new LinkedList<>();
        boolean isArgsOverride = false;
        for (ClassStaticMethodPoint each : staticMethodPoints) {
            if (each.isOverrideArgs()) {
                isArgsOverride = true;
            }
            if (null != each.getAdvice()) {
                classStaticMethodAroundAdvices.add(pluginLoader.getOrCreateInstance(each.getAdvice(), classLoader));
            }
        }
        return isArgsOverride ? new ShardingSphereTransformationPoint<>(methodDescription, new ComposeClassStaticMethodInterceptorArgsOverride(classStaticMethodAroundAdvices))
                : new ShardingSphereTransformationPoint<>(methodDescription, new ComposeClassStaticMethodAroundInterceptor(classStaticMethodAroundAdvices));
    }
    
    private Builder<?> interceptorInstanceMethodPoint(final TypeDescription description, final Collection<InstanceMethodPoint> instanceMethodAroundPoints,
                                                      final Builder<?> builder, final ClassLoader classLoader) {
        Collection<ShardingSphereTransformationPoint<?>> instanceMethodAdviceComposePoints = description.getDeclaredMethods().stream()
                .filter(each -> !(each.isAbstract() || each.isSynthetic()))
                .map(each -> getMatchedInstanceMethodPoint(instanceMethodAroundPoints, each, classLoader))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Builder<?> result = builder;
        for (ShardingSphereTransformationPoint<?> each : instanceMethodAdviceComposePoints) {
            try {
                if (each.getInterceptor() instanceof InstanceMethodInterceptorArgsOverride || each.getInterceptor() instanceof ComposeInstanceMethodInterceptorArgsOverride) {
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
    
    private ShardingSphereTransformationPoint<?> getMatchedInstanceMethodPoint(final Collection<InstanceMethodPoint> instanceMethodAroundPoints,
                                                                               final InDefinedShape methodDescription, final ClassLoader classLoader) {
        List<InstanceMethodPoint> instanceMethodPoints = instanceMethodAroundPoints.stream().filter(each -> each.getMatcher().matches(methodDescription)).collect(Collectors.toList());
        if (instanceMethodPoints.isEmpty()) {
            return null;
        }
        if (1 == instanceMethodPoints.size()) {
            return getSingleInstanceMethodPoint(methodDescription, instanceMethodPoints.get(0), classLoader);
        }
        return getComposeInstanceMethodPoint(methodDescription, instanceMethodPoints, classLoader);
    }
    
    private ShardingSphereTransformationPoint<?> getSingleInstanceMethodPoint(final InDefinedShape methodDescription, final InstanceMethodPoint instanceMethodPoint, final ClassLoader classLoader) {
        InstanceMethodAroundAdvice instanceMethodAroundAdvice = pluginLoader.getOrCreateInstance(instanceMethodPoint.getAdvice(), classLoader);
        return instanceMethodPoint.isOverrideArgs()
                ? new ShardingSphereTransformationPoint<>(methodDescription, new InstanceMethodInterceptorArgsOverride(instanceMethodAroundAdvice))
                : new ShardingSphereTransformationPoint<>(methodDescription, new InstanceMethodAroundInterceptor(instanceMethodAroundAdvice));
    }
    
    private ShardingSphereTransformationPoint<?> getComposeInstanceMethodPoint(final InDefinedShape methodDescription,
                                                                               final Collection<InstanceMethodPoint> instanceMethodPoints, final ClassLoader classLoader) {
        Collection<InstanceMethodAroundAdvice> instanceMethodAroundAdvices = new LinkedList<>();
        boolean isArgsOverride = false;
        for (InstanceMethodPoint each : instanceMethodPoints) {
            if (each.isOverrideArgs()) {
                isArgsOverride = true;
            }
            if (null != each.getAdvice()) {
                instanceMethodAroundAdvices.add(pluginLoader.getOrCreateInstance(each.getAdvice(), classLoader));
            }
        }
        return isArgsOverride
                ? new ShardingSphereTransformationPoint<>(methodDescription, new ComposeInstanceMethodInterceptorArgsOverride(instanceMethodAroundAdvices))
                : new ShardingSphereTransformationPoint<>(methodDescription, new ComposeInstanceMethodAroundInterceptor(instanceMethodAroundAdvices));
    }
}
