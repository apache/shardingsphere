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

package org.apache.shardingsphere.linkedserver.openquery;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.FunctionTableSegment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Utility class for OPENQUERY detection, extraction, and T-SQL escaping.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpenQueryUtils {
    
    private static final String OPENQUERY_FUNCTION_NAME = "OPENQUERY";
    
    /**
     * Check if a function table segment is an OPENQUERY call.
     *
     * @param functionTableSegment function table segment
     * @return true if OPENQUERY
     */
    public static boolean isOpenQuery(final FunctionTableSegment functionTableSegment) {
        ExpressionSegment tableFunction = functionTableSegment.getTableFunction();
        return tableFunction instanceof FunctionSegment && OPENQUERY_FUNCTION_NAME.equalsIgnoreCase(((FunctionSegment) tableFunction).getFunctionName());
    }
    
    /**
     * Extract linked server name from OPENQUERY function segment.
     *
     * @param functionSegment OPENQUERY function segment
     * @return linked server name
     */
    public static String extractLinkedServerName(final FunctionSegment functionSegment) {
        List<ExpressionSegment> params = new ArrayList<>(functionSegment.getParameters());
        ExpressionSegment firstParam = params.get(0);
        if (firstParam instanceof ColumnSegment) {
            return ((ColumnSegment) firstParam).getIdentifier().getValue();
        }
        return firstParam.getText();
    }
    
    /**
     * Extract inner SQL literal expression segment from OPENQUERY function segment.
     *
     * @param functionSegment OPENQUERY function segment
     * @return inner SQL literal expression segment
     */
    public static Optional<LiteralExpressionSegment> extractInnerSQLSegment(final FunctionSegment functionSegment) {
        List<ExpressionSegment> params = new ArrayList<>(functionSegment.getParameters());
        if (params.size() < 2) {
            return Optional.empty();
        }
        ExpressionSegment secondParam = params.get(1);
        return secondParam instanceof LiteralExpressionSegment ? Optional.of((LiteralExpressionSegment) secondParam) : Optional.empty();
    }
    
    /**
     * Decode T-SQL escaped single quotes in string literal content.
     * Converts {@code ''} to {@code '}.
     *
     * @param raw raw string with T-SQL escaping
     * @return decoded string
     */
    public static String decodeTSqlEscaping(final String raw) {
        return raw.replace("''", "'");
    }
    
    /**
     * Encode single quotes for T-SQL string literal context.
     * Converts {@code '} to {@code ''}.
     *
     * @param decoded decoded string
     * @return encoded string for T-SQL literal
     */
    public static String encodeTSqlEscaping(final String decoded) {
        return decoded.replace("'", "''");
    }
}
