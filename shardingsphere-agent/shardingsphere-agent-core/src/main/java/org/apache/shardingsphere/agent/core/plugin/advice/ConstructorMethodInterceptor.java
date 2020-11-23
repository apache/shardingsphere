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

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

/**
 * A proxy class for ByteBuddy to intercept methods of target and weave post-method after constructor.
 */
@Slf4j
public class ConstructorMethodInterceptor {
    private final ConstructorAdvice advice;
    
    public ConstructorMethodInterceptor(final ConstructorAdvice advice) {
        this.advice = advice;
    }

    /**
     * Intercept constructor.
     * @param target    the target object
     * @param args      the all constructor arguments
     */
    @RuntimeType
    public void intercept(final @This TargetObject target, final @AllArguments Object[] args) {
        try {
            advice.onConstructor(target, args);
            // CHECKSTYLE:OFF
        } catch (Throwable throwable) {
            // CHECKSTYLE:ON
            log.error("Constructor advice execution error. class: {}", target.getClass().getTypeName(), throwable);
        }
    }
    
}
