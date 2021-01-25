/*
 * Licensed to the Apache Software Foundation (final ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, final Version 2.0
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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.agent.api.advice.InstanceMethodAroundAdvice;
import org.apache.shardingsphere.agent.api.advice.AdviceTargetObject;
import org.apache.shardingsphere.agent.api.result.MethodInvocationResult;

import java.lang.reflect.Method;
import java.util.List;

@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public final class MockInstanceMethodAroundAdvice implements InstanceMethodAroundAdvice {
    
    private final boolean rebase;
    
    public MockInstanceMethodAroundAdvice() {
        this(false);
    }
    
    @Override
    public void beforeMethod(final AdviceTargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        List<String> queues = (List<String>) args[0];
        queues.add("before");
        if (rebase) {
            result.rebase("rebase invocation method");
        }
    }
    
    @Override
    public void afterMethod(final AdviceTargetObject target, final Method method, final Object[] args, final MethodInvocationResult result) {
        List<String> queues = (List<String>) args[0];
        queues.add("after");
    }
    
    @Override
    public void onThrowing(final AdviceTargetObject target, final Method method, final Object[] args, final Throwable throwable) {
        List<String> queues = (List<String>) args[0];
        queues.add("exception");
    }
}
