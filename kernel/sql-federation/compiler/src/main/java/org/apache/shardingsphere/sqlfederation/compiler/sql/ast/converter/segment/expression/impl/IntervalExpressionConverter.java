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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.avatica.util.TimeUnit;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlIntervalQualifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.IntervalExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.IntervalUnit;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.operator.common.SQLExtensionOperatorTable;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.segment.expression.ExpressionConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Interval expression converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IntervalExpressionConverter {
    
    /**
     * Convert unary operation expression to SQL node.
     *
     * @param segment unary operation expression
     * @return SQL node
     */
    public static SqlBasicCall convert(final IntervalExpression segment) {
        TimeUnit timeUnit = getTimeUnit(segment.getIntervalUnit());
        List<SqlNode> sqlNodes = new ArrayList<>();
        ExpressionConverter.convert(segment.getValue()).ifPresent(sqlNodes::add);
        sqlNodes.add(new SqlIntervalQualifier(timeUnit, timeUnit, SqlParserPos.ZERO));
        return new SqlBasicCall(SQLExtensionOperatorTable.INTERVAL_OPERATOR, sqlNodes, SqlParserPos.ZERO);
    }
    
    private static TimeUnit getTimeUnit(final IntervalUnit unit) {
        switch (unit) {
            case MICROSECOND:
                return TimeUnit.MICROSECOND;
            case SECOND:
                return TimeUnit.SECOND;
            case MINUTE:
                return TimeUnit.MINUTE;
            case HOUR:
                return TimeUnit.HOUR;
            case DAY:
                return TimeUnit.DAY;
            case WEEK:
                return TimeUnit.WEEK;
            case MONTH:
                return TimeUnit.MONTH;
            case QUARTER:
                return TimeUnit.QUARTER;
            case YEAR:
                return TimeUnit.YEAR;
            default:
                throw new UnsupportedOperationException("Unsupported interval unit");
        }
    }
}
