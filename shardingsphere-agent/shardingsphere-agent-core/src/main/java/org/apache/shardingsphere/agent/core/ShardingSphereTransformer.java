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
import org.apache.shardingsphere.agent.core.plugin.PluginAdviceDefine;
import org.apache.shardingsphere.agent.core.plugin.PluginLoader;
import org.apache.shardingsphere.agent.core.plugin.advice.ConstructorMethodInterceptor;
import org.apache.shardingsphere.agent.core.plugin.advice.MethodAroundInterceptor;
import org.apache.shardingsphere.agent.core.plugin.advice.StaticMethodAroundInterceptor;
import org.apache.shardingsphere.agent.core.plugin.advice.TargetObject;
import org.apache.shardingsphere.agent.core.plugin.point.ClassStaticMethodPoint;
import org.apache.shardingsphere.agent.core.plugin.point.ConstructorPoint;
import org.apache.shardingsphere.agent.core.plugin.point.InstanceMethodPoint;

import java.util.Map;

/**
 *  Shardingsphere transformer.
 */
@Slf4j
public class ShardingSphereTransformer implements AgentBuilder.Transformer {

    private final PluginLoader pluginLoader;

    public ShardingSphereTransformer(final PluginLoader pluginLoader) {
        this.pluginLoader = pluginLoader;
    }

    @Override
    public DynamicType.Builder<?> transform(final DynamicType.Builder<?> builder, final TypeDescription typeDescription, final ClassLoader classLoader, final JavaModule module) {
        if (pluginLoader.containsType(typeDescription)) {

            DynamicType.Builder<?> newBuilder = builder.defineField("_SSExtraData_", Map.class, Opcodes.ACC_PRIVATE | Opcodes.ACC_VOLATILE)
                    .implement(TargetObject.class)
                    .intercept(FieldAccessor.ofField("_SSExtraData_"));

            final PluginAdviceDefine define = pluginLoader.loadPluginAdviceDefine(typeDescription);
            for (ConstructorPoint point : define.getConstructorPoints()) {
                try {
                    final ConstructorMethodInterceptor interceptor = new ConstructorMethodInterceptor(pluginLoader.getInstance(point.getAdvice()));
                    newBuilder = newBuilder.constructor(point.getConstructorMatcher())
                            .intercept(SuperMethodCall.INSTANCE.andThen(MethodDelegation.withDefaultConfiguration().to(interceptor)));
                    // CHECKSTYLE:OFF
                } catch (Exception e) {
                    // CHECKSTYLE:ON
                    log.error("Failed to load advice class: {}", point.getAdvice(), e);
                }
            }

            for (ClassStaticMethodPoint point : define.getClassStaticMethodPoints()) {
                try {
                    final StaticMethodAroundInterceptor interceptor = new StaticMethodAroundInterceptor(pluginLoader.getInstance(point.getAdvice()));
                    newBuilder = newBuilder.method(point.getMethodsMatcher())
                            .intercept(MethodDelegation.withDefaultConfiguration().to(interceptor));
                    // CHECKSTYLE:OFF
                } catch (Exception e) {
                    // CHECKSTYLE:ON
                    log.error("Failed to load advice class: {}", point.getAdvice(), e);
                }
            }

            for (InstanceMethodPoint point : define.getInstanceMethodPoints()) {
                try {
                    final MethodAroundInterceptor interceptor = new MethodAroundInterceptor(pluginLoader.getInstance(point.getAdvice()));
                    newBuilder = newBuilder.method(point.getMethodMatcher())
                            .intercept(MethodDelegation.withDefaultConfiguration().to(interceptor));
                    // CHECKSTYLE:OFF
                } catch (Exception e) {
                    // CHECKSTYLE:ON
                    log.error("Failed to load advice class: {}", point.getAdvice(), e);
                }
            }
            return newBuilder;
        }

        return builder;
    }
}
