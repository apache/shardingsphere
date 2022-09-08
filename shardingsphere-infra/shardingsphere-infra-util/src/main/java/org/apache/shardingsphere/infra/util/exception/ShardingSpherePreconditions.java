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

package org.apache.shardingsphere.infra.util.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.util.exception.external.ShardingSphereExternalException;
import org.apache.shardingsphere.infra.util.exception.internal.ShardingSphereInternalException;

import java.sql.SQLException;

/**
 * ShardingSphere preconditions.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSpherePreconditions {
    
    /**
     * Ensures the truth of an expression involving the state of the calling instance.
     * 
     * @param expectedExpression expected expression
     * @param exceptionIfUnexpected exception thrown if expression is unexpected
     */
    public static void checkState(final boolean expectedExpression, final ShardingSphereExternalException exceptionIfUnexpected) {
        if (!expectedExpression) {
            throw exceptionIfUnexpected;
        }
    }
    
    /**
     * Ensures the truth of an expression involving the state of the calling instance.
     *
     * @param expectedExpression expected expression
     * @param exceptionIfUnexpected exception thrown if expression is unexpected
     * @throws ShardingSphereInternalException ShardingSphere internal exception
     */
    public static void checkState(final boolean expectedExpression, final ShardingSphereInternalException exceptionIfUnexpected) throws ShardingSphereInternalException {
        if (!expectedExpression) {
            throw exceptionIfUnexpected;
        }
    }
    
    /**
     * Ensures the truth of an expression involving the state of the calling instance.
     *
     * @param expectedExpression expected expression
     * @param exceptionIfUnexpected exception thrown if expression is unexpected
     * @throws SQLException SQL exception
     */
    public static void checkState(final boolean expectedExpression, final SQLException exceptionIfUnexpected) throws SQLException {
        if (!expectedExpression) {
            throw exceptionIfUnexpected;
        }
    }
}
