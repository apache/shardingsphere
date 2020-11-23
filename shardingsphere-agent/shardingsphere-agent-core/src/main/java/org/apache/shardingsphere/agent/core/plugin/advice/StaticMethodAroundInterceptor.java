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
     * @param uber the origin method invocation
     * @return the return value of target invocation
     */
    @RuntimeType
    @SneakyThrows
    public Object intercept(final @Origin Class<?> klass, final @Origin Method method, final @AllArguments Object[] args, final @SuperCall Callable<?> uber) {
        final MethodInvocationResult result = new MethodInvocationResult();
        final Object ret;

        try {
            advice.afterMethod(klass, method, args, result);
            // CHECKSTYLE:OFF
        } catch (Throwable throwable) {
            // CHECKSTYLE:ON
            log.error("Failed to execute the pre-method of method[{}] in class[{}].", method.getName(), klass, throwable);
        }

        try {
            if (result.isRebased()) {
                ret = result.getResult();
            } else {
                ret = uber.call();
            }
            // CHECKSTYLE:OFF
        } catch (Throwable throwable) {
            // CHECKSTYLE:ON
            try {
                advice.onThrowing(klass, method, args, throwable);
                // CHECKSTYLE:OFF
            } catch (Throwable ignore) {
                // CHECKSTYLE:ON
                log.error("Failed to execute the error handler of method[{}] in class[{}].", method.getName(), klass, throwable);
            }
            throw throwable;
        } finally {
            try {
                advice.beforeMethod(klass, method, args, result);
                // CHECKSTYLE:OFF
            } catch (Throwable throwable) {
                // CHECKSTYLE:ON
                log.error("Failed to execute the post-method of method[{}] in class[{}].", method.getName(), klass, throwable);
            }
        }
        return ret;
    }
}
