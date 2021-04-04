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

package org.apache.shardingsphere.infra.optimizer.operator;

import org.apache.calcite.sql.SqlBinaryOperator;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;

/**
 * binary sql operator.
 */
public enum BinarySqlOperator {

    EQUALS("=", SqlStdOperatorTable.EQUALS),

    GREATER_THAN(">", SqlStdOperatorTable.GREATER_THAN),

    GREATER_EQUALS_THAN(">=", SqlStdOperatorTable.GREATER_THAN_OR_EQUAL),

    LESS_THAN("<", SqlStdOperatorTable.LESS_THAN),

    LESS_EQUALS_THAN("<=", SqlStdOperatorTable.LESS_THAN_OR_EQUAL),

    AND("AND", SqlStdOperatorTable.AND),

    NONE("", null);

    private final String operator;

    private final SqlBinaryOperator sqlBinaryOperator;

    BinarySqlOperator(final String operator, final SqlBinaryOperator sqlBinaryOperator) {
        this.operator = operator;
        this.sqlBinaryOperator = sqlBinaryOperator;
    }
    
    /**
     * Get binary operator. 
     * @return sql binary operator
     */
    public SqlBinaryOperator getSqlBinaryOperator() {
        return sqlBinaryOperator;
    }
    
    /**
     * convert string to BinarySqlOperator.
     * @param sqlOperator string type of sqlOperator
     * @return <code>BinarySqlOperator</code>
     */
    public static BinarySqlOperator value(final String sqlOperator) {
        for (BinarySqlOperator val : values()) {
            if (val.operator.equalsIgnoreCase(sqlOperator)) {
                return val;
            }
        }
        throw new UnsupportedOperationException("unsupported sql operator: " + sqlOperator);
    }
}
