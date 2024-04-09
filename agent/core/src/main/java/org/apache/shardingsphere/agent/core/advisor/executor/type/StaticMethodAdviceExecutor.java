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
import lombok.SneakyThrows;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.api.plugin.AgentPluginEnable;
import org.apache.shardingsphere.agent.api.advice.type.StaticMethodAdvice;
import org.apache.shardingsphere.agent.core.advisor.executor.AdviceExecutor;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Static method advice executor.
 */
@RequiredArgsConstructor
public final class StaticMethodAdviceExecutor implements AdviceExecutor {
    
    private static final Logger LOGGER = Logger.getLogger(StaticMethodAdviceExecutor.class.getName());
    
    private final Map<String, Collection<StaticMethodAdvice>> advices;
    
    /**
     * Advice static method.
     *
     * @param klass target class
     * @param method advised method
     * @param args all arguments of method
     * @param callable origin method invocation
     * @return return value of target invocation
     */
    @RuntimeType
    @SneakyThrows
    public Object advice(@Origin final Class<?> klass, @Origin final Method method, @AllArguments final Object[] args, @SuperCall final Callable<?> callable) {
        adviceBefore(klass, method, args);
        Object result = null;
        try {
            result = callable.call();
            // CHECKSTYLE:OFF
        } catch (final Throwable ex) {
            // CHECKSTYLE:ON
            adviceThrow(klass, method, args, ex);
            throw ex;
        } finally {
            adviceAfter(klass, method, args, result);
        }
        return result;
    }
    
    private void adviceBefore(final Class<?> klass, final Method method, final Object[] args) {
        try {
            for (Entry<String, Collection<StaticMethodAdvice>> entry : advices.entrySet()) {
                for (StaticMethodAdvice each : entry.getValue()) {
                    if (isPluginEnabled(each)) {
                        each.beforeMethod(klass, method, args, entry.getKey());
                    }
                }
            }
            // CHECKSTYLE:OFF
        } catch (final Throwable ex) {
            // CHECKSTYLE:ON
            LOGGER.log(Level.SEVERE, "Failed to execute the pre-method of method `{0}` in class `{1}`, {2}.", new String[]{method.getName(), klass.getClass().getName(), ex.getMessage()});
        }
    }
    
    private void adviceThrow(final Class<?> klass, final Method method, final Object[] args, final Throwable ex) {
        try {
            for (Entry<String, Collection<StaticMethodAdvice>> entry : advices.entrySet()) {
                for (StaticMethodAdvice each : entry.getValue()) {
                    if (isPluginEnabled(each)) {
                        each.onThrowing(klass, method, args, ex, entry.getKey());
                    }
                }
            }
            // CHECKSTYLE:OFF
        } catch (final Throwable ignored) {
            // CHECKSTYLE:ON
            LOGGER.log(Level.SEVERE, "Failed to execute the error handler of method `{0}` in class `{1}`, {2}.", new String[]{method.getName(), klass.getClass().getName(), ex.getMessage()});
        }
    }
    
    private void adviceAfter(final Class<?> klass, final Method method, final Object[] args, final Object result) {
        try {
            for (Entry<String, Collection<StaticMethodAdvice>> entry : advices.entrySet()) {
                for (StaticMethodAdvice each : entry.getValue()) {
                    if (isPluginEnabled(each)) {
                        each.afterMethod(klass, method, args, result, entry.getKey());
                    }
                }
            }
            // CHECKSTYLE:OFF
        } catch (final Throwable ex) {
            // CHECKSTYLE:ON
            LOGGER.log(Level.SEVERE, "Failed to execute the post-method of method `{0}` in class `{1}` {2}.", new String[]{method.getName(), klass.getClass().getName(), ex.getMessage()});
        }
    }
    
    private boolean isPluginEnabled(final StaticMethodAdvice advice) {
        return !(advice instanceof AgentPluginEnable) || ((AgentPluginEnable) advice).isPluginEnabled();
    }
    
    @Override
    public Builder<?> intercept(final Builder<?> builder, final MethodDescription pointcut) {
        return builder.method(ElementMatchers.is(pointcut)).intercept(MethodDelegation.withDefaultConfiguration().to(this));
    }
}
