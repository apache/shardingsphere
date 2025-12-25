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
    void assertGetIntegerPositions() {
        List<IngestPosition> actualPositions = InventoryPositionEstimatedCalculator.getIntegerPositions(200L, Range.of(1L, 600L), 100L);
        assertThat(actualPositions.size(), is(2));
        assertPosition(actualPositions.get(0), new IntegerPrimaryKeyIngestPosition(1L, 300L));
        assertPosition(actualPositions.get(1), new IntegerPrimaryKeyIngestPosition(301L, 600L));
    }
    
    private void assertPosition(final IngestPosition actual, final IntegerPrimaryKeyIngestPosition expected) {
        assertThat(actual, isA(IntegerPrimaryKeyIngestPosition.class));
        assertThat(((IntegerPrimaryKeyIngestPosition) actual).getBeginValue(), is(expected.getBeginValue()));
        assertThat(((IntegerPrimaryKeyIngestPosition) actual).getEndValue(), is(expected.getEndValue()));
    }
    
    @Test
    void assertGetIntegerPositionsWithZeroTotalRecordsCount() {
        List<IngestPosition> actualPositions = InventoryPositionEstimatedCalculator.getIntegerPositions(0L, Range.of(0L, 0L), 1L);
        assertThat(actualPositions.size(), is(1));
        assertPosition(actualPositions.get(0), new IntegerPrimaryKeyIngestPosition(0L, 0L));
    }
    
    @Test
    void assertGetIntegerPositionsWithTheSameMinMax() {
        List<IngestPosition> actualPositions = InventoryPositionEstimatedCalculator.getIntegerPositions(200L, Range.of(5L, 5L), 100L);
        assertThat(actualPositions.size(), is(1));
        assertPosition(actualPositions.get(0), new IntegerPrimaryKeyIngestPosition(5L, 5L));
    }
    
    @Test
    void assertGetIntegerPositionsOverflow() {
        long tableRecordsCount = Long.MAX_VALUE - 1L;
        long shardingSize = tableRecordsCount / 2L;
        long minimum = Long.MIN_VALUE + 1L;
        long maximum = Long.MAX_VALUE;
        List<IngestPosition> actualPositions = InventoryPositionEstimatedCalculator.getIntegerPositions(tableRecordsCount, Range.of(minimum, maximum), shardingSize);
        assertThat(actualPositions.size(), is(2));
        assertPosition(actualPositions.get(0), new IntegerPrimaryKeyIngestPosition(minimum, 0L));
        assertPosition(actualPositions.get(1), new IntegerPrimaryKeyIngestPosition(1L, maximum));
    }
}
