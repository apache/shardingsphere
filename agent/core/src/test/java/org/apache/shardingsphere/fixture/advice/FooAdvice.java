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

package org.apache.shardingsphere.fixture.advice;

import org.apache.shardingsphere.agent.api.advice.TargetAdviceObject;
import org.apache.shardingsphere.agent.api.advice.type.ConstructorAdvice;
import org.apache.shardingsphere.agent.api.advice.type.InstanceMethodAdvice;
import org.apache.shardingsphere.agent.api.advice.type.StaticMethodAdvice;

import java.lang.reflect.Method;
import java.util.List;

@SuppressWarnings("unchecked")
public final class FooAdvice implements ConstructorAdvice, InstanceMethodAdvice, StaticMethodAdvice {
    
    @Override
    public void onConstructor(final TargetAdviceObject target, final Object[] args, final String pluginType) {
        ((List<String>) args[0]).add("foo constructor");
    }
    
    @Override
    public void beforeMethod(final TargetAdviceObject target, final Method method, final Object[] args, final String pluginType) {
        List<String> queue = (List<String>) args[0];
        queue.add("foo before instance method");
    }
    
    @Override
    public void beforeMethod(final Class<?> clazz, final Method method, final Object[] args, final String pluginType) {
        List<String> queue = (List<String>) args[0];
        queue.add("foo before static method");
    }
    
    @Override
    public void afterMethod(final TargetAdviceObject target, final Method method, final Object[] args, final Object result, final String pluginType) {
        List<String> queue = (List<String>) args[0];
        queue.add("foo after instance method");
    }
    
    @Override
    public void afterMethod(final Class<?> clazz, final Method method, final Object[] args, final Object result, final String pluginType) {
        List<String> queue = (List<String>) args[0];
        queue.add("foo after static method");
    }
    
    @Override
    public void onThrowing(final TargetAdviceObject target, final Method method, final Object[] args, final Throwable throwable, final String pluginType) {
        List<String> queue = (List<String>) args[0];
        queue.add("foo throw instance method exception");
    }
    
    @Override
    public void onThrowing(final Class<?> clazz, final Method method, final Object[] args, final Throwable throwable, final String pluginType) {
        List<String> queue = (List<String>) args[0];
        queue.add("foo throw static method exception");
    }
}
