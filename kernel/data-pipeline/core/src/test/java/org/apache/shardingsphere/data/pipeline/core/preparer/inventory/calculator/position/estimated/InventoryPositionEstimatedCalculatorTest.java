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

package org.apache.shardingsphere.data.pipeline.core.preparer.inventory.calculator.position.estimated;

import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.Range;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.UniqueKeyIngestPosition;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

class InventoryPositionEstimatedCalculatorTest {
    
    @Test
    void assertGetIntegerPositions() {
        List<IngestPosition> actualPositions = InventoryPositionEstimatedCalculator.getIntegerPositions(200L, Range.closed(BigInteger.ONE, BigInteger.valueOf(600L)), 100L);
        assertThat(actualPositions.size(), is(2));
        assertIntegerPosition(actualPositions.get(0), createIntegerPosition(1L, 300L));
        assertIntegerPosition(actualPositions.get(1), createIntegerPosition(301L, 600L));
    }
    
    private UniqueKeyIngestPosition<BigInteger> createIntegerPosition(final long lowerBound, final long upperBound) {
        return UniqueKeyIngestPosition.ofInteger(Range.closed(BigInteger.valueOf(lowerBound), BigInteger.valueOf(upperBound)));
    }
    
    private void assertIntegerPosition(final IngestPosition actual, final UniqueKeyIngestPosition<BigInteger> expected) {
        assertThat(actual, isA(UniqueKeyIngestPosition.class));
        assertThat(((UniqueKeyIngestPosition<?>) actual).getLowerBound(), is(expected.getLowerBound()));
        assertThat(((UniqueKeyIngestPosition<?>) actual).getUpperBound(), is(expected.getUpperBound()));
    }
    
    @Test
    void assertGetIntegerPositionsWithZeroTotalRecordsCount() {
        List<IngestPosition> actualPositions = InventoryPositionEstimatedCalculator.getIntegerPositions(0L, Range.closed(BigInteger.ZERO, BigInteger.ONE), 1L);
        assertThat(actualPositions.size(), is(1));
        assertIntegerPosition(actualPositions.get(0), UniqueKeyIngestPosition.ofInteger(Range.closed(null, null)));
    }
    
    @Test
    void assertGetIntegerPositionsWithNullValue() {
        List<IngestPosition> actualPositions = InventoryPositionEstimatedCalculator.getIntegerPositions(200L, Range.closed(null, null), 1L);
        assertThat(actualPositions.size(), is(1));
        assertIntegerPosition(actualPositions.get(0), UniqueKeyIngestPosition.ofInteger(Range.closed(null, null)));
    }
    
    @Test
    void assertGetIntegerPositionsWithTheSameMinMax() {
        List<IngestPosition> actualPositions = InventoryPositionEstimatedCalculator.getIntegerPositions(200L, Range.closed(BigInteger.valueOf(5L), BigInteger.valueOf(5L)), 100L);
        assertThat(actualPositions.size(), is(1));
        assertIntegerPosition(actualPositions.get(0), createIntegerPosition(5L, 5L));
    }
    
    @Test
    void assertGetIntegerPositionsOverflow() {
        long tableRecordsCount = Long.MAX_VALUE - 1L;
        long shardingSize = tableRecordsCount / 2L;
        BigInteger lowerBound = BigInteger.valueOf(Long.MIN_VALUE + 1L);
        BigInteger upperBound = BigInteger.valueOf(Long.MAX_VALUE);
        List<IngestPosition> actualPositions = InventoryPositionEstimatedCalculator.getIntegerPositions(tableRecordsCount, Range.closed(lowerBound, upperBound), shardingSize);
        assertThat(actualPositions.size(), is(2));
        assertIntegerPosition(actualPositions.get(0), createIntegerPosition(lowerBound.longValue(), 0L));
        assertIntegerPosition(actualPositions.get(1), createIntegerPosition(1L, upperBound.longValue()));
    }
}
