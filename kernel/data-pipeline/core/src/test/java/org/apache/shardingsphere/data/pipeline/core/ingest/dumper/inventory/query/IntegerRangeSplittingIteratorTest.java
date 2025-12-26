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

import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IntegerRangeSplittingIteratorTest {
    
    @Test
    void assertMinimumGreaterThanMaximum() {
        assertThrows(IllegalArgumentException.class, () -> new IntegerRangeSplittingIterator(200L, 100L, 10L));
    }
    
    @Test
    void assertIntervalLessThanZero() {
        assertThrows(IllegalArgumentException.class, () -> new IntegerRangeSplittingIterator(100L, 200L, -10L));
    }
    
    @Test
    void assertInvalidNext() {
        IntegerRangeSplittingIterator iterator = new IntegerRangeSplittingIterator(200L, 200L, 0L);
        if (iterator.hasNext()) {
            iterator.next();
        }
        assertThrows(NoSuchElementException.class, iterator::next);
    }
    
    @Test
    void assertSmallRangeCorrect() {
        IntegerRangeSplittingIterator iterator = new IntegerRangeSplittingIterator(200L, 200L, 0L);
        List<Range<Long>> actual = new LinkedList<>();
        while (iterator.hasNext()) {
            actual.add(iterator.next());
        }
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getLowerBound(), is(200L));
        assertThat(actual.get(0).getUpperBound(), is(200L));
    }
    
    @Test
    void assertLargeRangeCorrect() {
        IntegerRangeSplittingIterator iterator = new IntegerRangeSplittingIterator(200L, 400L, 100L);
        List<Range<Long>> actual = new LinkedList<>();
        while (iterator.hasNext()) {
            actual.add(iterator.next());
        }
        assertThat(actual.size(), is(2));
        assertThat(actual.get(0).getLowerBound(), is(200L));
        assertThat(actual.get(0).getUpperBound(), is(300L));
        assertThat(actual.get(1).getLowerBound(), is(301L));
        assertThat(actual.get(1).getUpperBound(), is(400L));
    }
}
