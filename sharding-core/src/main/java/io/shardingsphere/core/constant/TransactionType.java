/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * TransactionType Enum.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
@Getter
public enum TransactionType {
    
    /**
     * default local transaction.
     */
    NONE(""),
    
    /**
     * XA distribute transaction provided by RDBMS vendor.
     */
    XA("XA"),
    
    /**
     * TCC (Try-Confirm-Cancel) distribute transaction mode.
     */
    TCC("TCC");
    
    private final String type;
    
    /**
     * Find enum by type value.
     *
     * @param type property type
     * @return value enum, return {@code NONE} if not found
     */
    public static TransactionType findByValue(final String type) {
        for (TransactionType each : TransactionType.values()) {
            if (each.getType().equals(type)) {
                return each;
            }
        }
        return NONE;
    }
}
