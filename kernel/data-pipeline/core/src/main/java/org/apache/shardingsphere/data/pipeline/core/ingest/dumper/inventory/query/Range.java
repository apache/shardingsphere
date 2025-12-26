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

package org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Range.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class Range {
    
    private final Object lowerBound;
    
    private final boolean lowerInclusive;
    
    private final Object upperBound;
    
    /**
     * Create closed range.
     *
     * @param lowerBound lower bound
     * @param upperBound upper bound
     * @return closed range
     */
    public static Range closed(final Object lowerBound, final Object upperBound) {
        return new Range(lowerBound, true, upperBound);
    }
    
    /**
     * Create open-closed range.
     *
     * @param lowerBound lower bound
     * @param upperBound upper bound
     * @return open-closed range
     */
    public static Range openClosed(final Object lowerBound, final Object upperBound) {
        return new Range(lowerBound, false, upperBound);
    }
}
