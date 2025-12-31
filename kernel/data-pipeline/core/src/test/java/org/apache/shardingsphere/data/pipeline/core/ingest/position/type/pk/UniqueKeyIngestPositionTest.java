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

package org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk;

import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.Range;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UniqueKeyIngestPositionTest {
    
    @Test
    void assertEncodeInteger() {
        assertThat(createIntegerPosition(1L, 100L).encode(), is("i,1,100"));
    }
    
    private UniqueKeyIngestPosition<BigInteger> createIntegerPosition(final Long lowerBound, final Long upperBound) {
        return UniqueKeyIngestPosition.ofInteger(Range.closed(null == lowerBound ? null : BigInteger.valueOf(lowerBound), null == upperBound ? null : BigInteger.valueOf(upperBound)));
    }
    
    @Test
    void assertEncodeIntegerWithNullValue() {
        assertThat(createIntegerPosition(1L, null).encode(), is("i,1,"));
        assertThat(createIntegerPosition(null, 100L).encode(), is("i,,100"));
        assertThat(createIntegerPosition(null, null).encode(), is("i,,"));
    }
    
    @Test
    void assertEncodeBigInteger() {
        assertThat(UniqueKeyIngestPosition.ofInteger(Range.closed(new BigInteger("12345678901234567890"), new BigInteger("12345678901234567891"))).encode(),
                is("i,12345678901234567890,12345678901234567891"));
    }
    
    @Test
    void assertEncodeString() {
        assertThat(UniqueKeyIngestPosition.ofString(Range.closed("hi", "jk")).encode(), is("s,hi,jk"));
    }
    
    @Test
    void assertEncodeStringWithNullValue() {
        assertThat(UniqueKeyIngestPosition.ofString(Range.closed(null, null)).encode(), is("s,,"));
    }
    
    @Test
    void assertEncodeUnsplit() {
        assertThat(UniqueKeyIngestPosition.ofUnsplit().encode(), is("u,,"));
    }
    
    @Test
    void assertDecodeIntegerPosition() {
        assertIntegerPosition0(UniqueKeyIngestPosition.decode("i,100,200"), createIntegerPosition(100L, 200L));
        assertIntegerPosition0(UniqueKeyIngestPosition.decode("i,100,"), createIntegerPosition(100L, null));
        assertIntegerPosition0(UniqueKeyIngestPosition.decode("i,,200"), createIntegerPosition(null, 200L));
        assertIntegerPosition0(UniqueKeyIngestPosition.decode("i,,"), createIntegerPosition(null, null));
    }
    
    private void assertIntegerPosition0(final UniqueKeyIngestPosition<?> actual, final UniqueKeyIngestPosition<BigInteger> expected) {
        assertThat(actual, isA(UniqueKeyIngestPosition.class));
        assertThat(actual.getType(), is(expected.getType()));
        assertThat(actual.getLowerBound(), is(expected.getLowerBound()));
        assertThat(actual.getUpperBound(), is(expected.getUpperBound()));
    }
    
    @Test
    void assertDecodeStringPosition() {
        UniqueKeyIngestPosition<?> actual = UniqueKeyIngestPosition.decode("s,a,b");
        assertThat(actual, isA(UniqueKeyIngestPosition.class));
        assertThat(actual.getType(), is('s'));
        assertThat(actual.getLowerBound(), is("a"));
        assertThat(actual.getUpperBound(), is("b"));
    }
    
    @Test
    void assertDecodeUnsplitPosition() {
        UniqueKeyIngestPosition<?> actual = UniqueKeyIngestPosition.decode("u,a,b");
        assertThat(actual, isA(UniqueKeyIngestPosition.class));
        assertThat(actual.getType(), is('u'));
        assertNull(actual.getLowerBound());
        assertNull(actual.getUpperBound());
    }
    
    @Test
    void assertDecodeIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> UniqueKeyIngestPosition.decode("z,100"));
        assertThrows(IllegalArgumentException.class, () -> UniqueKeyIngestPosition.decode("zz,100,200"));
        assertThrows(IllegalArgumentException.class, () -> UniqueKeyIngestPosition.decode("z,100,200"));
    }
    
    @Test
    void assertNewInstanceWithNumberRange() {
        UniqueKeyIngestPosition<?> actual = UniqueKeyIngestPosition.newInstance(Range.closed(BigInteger.valueOf(100L), BigInteger.valueOf(200L)));
        assertThat(actual.getType(), is('i'));
        assertThat(actual.getLowerBound(), is(BigInteger.valueOf(100L)));
        assertThat(actual.getUpperBound(), is(BigInteger.valueOf(200L)));
    }
    
    @Test
    void assertNewInstanceWithNumberNullEndRange() {
        UniqueKeyIngestPosition<?> actual = UniqueKeyIngestPosition.newInstance(Range.closed(BigInteger.valueOf(100L), null));
        assertThat(actual.getType(), is('i'));
        assertThat(actual.getLowerBound(), is(BigInteger.valueOf(100L)));
        assertNull(actual.getUpperBound());
    }
    
    @Test
    void assertNewInstanceWithStringRange() {
        UniqueKeyIngestPosition<?> actual = UniqueKeyIngestPosition.newInstance(Range.closed("a", "b"));
        assertThat(actual.getType(), is('s'));
        assertThat(actual.getLowerBound(), is("a"));
        assertThat(actual.getUpperBound(), is("b"));
    }
    
    @Test
    void assertNewInstanceWithStringNullEndRange() {
        UniqueKeyIngestPosition<?> actual = UniqueKeyIngestPosition.newInstance(Range.closed("a", null));
        assertThat(actual.getType(), is('s'));
        assertThat(actual.getLowerBound(), is("a"));
        assertNull(actual.getUpperBound());
    }
    
    @Test
    void assertNewInstanceWithUnsplitRange() {
        UniqueKeyIngestPosition<?> actual = UniqueKeyIngestPosition.newInstance(Range.closed(null, null));
        assertThat(actual.getType(), is('u'));
        assertNull(actual.getLowerBound());
        assertNull(actual.getUpperBound());
    }
}
