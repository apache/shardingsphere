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
 *
 */

package org.apache.shardingsphere.agent.core;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.utility.JavaModule;
import org.apache.shardingsphere.agent.core.plugin.AgentPluginLoader;
import org.apache.shardingsphere.agent.core.plugin.PluginAdviceDefinition;
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
public class ShardingSphereTransformer implements AgentBuilder.Transformer {
    
    private static final String SS_EXTRA_DATA = "_$EXTRA_DATA$_";
    
    private final AgentPluginLoader agentPluginLoader;
    
    public ShardingSphereTransformer(final AgentPluginLoader agentPluginLoader) {
        this.agentPluginLoader = agentPluginLoader;
    }
    
    @Override
    public DynamicType.Builder<?> transform(final DynamicType.Builder<?> builder, final TypeDescription typeDescription, final ClassLoader classLoader, final JavaModule module) {
        if (agentPluginLoader.containsType(typeDescription)) {
            DynamicType.Builder<?> newBuilder = builder;
            newBuilder = newBuilder.defineField(SS_EXTRA_DATA, Object.class, Opcodes.ACC_PRIVATE | Opcodes.ACC_VOLATILE)
                    .implement(TargetObject.class)
                    .intercept(FieldAccessor.ofField(SS_EXTRA_DATA));
            final PluginAdviceDefinition define = agentPluginLoader.loadPluginAdviceDefine(typeDescription);
            for (ConstructorPoint point : define.getConstructorPoints()) {
                try {
                    final ConstructorMethodInterceptor interceptor = new ConstructorMethodInterceptor(agentPluginLoader.getOrCreateInstance(point.getAdvice()));
                    newBuilder = newBuilder.constructor(point.getConstructorMatcher()).intercept(SuperMethodCall.INSTANCE.andThen(MethodDelegation.withDefaultConfiguration().to(interceptor)));
                    // CHECKSTYLE:OFF
                } catch (Throwable e) {
                    // CHECKSTYLE:ON
                    log.error("Failed to load advice class: {}", point.getAdvice(), e);
                }
            }
            for (ClassStaticMethodPoint point : define.getClassStaticMethodPoints()) {
                try {
                    final StaticMethodAroundInterceptor interceptor = new StaticMethodAroundInterceptor(agentPluginLoader.getOrCreateInstance(point.getAdvice()));
                    newBuilder = newBuilder.method(point.getMethodsMatcher()).intercept(MethodDelegation.withDefaultConfiguration().to(interceptor));
                    // CHECKSTYLE:OFF
                } catch (Throwable e) {
                    // CHECKSTYLE:ON
                    log.error("Failed to load advice class: {}", point.getAdvice(), e);
                }
            }
            for (InstanceMethodPoint point : define.getInstanceMethodPoints()) {
                try {
                    final MethodAroundInterceptor interceptor = new MethodAroundInterceptor(agentPluginLoader.getOrCreateInstance(point.getAdvice()));
                    newBuilder = newBuilder.method(point.getMethodMatcher()).intercept(MethodDelegation.withDefaultConfiguration().to(interceptor));
                    // CHECKSTYLE:OFF
                } catch (Throwable e) {
                    // CHECKSTYLE:ON
                    log.error("Failed to load advice class: {}", point.getAdvice(), e);
                }
            }
            return newBuilder;
        }
        return builder;
    }
}
