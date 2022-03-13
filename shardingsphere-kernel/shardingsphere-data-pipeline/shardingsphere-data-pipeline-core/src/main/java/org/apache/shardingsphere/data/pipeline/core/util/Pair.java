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

package org.apache.shardingsphere.data.pipeline.core.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Pair implementation.
 *
 * @param <L> left element type
 * @param <R> right element type
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class Pair<L, R> {
    
    private final L left;
    
    private final R right;
    
    /**
     * Create pair from <code>left</code> element and <code>right</code> element.
     *
     * @param left left element, could be null
     * @param right right element, could be null
     * @param <L> left element type
     * @param <R> right element type
     * @return pair
     */
    public static <L, R> Pair<L, R> of(final L left, final R right) {
        return new Pair<>(left, right);
    }
}
