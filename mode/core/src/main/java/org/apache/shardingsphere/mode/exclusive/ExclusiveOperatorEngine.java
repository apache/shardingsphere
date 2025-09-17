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

package org.apache.shardingsphere.mode.exclusive;

import lombok.RequiredArgsConstructor;

import java.sql.SQLException;

/**
 * Exclusive operator engine.
 */
@RequiredArgsConstructor
public final class ExclusiveOperatorEngine {
    
    private final ExclusiveOperatorContext exclusiveOperatorContext;
    
    /**
     * Operate with exclusive operation and void callback.
     *
     * @param operation exclusive operation
     * @param timeoutMillis timeout millis
     * @param voidCallback void callback
     * @throws SQLException SQL exception
     */
    public void operate(final ExclusiveOperation operation, final long timeoutMillis, final ExclusiveOperationVoidCallback voidCallback) throws SQLException {
        operateWithResult(operation, timeoutMillis, asVoidCallback(voidCallback));
    }
    
    private ExclusiveOperationCallback<Void> asVoidCallback(final ExclusiveOperationVoidCallback voidCallback) {
        return () -> {
            voidCallback.execute();
            return null;
        };
    }
    
    /**
     * Operate with exclusive operation and return result.
     *
     * @param operation exclusive operation
     * @param timeoutMillis timeout millis
     * @param callback callback
     * @param <T> type of return value
     * @return execution result
     * @throws SQLException SQL exception
     */
    public <T> T operateWithResult(final ExclusiveOperation operation, final long timeoutMillis, final ExclusiveOperationCallback<T> callback) throws SQLException {
        if (exclusiveOperatorContext.start(operation, timeoutMillis)) {
            try {
                return callback.execute();
            } finally {
                exclusiveOperatorContext.stop(operation);
            }
        }
        return null;
    }
}
