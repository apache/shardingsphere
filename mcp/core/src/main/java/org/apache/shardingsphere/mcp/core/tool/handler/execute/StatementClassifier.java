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
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;

import java.util.Locale;

/**
 * Classify one SQL statement into the MCP statement classes.
 */
public final class StatementClassifier {
    
    private final SQLStatementScanner scanner = new SQLStatementScanner();
    
    private final SQLStatementSafetyValidator safetyValidator = new SQLStatementSafetyValidator(scanner);
    
    private final SQLStatementStructureResolver structureResolver = new SQLStatementStructureResolver(scanner);
    
    private final SQLStatementClassResolver statementClassResolver = new SQLStatementClassResolver();
    
    private final SQLStatementTargetResolver targetResolver = new SQLStatementTargetResolver(scanner, structureResolver);
    
    /**
     * Classify one SQL statement.
     *
     * @param sql SQL text
     * @return classification result
     */
    public ClassificationResult classify(final String sql) {
        String actualSql = scanner.normalizeSingleStatement(sql);
        String leadingSql = actualSql.substring(scanner.skipInsignificant(actualSql, 0)).trim();
        String upperLeadingSql = leadingSql.toUpperCase(Locale.ENGLISH);
        safetyValidator.checkLeadingStatement(upperLeadingSql, actualSql);
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
        safetyValidator.checkStructuredStatement(statementClass, statementStructure);
        return new ClassificationResult(statementClass, statementStructure.statementType(), actualSql, targetResolver.resolve(statementStructure), "", null,
                targetResolver.resolveAll(statementStructure));
    }
    
    private boolean isTransactionControlStatement(final String upperSql) {
        return "BEGIN".equals(upperSql)
                || "START TRANSACTION".equals(upperSql)
                || "COMMIT".equals(upperSql)
                || "ROLLBACK".equals(upperSql);
    }
    
    private boolean isSavepointStatement(final String upperSql) {
        return "SAVEPOINT".equals(upperSql)
                || upperSql.startsWith("SAVEPOINT ")
                || "ROLLBACK TO".equals(upperSql)
                || upperSql.startsWith("ROLLBACK TO ")
                || "RELEASE SAVEPOINT".equals(upperSql)
                || upperSql.startsWith("RELEASE SAVEPOINT ");
    }
    
    private String extractStatementType(final String upperSql) {
        if (upperSql.startsWith("START TRANSACTION")) {
            return "START TRANSACTION";
        }
        if (upperSql.startsWith("ROLLBACK TO SAVEPOINT")) {
            return "ROLLBACK TO SAVEPOINT";
        }
        if (upperSql.startsWith("ROLLBACK TO")) {
            return "ROLLBACK TO";
        }
        if (upperSql.startsWith("RELEASE SAVEPOINT")) {
            return "RELEASE SAVEPOINT";
        }
        return upperSql.split("\\s+")[0];
    }
    
    private String extractSavepointName(final String sql) {
        String[] tokens = sql.split("\\s+");
        if ("SAVEPOINT".equalsIgnoreCase(tokens[0]) && 2 == tokens.length) {
            return tokens[1];
        }
        if ("RELEASE".equalsIgnoreCase(tokens[0]) && 3 == tokens.length && "SAVEPOINT".equalsIgnoreCase(tokens[1])) {
            return tokens[2];
        }
        if ("ROLLBACK".equalsIgnoreCase(tokens[0]) && tokens.length >= 3 && "TO".equalsIgnoreCase(tokens[1])) {
            if (3 == tokens.length && !"SAVEPOINT".equalsIgnoreCase(tokens[2])) {
                return tokens[2];
            }
            if (4 == tokens.length && "SAVEPOINT".equalsIgnoreCase(tokens[2])) {
                return tokens[3];
            }
        }
        return "";
    }
    
    private void validateSavepointName(final String statementType, final String savepointName) {
        if (!savepointName.isEmpty()) {
            return;
        }
        ShardingSpherePreconditions.checkState(!isSavepointStatementType(statementType), () -> new IllegalArgumentException("Savepoint name is required."));
    }
    
    private boolean isSavepointStatementType(final String statementType) {
        return "SAVEPOINT".equals(statementType)
                || "ROLLBACK TO".equals(statementType)
                || "ROLLBACK TO SAVEPOINT".equals(statementType)
                || "RELEASE SAVEPOINT".equals(statementType);
    }
}
