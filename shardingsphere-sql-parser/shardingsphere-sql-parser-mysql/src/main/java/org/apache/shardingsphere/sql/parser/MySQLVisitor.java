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

package org.apache.shardingsphere.sql.parser;

import org.apache.shardingsphere.sql.parser.api.SQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementBaseVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FromSchemaContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.IdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SchemaNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowLikeContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowTableStatusContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StringLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.UnreservedWord_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.UseContext;
import org.apache.shardingsphere.sql.parser.sql.ASTNode;
import org.apache.shardingsphere.sql.parser.sql.segment.dal.FromSchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dal.ShowLikeSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowTableStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.UseStatement;
import org.apache.shardingsphere.sql.parser.sql.value.LiteralValue;

/**
 * MySQL visitor.
 *
 * @author panjuan
 */
public final class MySQLVisitor extends MySQLStatementBaseVisitor<ASTNode> implements SQLVisitor {
    
    // DALStatement.g4
    @Override
    public ASTNode visitUse(final UseContext ctx) {
        LiteralValue schema = (LiteralValue) visit(ctx.schemaName());
        UseStatement useStatement = new UseStatement();
        useStatement.setSchema(schema.getLiteral());
        return useStatement;
    }
    
    @Override
    public ASTNode visitShowTableStatus(final ShowTableStatusContext ctx) {
        ShowTableStatusStatement showTableStatusStatement = new ShowTableStatusStatement();
        FromSchemaContext fromSchemaContext = ctx.fromSchema();
        ShowLikeContext showLikeContext = ctx.showLike();
        if (null != fromSchemaContext) {
            FromSchemaSegment fromSchemaSegment = (FromSchemaSegment) visit(ctx.fromSchema());
            showTableStatusStatement.getAllSQLSegments().add(fromSchemaSegment);
        }
        if (null != showLikeContext) {
            ShowLikeSegment showLikeSegment = (ShowLikeSegment) visit(ctx.showLike());
            showTableStatusStatement.getAllSQLSegments().add(showLikeSegment);
        }
        return showTableStatusStatement;
    }
    
    @Override
    public ASTNode visitFromSchema(final FromSchemaContext ctx) {
        return new FromSchemaSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
    }
    
    @Override
    public ASTNode visitShowLike(final ShowLikeContext ctx) {
        LiteralValue literalValue = (LiteralValue) visit(ctx.stringLiterals());
        return new ShowLikeSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), literalValue.getLiteral());
    }
    
    // DCLStatement.g4
    // DDLStatement.g4
    // DMLStatement.g4
    // TCLStatement.g4
    // StoreProcedure.g4
    
    // BaseRule.g4
    @Override
    public ASTNode visitSchemaName(final SchemaNameContext ctx) {
        return visit(ctx.identifier());
    }
    
    @Override
    public ASTNode visitStringLiterals(final StringLiteralsContext ctx) {
        return new LiteralValue(ctx.STRING_().getText());
    }
    
    @Override
    public ASTNode visitIdentifier(final IdentifierContext ctx) {
        UnreservedWord_Context unreservedWord = ctx.unreservedWord_();
        if (null != unreservedWord) {
            return visit(unreservedWord);
        }
        return new LiteralValue(ctx.getText());
    }
    
    @Override
    public ASTNode visitUnreservedWord_(final UnreservedWord_Context ctx) {
        return new LiteralValue(ctx.getText());
    }
}
