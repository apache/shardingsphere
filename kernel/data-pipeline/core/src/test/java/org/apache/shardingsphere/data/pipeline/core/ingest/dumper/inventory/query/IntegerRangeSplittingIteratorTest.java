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

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IntegerRangeSplittingIteratorTest {
    
    @Test
    void assertLowerGtUpper() {
        assertThrows(IllegalArgumentException.class, () -> new IntegerRangeSplittingIterator(BigInteger.valueOf(200L), BigInteger.valueOf(100L), BigInteger.valueOf(10L)));
    }
    
    @Test
    void assertStepSizeLtZero() {
        assertThrows(IllegalArgumentException.class, () -> new IntegerRangeSplittingIterator(BigInteger.valueOf(100L), BigInteger.valueOf(200L), BigInteger.valueOf(-10L)));
    }
    
    @Test
    void assertInvalidNext() {
        IntegerRangeSplittingIterator iterator = new IntegerRangeSplittingIterator(BigInteger.valueOf(200L), BigInteger.valueOf(200L), BigInteger.valueOf(0L));
        if (iterator.hasNext()) {
            iterator.next();
        }
        assertThrows(NoSuchElementException.class, iterator::next);
    }
    
    @Test
    void assertStepSizeEqZero() {
        IntegerRangeSplittingIterator iterator = new IntegerRangeSplittingIterator(BigInteger.valueOf(200L), BigInteger.valueOf(200L), BigInteger.valueOf(0L));
        List<Range<BigInteger>> actual = new LinkedList<>();
        while (iterator.hasNext()) {
            actual.add(iterator.next());
        }
        assertThat(actual.size(), is(1));
        assertRange(actual.get(0), Range.closed(BigInteger.valueOf(200L), BigInteger.valueOf(200L)));
    }
    
    private void assertRange(final Range<BigInteger> actual, final Range<BigInteger> expected) {
        assertThat(actual.getLowerBound(), is(expected.getLowerBound()));
        assertThat(actual.getUpperBound(), is(expected.getUpperBound()));
    }
    
    @Test
    void assertIntegerRange() {
        IntegerRangeSplittingIterator iterator = new IntegerRangeSplittingIterator(BigInteger.valueOf(200L), BigInteger.valueOf(400L), BigInteger.valueOf(100L));
        List<Range<BigInteger>> actual = new LinkedList<>();
        while (iterator.hasNext()) {
            actual.add(iterator.next());
        }
        assertThat(actual.size(), is(2));
        assertRange(actual.get(0), Range.closed(BigInteger.valueOf(200L), BigInteger.valueOf(300L)));
        assertRange(actual.get(1), Range.closed(BigInteger.valueOf(301L), BigInteger.valueOf(400L)));
    }
    
    @Test
    void assertBigIntegerRange() {
        IntegerRangeSplittingIterator iterator = new IntegerRangeSplittingIterator(
                new BigInteger("1234567890123456789012345678"), new BigInteger("1234567890123456789032345678"), new BigInteger("10000000"));
        List<Range<BigInteger>> actual = new LinkedList<>();
        while (iterator.hasNext()) {
            actual.add(iterator.next());
        }
        assertThat(actual.size(), is(2));
        assertRange(actual.get(0), Range.closed(new BigInteger("1234567890123456789012345678"), new BigInteger("1234567890123456789022345678")));
        assertRange(actual.get(1), Range.closed(new BigInteger("1234567890123456789022345679"), new BigInteger("1234567890123456789032345678")));
    }
}
