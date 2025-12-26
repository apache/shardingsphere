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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.ingest.dumper.inventory.query.Range;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Inventory data sparseness calculator.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class InventoryDataSparsenessCalculator {
    
    private static final long EXACT_SPLITTING_RECORDS_COUNT_THRESHOLD = 1000000L;
    
    private static final BigDecimal MULTIPLE_THRESHOLD = new BigDecimal("1.50");
    
    /**
     * Is integer unique key data sparse.
     *
     * @param tableRecordsCount table records count
     * @param uniqueKeyValuesRange unique key values range
     * @return true if sparse
     */
    public static boolean isIntegerUniqueKeyDataSparse(final long tableRecordsCount, final Range<Long> uniqueKeyValuesRange) {
        boolean result = false;
        Long lowerValue = uniqueKeyValuesRange.getLowerBound();
        Long upperValue = uniqueKeyValuesRange.getUpperBound();
        if (tableRecordsCount >= EXACT_SPLITTING_RECORDS_COUNT_THRESHOLD && null != lowerValue && null != upperValue) {
            BigDecimal multiple = BigDecimal.valueOf(upperValue).subtract(BigDecimal.valueOf(lowerValue)).add(BigDecimal.ONE)
                    .divide(BigDecimal.valueOf(tableRecordsCount), 2, RoundingMode.HALF_UP);
            if (multiple.compareTo(MULTIPLE_THRESHOLD) >= 0) {
                log.info("Table is sparse for integer unique key, table records count: {}, unique key values range: {}, multiple: {}", tableRecordsCount, uniqueKeyValuesRange, multiple);
                result = true;
            }
        }
        return result;
    }
}
