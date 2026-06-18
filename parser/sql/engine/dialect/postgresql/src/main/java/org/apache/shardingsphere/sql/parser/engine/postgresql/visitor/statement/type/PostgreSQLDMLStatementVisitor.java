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

package org.apache.shardingsphere.sql.parser.engine.postgresql.visitor.statement.type;

import org.antlr.v4.runtime.misc.Interval;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.sql.parser.api.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.statement.type.DMLStatementVisitor;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CallArgumentContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CallContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CopyContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CopyWithTableBinaryContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CopyWithTableOrQueryBinaryCsvContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CopyWithTableOrQueryContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DoStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.PreparableStmtContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.ReturningClauseContext;
import org.apache.shardingsphere.sql.parser.engine.postgresql.visitor.statement.PostgreSQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.ReturningSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.prepare.PrepareStatementQuerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.CallStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DoStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.postgresql.dml.PostgreSQLCopyStatement;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DML statement visitor for PostgreSQL.
 */
public final class PostgreSQLDMLStatementVisitor extends PostgreSQLStatementVisitor implements DMLStatementVisitor {
    
    public PostgreSQLDMLStatementVisitor(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @Override
    public ASTNode visitCall(final CallContext ctx) {
        String procedureName = ((IdentifierValue) visit(ctx.identifier())).getValue();
        List<ExpressionSegment> params = null == ctx.callArguments()
                ? Collections.emptyList()
                : ctx.callArguments().callArgument().stream().map(each -> (ExpressionSegment) visit(each)).collect(Collectors.toList());
        return new CallStatement(getDatabaseType(), procedureName, params);
    }
    
    @Override
    public ASTNode visitCallArgument(final CallArgumentContext ctx) {
        if (null == ctx.positionalNotation()) {
            String text = ctx.namedNotation().start.getInputStream().getText(new Interval(ctx.namedNotation().start.getStartIndex(), ctx.namedNotation().stop.getStopIndex()));
            return new CommonExpressionSegment(ctx.namedNotation().getStart().getStartIndex(), ctx.namedNotation().getStop().getStopIndex(), text);
        }
        return visit(ctx.positionalNotation().aExpr());
    }
    
    @Override
    public ASTNode visitDoStatement(final DoStatementContext ctx) {
        return new DoStatement(getDatabaseType(), Collections.emptyList());
    }
    
    @Override
    public ASTNode visitCopy(final CopyContext ctx) {
        if (null != ctx.copyWithTableOrQuery()) {
            return visit(ctx.copyWithTableOrQuery());
        }
        if (null != ctx.copyWithTableOrQueryBinaryCsv()) {
            return visit(ctx.copyWithTableOrQueryBinaryCsv());
        }
        return visit(ctx.copyWithTableBinary());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitCopyWithTableOrQuery(final CopyWithTableOrQueryContext ctx) {
        return new PostgreSQLCopyStatement(getDatabaseType(), null == ctx.qualifiedName() ? null : (SimpleTableSegment) visit(ctx.qualifiedName()),
                null == ctx.columnNames() ? Collections.emptyList() : ((CollectionValue<ColumnSegment>) visit(ctx.columnNames())).getValue(),
                null == ctx.preparableStmt() ? null : extractPrepareStatementQuerySegmentFromPreparableStmt(ctx.preparableStmt()));
    }
    
    private PrepareStatementQuerySegment extractPrepareStatementQuerySegmentFromPreparableStmt(final PreparableStmtContext ctx) {
        PrepareStatementQuerySegment result = new PrepareStatementQuerySegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        if (null != ctx.select()) {
            result.setSelect((SelectStatement) visit(ctx.select()));
        } else if (null != ctx.insert()) {
            result.setInsert((InsertStatement) visit(ctx.insert()));
        } else if (null != ctx.update()) {
            result.setUpdate((UpdateStatement) visit(ctx.update()));
        } else {
            result.setDelete((DeleteStatement) visit(ctx.delete()));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ASTNode visitCopyWithTableOrQueryBinaryCsv(final CopyWithTableOrQueryBinaryCsvContext ctx) {
        return new PostgreSQLCopyStatement(getDatabaseType(), null == ctx.qualifiedName() ? null : (SimpleTableSegment) visit(ctx.qualifiedName()),
                null == ctx.columnNames() ? Collections.emptyList() : ((CollectionValue<ColumnSegment>) visit(ctx.columnNames())).getValue(),
                null == ctx.preparableStmt() ? null : extractPrepareStatementQuerySegmentFromPreparableStmt(ctx.preparableStmt()));
    }
    
    @Override
    public ASTNode visitCopyWithTableBinary(final CopyWithTableBinaryContext ctx) {
        return new PostgreSQLCopyStatement(getDatabaseType(), null == ctx.qualifiedName() ? null : (SimpleTableSegment) visit(ctx.qualifiedName()), Collections.emptyList(), null);
    }
    
    @Override
    public ASTNode visitReturningClause(final ReturningClauseContext ctx) {
        return new ReturningSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), (ProjectionsSegment) visit(ctx.targetList()));
    }
}
