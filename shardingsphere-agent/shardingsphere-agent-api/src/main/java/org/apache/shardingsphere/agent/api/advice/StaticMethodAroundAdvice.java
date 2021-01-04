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

package org.apache.shardingsphere.agent.api.advice;

import java.lang.reflect.Method;
import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;

/**
 * Weaving the advice around the static methods of target class.
 */
public interface StaticMethodAroundAdvice {
    
    /**
     * Intercept the target method and weave the method before origin method. It will invoke before the origin calling.
     *
     * @param clazz the target class
     * @param method the target method
     * @param args all method arguments
     * @param result wrapped class of result to detect whether or not to execute the origin method
     */
    default void beforeMethod(Class<?> clazz, Method method, Object[] args, MethodInvocationResult result) {
    }
    
    /**
     * Intercept the target method and weave the method after origin method. It will invoke after the origin calling.
     *
     * @param clazz the target class
     * @param method the target method
     * @param args all method arguments
     * @param result wrapped class of result to detect whether or not to execute the origin method
     */
    default void afterMethod(Class<?> clazz, Method method, Object[] args, MethodInvocationResult result) {
    }
    
    /**
     * Weaving the method after origin method throwing.
     *
     * @param clazz the target class
     * @param method the target method
     * @param args all method arguments
     * @param throwable exception from target method
     */
    default void onThrowing(Class<?> clazz, Method method, Object[] args, Throwable throwable) {
    }
}
