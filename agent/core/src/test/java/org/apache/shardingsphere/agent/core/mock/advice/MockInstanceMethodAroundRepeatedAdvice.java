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

package org.apache.shardingsphere.agent.core.mock.advice;

import org.apache.shardingsphere.agent.core.plugin.advice.InstanceMethodAroundAdvice;
import org.apache.shardingsphere.agent.core.plugin.TargetAdviceObject;
import org.apache.shardingsphere.agent.core.plugin.MethodInvocationResult;

import java.lang.reflect.Method;
import java.util.List;

@SuppressWarnings("unchecked")
public final class MockInstanceMethodAroundRepeatedAdvice implements InstanceMethodAroundAdvice {
    
    @Override
    public void beforeMethod(final TargetAdviceObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        List<String> queues = (List<String>) args[0];
        queues.add("twice_before");
    }
    
    @Override
    public void afterMethod(final TargetAdviceObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        List<String> queues = (List<String>) args[0];
        queues.add("twice_after");
    }
    
    @Override
    public void onThrowing(final TargetAdviceObject target, final Method method, final Object[] args, final Throwable throwable) {
        List<String> queues = (List<String>) args[0];
        queues.add("twice_exception");
    }
}
