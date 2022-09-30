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

package org.apache.shardingsphere.sql.parser.postgresql.visitor.statement.impl;

import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.Interval;
import org.apache.shardingsphere.sql.parser.api.visitor.ASTNode;
import org.apache.shardingsphere.sql.parser.api.visitor.operation.SQLStatementVisitor;
import org.apache.shardingsphere.sql.parser.api.visitor.type.DMLSQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CallArgumentContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CallContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CheckpointContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CopyContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CopyWithTableBinaryContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CopyWithTableOrQueryBinaryCsvContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.CopyWithTableOrQueryContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.DoStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.PreparableStmtContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.prepare.PrepareStatementQuerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.collection.CollectionValue;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLCallStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLCheckpointStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLCopyStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLDoStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

/**
 * DML Statement SQL visitor for PostgreSQL.
 */
@NoArgsConstructor
public final class PostgreSQLDMLStatementSQLVisitor extends PostgreSQLStatementSQLVisitor implements DMLSQLVisitor, SQLStatementVisitor {
    
    public PostgreSQLDMLStatementSQLVisitor(final Properties props) {
        super(props);
    }
    
    @Override
    public ASTNode visitCall(final CallContext ctx) {
        PostgreSQLCallStatement result = new PostgreSQLCallStatement();
        result.setProcedureName(((IdentifierValue) visit(ctx.identifier())).getValue());
        if (null != ctx.callArguments()) {
            Collection<ExpressionSegment> parameters = new LinkedList<>();
            for (CallArgumentContext each : ctx.callArguments().callArgument()) {
                parameters.add((ExpressionSegment) visit(each));
            }
            result.getParameters().addAll(parameters);
        }
        return result;
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
        return new PostgreSQLDoStatement();
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
    
    @Override
    public ASTNode visitCopyWithTableOrQuery(final CopyWithTableOrQueryContext ctx) {
        PostgreSQLCopyStatement result = new PostgreSQLCopyStatement();
        if (null != ctx.qualifiedName()) {
            result.setTableSegment((SimpleTableSegment) visit(ctx.qualifiedName()));
            if (null != ctx.columnNames()) {
                result.getColumns().addAll(((CollectionValue<ColumnSegment>) visit(ctx.columnNames())).getValue());
            }
        }
        if (null != ctx.preparableStmt()) {
            result.setPrepareStatementQuerySegment(extractPrepareStatementQuerySegmentFromPreparableStmt(ctx.preparableStmt()));
        }
        return result;
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
    
    @Override
    public ASTNode visitCopyWithTableOrQueryBinaryCsv(final CopyWithTableOrQueryBinaryCsvContext ctx) {
        PostgreSQLCopyStatement result = new PostgreSQLCopyStatement();
        if (null != ctx.qualifiedName()) {
            result.setTableSegment((SimpleTableSegment) visit(ctx.qualifiedName()));
            if (null != ctx.columnNames()) {
                result.getColumns().addAll(((CollectionValue<ColumnSegment>) visit(ctx.columnNames())).getValue());
            }
        }
        if (null != ctx.preparableStmt()) {
            result.setPrepareStatementQuerySegment(extractPrepareStatementQuerySegmentFromPreparableStmt(ctx.preparableStmt()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitCopyWithTableBinary(final CopyWithTableBinaryContext ctx) {
        PostgreSQLCopyStatement result = new PostgreSQLCopyStatement();
        if (null != ctx.qualifiedName()) {
            result.setTableSegment((SimpleTableSegment) visit(ctx.qualifiedName()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitCheckpoint(final CheckpointContext ctx) {
        return new PostgreSQLCheckpointStatement();
    }
}
