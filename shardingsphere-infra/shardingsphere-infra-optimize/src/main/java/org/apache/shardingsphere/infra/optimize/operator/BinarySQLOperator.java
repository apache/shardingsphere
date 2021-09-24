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

package org.apache.shardingsphere.infra.optimize.operator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.calcite.sql.SqlBinaryOperator;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;

/**
 * Binary SQL operator.
 */
@RequiredArgsConstructor
public enum BinarySQLOperator {
    
    EQUALS("=", SqlStdOperatorTable.EQUALS),
    
    GREATER_THAN(">", SqlStdOperatorTable.GREATER_THAN),
    
    GREATER_EQUALS_THAN(">=", SqlStdOperatorTable.GREATER_THAN_OR_EQUAL),
    
    LESS_THAN("<", SqlStdOperatorTable.LESS_THAN),
    
    LESS_EQUALS_THAN("<=", SqlStdOperatorTable.LESS_THAN_OR_EQUAL),
    
    AND("AND", SqlStdOperatorTable.AND),
    
    NONE("", null);
    
    private final String operator;
    
    @Getter
    private final SqlBinaryOperator sqlBinaryOperator;
    
    /**
     * Convert string to binary SQL operator.
     * 
     * @param sqlOperator SQL operator type
     * @return converted binary SQL operator
     */
    public static BinarySQLOperator value(final String sqlOperator) {
        for (BinarySQLOperator each : values()) {
            if (each.operator.equalsIgnoreCase(sqlOperator)) {
                return each;
            }
        }
        throw new UnsupportedOperationException("unsupported sql operator: " + sqlOperator);
    }
}
