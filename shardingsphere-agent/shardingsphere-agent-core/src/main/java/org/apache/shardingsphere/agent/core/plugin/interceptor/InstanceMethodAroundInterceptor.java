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

package org.apache.shardingsphere.agent.core.plugin.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.implementation.bind.annotation.This;
import org.apache.shardingsphere.agent.api.advice.InstanceMethodAroundAdvice;
import org.apache.shardingsphere.agent.api.advice.AdviceTargetObject;
import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * Proxy class for ByteBuddy to intercept methods of target and weave pre- and post-method around the target method.
 */
@RequiredArgsConstructor
@Slf4j
public class InstanceMethodAroundInterceptor {
    
    private final InstanceMethodAroundAdvice instanceMethodAroundAdvice;
    
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
    public Object intercept(@This final Object target, @Origin final Method method, @AllArguments final Object[] args, @SuperCall final Callable<?> callable) {
        AdviceTargetObject instance = (AdviceTargetObject) target;
        MethodInvocationResult methodResult = new MethodInvocationResult();
        Object result;
        try {
            instanceMethodAroundAdvice.beforeMethod(instance, method, args, methodResult);
            // CHECKSTYLE:OFF
        } catch (final Throwable ex) {
            // CHECKSTYLE:ON
            log.error("Failed to execute the pre-method of method[{}] in class[{}].", method.getName(), target.getClass(), ex);
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
                instanceMethodAroundAdvice.onThrowing(instance, method, args, ex);
                // CHECKSTYLE:OFF
            } catch (final Throwable ignored) {
                // CHECKSTYLE:ON
                log.error("Failed to execute the error handler of method[{}] in class[{}].", method.getName(), target.getClass(), ex);
            }
            throw ex;
        } finally {
            try {
                instanceMethodAroundAdvice.afterMethod(instance, method, args, methodResult);
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                log.error("Failed to execute the post-method of method[{}] in class[{}].", method.getName(), target.getClass(), ex);
            }
        }
        return result;
    }
}
