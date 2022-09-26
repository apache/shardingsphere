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

package org.apache.shardingsphere.parser.distsql.parser.core;

import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.shardingsphere.distsql.parser.autogen.SQLParserDistSQLStatementBaseVisitor;
import org.apache.shardingsphere.distsql.parser.autogen.SQLParserDistSQLStatementParser.AlterSQLParserRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.SQLParserDistSQLStatementParser.CacheOptionContext;
import org.apache.shardingsphere.distsql.parser.autogen.SQLParserDistSQLStatementParser.ShowSQLParserRuleContext;
import org.apache.shardingsphere.distsql.parser.autogen.SQLParserDistSQLStatementParser.SqlParserRuleDefinitionContext;
import org.apache.shardingsphere.parser.distsql.parser.segment.CacheOptionSegment;
import org.apache.shardingsphere.parser.distsql.parser.statement.queryable.ShowSQLParserRuleStatement;
import org.apache.shardingsphere.parser.distsql.parser.statement.updatable.AlterSQLParserRuleStatement;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.SQLVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

/**
 * SQL statement visitor for SQL parser dist SQL.
 */
public final class SQLParserDistSQLStatementVisitor extends SQLParserDistSQLStatementBaseVisitor<ASTNode> implements SQLVisitor {
    
    @Override
    public ASTNode visitShowSQLParserRule(final ShowSQLParserRuleContext ctx) {
        return new ShowSQLParserRuleStatement();
    }
    
    @Override
    public ASTNode visitAlterSQLParserRule(final AlterSQLParserRuleContext ctx) {
        return super.visit(ctx.sqlParserRuleDefinition());
    }
    
    @Override
    public ASTNode visitSqlParserRuleDefinition(final SqlParserRuleDefinitionContext ctx) {
        Boolean sqlCommentParseEnable = null == ctx.sqlCommentParseEnable() ? null : Boolean.parseBoolean(getIdentifierValue(ctx.sqlCommentParseEnable()));
        CacheOptionSegment parseTreeCache = null == ctx.parseTreeCache() ? null : visitCacheOption(ctx.parseTreeCache().cacheOption());
        CacheOptionSegment sqlStatementCache = null == ctx.sqlStatementCache() ? null : visitCacheOption(ctx.sqlStatementCache().cacheOption());
        return new AlterSQLParserRuleStatement(sqlCommentParseEnable, parseTreeCache, sqlStatementCache);
    }
    
    @Override
    public CacheOptionSegment visitCacheOption(final CacheOptionContext ctx) {
        return new CacheOptionSegment(
                null == ctx.initialCapacity() ? null : Integer.parseInt(getIdentifierValue(ctx.initialCapacity())),
                null == ctx.maximumSize() ? null : Long.parseLong(getIdentifierValue(ctx.maximumSize())),
                null == ctx.persistent() ? null : Boolean.parseBoolean(getIdentifierValue(ctx.persistent())));
    }
    
    private String getIdentifierValue(final ParseTree context) {
        return null == context ? null : new IdentifierValue(context.getText()).getValue();
    }
}
