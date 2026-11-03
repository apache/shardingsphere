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

package org.apache.shardingsphere.proxy.backend.connector.records;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Result set that captures execution record counts after the last row.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExecutionRecordCountsResultSet {
    
    /**
     * Wrap a result set and capture record counts when it is exhausted.
     *
     * @param resultSet result set to wrap
     * @param capture record counts capture
     * @return wrapped result set
     */
    public static ResultSet wrap(final ResultSet resultSet, final RecordCountsCapture capture) {
        boolean[] exhausted = {false};
        return (ResultSet) Proxy.newProxyInstance(ExecutionRecordCountsResultSet.class.getClassLoader(), new Class<?>[]{ResultSet.class}, (proxy, method, args) -> {
            Object result;
            try {
                result = method.invoke(resultSet, args);
            } catch (final InvocationTargetException ex) {
                throw ex.getCause();
            }
            if (!exhausted[0] && "next".equals(method.getName()) && Boolean.FALSE.equals(result)) {
                exhausted[0] = true;
                capture.capture();
            }
            return result;
        });
    }
    
    /**
     * Record counts capture of an exhausted result set.
     */
    @FunctionalInterface
    public interface RecordCountsCapture {
        
        /**
         * Capture record counts.
         *
         * @throws SQLException if the counts cannot be obtained
         */
        void capture() throws SQLException;
    }
}
