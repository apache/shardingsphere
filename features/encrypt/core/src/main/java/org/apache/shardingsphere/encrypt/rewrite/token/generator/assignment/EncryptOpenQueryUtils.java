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

package org.apache.shardingsphere.encrypt.rewrite.token.generator.assignment;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.table.EncryptTable;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.FunctionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encrypt OPENQUERY utility.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EncryptOpenQueryUtils {
    
    private static final Pattern FROM_TABLE_PATTERN = Pattern.compile("\\bFROM\\s+([^\\s,;]+)", Pattern.CASE_INSENSITIVE);
    
    /**
     * Whether table segment is OPENQUERY function table.
     *
     * @param tableSegment table segment
     * @return whether OPENQUERY function table
     */
    public static boolean isOpenQueryFunctionTable(final TableSegment tableSegment) {
        return tableSegment instanceof FunctionTableSegment && ((FunctionTableSegment) tableSegment).getTableFunction() instanceof FunctionSegment
                && "OPENQUERY".equalsIgnoreCase(((FunctionSegment) ((FunctionTableSegment) tableSegment).getTableFunction()).getFunctionName());
    }
    
    /**
     * Find OPENQUERY SQL literal.
     *
     * @param tableSegment table segment
     * @return OPENQUERY SQL literal
     */
    public static Optional<LiteralExpressionSegment> findOpenQuerySQLLiteral(final TableSegment tableSegment) {
        if (!isOpenQueryFunctionTable(tableSegment)) {
            return Optional.empty();
        }
        int parameterIndex = 0;
        for (ExpressionSegment each : ((FunctionSegment) ((FunctionTableSegment) tableSegment).getTableFunction()).getParameters()) {
            if (1 == parameterIndex) {
                return each instanceof LiteralExpressionSegment ? Optional.of((LiteralExpressionSegment) each) : Optional.empty();
            }
            parameterIndex++;
        }
        return Optional.empty();
    }
    
    /**
     * Find encrypt table from OPENQUERY target.
     *
     * @param rule encrypt rule
     * @param tableSegment table segment
     * @return encrypt table
     */
    public static Optional<EncryptTable> findEncryptTable(final EncryptRule rule, final TableSegment tableSegment) {
        Optional<LiteralExpressionSegment> openQuerySQL = findOpenQuerySQLLiteral(tableSegment);
        if (!openQuerySQL.isPresent()) {
            return Optional.empty();
        }
        Matcher matcher = FROM_TABLE_PATTERN.matcher(openQuerySQL.get().getText());
        if (!matcher.find()) {
            return Optional.empty();
        }
        String actualTableName = QuoteCharacter.unwrapAndTrimText(matcher.group(1).substring(matcher.group(1).lastIndexOf('.') + 1));
        for (String each : rule.getAllTableNames()) {
            Optional<EncryptTable> encryptTable = rule.findEncryptTable(each);
            if (encryptTable.isPresent() && each.equalsIgnoreCase(actualTableName)) {
                return encryptTable;
            }
        }
        return Optional.empty();
    }
    
    /**
     * Find schema name from OPENQUERY SQL.
     *
     * @param tableSegment table segment
     * @return schema name
     */
    public static Optional<String> findSchemaName(final TableSegment tableSegment) {
        Optional<LiteralExpressionSegment> openQuerySQL = findOpenQuerySQLLiteral(tableSegment);
        if (!openQuerySQL.isPresent()) {
            return Optional.empty();
        }
        Matcher matcher = FROM_TABLE_PATTERN.matcher(openQuerySQL.get().getText());
        if (!matcher.find()) {
            return Optional.empty();
        }
        String tableExpression = matcher.group(1);
        int delimiterIndex = tableExpression.lastIndexOf('.');
        return -1 == delimiterIndex ? Optional.empty() : Optional.of(QuoteCharacter.unwrapAndTrimText(tableExpression.substring(0, delimiterIndex)));
    }
}
