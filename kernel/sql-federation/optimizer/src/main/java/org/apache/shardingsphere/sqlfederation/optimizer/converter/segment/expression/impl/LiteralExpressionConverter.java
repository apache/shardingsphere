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

package org.apache.shardingsphere.sqlfederation.optimizer.converter.segment.expression.impl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.avatica.util.TimeUnit;
import org.apache.calcite.sql.SqlIntervalQualifier;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.fun.SqlTrimFunction.Flag;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

/**
 * Literal expression converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LiteralExpressionConverter {
    
    private static final Collection<String> TRIM_FUNCTION_FLAGS = new HashSet<>(3, 1F);
    
    private static final Collection<String> TIME_UNIT_NAMES = new HashSet<>(6, 1F);
    
    static {
        TRIM_FUNCTION_FLAGS.add("BOTH");
        TRIM_FUNCTION_FLAGS.add("LEADING");
        TRIM_FUNCTION_FLAGS.add("TRAILING");
        TIME_UNIT_NAMES.add("YEAR");
        TIME_UNIT_NAMES.add("MONTH");
        TIME_UNIT_NAMES.add("DAY");
        TIME_UNIT_NAMES.add("HOUR");
        TIME_UNIT_NAMES.add("MINUTE");
        TIME_UNIT_NAMES.add("SECOND");
    }
    
    /**
     * Convert literal expression segment to sql node.
     * 
     * @param segment literal expression segment
     * @return sql node
     */
    public static Optional<SqlNode> convert(final LiteralExpressionSegment segment) {
        if (null == segment.getLiterals()) {
            return Optional.of(SqlLiteral.createNull(SqlParserPos.ZERO));
        }
        String literalValue = String.valueOf(segment.getLiterals());
        if (TRIM_FUNCTION_FLAGS.contains(literalValue)) {
            return Optional.of(SqlLiteral.createSymbol(Flag.valueOf(literalValue), SqlParserPos.ZERO));
        }
        if (TIME_UNIT_NAMES.contains(literalValue)) {
            return Optional.of(new SqlIntervalQualifier(TimeUnit.valueOf(literalValue), null, SqlParserPos.ZERO));
        }
        if (segment.getLiterals() instanceof Number) {
            return Optional.of(SqlLiteral.createExactNumeric(literalValue, SqlParserPos.ZERO));
        }
        if (segment.getLiterals() instanceof String) {
            return Optional.of(SqlLiteral.createCharString(literalValue, SqlParserPos.ZERO));
        }
        return Optional.empty();
    }
}
