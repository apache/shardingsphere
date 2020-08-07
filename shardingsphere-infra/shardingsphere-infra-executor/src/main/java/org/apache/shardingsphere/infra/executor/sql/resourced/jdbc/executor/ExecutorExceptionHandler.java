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

package org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.executor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;

import java.sql.SQLException;

/**
 * Executor exception handler.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class ExecutorExceptionHandler {
    
    private static final ThreadLocal<Boolean> IS_EXCEPTION_THROWN = ThreadLocal.withInitial(() -> true);
    
    /**
     * Set throw exception if error occur or not.
     *
     * @param isExceptionThrown throw exception if error occur or not
     */
    public static void setExceptionThrown(final boolean isExceptionThrown) {
        IS_EXCEPTION_THROWN.set(isExceptionThrown);
    }
    
    /**
     * Get throw exception if error occur or not.
     * 
     * @return throw exception if error occur or not
     */
    public static boolean isExceptionThrown() {
        return IS_EXCEPTION_THROWN.get();
    }
    
    /**
     * Handle exception. 
     * 
     * @param exception to be handled exception
     * @throws SQLException SQL exception
     */
    public static void handleException(final Exception exception) throws SQLException {
        if (isExceptionThrown()) {
            if (exception instanceof SQLException) {
                throw (SQLException) exception;
            }
            throw new ShardingSphereException(exception);
        }
        log.error("exception occur: ", exception);
    }
}
