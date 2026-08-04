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

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.distsql.statement.type.rdl.rule.database.DatabaseRuleDefinitionStatement;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.parser.SQLParserEngine;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.api.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPUnsupportedSQLStatementException;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapability;
import org.apache.shardingsphere.mcp.support.database.capability.SupportedMCPStatement;
import org.apache.shardingsphere.mcp.support.database.spi.MCPRuleDistSQLStatementClassifier;
import org.apache.shardingsphere.sql.parser.engine.api.CacheOption;
import org.apache.shardingsphere.sql.parser.spi.DialectSQLParserFacade;
import org.apache.shardingsphere.sql.parser.spi.SQLStatementVisitorFacade;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.GrantStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.RevokeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.BeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.ReleaseSavepointStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.SavepointStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.StartTransactionStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.TCLStatement;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

final class MCPStatementAnalyzer {
    
    private static final CacheOption DISABLED_CACHE_OPTION = new CacheOption(0, 0L);
    
    private static final Set<String> DDL_STATEMENT_TYPES = Set.of("CREATE", "ALTER", "DROP", "TRUNCATE");
    
    private final Collection<MCPRuleDistSQLStatementClassifier> ruleDistSQLStatementClassifiers =
            ShardingSphereServiceLoader.getServiceInstances(MCPRuleDistSQLStatementClassifier.class);
    
    private final Map<String, SQLParserEngine> parserEngines = new ConcurrentHashMap<>();
    
    ClassificationResult analyze(final String sql, final MCPDatabaseCapability databaseCapability) {
        String databaseType = databaseCapability.getDatabaseType();
        SQLStatementScanner scanner = new SQLStatementScanner(databaseType, sql);
        String actualSql = scanner.sql();
        String leadingSql = scanner.leadingSql();
        String upperLeadingSql = leadingSql.toUpperCase(Locale.ENGLISH);
        SQLStatementSafetyValidator safetyValidator = new SQLStatementSafetyValidator();
        safetyValidator.checkLeadingStatement(upperLeadingSql, scanner.containsExecutableComment());
        if (isSavepointStatement(upperLeadingSql)) {
            validateSavepointStatement(leadingSql);
        }
        SQLStatement sqlStatement = parse(actualSql, databaseType);
        safetyValidator.checkParsedStatement(sqlStatement);
        Optional<ClassificationResult> tclStatement = analyzeTCLStatement(sqlStatement, actualSql, upperLeadingSql);
        if (tclStatement.isPresent()) {
            return tclStatement.get();
        }
        boolean ruleDistSQL = isRuleDistSQL(sqlStatement);
        String leadingKeyword = scanner.extractLeadingKeyword();
        SupportedMCPStatement statementClass = resolveStatementClass(sqlStatement, leadingKeyword, ruleDistSQL);
        String statementType = resolveStatementType(sqlStatement, statementClass, leadingKeyword);
        return new ClassificationResult(statementClass, statementType, actualSql, "", new SQLStatementObjectExtractor().extract(sqlStatement), ruleDistSQL);
    }
    
    private void validateSavepointStatement(final String leadingSql) {
        String savepointName = extractSavepointName(leadingSql);
        ShardingSpherePreconditions.checkState(!savepointName.isEmpty(), () -> new MCPInvalidRequestException("Savepoint name is required."));
    }
    
    private Optional<ClassificationResult> analyzeTCLStatement(final SQLStatement sqlStatement, final String actualSql, final String upperLeadingSql) {
        if (sqlStatement instanceof BeginTransactionStatement || sqlStatement instanceof StartTransactionStatement) {
            if ("BEGIN".equals(upperLeadingSql)) {
                return Optional.of(createTCLResult(SupportedMCPStatement.TRANSACTION_CONTROL, "BEGIN", actualSql, ""));
            }
            if ("START TRANSACTION".equals(upperLeadingSql)) {
                return Optional.of(createTCLResult(SupportedMCPStatement.TRANSACTION_CONTROL, "START TRANSACTION", actualSql, ""));
            }
        }
        if (sqlStatement instanceof CommitStatement && "COMMIT".equals(upperLeadingSql)) {
            return Optional.of(createTCLResult(SupportedMCPStatement.TRANSACTION_CONTROL, "COMMIT", actualSql, ""));
        }
        if (sqlStatement instanceof RollbackStatement) {
            Optional<String> savepointName = ((RollbackStatement) sqlStatement).getSavepointName();
            if (savepointName.isPresent()) {
                return Optional.of(createTCLResult(SupportedMCPStatement.SAVEPOINT, "ROLLBACK TO SAVEPOINT", actualSql, savepointName.get()));
            }
            if ("ROLLBACK".equals(upperLeadingSql)) {
                return Optional.of(createTCLResult(SupportedMCPStatement.TRANSACTION_CONTROL, "ROLLBACK", actualSql, ""));
            }
        }
        if (sqlStatement instanceof SavepointStatement) {
            return Optional.of(createTCLResult(SupportedMCPStatement.SAVEPOINT, "SAVEPOINT", actualSql, ((SavepointStatement) sqlStatement).getSavepointName()));
        }
        if (sqlStatement instanceof ReleaseSavepointStatement) {
            return Optional.of(createTCLResult(SupportedMCPStatement.SAVEPOINT, "RELEASE SAVEPOINT", actualSql, ((ReleaseSavepointStatement) sqlStatement).getSavepointName()));
        }
        if (sqlStatement instanceof TCLStatement) {
            throw new MCPUnsupportedSQLStatementException();
        }
        return Optional.empty();
    }
    
    private ClassificationResult createTCLResult(final SupportedMCPStatement statementClass, final String statementType, final String sql, final String savepointName) {
        return new ClassificationResult(statementClass, statementType, sql, savepointName, Set.of(), false);
    }
    
    private SQLStatement parse(final String sql, final String databaseType) {
        try {
            return parserEngines.computeIfAbsent(databaseType, this::createParserEngine).parse(sql, false);
        } catch (final MCPUnsupportedException ex) {
            throw ex;
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            throw new MCPUnsupportedSQLStatementException(ex);
        }
    }
    
    private SQLParserEngine createParserEngine(final String databaseTypeName) {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, databaseTypeName);
        ShardingSpherePreconditions.checkState(DatabaseTypedSPILoader.findService(DialectSQLParserFacade.class, databaseType).isPresent()
                && DatabaseTypedSPILoader.findService(SQLStatementVisitorFacade.class, databaseType).isPresent(),
                () -> new MCPUnsupportedException(String.format("SQL parser is not available for database type `%s`.", databaseTypeName)));
        return new ShardingSphereSQLParserEngine(databaseType, DISABLED_CACHE_OPTION, DISABLED_CACHE_OPTION);
    }
    
    private SupportedMCPStatement resolveStatementClass(final SQLStatement sqlStatement, final String leadingKeyword, final boolean ruleDistSQL) {
        if (ruleDistSQL && DDL_STATEMENT_TYPES.contains(leadingKeyword)) {
            return SupportedMCPStatement.DDL;
        }
        if (sqlStatement instanceof SelectStatement && hasExpectedLeadingKeyword(leadingKeyword, "SELECT")) {
            return SupportedMCPStatement.QUERY;
        }
        if (sqlStatement instanceof InsertStatement && hasExpectedLeadingKeyword(leadingKeyword, "INSERT")
                || sqlStatement instanceof UpdateStatement && hasExpectedLeadingKeyword(leadingKeyword, "UPDATE")
                || sqlStatement instanceof DeleteStatement && hasExpectedLeadingKeyword(leadingKeyword, "DELETE")
                || sqlStatement instanceof MergeStatement && hasExpectedLeadingKeyword(leadingKeyword, "MERGE")) {
            return SupportedMCPStatement.DML;
        }
        if (sqlStatement instanceof DDLStatement && DDL_STATEMENT_TYPES.contains(leadingKeyword)) {
            return SupportedMCPStatement.DDL;
        }
        if (sqlStatement instanceof GrantStatement && "GRANT".equals(leadingKeyword) || sqlStatement instanceof RevokeStatement && "REVOKE".equals(leadingKeyword)) {
            return SupportedMCPStatement.DCL;
        }
        throw new MCPUnsupportedSQLStatementException();
    }
    
    private boolean hasExpectedLeadingKeyword(final String leadingKeyword, final String expectedKeyword) {
        return expectedKeyword.equals(leadingKeyword) || "WITH".equals(leadingKeyword);
    }
    
    private String resolveStatementType(final SQLStatement sqlStatement, final SupportedMCPStatement statementClass, final String leadingKeyword) {
        if (SupportedMCPStatement.QUERY == statementClass) {
            return "SELECT";
        }
        if (SupportedMCPStatement.DML == statementClass) {
            if (sqlStatement instanceof InsertStatement) {
                return "INSERT";
            }
            if (sqlStatement instanceof UpdateStatement) {
                return "UPDATE";
            }
            if (sqlStatement instanceof DeleteStatement) {
                return "DELETE";
            }
            return "MERGE";
        }
        return leadingKeyword;
    }
    
    private boolean isRuleDistSQL(final SQLStatement sqlStatement) {
        if (sqlStatement instanceof DatabaseRuleDefinitionStatement) {
            return true;
        }
        for (MCPRuleDistSQLStatementClassifier each : ruleDistSQLStatementClassifiers) {
            if (each.isRuleDistSQL(sqlStatement)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isSavepointStatement(final String upperSql) {
        return "SAVEPOINT".equals(upperSql) || upperSql.startsWith("SAVEPOINT ")
                || "ROLLBACK TO".equals(upperSql) || upperSql.startsWith("ROLLBACK TO ")
                || "RELEASE SAVEPOINT".equals(upperSql) || upperSql.startsWith("RELEASE SAVEPOINT ");
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
}
