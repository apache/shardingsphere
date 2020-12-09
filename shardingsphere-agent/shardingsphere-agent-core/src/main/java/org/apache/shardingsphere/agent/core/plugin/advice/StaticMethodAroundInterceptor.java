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

package org.apache.shardingsphere.agent.core.plugin.advice;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * Proxy class for ByteBuddy to intercept methods of target and weave pre- and post-method around the target method.
 */
@Slf4j
public class StaticMethodAroundInterceptor {
    
    private final StaticMethodAroundAdvice advice;
    
    public StaticMethodAroundInterceptor(final StaticMethodAroundAdvice advice) {
        this.advice = advice;
    }
    
    /**
     * Only intercept static method.
     *
     * @param klass the class of target
     * @param method the intercepted method
     * @param args the all arguments of method
     * @param callable the origin method invocation
     * @return the return value of target invocation
     */
    @RuntimeType
    @SneakyThrows
    public Object intercept(final @Origin Class<?> klass, final @Origin Method method, final @AllArguments Object[] args, final @SuperCall Callable<?> callable) {
        final MethodInvocationResult invocationResult = new MethodInvocationResult();
        final Object result;
        try {
            advice.beforeMethod(klass, method, args, invocationResult);
            // CHECKSTYLE:OFF
        } catch (Throwable ex) {
            // CHECKSTYLE:ON
            log.error("Failed to execute the pre-method of method[{}] in class[{}].", method.getName(), klass, ex);
        }
        try {
            if (invocationResult.isRebased()) {
                result = invocationResult.getResult();
            } else {
                result = callable.call();
            }
            invocationResult.rebase(result);
            // CHECKSTYLE:OFF
        } catch (final Throwable ex) {
            // CHECKSTYLE:ON
            try {
                advice.onThrowing(klass, method, args, ex);
                // CHECKSTYLE:OFF
            } catch (final Throwable ignored) {
                // CHECKSTYLE:ON
                log.error("Failed to execute the error handler of method[{}] in class[{}].", method.getName(), klass, ex);
            }
            throw ex;
        } finally {
            try {
                advice.afterMethod(klass, method, args, invocationResult);
                // CHECKSTYLE:OFF
            } catch (final Throwable ex) {
                // CHECKSTYLE:ON
                log.error("Failed to execute the post-method of method[{}] in class[{}].", method.getName(), klass, ex);
            }
        }
        return result;
    }
}
