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

package org.apache.shardingsphere.data.pipeline.core.preparer.inventory.calculator;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Range;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.type.pk.type.IntegerPrimaryKeyIngestPosition;
import org.apache.shardingsphere.data.pipeline.core.util.IntervalToRangeIterator;

import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Inventory position calculator.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class InventoryPositionCalculator {
    
    /**
     * Get position by integer unique key range.
     *
     * @param tableRecordsCount table records count
     * @param uniqueKeyValuesRange unique key values range
     * @param shardingSize sharding size
     * @return position collection
     */
    public static List<IngestPosition> getPositionByIntegerUniqueKeyRange(final long tableRecordsCount, final Range<Long> uniqueKeyValuesRange, final long shardingSize) {
        if (0 == tableRecordsCount) {
            return Collections.singletonList(new IntegerPrimaryKeyIngestPosition(0L, 0L));
        }
        List<IngestPosition> result = new LinkedList<>();
        long splitCount = tableRecordsCount / shardingSize + (tableRecordsCount % shardingSize > 0 ? 1 : 0);
        long interval = BigInteger.valueOf(uniqueKeyValuesRange.getMaximum()).subtract(BigInteger.valueOf(uniqueKeyValuesRange.getMinimum())).divide(BigInteger.valueOf(splitCount)).longValue();
        IntervalToRangeIterator rangeIterator = new IntervalToRangeIterator(uniqueKeyValuesRange.getMinimum(), uniqueKeyValuesRange.getMaximum(), interval);
        while (rangeIterator.hasNext()) {
            Range<Long> range = rangeIterator.next();
            result.add(new IntegerPrimaryKeyIngestPosition(range.getMinimum(), range.getMaximum()));
        }
        return result;
    }
}
