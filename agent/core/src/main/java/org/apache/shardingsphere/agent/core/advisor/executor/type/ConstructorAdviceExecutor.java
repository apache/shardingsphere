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

package org.apache.shardingsphere.agent.core.advisor.executor.type;

import lombok.RequiredArgsConstructor;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.api.advice.type.ConstructorAdvice;
import org.apache.shardingsphere.agent.api.plugin.AgentPluginEnable;
import org.apache.shardingsphere.agent.core.advisor.executor.AdviceExecutor;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Constructor advice executor.
 */
@RequiredArgsConstructor
public final class ConstructorAdviceExecutor implements AdviceExecutor {
    
    private static final Logger LOGGER = Logger.getLogger(ConstructorAdviceExecutor.class.getName());
    
    private final Map<String, Collection<ConstructorAdvice>> advices;
    
    /**
     * Advice constructor.
     *
     * @param target target object
     * @param args all constructor arguments
     */
    @RuntimeType
    public void advice(@This final TargetAdviceObject target, @AllArguments final Object[] args) {
        try {
            for (Entry<String, Collection<ConstructorAdvice>> entry : advices.entrySet()) {
                for (ConstructorAdvice each : entry.getValue()) {
                    if (isPluginEnabled(each)) {
                        each.onConstructor(target, args, entry.getKey());
                    }
                }
            }
            // CHECKSTYLE:OFF
        } catch (final Throwable ex) {
            // CHECKSTYLE:ON
            LOGGER.log(Level.SEVERE, "Constructor advice execution error. class: {0}, {1}", new String[]{target.getClass().getTypeName(), ex.getMessage()});
        }
    }
    
    private boolean isPluginEnabled(final ConstructorAdvice advice) {
        return !(advice instanceof AgentPluginEnable) || ((AgentPluginEnable) advice).isPluginEnabled();
    }
    
    @Override
    public Builder<?> intercept(final Builder<?> builder, final MethodDescription pointcut) {
        return builder.constructor(ElementMatchers.is(pointcut)).intercept(SuperMethodCall.INSTANCE.andThen(MethodDelegation.withDefaultConfiguration().to(this)));
    }
}
