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

package org.apache.shardingsphere.infra.exception;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Map;
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
     * @param <T> type of exception
     * @param reference object reference to be checked
     * @param exceptionSupplierIfUnexpected exception from this supplier will be thrown if expression is unexpected
     * @throws T exception to be thrown
     */
    public static <T extends Throwable> void checkNotNull(final Object reference, final Supplier<T> exceptionSupplierIfUnexpected) throws T {
        if (null == reference) {
            throw exceptionSupplierIfUnexpected.get();
        }
    }
    
    /**
     * Ensures that a string passed as a parameter to the calling method is not empty.
     *
     * @param <T> type of exception
     * @param value string to be checked
     * @param exceptionSupplierIfUnexpected exception from this supplier will be thrown if expression is unexpected
     * @throws T exception to be thrown
     */
    public static <T extends Throwable> void checkNotEmpty(final String value, final Supplier<T> exceptionSupplierIfUnexpected) throws T {
        if (Strings.isNullOrEmpty(value)) {
            throw exceptionSupplierIfUnexpected.get();
        }
    }
    
    /**
     * Ensures that a collection passed as a parameter to the calling method is not empty.
     *
     * @param <T> type of exception
     * @param values collection to be checked
     * @param exceptionSupplierIfUnexpected exception from this supplier will be thrown if expression is unexpected
     * @throws T exception to be thrown
     */
    public static <T extends Throwable> void checkNotEmpty(final Collection<?> values, final Supplier<T> exceptionSupplierIfUnexpected) throws T {
        if (values.isEmpty()) {
            throw exceptionSupplierIfUnexpected.get();
        }
    }
    
    /**
     * Ensures that a collection passed as a parameter to the calling method is not empty.
     *
     * @param <T> type of exception
     * @param map map to be checked
     * @param exceptionSupplierIfUnexpected exception from this supplier will be thrown if expression is unexpected
     * @throws T exception to be thrown
     */
    public static <T extends Throwable> void checkNotEmpty(final Map<?, ?> map, final Supplier<T> exceptionSupplierIfUnexpected) throws T {
        if (map.isEmpty()) {
            throw exceptionSupplierIfUnexpected.get();
        }
    }
    
    /**
     * Ensures that a collection passed as a parameter to the calling method must empty.
     *
     * @param <T> type of exception
     * @param values collection to be checked
     * @param exceptionSupplierIfUnexpected exception from this supplier will be thrown if expression is unexpected
     * @throws T exception to be thrown
     */
    public static <T extends Throwable> void checkMustEmpty(final Collection<?> values, final Supplier<T> exceptionSupplierIfUnexpected) throws T {
        if (!values.isEmpty()) {
            throw exceptionSupplierIfUnexpected.get();
        }
    }
    
    /**
     * Ensures that a map passed as a parameter to the calling method must empty.
     *
     * @param <T> type of exception
     * @param map map to be checked
     * @param exceptionSupplierIfUnexpected exception from this supplier will be thrown if expression is unexpected
     * @throws T exception to be thrown
     */
    public static <T extends Throwable> void checkMustEmpty(final Map<?, ?> map, final Supplier<T> exceptionSupplierIfUnexpected) throws T {
        if (!map.isEmpty()) {
            throw exceptionSupplierIfUnexpected.get();
        }
    }
    
    /**
     * Ensures that a collection passed as a parameter to the calling method must contain element.
     *
     * @param <T> type of exception
     * @param values values to be checked
     * @param element element to be checked
     * @param exceptionSupplierIfUnexpected exception from this supplier will be thrown if expression is unexpected
     * @throws T exception to be thrown
     */
    public static <T extends Throwable> void checkContains(final Collection<?> values, final Object element, final Supplier<T> exceptionSupplierIfUnexpected) throws T {
        if (!values.contains(element)) {
            throw exceptionSupplierIfUnexpected.get();
        }
    }
    
    /**
     * Ensures that a collection passed as a parameter to the calling method must not contain element.
     *
     * @param <T> type of exception
     * @param values values to be checked
     * @param element element to be checked
     * @param exceptionSupplierIfUnexpected exception from this supplier will be thrown if expression is unexpected
     * @throws T exception to be thrown
     */
    public static <T extends Throwable> void checkNotContains(final Collection<?> values, final Object element, final Supplier<T> exceptionSupplierIfUnexpected) throws T {
        if (values.contains(element)) {
            throw exceptionSupplierIfUnexpected.get();
        }
    }
    
    /**
     * Ensures that a map passed as a parameter to the calling method must contain key.
     *
     * @param <T> type of exception
     * @param map map to be checked
     * @param key key to be checked
     * @param exceptionSupplierIfUnexpected exception from this supplier will be thrown if expression is unexpected
     * @throws T exception to be thrown
     */
    public static <T extends Throwable> void checkContainsKey(final Map<?, ?> map, final Object key, final Supplier<T> exceptionSupplierIfUnexpected) throws T {
        if (!map.containsKey(key)) {
            throw exceptionSupplierIfUnexpected.get();
        }
    }
}
