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

package org.apache.shardingsphere.sql.parser.presto.visitor.statement.type;

import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DALStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.BinaryLogFileIndexNumberContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.ChannelOptionContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.ExplainContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.FromSchemaContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.FromTableContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.ShowContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.ShowFilterContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.ShowLikeContext;
import org.apache.shardingsphere.sql.parser.autogen.PrestoStatementParser.ShowWhereClauseContext;
import org.apache.shardingsphere.sql.parser.presto.visitor.statement.PrestoStatementVisitor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.FromSchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.FromTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.ShowFilterSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dal.ShowLikeSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.DatabaseSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.EmptyStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.NumberLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.literal.impl.StringLiteralValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dal.MySQLExplainStatement;

/**
 * DAL statement visitor for MySQL.
 */
public final class PrestoDALStatementVisitor extends PrestoStatementVisitor implements DALStatementVisitor {
    
    @Override
    public ASTNode visitExplain(final ExplainContext ctx) {
        MySQLExplainStatement result = new MySQLExplainStatement();
        if (null != ctx.tableName()) {
            result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitShow(final ShowContext ctx) {
        return new EmptyStatement();
    }
    
    @Override
    public ASTNode visitChannelOption(final ChannelOptionContext ctx) {
        return visit(ctx.string_());
    }
    
    @Override
    public ASTNode visitBinaryLogFileIndexNumber(final BinaryLogFileIndexNumberContext ctx) {
        return new NumberLiteralValue(ctx.getText());
    }
    
    @Override
    public ASTNode visitShowWhereClause(final ShowWhereClauseContext ctx) {
        return new WhereSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ExpressionSegment) visit(ctx.expr()));
    }
    
    @Override
    public ASTNode visitShowFilter(final ShowFilterContext ctx) {
        ShowFilterSegment result = new ShowFilterSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex());
        if (null != ctx.showLike()) {
            result.setLike((ShowLikeSegment) visit(ctx.showLike()));
        }
        if (null != ctx.showWhereClause()) {
            result.setWhere((WhereSegment) visit(ctx.showWhereClause()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitFromTable(final FromTableContext ctx) {
        FromTableSegment result = new FromTableSegment();
        result.setTable((SimpleTableSegment) visit(ctx.tableName()));
        return result;
    }
    
    @Override
    public ASTNode visitFromSchema(final FromSchemaContext ctx) {
        return new FromSchemaSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (DatabaseSegment) visit(ctx.schemaName()));
    }
    
    @Override
    public ASTNode visitShowLike(final ShowLikeContext ctx) {
        StringLiteralValue literalValue = (StringLiteralValue) visit(ctx.stringLiterals());
        return new ShowLikeSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), literalValue.getValue());
    }
    
}
