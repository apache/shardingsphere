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

import org.apache.shardingsphere.mcp.core.protocol.exception.MCPBannedSQLStatementException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPLockingReadStatementException;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.CommentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.role.AlterRoleStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.role.CreateRoleStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.role.DropRoleStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.user.AlterUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.user.CreateUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dcl.user.DropUserStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.CallStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

import java.util.List;
import java.util.Locale;

final class SQLStatementSafetyValidator {
    
    private static final List<String> SIDE_EFFECTING_FUNCTION_NAMES = List.of("NEXTVAL", "NEXT VALUE FOR", "SETVAL", "GET_LOCK", "RELEASE_LOCK", "RELEASE_ALL_LOCKS",
            "PG_ADVISORY_LOCK", "PG_ADVISORY_XACT_LOCK", "PG_TRY_ADVISORY_LOCK", "PG_TRY_ADVISORY_XACT_LOCK", "PG_ADVISORY_UNLOCK", "PG_ADVISORY_UNLOCK_ALL",
            "SET_CONFIG", "PG_REPLICATION_SLOT_ADVANCE", "PG_LOGICAL_SLOT_GET_CHANGES", "PG_LOGICAL_SLOT_GET_BINARY_CHANGES", "PG_LOGICAL_EMIT_MESSAGE", "PG_SWITCH_WAL",
            "PG_RELOAD_CONF", "PG_CANCEL_BACKEND", "PG_TERMINATE_BACKEND");
    
    private static final List<String> METADATA_LOOKUP_FUNCTION_NAMES = List.of("TO_REGCLASS", "TO_REGTYPE", "TO_REGPROC", "TO_REGPROCEDURE", "TO_REGOPER",
            "TO_REGOPERATOR", "TO_REGNAMESPACE", "TO_REGROLE", "OBJECT_ID");
    
    void checkLeadingStatement(final String upperSql, final boolean executableComment) {
        if (executableComment || upperSql.startsWith("USE ") || upperSql.startsWith("SET ") || upperSql.startsWith("COPY ") || upperSql.startsWith("LOAD ")
                || upperSql.startsWith("CALL ") || isAlterSystemStatement(upperSql)) {
            throw new MCPBannedSQLStatementException();
        }
        if (isMetadataIntrospectionStatement(upperSql)) {
            throw new MetadataIntrospectionSQLStatementException(extractStatementType(upperSql));
        }
    }
    
    void checkParsedStatement(final SQLStatement sqlStatement) {
        new SQLStatementTreeWalker(this::checkStatement, this::checkExpression).walk(sqlStatement);
    }
    
    private boolean isBannedStatementType(final SQLStatement sqlStatement) {
        return sqlStatement instanceof SetStatement || sqlStatement instanceof CallStatement
                || sqlStatement instanceof CreateUserStatement || sqlStatement instanceof AlterUserStatement || sqlStatement instanceof DropUserStatement
                || sqlStatement instanceof CreateRoleStatement || sqlStatement instanceof AlterRoleStatement || sqlStatement instanceof DropRoleStatement;
    }
    
    private boolean containsExecutableComment(final SQLStatement sqlStatement) {
        for (CommentSegment each : sqlStatement.getComments()) {
            String text = each.getText().trim();
            if (text.startsWith("/*!") || text.toUpperCase(Locale.ENGLISH).startsWith("/*M!")) {
                return true;
            }
        }
        return false;
    }
    
    private void checkStatement(final SQLStatement sqlStatement) {
        if (isBannedStatementType(sqlStatement) || containsExecutableComment(sqlStatement)) {
            throw new MCPBannedSQLStatementException();
        }
        if (sqlStatement instanceof SelectStatement) {
            SelectStatement select = (SelectStatement) sqlStatement;
            if (select.getInto().isPresent() || select.getOutfile().isPresent()) {
                throw new MCPBannedSQLStatementException();
            }
            if (select.getLock().isPresent()) {
                throw new MCPLockingReadStatementException();
            }
        }
    }
    
    private void checkExpression(final ExpressionSegment expression) {
        if (expression instanceof FunctionSegment) {
            checkFunction((FunctionSegment) expression);
        } else if (expression instanceof BinaryOperationExpression && ":=".equals(((BinaryOperationExpression) expression).getOperator())) {
            throw new MCPBannedSQLStatementException();
        } else if (expression instanceof ColumnSegment) {
            ColumnSegment column = (ColumnSegment) expression;
            if (column.getOwner().isPresent() && "NEXTVAL".equalsIgnoreCase(column.getIdentifier().getValue())) {
                throw new MCPBannedSQLStatementException();
            }
        }
    }
    
    private void checkFunction(final FunctionSegment function) {
        String functionName = function.getFunctionName().toUpperCase(Locale.ENGLISH);
        if (SIDE_EFFECTING_FUNCTION_NAMES.contains(functionName) || METADATA_LOOKUP_FUNCTION_NAMES.contains(functionName)
                || "LAST_INSERT_ID".equals(functionName) && !function.getParameters().isEmpty()) {
            throw new MCPBannedSQLStatementException();
        }
    }
    
    private boolean isMetadataIntrospectionStatement(final String upperSql) {
        return "SHOW".equals(upperSql) || upperSql.startsWith("SHOW ") || "DESCRIBE".equals(upperSql) || upperSql.startsWith("DESCRIBE ")
                || "DESC".equals(upperSql) || upperSql.startsWith("DESC ");
    }
    
    private boolean isAlterSystemStatement(final String upperSql) {
        String[] words = upperSql.split("\\s+", 3);
        return 2 <= words.length && "ALTER".equals(words[0]) && "SYSTEM".equals(words[1]);
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
