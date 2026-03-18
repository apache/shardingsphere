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

package org.apache.shardingsphere.infra.binder.context.segment.select.projection;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Derived column alias.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum DerivedColumn {
    
    AVG_COUNT_ALIAS("AVG_DERIVED_COUNT_"),
    AVG_SUM_ALIAS("AVG_DERIVED_SUM_"),
    ORDER_BY_ALIAS("ORDER_BY_DERIVED_"),
    GROUP_BY_ALIAS("GROUP_BY_DERIVED_"),
    AGGREGATION_DISTINCT_DERIVED("AGGREGATION_DISTINCT_DERIVED_");
    
    private static final DerivedColumn[] VALUES = values();
    
    private static final Collection<DerivedColumn> VALUES_WITHOUT_AGGREGATION_DISTINCT_DERIVED = getValues();
    
    private final String pattern;
    
    /**
     * Get alias of derived column.
     *
     * @param derivedColumnCount derived column count
     * @return alias of derived column
     */
    public String getDerivedColumnAlias(final int derivedColumnCount) {
        return pattern + derivedColumnCount;
    }
    
    /**
     * Judge is derived column name or not.
     *
     * @param columnName column name to be judged
     * @return is derived column name or not
     */
    public static boolean isDerivedColumnName(final String columnName) {
        for (DerivedColumn each : VALUES) {
            if (columnName.startsWith(each.pattern)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Judge is derived column or not.
     *
     * @param columnName column name to be judged
     * @return is derived column or not
     */
    public static boolean isDerivedColumn(final String columnName) {
        for (DerivedColumn each : VALUES_WITHOUT_AGGREGATION_DISTINCT_DERIVED) {
            if (columnName.startsWith(each.pattern)) {
                return true;
            }
        }
        return false;
    }
    
    private static Collection<DerivedColumn> getValues() {
        Collection<DerivedColumn> result = new ArrayList<>(Arrays.asList(values()));
        result.remove(AGGREGATION_DISTINCT_DERIVED);
        return result;
    }
}
