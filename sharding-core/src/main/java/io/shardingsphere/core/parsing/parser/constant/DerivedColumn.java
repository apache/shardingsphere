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

package io.shardingsphere.core.parsing.parser.constant;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * Derived column alias.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum DerivedColumn {
    
    AVG_COUNT_ALIAS("AVG_DERIVED_COUNT_"), 
    AVG_SUM_ALIAS("AVG_DERIVED_SUM_"), 
    ORDER_BY_ALIAS("ORDER_BY_DERIVED_"), 
    GROUP_BY_ALIAS("GROUP_BY_DERIVED_");
    
    private final String pattern;
    
    /**
     * Get alias of derived column.
     *
     * @param derivedColumnCount derived column count
     * @return alias of derived column
     */
    public String getDerivedColumnAlias(final int derivedColumnCount) {
        return String.format(pattern + "%s", derivedColumnCount);
    }
    
    /**
     * Judge is derived column or not.
     * 
     * @param columnName column name to be judged
     * @return is derived column or not
     */
    public static boolean isDerivedColumn(final String columnName) {
        for (DerivedColumn each : DerivedColumn.values()) {
            if (columnName.startsWith(each.pattern)) {
                return true;
            }
        }
        return false;
    }
}
