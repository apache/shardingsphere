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

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsistencyCheckSequenceTest {
    
    @Test
    void assertGetNextSequence() {
        int currentSequence = ConsistencyCheckSequence.MIN_SEQUENCE;
        assertThat(currentSequence = ConsistencyCheckSequence.getNextSequence(currentSequence), is(2));
        assertThat(currentSequence = ConsistencyCheckSequence.getNextSequence(currentSequence), is(3));
        assertThat(ConsistencyCheckSequence.getNextSequence(currentSequence), is(1));
    }
    
    @Test
    void assertGetPreviousSequence() {
        List<Integer> sequences = Arrays.asList(2, 3, 1);
        Optional<Integer> previousSequence = ConsistencyCheckSequence.getPreviousSequence(sequences, 3);
        assertTrue(previousSequence.isPresent());
        assertThat(previousSequence.get(), is(2));
        previousSequence = ConsistencyCheckSequence.getPreviousSequence(sequences, 2);
        assertTrue(previousSequence.isPresent());
        assertThat(previousSequence.get(), is(1));
        previousSequence = ConsistencyCheckSequence.getPreviousSequence(sequences, 1);
        assertTrue(previousSequence.isPresent());
        assertThat(previousSequence.get(), is(3));
        previousSequence = ConsistencyCheckSequence.getPreviousSequence(sequences, 4);
        assertFalse(previousSequence.isPresent());
    }
}
