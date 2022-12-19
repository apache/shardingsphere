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

package org.apache.shardingsphere.agent.core.plugin.executor.type;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.shardingsphere.agent.advice.MethodInvocationResult;
import org.apache.shardingsphere.agent.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.advice.type.InstanceMethodAdvice;
import org.apache.shardingsphere.agent.core.logging.LoggerFactory;
import org.apache.shardingsphere.agent.core.plugin.PluginContext;
import org.apache.shardingsphere.agent.core.plugin.executor.AdviceExecutor;
import org.apache.shardingsphere.agent.core.transformer.MethodAdvisor;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * Instance method advice executor.
 */
@RequiredArgsConstructor
public final class InstanceMethodAdviceExecutor implements AdviceExecutor {
    
    private static final LoggerFactory.Logger LOGGER = LoggerFactory.getLogger(InstanceMethodAdviceExecutor.class);
    
    private final Collection<InstanceMethodAdvice> advices;
    
    /**
     * Intercept instance method.
     *
     * @param target target object
     * @param method intercepted method
     * @param args all arguments of method
     * @param callable origin method invocation
     * @return return value of target invocation
     */
    @RuntimeType
    @SneakyThrows
    public Object intercept(@This final TargetAdviceObject target, @Origin final Method method, @AllArguments final Object[] args, @SuperCall final Callable<?> callable) {
        MethodInvocationResult invocationResult = new MethodInvocationResult();
        boolean adviceEnabled = PluginContext.isPluginEnabled();
        if (adviceEnabled) {
            interceptBefore(target, method, args, invocationResult);
        }
        Object result;
        try {
            result = invocationResult.isRebased() ? invocationResult.getResult() : callable.call();
            invocationResult.rebase(result);
            // CHECKSTYLE:OFF
        } catch (final Throwable ex) {
            // CHECKSTYLE:ON
            if (adviceEnabled) {
                interceptThrow(target, method, args, ex);
            }
            throw ex;
        } finally {
            if (adviceEnabled) {
                interceptAfter(target, method, args, invocationResult);
            }
        }
        return invocationResult.isRebased() ? invocationResult.getResult() : result;
    }
    
    private void interceptBefore(final TargetAdviceObject target, final Method method, final Object[] args, final MethodInvocationResult invocationResult) {
        try {
            for (InstanceMethodAdvice each : advices) {
                each.beforeMethod(target, method, args, invocationResult);
            }
            // CHECKSTYLE:OFF
        } catch (final Throwable ex) {
            // CHECKSTYLE:ON
            LOGGER.error("Failed to execute the pre-method of method `{}` in class `{}`.", method.getName(), target.getClass(), ex);
        }
    }
    
    private void interceptThrow(final TargetAdviceObject target, final Method method, final Object[] args, final Throwable ex) {
        try {
            for (InstanceMethodAdvice each : advices) {
                each.onThrowing(target, method, args, ex);
            }
            // CHECKSTYLE:OFF
        } catch (final Throwable ignored) {
            // CHECKSTYLE:ON
            LOGGER.error("Failed to execute the error handler of method `{}` in class `{}`.", method.getName(), target.getClass(), ex);
        }
    }
    
    private void interceptAfter(final TargetAdviceObject target, final Method method, final Object[] args, final MethodInvocationResult invocationResult) {
        try {
            for (InstanceMethodAdvice each : advices) {
                each.afterMethod(target, method, args, invocationResult);
            }
            // CHECKSTYLE:OFF
        } catch (final Throwable ex) {
            // CHECKSTYLE:ON
            LOGGER.error("Failed to execute the post-method of method `{}` in class `{}`.", method.getName(), target.getClass(), ex);
        }
    }
    
    @Override
    public Builder<?> buildAdvisor(final Builder<?> builder, final MethodAdvisor methodAdvisor) {
        return builder.method(ElementMatchers.is(methodAdvisor.getPointcut())).intercept(MethodDelegation.withDefaultConfiguration().to(methodAdvisor.getAdviceExecutor()));
    }
}
