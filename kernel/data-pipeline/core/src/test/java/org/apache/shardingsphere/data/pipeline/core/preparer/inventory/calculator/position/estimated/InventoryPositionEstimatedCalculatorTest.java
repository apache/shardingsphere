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

import org.apache.commons.lang3.Range;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.type.IntegerPrimaryKeyIngestPosition;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

class InventoryPositionEstimatedCalculatorTest {
    
    @Test
    void assertGetPositionByIntegerUniqueKeyRange() {
        List<IngestPosition> actualPositions = InventoryPositionEstimatedCalculator.getPositionByIntegerUniqueKeyRange(200L, Range.of(1L, 600L), 100L);
        assertThat(actualPositions.size(), is(2));
        for (IngestPosition each : actualPositions) {
            assertThat(each, isA(IntegerPrimaryKeyIngestPosition.class));
        }
        assertPosition(new IntegerPrimaryKeyIngestPosition(1L, 300L), (IntegerPrimaryKeyIngestPosition) actualPositions.get(0));
        assertPosition(new IntegerPrimaryKeyIngestPosition(301L, 600L), (IntegerPrimaryKeyIngestPosition) actualPositions.get(1));
    }
    
    private void assertPosition(final IntegerPrimaryKeyIngestPosition expected, final IntegerPrimaryKeyIngestPosition actual) {
        assertThat(actual.getBeginValue(), is(expected.getBeginValue()));
        assertThat(actual.getEndValue(), is(expected.getEndValue()));
    }
    
    @Test
    void assertGetPositionByIntegerUniqueKeyRangeWithZeroTotalRecordsCount() {
        List<IngestPosition> actualPositions = InventoryPositionEstimatedCalculator.getPositionByIntegerUniqueKeyRange(0L, Range.of(0L, 0L), 1L);
        assertThat(actualPositions.size(), is(1));
        assertThat(actualPositions.get(0), isA(IntegerPrimaryKeyIngestPosition.class));
        assertPosition(new IntegerPrimaryKeyIngestPosition(0L, 0L), (IntegerPrimaryKeyIngestPosition) actualPositions.get(0));
    }
    
    @Test
    void assertGetPositionByIntegerUniqueKeyRangeWithTheSameMinMax() {
        List<IngestPosition> actualPositions = InventoryPositionEstimatedCalculator.getPositionByIntegerUniqueKeyRange(200L, Range.of(5L, 5L), 100L);
        assertThat(actualPositions.size(), is(1));
        assertThat(actualPositions.get(0), isA(IntegerPrimaryKeyIngestPosition.class));
        assertPosition(new IntegerPrimaryKeyIngestPosition(5L, 5L), (IntegerPrimaryKeyIngestPosition) actualPositions.get(0));
    }
    
    @Test
    void assertGetPositionByIntegerUniqueKeyRangeOverflow() {
        long tableRecordsCount = Long.MAX_VALUE - 1L;
        long shardingSize = tableRecordsCount / 2L;
        long minimum = Long.MIN_VALUE + 1L;
        long maximum = Long.MAX_VALUE;
        List<IngestPosition> actualPositions = InventoryPositionEstimatedCalculator.getPositionByIntegerUniqueKeyRange(tableRecordsCount, Range.of(minimum, maximum), shardingSize);
        assertThat(actualPositions.size(), is(2));
        for (IngestPosition each : actualPositions) {
            assertThat(each, isA(IntegerPrimaryKeyIngestPosition.class));
        }
        assertPosition(new IntegerPrimaryKeyIngestPosition(minimum, 0L), (IntegerPrimaryKeyIngestPosition) actualPositions.get(0));
        assertPosition(new IntegerPrimaryKeyIngestPosition(1L, maximum), (IntegerPrimaryKeyIngestPosition) actualPositions.get(1));
    }
}
