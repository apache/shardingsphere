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
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.utility.JavaModule;
import org.apache.shardingsphere.agent.core.plugin.loader.AgentPluginLoader;
import org.apache.shardingsphere.agent.core.plugin.point.PluginInterceptorPoint;
import org.apache.shardingsphere.agent.core.plugin.advice.ConstructorMethodInterceptor;
import org.apache.shardingsphere.agent.core.plugin.advice.MethodAroundInterceptor;
import org.apache.shardingsphere.agent.core.plugin.advice.StaticMethodAroundInterceptor;
import org.apache.shardingsphere.agent.core.plugin.advice.TargetObject;
import org.apache.shardingsphere.agent.core.plugin.point.ClassStaticMethodPoint;
import org.apache.shardingsphere.agent.core.plugin.point.ConstructorPoint;
import org.apache.shardingsphere.agent.core.plugin.point.InstanceMethodPoint;

/**
 * Shardingsphere transformer.
 */
@Slf4j
@RequiredArgsConstructor
public final class ShardingSphereTransformer implements Transformer {
    
    private static final String EXTRA_DATA = "_$EXTRA_DATA$_";
    
    private final AgentPluginLoader agentPluginLoader;
    
    @Override
    public Builder<?> transform(final Builder<?> builder, final TypeDescription typeDescription, final ClassLoader classLoader, final JavaModule module) {
        if (agentPluginLoader.containsType(typeDescription)) {
            Builder<?> newBuilder = builder;
            newBuilder = newBuilder.defineField(EXTRA_DATA, Object.class, Opcodes.ACC_PRIVATE | Opcodes.ACC_VOLATILE)
                    .implement(TargetObject.class)
                    .intercept(FieldAccessor.ofField(EXTRA_DATA));
            final PluginInterceptorPoint pluginInterceptorPoint = agentPluginLoader.loadPluginInterceptorPoint(typeDescription);
            newBuilder = interceptorConstructorPoint(pluginInterceptorPoint, newBuilder);
            newBuilder = interceptorClassStaticMethodPoint(pluginInterceptorPoint, newBuilder);
            newBuilder = interceptorInstanceMethodPoint(pluginInterceptorPoint, newBuilder);
            return newBuilder;
        }
        return builder;
    }
    
    private Builder<?> interceptorConstructorPoint(final PluginInterceptorPoint pluginInterceptorPoint, final Builder<?> builder) {
        for (ConstructorPoint each : pluginInterceptorPoint.getConstructorPoints()) {
            try {
                ConstructorMethodInterceptor interceptor = new ConstructorMethodInterceptor(agentPluginLoader.getOrCreateInstance(each.getAdvice()));
                return builder.constructor(each.getMatcher()).intercept(SuperMethodCall.INSTANCE.andThen(MethodDelegation.withDefaultConfiguration().to(interceptor)));
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                log.error("Failed to load advice class: {}", each.getAdvice(), ex);
            }
        }
        return builder;
    }
    
    private Builder<?> interceptorClassStaticMethodPoint(final PluginInterceptorPoint pluginInterceptorPoint, final Builder<?> builder) {
        for (ClassStaticMethodPoint each : pluginInterceptorPoint.getClassStaticMethodPoints()) {
            try {
                StaticMethodAroundInterceptor interceptor = new StaticMethodAroundInterceptor(agentPluginLoader.getOrCreateInstance(each.getAdvice()));
                return builder.method(each.getMatcher()).intercept(MethodDelegation.withDefaultConfiguration().to(interceptor));
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                log.error("Failed to load advice class: {}", each.getAdvice(), ex);
            }
        }
        return builder;
    }
    
    private Builder<?> interceptorInstanceMethodPoint(final PluginInterceptorPoint pluginInterceptorPoint, final Builder<?> builder) {
        for (InstanceMethodPoint each : pluginInterceptorPoint.getInstanceMethodPoints()) {
            try {
                MethodAroundInterceptor interceptor = new MethodAroundInterceptor(agentPluginLoader.getOrCreateInstance(each.getAdvice()));
                return builder.method(each.getMatcher()).intercept(MethodDelegation.withDefaultConfiguration().to(interceptor));
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                log.error("Failed to load advice class: {}", each.getAdvice(), ex);
            }
        }
        return builder;
    }
}
