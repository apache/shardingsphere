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

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPBannedSQLStatementException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPLockingReadStatementException;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;

import java.util.List;
import java.util.Locale;

/**
 * Classify one SQL statement into the MCP statement classes.
 */
public final class StatementClassifier {

    private final SQLStatementScanner scanner = new SQLStatementScanner();

    private final SQLStatementStructureResolver structureResolver = new SQLStatementStructureResolver(scanner);

    private final SQLStatementClassResolver statementClassResolver = new SQLStatementClassResolver();

    private final SQLStatementTargetResolver targetResolver = new SQLStatementTargetResolver(scanner, statementClassResolver);

    /**
     * Classify one SQL statement.
     *
     * @param sql SQL text
     * @return classification result
     * @throws MCPBannedSQLStatementException when the SQL is banned by contract
     * @throws MetadataIntrospectionSQLStatementException when the SQL should use MCP metadata resources
     */
    public ClassificationResult classify(final String sql) {
        String actualSql = scanner.normalizeSingleStatement(sql);
        String leadingSql = actualSql.substring(scanner.skipInsignificant(actualSql, 0)).trim();
        String upperLeadingSql = leadingSql.toUpperCase(Locale.ENGLISH);
        if (isBannedCommand(upperLeadingSql, actualSql)) {
            throw new MCPBannedSQLStatementException();
        }
        if (isMetadataIntrospectionStatement(upperLeadingSql)) {
            throw new MetadataIntrospectionSQLStatementException(extractStatementType(upperLeadingSql));
        }
        if (upperLeadingSql.startsWith("EXPLAIN ANALYZE")) {
            String explainedSql = leadingSql.substring("EXPLAIN ANALYZE".length()).trim();
            SQLStatementStructure explainedStatementStructure = structureResolver.resolve(explainedSql);
            SupportedMCPStatement explainedStatementClass = statementClassResolver.resolve(explainedStatementStructure);
            checkLockingRead(explainedStatementClass, scanner.tokenize(explainedSql));
            return new ClassificationResult(SupportedMCPStatement.EXPLAIN_ANALYZE, "EXPLAIN ANALYZE", actualSql,
                    targetResolver.resolve(explainedStatementStructure, explainedStatementClass), "", explainedStatementClass);
        }
        if (isSavepointStatement(upperLeadingSql)) {
            String statementType = extractStatementType(upperLeadingSql);
            String savepointName = extractSavepointName(leadingSql);
            validateSavepointName(statementType, savepointName);
            return new ClassificationResult(SupportedMCPStatement.SAVEPOINT, statementType, actualSql, "", savepointName);
        }
        if (isTransactionControlStatement(upperLeadingSql)) {
            return new ClassificationResult(SupportedMCPStatement.TRANSACTION_CONTROL, extractStatementType(upperLeadingSql), actualSql, "", "");
        }
        SQLStatementStructure statementStructure = structureResolver.resolve(actualSql);
        SupportedMCPStatement statementClass = statementClassResolver.resolve(statementStructure);
        checkLockingRead(statementClass, scanner.tokenize(actualSql));
        return new ClassificationResult(statementClass, statementStructure.statementType(), actualSql, targetResolver.resolve(statementStructure, statementClass), "");
    }

    private boolean isBannedCommand(final String upperSql, final String sql) {
        return upperSql.startsWith("USE ")
                || upperSql.startsWith("SET ")
                || upperSql.startsWith("COPY ")
                || upperSql.startsWith("LOAD ")
                || upperSql.startsWith("CALL ")
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
        return containsSelectIntoFile(tokens) || containsKeywordSequence(tokens, "ALTER", "SYSTEM") || containsUserOrRoleManagement(tokens);
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

    private boolean containsKeywordSequence(final List<SQLStatementToken> tokens, final String firstKeyword, final String secondKeyword) {
        for (int index = 0; index + 1 < tokens.size(); index++) {
            if (scanner.isKeyword(tokens.get(index), firstKeyword) && scanner.isKeyword(tokens.get(index + 1), secondKeyword)) {
                return true;
            }
        }
        return false;
    }

    private void checkLockingRead(final SupportedMCPStatement statementClass, final List<SQLStatementToken> tokens) {
        if (SupportedMCPStatement.QUERY == statementClass && containsLockingReadClause(tokens)) {
            throw new MCPLockingReadStatementException();
        }
    }

    private boolean containsLockingReadClause(final List<SQLStatementToken> tokens) {
        for (int index = 0; index < tokens.size(); index++) {
            if (containsLockingReadForClause(tokens, index) || containsLockInShareModeClause(tokens, index)) {
                return true;
            }
        }
        return false;
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

    private boolean isTransactionControlStatement(final String upperSql) {
        return "BEGIN".equals(upperSql)
                || upperSql.startsWith("START TRANSACTION")
                || "COMMIT".equals(upperSql)
                || "ROLLBACK".equals(upperSql);
    }

    private boolean isSavepointStatement(final String upperSql) {
        return "SAVEPOINT".equals(upperSql)
                || upperSql.startsWith("SAVEPOINT ")
                || upperSql.startsWith("ROLLBACK TO SAVEPOINT")
                || upperSql.startsWith("RELEASE SAVEPOINT");
    }

    private String extractStatementType(final String upperSql) {
        if (upperSql.startsWith("START TRANSACTION")) {
            return "START TRANSACTION";
        }
        if (upperSql.startsWith("ROLLBACK TO SAVEPOINT")) {
            return "ROLLBACK TO SAVEPOINT";
        }
        if (upperSql.startsWith("RELEASE SAVEPOINT")) {
            return "RELEASE SAVEPOINT";
        }
        return upperSql.split("\\s+")[0];
    }

    private String extractSavepointName(final String sql) {
        String[] tokens = sql.split("\\s+");
        if ("SAVEPOINT".equalsIgnoreCase(tokens[0]) && tokens.length >= 2) {
            return tokens[tokens.length - 1];
        }
        if ("RELEASE".equalsIgnoreCase(tokens[0]) && tokens.length >= 3) {
            return tokens[tokens.length - 1];
        }
        if ("ROLLBACK".equalsIgnoreCase(tokens[0]) && tokens.length >= 4) {
            return tokens[tokens.length - 1];
        }
        return "";
    }

    private void validateSavepointName(final String statementType, final String savepointName) {
        if (!savepointName.isEmpty()) {
            return;
        }
        ShardingSpherePreconditions.checkState(!"SAVEPOINT".equals(statementType) && !"ROLLBACK TO SAVEPOINT".equals(statementType) && !"RELEASE SAVEPOINT".equals(statementType),
                () -> new IllegalArgumentException("Savepoint name is required."));
    }
}
