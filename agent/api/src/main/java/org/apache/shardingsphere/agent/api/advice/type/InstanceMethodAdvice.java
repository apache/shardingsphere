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

package org.apache.shardingsphere.agent.api.advice.type;

import org.apache.shardingsphere.agent.api.advice.TargetAdviceMethod;
import org.apache.shardingsphere.agent.api.advice.AgentAdvice;
import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;

/**
 * Instance method advice.
 */
public interface InstanceMethodAdvice extends AgentAdvice {
    
    /**
     * Intercept the target method and weave the method before origin method.
     * It will invoke before the origin calling.
     *
     * @param target the target object
     * @param method the target method
     * @param args all method arguments
     * @param pluginType plugin type
     */
    default void beforeMethod(final TargetAdviceObject target, final TargetAdviceMethod method, final Object[] args, final String pluginType) {
    }
    
    /**
     * Intercept the target method and weave the method after origin method.
     * It will invoke after the origin calling
     *
     * @param target the target object
     * @param method the target method
     * @param args all method arguments
     * @param result original call result
     * @param pluginType plugin type
     */
    default void afterMethod(final TargetAdviceObject target, final TargetAdviceMethod method, final Object[] args, final Object result, final String pluginType) {
    }
    
    /**
     * Weaving the method after origin method throwing.
     *
     * @param target the target object
     * @param method the target method
     * @param args all method arguments
     * @param throwable exception from target method
     * @param pluginType plugin type
     */
    default void onThrowing(final TargetAdviceObject target, final TargetAdviceMethod method, final Object[] args, final Throwable throwable, final String pluginType) {
    }
}
