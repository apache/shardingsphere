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

package org.apache.shardingsphere.mcp.core.tool.handler.execute;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPBannedSQLStatementException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPLockingReadStatementException;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
final class SQLStatementSafetyValidator {
    
    private static final List<String> SIDE_EFFECTING_FUNCTION_NAMES = List.of("NEXTVAL", "SETVAL", "GET_LOCK", "RELEASE_LOCK", "RELEASE_ALL_LOCKS", "PG_ADVISORY_LOCK",
            "PG_ADVISORY_XACT_LOCK", "PG_TRY_ADVISORY_LOCK", "PG_TRY_ADVISORY_XACT_LOCK", "PG_ADVISORY_UNLOCK", "PG_ADVISORY_UNLOCK_ALL", "SET_CONFIG",
            "PG_REPLICATION_SLOT_ADVANCE", "PG_LOGICAL_SLOT_GET_CHANGES", "PG_LOGICAL_SLOT_GET_BINARY_CHANGES", "PG_LOGICAL_EMIT_MESSAGE", "PG_SWITCH_WAL", "PG_RELOAD_CONF",
            "PG_CANCEL_BACKEND", "PG_TERMINATE_BACKEND");
    
    private static final List<String> METADATA_LOOKUP_FUNCTION_NAMES = List.of("TO_REGCLASS", "TO_REGTYPE", "TO_REGPROC", "TO_REGPROCEDURE", "TO_REGOPER", "TO_REGOPERATOR",
            "TO_REGNAMESPACE", "TO_REGROLE", "OBJECT_ID");
    
    private final SQLStatementScanner scanner;
    
    void checkLeadingStatement(final String upperSql, final String sql) {
        if (isBannedCommand(upperSql, sql)) {
            throw new MCPBannedSQLStatementException();
        }
        if (isMetadataIntrospectionStatement(upperSql)) {
            throw new MetadataIntrospectionSQLStatementException(extractStatementType(upperSql));
        }
    }
    
    void checkParsedStatement(final SQLStatement sqlStatement, final String sql) {
        if (sqlStatement instanceof SelectStatement || findWithSegment(sqlStatement).isPresent()) {
            checkLockingRead(scanner.tokenize(sql));
        }
        checkParsedStatementTree(sqlStatement);
    }
    
    private void checkParsedStatementTree(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof SelectStatement) {
            SelectStatement selectStatement = (SelectStatement) sqlStatement;
            if (selectStatement.getInto().isPresent() || selectStatement.getOutfile().isPresent()) {
                throw new MCPBannedSQLStatementException();
            }
            if (selectStatement.getLock().isPresent()) {
                throw new MCPLockingReadStatementException();
            }
        }
        findWithSegment(sqlStatement).ifPresent(with -> {
            for (CommonTableExpressionSegment each : with.getCommonTableExpressions()) {
                checkParsedStatementTree(each.getSubquery().getSelect());
            }
        });
    }
    
    private Optional<WithSegment> findWithSegment(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof SelectStatement) {
            return ((SelectStatement) sqlStatement).getWith();
        }
        if (sqlStatement instanceof InsertStatement) {
            return ((InsertStatement) sqlStatement).getWith();
        }
        if (sqlStatement instanceof UpdateStatement) {
            return ((UpdateStatement) sqlStatement).getWith();
        }
        if (sqlStatement instanceof DeleteStatement) {
            return ((DeleteStatement) sqlStatement).getWith();
        }
        return sqlStatement instanceof MergeStatement ? ((MergeStatement) sqlStatement).getWith() : Optional.empty();
    }
    
    private boolean isBannedCommand(final String upperSql, final String sql) {
        return upperSql.startsWith("USE ")
                || upperSql.startsWith("SET ")
                || upperSql.startsWith("COPY ")
                || upperSql.startsWith("LOAD ")
                || upperSql.startsWith("CALL ")
                || scanner.containsExecutableComment(sql)
                || scanner.containsUserVariableAssignment(sql)
                || containsBannedDialectPattern(scanner.tokenize(sql));
    }
    
    private boolean isMetadataIntrospectionStatement(final String upperSql) {
        return "SHOW".equals(upperSql)
                || upperSql.startsWith("SHOW ")
                || "DESCRIBE".equals(upperSql)
                || upperSql.startsWith("DESCRIBE ")
                || "DESC".equals(upperSql)
                || upperSql.startsWith("DESC ");
    }
    
    private boolean containsBannedDialectPattern(final List<SQLStatementToken> tokens) {
        return containsSelectIntoFile(tokens)
                || containsKeywordSequence(tokens, "ALTER", "SYSTEM")
                || containsUserOrRoleManagement(tokens)
                || containsSideEffectingQueryPattern(tokens)
                || containsMetadataLookupFunction(tokens);
    }
    
    private boolean containsSelectIntoFile(final List<SQLStatementToken> tokens) {
        for (int index = 0; index + 1 < tokens.size(); index++) {
            if (!scanner.isKeyword(tokens.get(index), "INTO")) {
                continue;
            }
            if (scanner.isKeyword(tokens.get(index + 1), "OUTFILE") || scanner.isKeyword(tokens.get(index + 1), "DUMPFILE")) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsUserOrRoleManagement(final List<SQLStatementToken> tokens) {
        return containsKeywordSequence(tokens, "CREATE", "USER")
                || containsKeywordSequence(tokens, "ALTER", "USER")
                || containsKeywordSequence(tokens, "DROP", "USER")
                || containsKeywordSequence(tokens, "CREATE", "ROLE")
                || containsKeywordSequence(tokens, "ALTER", "ROLE")
                || containsKeywordSequence(tokens, "DROP", "ROLE");
    }
    
    private boolean containsSideEffectingQueryPattern(final List<SQLStatementToken> tokens) {
        return containsKeywordSequence(tokens, "NEXT", "VALUE", "FOR")
                || containsSequenceNextvalPseudocolumn(tokens)
                || containsSideEffectingFunction(tokens)
                || containsLastInsertIdMutation(tokens);
    }
    
    private boolean containsSideEffectingFunction(final List<SQLStatementToken> tokens) {
        for (int index = 0; index + 1 < tokens.size(); index++) {
            if ("(".equals(tokens.get(index + 1).text()) && SIDE_EFFECTING_FUNCTION_NAMES.contains(tokens.get(index).upperText())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsMetadataLookupFunction(final List<SQLStatementToken> tokens) {
        for (int index = 0; index + 1 < tokens.size(); index++) {
            if ("(".equals(tokens.get(index + 1).text()) && METADATA_LOOKUP_FUNCTION_NAMES.contains(tokens.get(index).upperText())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsLastInsertIdMutation(final List<SQLStatementToken> tokens) {
        for (int index = 0; index + 2 < tokens.size(); index++) {
            if (scanner.isKeyword(tokens.get(index), "LAST_INSERT_ID") && "(".equals(tokens.get(index + 1).text()) && !")".equals(tokens.get(index + 2).text())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsSequenceNextvalPseudocolumn(final List<SQLStatementToken> tokens) {
        for (int index = 2; index < tokens.size(); index++) {
            if (scanner.isKeyword(tokens.get(index), "NEXTVAL") && ".".equals(tokens.get(index - 1).text()) && tokens.get(index - 2).identifier()) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsKeywordSequence(final List<SQLStatementToken> tokens, final String... keywords) {
        for (int index = 0; index + keywords.length <= tokens.size(); index++) {
            if (containsKeywordSequence(tokens, index, keywords)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean containsKeywordSequence(final List<SQLStatementToken> tokens, final int startIndex, final String... keywords) {
        for (int index = 0; index < keywords.length; index++) {
            if (!scanner.isKeyword(tokens.get(startIndex + index), keywords[index])) {
                return false;
            }
        }
        return true;
    }
    
    private void checkLockingRead(final List<SQLStatementToken> tokens) {
        for (int index = 0; index < tokens.size(); index++) {
            if (containsLockingReadForClause(tokens, index) || containsLockInShareModeClause(tokens, index)) {
                throw new MCPLockingReadStatementException();
            }
        }
    }
    
    private boolean containsLockingReadForClause(final List<SQLStatementToken> tokens, final int index) {
        if (!scanner.isKeyword(tokens.get(index), "FOR") || index + 1 >= tokens.size()) {
            return false;
        }
        return scanner.isKeyword(tokens.get(index + 1), "UPDATE", "SHARE")
                || index + 2 < tokens.size() && scanner.isKeyword(tokens.get(index + 1), "KEY") && scanner.isKeyword(tokens.get(index + 2), "SHARE")
                || index + 3 < tokens.size() && scanner.isKeyword(tokens.get(index + 1), "NO") && scanner.isKeyword(tokens.get(index + 2), "KEY")
                        && scanner.isKeyword(tokens.get(index + 3), "UPDATE");
    }
    
    private boolean containsLockInShareModeClause(final List<SQLStatementToken> tokens, final int index) {
        return index + 3 < tokens.size()
                && scanner.isKeyword(tokens.get(index), "LOCK")
                && scanner.isKeyword(tokens.get(index + 1), "IN")
                && scanner.isKeyword(tokens.get(index + 2), "SHARE")
                && scanner.isKeyword(tokens.get(index + 3), "MODE");
    }
    
    private String extractStatementType(final String upperSql) {
        if (upperSql.startsWith("SHOW RULES USED STORAGE UNIT")) {
            return "SHOW RULES USED STORAGE UNIT";
        }
        if (upperSql.startsWith("SHOW STORAGE UNITS")) {
            return "SHOW STORAGE UNITS";
        }
        if (upperSql.startsWith("SHOW DEFAULT SINGLE TABLE STORAGE UNIT")) {
            return "SHOW DEFAULT SINGLE TABLE STORAGE UNIT";
        }
        if (upperSql.startsWith("SHOW SINGLE TABLES")) {
            return "SHOW SINGLE TABLES";
        }
        if (upperSql.startsWith("SHOW SINGLE TABLE")) {
            return "SHOW SINGLE TABLE";
        }
        return upperSql.split("\\s+")[0];
    }
}
