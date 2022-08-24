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

package org.apache.shardingsphere.data.pipeline.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Data consistency check utils.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DataConsistencyCheckUtils {
    
    /**
     * Check two BigDecimal whether equals or not.
     *
     * <p>Scale will be ignored, so <code>332.2</code> is equals to <code>332.20</code>.</p>
     *
     * @param one first BigDecimal
     * @param another second BigDecimal
     * @return equals or not
     */
    public static boolean isBigDecimalEquals(final BigDecimal one, final BigDecimal another) {
        BigDecimal decimalOne;
        BigDecimal decimalTwo;
        if (one.scale() != another.scale()) {
            if (one.scale() > another.scale()) {
                decimalOne = one;
                decimalTwo = another.setScale(one.scale(), RoundingMode.UNNECESSARY);
            } else {
                decimalOne = one.setScale(another.scale(), RoundingMode.UNNECESSARY);
                decimalTwo = another;
            }
        } else {
            decimalOne = one;
            decimalTwo = another;
        }
        return decimalOne.equals(decimalTwo);
    }
}
