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

package org.apache.shardingsphere.sql.parser.sql.util;

import com.google.common.collect.BoundType;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class SafeNumberOperationUtilsTest {
    
    @Test
    public void assertSafeIntersectionForInteger() {
        Range<Comparable<?>> range = Range.closed(10, 2000);
        Range<Comparable<?>> connectedRange = Range.closed(1500, 4000);
        Range<Comparable<?>> newRange = SafeNumberOperationUtils.safeIntersection(range, connectedRange);
        assertThat(newRange.lowerEndpoint(), is(1500));
        assertThat(newRange.lowerBoundType(), is(BoundType.CLOSED));
        assertThat(newRange.upperEndpoint(), is(2000));
        assertThat(newRange.upperBoundType(), is(BoundType.CLOSED));
    }
    
    @Test
    public void assertSafeIntersectionForLong() {
        Range<Comparable<?>> range = Range.upTo(3147483647L, BoundType.OPEN);
        Range<Comparable<?>> connectedRange = Range.downTo(3, BoundType.OPEN);
        Range<Comparable<?>> newRange = SafeNumberOperationUtils.safeIntersection(range, connectedRange);
        assertThat(newRange.lowerEndpoint(), is(3L));
        assertThat(newRange.lowerBoundType(), is(BoundType.OPEN));
        assertThat(newRange.upperEndpoint(), is(3147483647L));
        assertThat(newRange.upperBoundType(), is(BoundType.OPEN));
    }
    
    @Test
    public void assertSafeIntersectionForBigInteger() {
        Range<Comparable<?>> range = Range.upTo(new BigInteger("131323233123211"), BoundType.CLOSED);
        Range<Comparable<?>> connectedRange = Range.downTo(35, BoundType.OPEN);
        Range<Comparable<?>> newRange = SafeNumberOperationUtils.safeIntersection(range, connectedRange);
        assertThat(newRange.lowerEndpoint(), is(new BigInteger("35")));
        assertThat(newRange.lowerBoundType(), is(BoundType.OPEN));
        assertThat(newRange.upperEndpoint(), is(new BigInteger("131323233123211")));
        assertThat(newRange.upperBoundType(), is(BoundType.CLOSED));
    }
    
    @Test
    public void assertSafeIntersectionForFloat() {
        Range<Comparable<?>> range = Range.closed(5.5F, 13.8F);
        Range<Comparable<?>> connectedRange = Range.closed(7.14F, 11.3F);
        Range<Comparable<?>> newRange = SafeNumberOperationUtils.safeIntersection(range, connectedRange);
        assertThat(newRange.lowerEndpoint(), is(7.14F));
        assertThat(newRange.lowerBoundType(), is(BoundType.CLOSED));
        assertThat(newRange.upperEndpoint(), is(11.3F));
        assertThat(newRange.upperBoundType(), is(BoundType.CLOSED));
    }
    
    @Test
    public void assertSafeIntersectionForDouble() {
        Range<Comparable<?>> range = Range.closed(1242.114, 31474836.12);
        Range<Comparable<?>> connectedRange = Range.downTo(567.34F, BoundType.OPEN);
        Range<Comparable<?>> newRange = SafeNumberOperationUtils.safeIntersection(range, connectedRange);
        assertThat(newRange.lowerEndpoint(), is(1242.114));
        assertThat(newRange.lowerBoundType(), is(BoundType.CLOSED));
        assertThat(newRange.upperEndpoint(), is(31474836.12));
        assertThat(newRange.upperBoundType(), is(BoundType.CLOSED));
    }
    
    @Test
    public void assertSafeIntersectionForBigDecimal() {
        Range<Comparable<?>> range = Range.upTo(new BigDecimal("2331.23211"), BoundType.CLOSED);
        Range<Comparable<?>> connectedRange = Range.open(135.13F, 45343.23F);
        Range<Comparable<?>> newRange = SafeNumberOperationUtils.safeIntersection(range, connectedRange);
        assertThat(newRange.lowerEndpoint(), is(new BigDecimal("135.13")));
        assertThat(newRange.lowerBoundType(), is(BoundType.OPEN));
        assertThat(newRange.upperEndpoint(), is(new BigDecimal("2331.23211")));
        assertThat(newRange.upperBoundType(), is(BoundType.CLOSED));
    }
    
    @Test
    public void assertSafeClosedForInteger() {
        Range<Comparable<?>> range = SafeNumberOperationUtils.safeClosed(12, 500);
        assertThat(range.lowerEndpoint(), is(12));
        assertThat(range.upperEndpoint(), is(500));
    }
    
    @Test
    public void assertSafeClosedForLong() {
        Range<Comparable<?>> range = SafeNumberOperationUtils.safeClosed(12, 5001L);
        assertThat(range.lowerEndpoint(), is(12L));
        assertThat(range.upperEndpoint(), is(5001L));
    }
    
    @Test
    public void assertSafeClosedForBigInteger() {
        Range<Comparable<?>> range = SafeNumberOperationUtils.safeClosed(12L, new BigInteger("12344"));
        assertThat(range.lowerEndpoint(), is(new BigInteger("12")));
        assertThat(range.upperEndpoint(), is(new BigInteger("12344")));
    }
    
    @Test
    public void assertSafeClosedForFloat() {
        Range<Comparable<?>> range = SafeNumberOperationUtils.safeClosed(4.5F, 11.13F);
        assertThat(range.lowerEndpoint(), is(4.5F));
        assertThat(range.upperEndpoint(), is(11.13F));
    }
    
    @Test
    public void assertSafeClosedForDouble() {
        Range<Comparable<?>> range = SafeNumberOperationUtils.safeClosed(5.12F, 13.75);
        assertThat(range.lowerEndpoint(), is(5.12));
        assertThat(range.upperEndpoint(), is(13.75));
    }
    
    @Test
    public void assertSafeClosedForBigDecimal() {
        Range<Comparable<?>> range = SafeNumberOperationUtils.safeClosed(5.1F, new BigDecimal("17.666"));
        assertThat(range.lowerEndpoint(), is(new BigDecimal("5.1")));
        assertThat(range.upperEndpoint(), is(new BigDecimal("17.666")));
    }
    
    @Test
    public void assertSafeContainsForInteger() {
        Range<Comparable<?>> range = Range.closed(12, 100);
        assertFalse(SafeNumberOperationUtils.safeContains(range, 500));
    }
    
    @Test
    public void assertSafeContainsForLong() {
        Range<Comparable<?>> range = Range.closed(12L, 1000L);
        assertTrue(SafeNumberOperationUtils.safeContains(range, 500));
    }
    
    @Test
    public void assertSafeContainsForBigInteger() {
        Range<Comparable<?>> range = Range.closed(new BigInteger("123"), new BigInteger("1000"));
        assertTrue(SafeNumberOperationUtils.safeContains(range, 510));
    }
    
    @Test
    public void assertSafeContainsForFloat() {
        Range<Comparable<?>> range = Range.closed(123.11F, 9999.123F);
        assertTrue(SafeNumberOperationUtils.safeContains(range, 510.12));
    }
    
    @Test
    public void assertSafeContainsForDouble() {
        Range<Comparable<?>> range = Range.closed(11.11, 9999.99);
        assertTrue(SafeNumberOperationUtils.safeContains(range, new BigDecimal("510.12")));
    }
    
    @Test
    public void assertSafeContainsForBigDecimal() {
        Range<Comparable<?>> range = Range.closed(new BigDecimal("123.11"), new BigDecimal("9999.123"));
        assertTrue(SafeNumberOperationUtils.safeContains(range, 510.12));
    }

    @Test
    public void assertSafeEqualsForInteger() {
        List<Comparable<?>> sourceCollection = Lists.newArrayList(10, 12);
        List<Comparable<?>> targetCollection = Lists.newArrayList(10, 12);
        assertTrue(SafeNumberOperationUtils.safeEquals(sourceCollection, targetCollection));
    }

    @Test
    public void assertSafeEqualsForLong() {
        List<Comparable<?>> sourceCollection = Lists.newArrayList(10, 12);
        List<Comparable<?>> targetCollection = Lists.newArrayList(10L, 12L);
        assertTrue(SafeNumberOperationUtils.safeEquals(sourceCollection, targetCollection));
    }

    @Test
    public void assertSafeEqualsForBigInteger() {
        List<Comparable<?>> sourceCollection = Lists.newArrayList(10, 12);
        List<Comparable<?>> targetCollection = Lists.newArrayList(BigInteger.valueOf(10), BigInteger.valueOf(12L));
        assertTrue(SafeNumberOperationUtils.safeEquals(sourceCollection, targetCollection));
    }

    @Test
    public void assertSafeEqualsForFloat() {
        List<Comparable<?>> sourceCollection = Lists.newArrayList(10.01F, 12.01F);
        List<Comparable<?>> targetCollection = Lists.newArrayList(10.01F, 12.01F);
        assertTrue(SafeNumberOperationUtils.safeEquals(sourceCollection, targetCollection));
    }

    @Test
    public void assertSafeEqualsForDouble() {
        List<Comparable<?>> sourceCollection = Lists.newArrayList(10.01, 12.01);
        List<Comparable<?>> targetCollection = Lists.newArrayList(10.01F, 12.01);
        assertTrue(SafeNumberOperationUtils.safeEquals(sourceCollection, targetCollection));
    }

    @Test
    public void assertSafeEqualsForBigDecimal() {
        List<Comparable<?>> sourceCollection = Lists.newArrayList(10.01, 12.01);
        List<Comparable<?>> targetCollection = Lists.newArrayList(BigDecimal.valueOf(10.01), BigDecimal.valueOf(12.01));
        assertTrue(SafeNumberOperationUtils.safeEquals(sourceCollection, targetCollection));
    }
}
