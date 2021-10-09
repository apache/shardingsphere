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

package org.apache.shardingsphere.driver.jdbc.adapter.invocation;

import lombok.SneakyThrows;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Method invocation recorder.
 */
public final class MethodInvocationRecorder {
    
    private final Collection<JdbcMethodInvocation> jdbcMethodInvocations = new LinkedList<>();
    
    /**
     * record method invocation.
     *
     * @param targetClass target class
     * @param methodName method name
     * @param argumentTypes argument types
     * @param arguments arguments
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public void recordMethodInvocation(final Class<?> targetClass, final String methodName, final Class<?>[] argumentTypes, final Object[] arguments) {
        jdbcMethodInvocations.add(new JdbcMethodInvocation(targetClass.getMethod(methodName, argumentTypes), arguments));
    }
    
    /**
     * Replay methods invocation.
     *
     * @param target target object
     */
    public void replayMethodsInvocation(final Object target) {
        jdbcMethodInvocations.forEach(each -> each.invoke(target));
    }
}
