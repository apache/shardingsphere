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

package org.apache.shardingsphere.data.pipeline.scenario.consistencycheck.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Optional;

/**
 * Consistency check sequence.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(callSuper = true)
public final class ConsistencyCheckSequence {
    
    public static final int MIN_SEQUENCE = 1;
    
    public static final int MAX_SEQUENCE = 3;
    
    /**
     * Get next sequence.
     *
     * @param currentSequence current sequence
     * @return next sequence
     */
    public static int getNextSequence(final int currentSequence) {
        int nextSequence = currentSequence + 1;
        return nextSequence > MAX_SEQUENCE ? MIN_SEQUENCE : nextSequence;
    }
    
    /**
     * Get previous sequence.
     *
     * @param sequences sequence list
     * @param currentSequence current sequence
     * @return previous sequence
     */
    public static Optional<Integer> getPreviousSequence(final List<Integer> sequences, final int currentSequence) {
        if (sequences.size() <= 1) {
            return Optional.empty();
        }
        sequences.sort(Integer::compareTo);
        Integer index = null;
        for (int i = 0; i < sequences.size(); i++) {
            if (sequences.get(i) == currentSequence) {
                index = i;
                break;
            }
        }
        if (null == index) {
            return Optional.empty();
        }
        return Optional.of(index >= 1 ? sequences.get(index - 1) : MAX_SEQUENCE);
    }
}
