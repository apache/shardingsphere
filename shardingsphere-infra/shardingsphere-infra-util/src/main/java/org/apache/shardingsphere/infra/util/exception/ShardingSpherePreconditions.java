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
import java.util.function.Supplier;

/**
 * ShardingSphere preconditions.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingSpherePreconditions {
    
    /**
     * Ensures the truth of an expression involving the state of the calling instance.
     *
     * @param <T> type of exception
     * @param expectedExpression expected expression
     * @param exceptionSupplierIfUnexpected exception from this supplier will be thrown if expression is unexpected
     * @throws T exception to be thrown
     */
    public static <T extends Throwable> void checkState(final boolean expectedExpression, final Supplier<T> exceptionSupplierIfUnexpected) throws T {
        if (!expectedExpression) {
            throw exceptionSupplierIfUnexpected.get();
        }
    }
    
    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param reference object reference to be checked
     * @param exceptionIfUnexpected exception thrown if object is null
     */
    public static void checkNotNull(final Object reference, final ShardingSphereExternalException exceptionIfUnexpected) {
        if (null == reference) {
            throw exceptionIfUnexpected;
        }
    }
    
    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param reference object reference to be checked
     * @param exceptionIfUnexpected exception thrown if object is null
     * @throws ShardingSphereInternalException ShardingSphere internal exception
     */
    public static void checkNotNull(final Object reference, final ShardingSphereInternalException exceptionIfUnexpected) throws ShardingSphereInternalException {
        if (null == reference) {
            throw exceptionIfUnexpected;
        }
    }
    
    /**
     * Ensures that an object reference passed as a parameter to the calling method is not null.
     *
     * @param reference object reference to be checked
     * @param exceptionIfUnexpected exception thrown if object is null
     * @throws SQLException SQL exception
     */
    public static void checkNotNull(final Object reference, final SQLException exceptionIfUnexpected) throws SQLException {
        if (null == reference) {
            throw exceptionIfUnexpected;
        }
    }
}
