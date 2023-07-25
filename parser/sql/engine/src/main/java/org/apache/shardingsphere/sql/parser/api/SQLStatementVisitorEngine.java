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

package org.apache.shardingsphere.sql.parser.api;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.Token;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.SQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.core.ParseASTNode;
import org.apache.shardingsphere.sql.parser.core.database.visitor.SQLStatementVisitorFactory;
import org.apache.shardingsphere.sql.parser.core.database.visitor.SQLVisitorRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.CommentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.AbstractSQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

/**
 * SQL statement visitor engine.
 */
@RequiredArgsConstructor
public final class SQLStatementVisitorEngine {
    
    private final DatabaseType databaseType;
    
    private final boolean isParseComment;
    
    public SQLStatementVisitorEngine(final String databaseType, final boolean isParseComment) {
        this(TypedSPILoader.getService(DatabaseType.class, databaseType), isParseComment);
    }
    
    /**
     * Visit parse context.
     *
     * @param parseASTNode parse AST node
     * @return SQL visitor result
     */
    public SQLStatement visit(final ParseASTNode parseASTNode) {
        SQLStatementVisitor visitor = SQLStatementVisitorFactory.newInstance(databaseType, SQLVisitorRule.valueOf(parseASTNode.getRootNode().getClass()));
        ASTNode result = parseASTNode.getRootNode().accept(visitor);
        if (isParseComment) {
            appendSQLComments(parseASTNode, result);
        }
        return (SQLStatement) result;
    }
    
    private <T> void appendSQLComments(final ParseASTNode parseASTNode, final T visitResult) {
        if (visitResult instanceof AbstractSQLStatement) {
            for (Token each : parseASTNode.getHiddenTokens()) {
                ((AbstractSQLStatement) visitResult).getCommentSegments().add(new CommentSegment(each.getText(), each.getStartIndex(), each.getStopIndex()));
            }
        }
    }
}
