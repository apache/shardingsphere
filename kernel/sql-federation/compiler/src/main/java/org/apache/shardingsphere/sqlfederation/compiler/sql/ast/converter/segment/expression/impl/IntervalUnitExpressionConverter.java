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

import com.cedarsoftware.util.CaseInsensitiveSet;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.calcite.avatica.util.TimeUnit;
import org.apache.calcite.sql.SqlIntervalQualifier;
import org.apache.calcite.sql.SqlLiteral;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.interval.IntervalUnitExpression;

import java.util.Collection;
import java.util.Optional;

/**
 * Interval unit expression converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IntervalUnitExpressionConverter {
    
    private static final Collection<String> TIME_UNIT_NAMES = new CaseInsensitiveSet<>(7, 1F);
    
    static {
        TIME_UNIT_NAMES.add("YEAR");
        TIME_UNIT_NAMES.add("MONTH");
        TIME_UNIT_NAMES.add("WEEK");
        TIME_UNIT_NAMES.add("DAY");
        TIME_UNIT_NAMES.add("HOUR");
        TIME_UNIT_NAMES.add("MINUTE");
        TIME_UNIT_NAMES.add("SECOND");
    }
    
    /**
     * Convert interval unit expression to SQL node.
     *
     * @param segment interval unit expression
     * @return SQL node
     */
    public static Optional<SqlNode> convert(final IntervalUnitExpression segment) {
        String intervalUnit = segment.getIntervalUnit().name();
        if (TIME_UNIT_NAMES.contains(intervalUnit)) {
            return Optional.of(new SqlIntervalQualifier(TimeUnit.valueOf(intervalUnit), null, SqlParserPos.ZERO));
        }
        return Optional.of(SqlLiteral.createCharString(intervalUnit, SqlParserPos.ZERO));
    }
}
