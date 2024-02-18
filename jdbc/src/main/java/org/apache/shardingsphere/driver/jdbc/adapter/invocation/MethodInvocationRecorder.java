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

import org.apache.shardingsphere.driver.jdbc.adapter.executor.ForceExecuteCallback;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Method invocation recorder.
 *
 * @param <T> type of target
 */
public final class MethodInvocationRecorder<T> {
    
    private final Map<String, ForceExecuteCallback<T>> methodInvocations = new LinkedHashMap<>();
    
    /**
     * Record method invocation.
     *
     * @param methodName method name
     * @param callback callback
     */
    public void record(final String methodName, final ForceExecuteCallback<T> callback) {
        methodInvocations.put(methodName, callback);
    }
    
    /**
     * Replay methods invocation.
     *
     * @param target target object
     * @throws SQLException SQL Exception
     */
    public void replay(final T target) throws SQLException {
        for (ForceExecuteCallback<T> each : methodInvocations.values()) {
            each.execute(target);
        }
    }
}
