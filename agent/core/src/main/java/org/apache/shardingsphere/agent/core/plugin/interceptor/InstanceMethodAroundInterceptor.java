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

package org.apache.shardingsphere.agent.core.plugin.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import org.apache.shardingsphere.agent.core.logging.LoggerFactory;
import org.apache.shardingsphere.agent.core.plugin.MethodInvocationResult;
import org.apache.shardingsphere.agent.core.plugin.PluginContext;
import org.apache.shardingsphere.agent.core.plugin.TargetAdviceObject;
import org.apache.shardingsphere.agent.core.plugin.interceptor.executor.InstanceMethodAdviceExecutor;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * Proxy class for ByteBuddy to intercept methods of target and weave pre- and post-method around the target method.
 */
@RequiredArgsConstructor
public class InstanceMethodAroundInterceptor {
    
    private static final LoggerFactory.Logger LOGGER = LoggerFactory.getLogger(InstanceMethodAroundInterceptor.class);
    
    private final Collection<InstanceMethodAdviceExecutor> executors;
    
    /**
     * Only intercept instance method.
     *
     * @param target the target object
     * @param method the intercepted method
     * @param args the all arguments of method
     * @param callable the origin method invocation
     * @return the return value of target invocation
     */
    @RuntimeType
    @SneakyThrows
    public Object intercept(@This final TargetAdviceObject target, @Origin final Method method, @AllArguments final Object[] args, @SuperCall final Callable<?> callable) {
        MethodInvocationResult methodResult = new MethodInvocationResult();
        Object result;
        boolean adviceEnabled = PluginContext.isPluginEnabled();
        try {
            if (adviceEnabled) {
                for (InstanceMethodAdviceExecutor each : executors) {
                    each.beforeMethod(target, method, args, methodResult);
                }
            }
            // CHECKSTYLE:OFF
        } catch (final Throwable ex) {
            // CHECKSTYLE:ON
            LOGGER.error("Failed to execute the pre-method of method[{}] in class[{}]", method.getName(), target.getClass(), ex);
        }
        try {
            if (methodResult.isRebased()) {
                result = methodResult.getResult();
            } else {
                result = callable.call();
            }
            methodResult.rebase(result);
            // CHECKSTYLE:OFF
        } catch (final Throwable ex) {
            // CHECKSTYLE:ON
            try {
                if (adviceEnabled) {
                    for (InstanceMethodAdviceExecutor each : executors) {
                        each.onThrowing(target, method, args, ex);
                    }
                }
                // CHECKSTYLE:OFF
            } catch (final Throwable ignored) {
                // CHECKSTYLE:ON
                LOGGER.error("Failed to execute the error handler of method[{}] in class[{}]", method.getName(), target.getClass(), ex);
            }
            throw ex;
        } finally {
            try {
                if (adviceEnabled) {
                    for (InstanceMethodAdviceExecutor each : executors) {
                        each.afterMethod(target, method, args, methodResult);
                    }
                }
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                LOGGER.error("Failed to execute the post-method of method[{}] in class[{}]", method.getName(), target.getClass(), ex);
            }
        }
        return methodResult.isRebased() ? methodResult.getResult() : result;
    }
}
