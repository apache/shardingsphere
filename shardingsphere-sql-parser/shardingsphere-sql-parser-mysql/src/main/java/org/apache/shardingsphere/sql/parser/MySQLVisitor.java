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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.sql.parser.api.SQLVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementBaseVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowCreateTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowIndexContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowColumnsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowTablesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowDatabasesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DescContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AggregationFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AssignmentContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AssignmentValueContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AssignmentValuesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AutoCommitValueContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.BeginTransactionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.BitExprContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.BlobValueContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.BooleanLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.BooleanPrimaryContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CastFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CharFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ColumnNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ColumnNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CommitContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ConvertFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DeleteContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.EscapedTableReferenceContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ExprContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ExtractFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FromSchemaContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FunctionCallContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.GroupConcatFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.IdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.InsertContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.InsertValuesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.IntervalExpressionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.JoinedTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.LiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.MultipleTableNames_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.MultipleTablesClause_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.NumberLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.OnDuplicateKeyClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.OwnerContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ParameterMarkerContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PositionFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PredicateContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RegularFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RollbackContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SavepointContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SchemaNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetAssignmentsClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetAutoCommitContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetTransactionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowLikeContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ShowTableStatusContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SimpleExprContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SingleTableClause_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SpecialFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StringLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SubstringFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableFactorContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableReferenceContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableReferencesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.UnreservedWord_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.UpdateContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.UseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.WeightStringFunctionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.WhereClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.WindowFunctionContext;
import org.apache.shardingsphere.sql.parser.core.constant.AggregationType;
import org.apache.shardingsphere.sql.parser.core.constant.LogicalOperator;
import org.apache.shardingsphere.sql.parser.sql.ASTNode;
import org.apache.shardingsphere.sql.parser.sql.segment.dal.FromSchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dal.ShowLikeSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.column.InsertColumnsSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.complex.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.AggregationDistinctProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.OrPredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateBetweenRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateInRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.DescribeStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.UseStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowIndexStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowTablesStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowDatabasesStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowTableStatusStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dal.dialect.mysql.ShowColumnsStatement;
import org.apache.shardingsphere.sql.parser.sql.segment.tcl.AutoCommitSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.BeginTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.RollbackStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.SavepointStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.SetAutoCommitStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.tcl.SetTransactionStatement;
import org.apache.shardingsphere.sql.parser.sql.value.BooleanValue;
import org.apache.shardingsphere.sql.parser.sql.value.ListValue;
import org.apache.shardingsphere.sql.parser.sql.value.LiteralValue;
import org.apache.shardingsphere.sql.parser.sql.value.NumberValue;
import org.apache.shardingsphere.sql.parser.sql.value.ParameterValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * MySQL visitor.
 *
 * @author panjuan
 */
public final class MySQLVisitor extends MySQLStatementBaseVisitor<ASTNode> implements SQLVisitor {
    
    private int currentParameterIndex;
    
    // DALStatement.g4
    @Override
    public ASTNode visitUse(final UseContext ctx) {
        LiteralValue schema = (LiteralValue) visit(ctx.schemaName());
        UseStatement result = new UseStatement();
        result.setSchema(schema.getLiteral());
        return result;
    }

    @Override
    public ASTNode visitDesc(final DescContext ctx) {
        TableSegment table = (TableSegment) visit(ctx.tableName());
        DescribeStatement result = new DescribeStatement();
        result.setTable(table);
        return result;
    }

    @Override
    public ASTNode visitShowDatabases(final ShowDatabasesContext ctx) {
        ShowDatabasesStatement result = new ShowDatabasesStatement();
        ShowLikeContext showLikeContext = ctx.showLike();
        if (null != showLikeContext) {
            ShowLikeSegment showLikeSegment = (ShowLikeSegment) visit(ctx.showLike());
            result.getAllSQLSegments().add(showLikeSegment);
        }
        return result;
    }

    @Override
    public ASTNode visitShowTables(final ShowTablesContext ctx) {
        ShowTablesStatement result = new ShowTablesStatement();
        FromSchemaContext fromSchemaContext = ctx.fromSchema();
        ShowLikeContext showLikeContext = ctx.showLike();
        if (null != fromSchemaContext) {
            FromSchemaSegment fromSchemaSegment = (FromSchemaSegment) visit(ctx.fromSchema());
            result.getAllSQLSegments().add(fromSchemaSegment);
        }
        if (null != showLikeContext) {
            ShowLikeSegment showLikeSegment = (ShowLikeSegment) visit(ctx.showLike());
            result.getAllSQLSegments().add(showLikeSegment);
        }
        return result;
    }
    
    @Override
    public ASTNode visitShowTableStatus(final ShowTableStatusContext ctx) {
        ShowTableStatusStatement result = new ShowTableStatusStatement();
        FromSchemaContext fromSchemaContext = ctx.fromSchema();
        ShowLikeContext showLikeContext = ctx.showLike();
        if (null != fromSchemaContext) {
            FromSchemaSegment fromSchemaSegment = (FromSchemaSegment) visit(ctx.fromSchema());
            result.getAllSQLSegments().add(fromSchemaSegment);
        }
        if (null != showLikeContext) {
            ShowLikeSegment showLikeSegment = (ShowLikeSegment) visit(ctx.showLike());
            result.getAllSQLSegments().add(showLikeSegment);
        }
        return result;
    }

    @Override
    public ASTNode visitShowColumns(final ShowColumnsContext ctx) {
        ShowColumnsStatement result = new ShowColumnsStatement();
        FromSchemaContext fromSchemaContext = ctx.fromSchema();
        ShowLikeContext showLikeContext = ctx.showLike();
        if (null != fromSchemaContext) {
            FromSchemaSegment fromSchemaSegment = (FromSchemaSegment) visit(ctx.fromSchema());
            result.getAllSQLSegments().add(fromSchemaSegment);
        }
        if (null != showLikeContext) {
            ShowLikeSegment showLikeSegment = (ShowLikeSegment) visit(ctx.showLike());
            result.getAllSQLSegments().add(showLikeSegment);
        }
        return result;
    }

    @Override
    public ASTNode visitShowIndex(final ShowIndexContext ctx) {
        ShowIndexStatement result = new ShowIndexStatement();
        FromSchemaContext fromSchemaContext = ctx.fromSchema();
        if (null != fromSchemaContext) {
            FromSchemaSegment fromSchemaSegment = (FromSchemaSegment) visit(ctx.fromSchema());
            result.getAllSQLSegments().add(fromSchemaSegment);
        }
        return result;
    }

    @Override
    public ASTNode visitShowCreateTable(final ShowCreateTableContext ctx) {
        ShowCreateTableStatement result = new ShowCreateTableStatement();
        TableSegment table = (TableSegment) visit(ctx.tableName());
        result.setTable(table);
        return result;
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
    
    @Override
    public ASTNode visitInsert(final InsertContext ctx) {
        // TODO :FIXME, since there is no segment for insertValuesClause, InsertStatement is created by sub rule.
        InsertStatement result;
        if (null != ctx.insertValuesClause()) {
            result = (InsertStatement) visit(ctx.insertValuesClause());
        } else {
            result = new InsertStatement();
            SetAssignmentSegment segment = (SetAssignmentSegment) visit(ctx.setAssignmentsClause());
            result.setSetAssignment(segment);
            result.getAllSQLSegments().add(segment);
        }
        if (null != ctx.onDuplicateKeyClause()) {
            ListValue<AssignmentSegment> segments = (ListValue<AssignmentSegment>) visit(ctx.onDuplicateKeyClause());
            result.getAllSQLSegments().addAll(segments.getValues());
        }
        TableSegment table = (TableSegment) visit(ctx.tableName());
        result.setTable(table);
        result.getAllSQLSegments().add(table);
        result.setParametersCount(currentParameterIndex);
        return result;
    }
    
    @Override
    public ASTNode visitInsertValuesClause(final InsertValuesClauseContext ctx) {
        InsertStatement result = new InsertStatement();
        if (null != ctx.columnNames()) { 
            InsertColumnsSegment insertColumnsSegment = (InsertColumnsSegment) visit(ctx.columnNames());
            result.setColumns(insertColumnsSegment);
            result.getAllSQLSegments().add(insertColumnsSegment);
        }
        Collection<InsertValuesSegment> insertValuesSegments = createInsertValuesSegments(ctx.assignmentValues());
        result.getValues().addAll(insertValuesSegments);
        result.getAllSQLSegments().addAll(insertValuesSegments);
        return result;
    }
    
    @Override
    public ASTNode visitOnDuplicateKeyClause(final OnDuplicateKeyClauseContext ctx) {
        ListValue<AssignmentSegment> result = new ListValue<>(new LinkedList<AssignmentSegment>());
        for (AssignmentContext each : ctx.assignment()) {
            result.getValues().add((AssignmentSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitUpdate(final UpdateContext ctx) {
        UpdateStatement result = new UpdateStatement();
        ListValue<TableSegment> tables = (ListValue<TableSegment>) visit(ctx.tableReferences());
        SetAssignmentSegment setSegment = (SetAssignmentSegment) visit(ctx.setAssignmentsClause());
        result.getTables().addAll(tables.getValues());
        result.setSetAssignment(setSegment);
        result.getAllSQLSegments().addAll(tables.getValues());
        result.getAllSQLSegments().add(setSegment);
        if (null != ctx.whereClause()) {
            WhereSegment whereSegment = (WhereSegment) visit(ctx.whereClause());
            result.setWhere(whereSegment);
            result.getAllSQLSegments().add(whereSegment);
        }
        result.setParametersCount(currentParameterIndex);
        return result;
    }
    
    @Override
    public ASTNode visitSetAssignmentsClause(final SetAssignmentsClauseContext ctx) {
        Collection<AssignmentSegment> assignments = new LinkedList<>();
        for (AssignmentContext each : ctx.assignment()) {
            assignments.add((AssignmentSegment) visit(each));
        }
        return new SetAssignmentSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), assignments);
    }
    
    @Override
    public ASTNode visitAssignmentValues(final AssignmentValuesContext ctx) {
        List<ExpressionSegment> segments = new LinkedList<>();
        for (AssignmentValueContext each : ctx.assignmentValue()) {
            segments.add((ExpressionSegment) visit(each));
        }
        return new InsertValuesSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), segments);
    }
    
    @Override
    public ASTNode visitAssignment(final AssignmentContext ctx) {
        ColumnSegment column = (ColumnSegment) visitColumnName(ctx.columnName());
        ExpressionSegment value = (ExpressionSegment) visit(ctx.assignmentValue());
        return new AssignmentSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), column, value);
    }
    
    @Override
    public ASTNode visitAssignmentValue(final AssignmentValueContext ctx) {
        ExprContext expr = ctx.expr();
        if (null != expr) {
            return visit(expr);
        }
        return new CommonExpressionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText());
    }
    
    @Override
    public ASTNode visitBlobValue(final BlobValueContext ctx) {
        return new LiteralValue(ctx.STRING_().getText());
    }
    
    @Override
    public ASTNode visitDelete(final DeleteContext ctx) {
        DeleteStatement result = new DeleteStatement();
        if (null != ctx.multipleTablesClause_()) {
            ListValue<TableSegment> tables = (ListValue<TableSegment>) visit(ctx.multipleTablesClause_());
            result.getTables().addAll(tables.getValues());
            result.getAllSQLSegments().addAll(tables.getValues());
        } else {
            TableSegment table = (TableSegment) visit(ctx.singleTableClause_());
            result.getTables().add(table);
            result.getAllSQLSegments().add(table);
        }
        if (null != ctx.whereClause()) {
            WhereSegment where = (WhereSegment) visit(ctx.whereClause());
            result.setWhere(where);
            result.getAllSQLSegments().add(where);
        }
        result.setParametersCount(currentParameterIndex);
        return result;
    }
    
    @Override
    public ASTNode visitSingleTableClause_(final SingleTableClause_Context ctx) {
        TableSegment result = (TableSegment) visit(ctx.tableName());
        if (null != ctx.alias()) {
            result.setAlias(ctx.alias().getText());
        }
        return result;
    }
    
    @Override
    public ASTNode visitMultipleTablesClause_(final MultipleTablesClause_Context ctx) {
        ListValue<TableSegment> result = new ListValue<>(new LinkedList<TableSegment>());
        result.combine((ListValue<TableSegment>) visit(ctx.multipleTableNames_()));
        result.combine((ListValue<TableSegment>) visit(ctx.tableReferences()));
        return result;
    }
    
    @Override
    public ASTNode visitMultipleTableNames_(final MultipleTableNames_Context ctx) {
        ListValue<TableSegment> result = new ListValue<>(new LinkedList<TableSegment>());
        for (TableNameContext each : ctx.tableName()) {
            result.getValues().add((TableSegment) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitTableReferences(final TableReferencesContext ctx) {
        ListValue<TableSegment> result = new ListValue<>(new LinkedList<TableSegment>());
        for (EscapedTableReferenceContext each : ctx.escapedTableReference()) {
            result.combine((ListValue<TableSegment>) visit(each));
        }
        return result;
    }
    
    @Override
    public ASTNode visitEscapedTableReference(final EscapedTableReferenceContext ctx) {
        return visit(ctx.tableReference());
    }
    
    @Override
    public ASTNode visitTableReference(final TableReferenceContext ctx) {
        ListValue<TableSegment> result = new ListValue<>(new LinkedList<TableSegment>());
        if (null != ctx.joinedTable()) {
            for (JoinedTableContext each : ctx.joinedTable()) {
                result.getValues().add((TableSegment) visit(each));
            }
        }
        if (null != ctx.tableFactor()) {
            result.getValues().add((TableSegment) visit(ctx.tableFactor()));
        }
        return result;
    }
    
    @Override
    public ASTNode visitTableFactor(final TableFactorContext ctx) {
        if (null != ctx.tableReferences()) {
            return visit(ctx.tableReferences());
        }
        TableSegment table = (TableSegment) visit(ctx.tableName());
        if (null != ctx.alias()) {
            table.setAlias(ctx.alias().getText());
        }
        return table;
    }
    
    @Override
    public ASTNode visitJoinedTable(final JoinedTableContext ctx) {
        return visit(ctx.tableFactor());
    }
    
    @Override
    public ASTNode visitWhereClause(final WhereClauseContext ctx) {
        WhereSegment result = new WhereSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        result.setParameterMarkerStartIndex(currentParameterIndex);
        ASTNode segment = visit(ctx.expr());
        if (segment instanceof OrPredicateSegment) {
            result.getAndPredicates().addAll(((OrPredicateSegment) segment).getAndPredicates());
        } else if (segment instanceof PredicateSegment) {
            AndPredicate andPredicate = new AndPredicate();
            andPredicate.getPredicates().add((PredicateSegment) segment);
            result.getAndPredicates().add(andPredicate);
        }
        result.setParametersCount(currentParameterIndex);
        return result;
    }
    
    // TCLStatement.g4
    @Override
    public ASTNode visitSetTransaction(final SetTransactionContext ctx) {
        return new SetTransactionStatement();
    }
    
    @Override
    public ASTNode visitSetAutoCommit(final SetAutoCommitContext ctx) {
        SetAutoCommitStatement result = new SetAutoCommitStatement();
        AutoCommitValueContext autoCommitValueContext = ctx.autoCommitValue();
        if (null != autoCommitValueContext) {
            AutoCommitSegment autoCommitSegment = (AutoCommitSegment) visit(ctx.autoCommitValue());
            result.getAllSQLSegments().add(autoCommitSegment);
            result.setAutoCommit(autoCommitSegment.isAutoCommit());
        }
        return result;
    }
    
    @Override
    public ASTNode visitAutoCommitValue(final AutoCommitValueContext ctx) {
        boolean autoCommit = "1".equals(ctx.getText()) || "ON".equals(ctx.getText());
        return new AutoCommitSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), autoCommit);
    }
    
    @Override
    public ASTNode visitBeginTransaction(final BeginTransactionContext ctx) {
        return new BeginTransactionStatement();
    }
    
    @Override
    public ASTNode visitCommit(final CommitContext ctx) {
        return new CommitStatement();
    }
    
    @Override
    public ASTNode visitRollback(final RollbackContext ctx) {
        return new RollbackStatement();
    }
    
    @Override
    public ASTNode visitSavepoint(final SavepointContext ctx) {
        return new SavepointStatement();
    }
    
    // StoreProcedure.g4
    
    // BaseRule.g4
    @Override
    public ASTNode visitSchemaName(final SchemaNameContext ctx) {
        return visit(ctx.identifier());
    }
    
    @Override
    public ASTNode visitTableName(final TableNameContext ctx) {
        LiteralValue tableName = (LiteralValue) visit(ctx.name());
        TableSegment result = new TableSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), tableName.getLiteral());
        OwnerContext owner = ctx.owner();
        if (null != owner) {
            result.setOwner(createSchemaSegment(owner));
        }
        return result;
    }
    
    @Override
    public ASTNode visitColumnNames(final ColumnNamesContext ctx) {
        Collection<ColumnSegment> segments = new LinkedList<>();
        for (ColumnNameContext each : ctx.columnName()) {
            segments.add((ColumnSegment) visit(each));
        }
        InsertColumnsSegment result = new InsertColumnsSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex());
        result.getColumns().addAll(segments);
        return result;
    }
    
    @Override
    public ASTNode visitColumnName(final ColumnNameContext ctx) {
        LiteralValue columnName = (LiteralValue) visit(ctx.name());
        ColumnSegment result = new ColumnSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), columnName.getLiteral());
        OwnerContext owner = ctx.owner();
        if (null != owner) {
            result.setOwner(createTableSegment(owner));
        }
        return result;
    }
    
    @Override
    public ASTNode visitExpr(final ExprContext ctx) {
        BooleanPrimaryContext bool = ctx.booleanPrimary();
        if (null != bool) {
            return visit(bool);
        } else if (null != ctx.logicalOperator()) {
            return mergePredicateSegment(visit(ctx.expr(0)), visit(ctx.expr(1)), ctx.logicalOperator().getText());
        } else if (!ctx.expr().isEmpty()) {
            return visit(ctx.expr(0));
        }
        return createExpressionSegment(new LiteralValue(ctx.getText()), ctx);
    }
    
    @Override
    public ASTNode visitBooleanPrimary(final BooleanPrimaryContext ctx) {
        if (null != ctx.subquery()) {
            return new SubquerySegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.subquery().getText());
        }
        if (null != ctx.comparisonOperator()) {
            return createCompareSegment(ctx);
        }
        if (null != ctx.predicate()) {
            return visit(ctx.predicate());
        }
        return createExpressionSegment(new LiteralValue(ctx.getText()), ctx);
    }
    
    @Override
    public ASTNode visitPredicate(final PredicateContext ctx) {
        if (null != ctx.subquery()) {
            return new SubquerySegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.subquery().getText());
        }
        if (null != ctx.IN()) {
            return createInSegment(ctx);
        }
        if (null != ctx.BETWEEN()) {
            createBetweenSegment(ctx);
        }
        BitExprContext bitExpr = ctx.bitExpr(0);
        if (null != bitExpr) {
            return createExpressionSegment(visit(bitExpr), ctx);
        }
        return createExpressionSegment(new LiteralValue(ctx.getText()), ctx);
    }
    
    @Override
    public ASTNode visitBitExpr(final BitExprContext ctx) {
        SimpleExprContext simple = ctx.simpleExpr();
        if (null != simple) {
            return visit(simple);
        }
        return new LiteralValue(ctx.getText());
    }
    
    @Override
    public ASTNode visitSimpleExpr(final SimpleExprContext ctx) {
        if (null != ctx.subquery()) {
            return new SubquerySegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.subquery().getText());
        }
        if (null != ctx.parameterMarker()) {
            return visit(ctx.parameterMarker());
        }
        if (null != ctx.literals()) {
            return visit(ctx.literals());
        }
        if (null != ctx.intervalExpression()) {
            return visit(ctx.intervalExpression());
        }
        if (null != ctx.functionCall()) {
            return visit(ctx.functionCall());
        }
        if (null != ctx.columnName()) {
            return visit(ctx.columnName());
        }
        return new CommonExpressionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText());
    }
    
    @Override
    public ASTNode visitParameterMarker(final ParameterMarkerContext ctx) {
        return new ParameterValue(currentParameterIndex++);
    }
    
    @Override
    public ASTNode visitLiterals(final LiteralsContext ctx) {
        if (null != ctx.stringLiterals()) {
            return visit(ctx.stringLiterals());
        }
        if (null != ctx.numberLiterals()) {
            return visit(ctx.numberLiterals());
        }
        if (null != ctx.booleanLiterals()) {
            return visit(ctx.booleanLiterals());
        }
        if (null != ctx.nullValueLiterals()) {
            return new CommonExpressionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText());
        }
        return new LiteralValue(ctx.getText());
    }
    
    @Override
    public ASTNode visitStringLiterals(final StringLiteralsContext ctx) {
        String text = ctx.getText();
        return new LiteralValue(text.substring(1, text.length() - 1));
    }
    
    @Override
    public ASTNode visitNumberLiterals(final NumberLiteralsContext ctx) {
        return new NumberValue(ctx.getText());
    }
    
    @Override
    public ASTNode visitBooleanLiterals(final BooleanLiteralsContext ctx) {
        return new BooleanValue(ctx.getText());
    }
    
    @Override
    public ASTNode visitIntervalExpression(final IntervalExpressionContext ctx) {
        calculateParameterCount(Collections.singleton(ctx.expr()));
        return new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText());
    }
    
    @Override
    public ASTNode visitFunctionCall(final FunctionCallContext ctx) {
        if (null != ctx.aggregationFunction()) {
            return visit(ctx.aggregationFunction());
        }
        if (null != ctx.regularFunction()) {
            return visit(ctx.regularFunction());
        }
        if (null != ctx.specialFunction()) {
            return visit(ctx.specialFunction());
        }
        return new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText());
    }
    
    @Override
    public ASTNode visitAggregationFunction(final AggregationFunctionContext ctx) {
        if (AggregationType.isAggregationType(ctx.aggregationFunctionName_().getText())) {
            return createAggregationSegment(ctx);
        }
        return new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText());
    }
    
    @Override
    public ASTNode visitSpecialFunction(final SpecialFunctionContext ctx) {
        if (null != ctx.groupConcatFunction()) {
            return visit(ctx.groupConcatFunction());
        }
        if (null != ctx.windowFunction()) {
            return visit(ctx.windowFunction());
        }
        if (null != ctx.castFunction()) {
            return visit(ctx.castFunction());
        }
        if (null != ctx.convertFunction()) {
            return visit(ctx.convertFunction());
        }
        if (null != ctx.positionFunction()) {
            return visit(ctx.positionFunction());
        }
        if (null != ctx.substringFunction()) {
            return visit(ctx.substringFunction());
        }
        if (null != ctx.extractFunction()) {
            return visit(ctx.extractFunction());
        }
        if (null != ctx.charFunction()) {
            return visit(ctx.charFunction());
        }
        if (null != ctx.weightStringFunction()) {
            return visit(ctx.weightStringFunction());
        }
        return new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText());
    }
    
    @Override
    public ASTNode visitGroupConcatFunction(final GroupConcatFunctionContext ctx) {
        calculateParameterCount(ctx.expr());
        return new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText());
    }
    
    @Override
    public ASTNode visitWindowFunction(final WindowFunctionContext ctx) {
        calculateParameterCount(ctx.expr());
        return new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText());
    }
    
    @Override
    public ASTNode visitCastFunction(final CastFunctionContext ctx) {
        calculateParameterCount(Collections.singleton(ctx.expr()));
        return new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText());
    }
    
    @Override
    public ASTNode visitConvertFunction(final ConvertFunctionContext ctx) {
        calculateParameterCount(Collections.singleton(ctx.expr()));
        return new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText());
    }
    
    @Override
    public ASTNode visitPositionFunction(final PositionFunctionContext ctx) {
        calculateParameterCount(ctx.expr());
        return new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText());
    }
    
    @Override
    public ASTNode visitSubstringFunction(final SubstringFunctionContext ctx) {
        calculateParameterCount(Collections.singleton(ctx.expr()));
        return new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText());
    }
    
    @Override
    public ASTNode visitExtractFunction(final ExtractFunctionContext ctx) {
        calculateParameterCount(Collections.singleton(ctx.expr()));
        return new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText());
    }
    
    @Override
    public ASTNode visitCharFunction(final CharFunctionContext ctx) {
        calculateParameterCount(ctx.expr());
        return new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText());
    }
    
    @Override
    public ASTNode visitWeightStringFunction(final WeightStringFunctionContext ctx) {
        calculateParameterCount(Collections.singleton(ctx.expr()));
        return new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText());
    }
    
    @Override
    public ASTNode visitRegularFunction(final RegularFunctionContext ctx) {
        calculateParameterCount(ctx.expr());
        return new ExpressionProjectionSegment(ctx.start.getStartIndex(), ctx.stop.getStopIndex(), ctx.getText());
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
    
    // Segments
    private SchemaSegment createSchemaSegment(final OwnerContext ownerContext) {
        LiteralValue literalValue = (LiteralValue) visit(ownerContext.identifier());
        return new SchemaSegment(ownerContext.getStart().getStartIndex(), ownerContext.getStop().getStopIndex(), literalValue.getLiteral());
    }
    
    private TableSegment createTableSegment(final OwnerContext ownerContext) {
        LiteralValue literalValue = (LiteralValue) visit(ownerContext.identifier());
        return new TableSegment(ownerContext.getStart().getStartIndex(), ownerContext.getStop().getStopIndex(), literalValue.getLiteral());
    }
    
    private ASTNode createExpressionSegment(final ASTNode astNode, final ParserRuleContext context) {
        if (astNode instanceof LiteralValue) {
            return new LiteralExpressionSegment(context.start.getStartIndex(), context.stop.getStopIndex(), ((LiteralValue) astNode).getLiteral());
        }
        if (astNode instanceof NumberValue) {
            return new LiteralExpressionSegment(context.start.getStartIndex(), context.stop.getStopIndex(), ((NumberValue) astNode).getNumber());
        }
        if (astNode instanceof ParameterValue) {
            return new ParameterMarkerExpressionSegment(context.start.getStartIndex(), context.stop.getStopIndex(), ((ParameterValue) astNode).getParameterIndex());
        }
        return astNode;
    }
    
    private Collection<InsertValuesSegment> createInsertValuesSegments(final Collection<AssignmentValuesContext> assignmentValuesContexts) {
        Collection<InsertValuesSegment> result = new LinkedList<>();
        for (AssignmentValuesContext each : assignmentValuesContexts) {
            result.add((InsertValuesSegment) visit(each));
        }
        return result;
    }
    
    private ASTNode createAggregationSegment(final AggregationFunctionContext ctx) {
        AggregationType type = AggregationType.valueOf(ctx.aggregationFunctionName_().getText());
        int innerExpressionStartIndex = ((TerminalNode) ctx.getChild(1)).getSymbol().getStartIndex();
        if (null != ctx.distinct()) {
            return new AggregationDistinctProjectionSegment(ctx.getStart().getStartIndex(),
                    ctx.getStop().getStopIndex(), ctx.getText(), type, innerExpressionStartIndex, getDistinctExpression(ctx));
        }
        return new AggregationProjectionSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                ctx.getText(), type, innerExpressionStartIndex);
    }
    
    private String getDistinctExpression(final AggregationFunctionContext ctx) {
        StringBuilder result = new StringBuilder();
        for (int i = 3; i < ctx.getChildCount() - 1; i++) {
            result.append(ctx.getChild(i).getText());
        }
        return result.toString();
    }
    
    private ASTNode createCompareSegment(final BooleanPrimaryContext ctx) {
        ASTNode leftValue = visit(ctx.booleanPrimary());
        ASTNode rightValue = visit(ctx.predicate());
        if (rightValue instanceof ColumnSegment) {
            return new PredicateSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), (ColumnSegment) leftValue, (ColumnSegment) rightValue);
        }
        return new PredicateSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(),
                (ColumnSegment) leftValue, new PredicateCompareRightValue(ctx.comparisonOperator().getText(), (ExpressionSegment) rightValue));
    }
    
    private ASTNode createInSegment(final PredicateContext ctx) {
        ColumnSegment column = (ColumnSegment) visit(ctx.bitExpr(0));
        Collection<ExpressionSegment> segments = Lists.transform(ctx.expr(), new Function<ExprContext, ExpressionSegment>() {
            
            @Override
            public ExpressionSegment apply(final ExprContext input) {
                return (ExpressionSegment) visit(input);
            }
        });
        return new PredicateSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, new PredicateInRightValue(segments));
    }
    
    private void createBetweenSegment(final PredicateContext ctx) {
        ColumnSegment column = (ColumnSegment) visit(ctx.bitExpr(0));
        ExpressionSegment between = (ExpressionSegment) visit(ctx.bitExpr(1));
        ExpressionSegment and = (ExpressionSegment) visit(ctx.predicate());
        new PredicateSegment(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex(), column, new PredicateBetweenRightValue(between, and));
    }
    
    private OrPredicateSegment mergePredicateSegment(final ASTNode left, final ASTNode right, final String operator) {
        Optional<LogicalOperator> logicalOperator = LogicalOperator.valueFrom(operator);
        Preconditions.checkState(logicalOperator.isPresent());
        if (LogicalOperator.OR == logicalOperator.get()) {
            return mergeOrPredicateSegment(left, right);
        }
        return mergeAndPredicateSegment(left, right);
    }
    
    private OrPredicateSegment mergeOrPredicateSegment(final ASTNode left, final ASTNode right) {
        OrPredicateSegment result = new OrPredicateSegment();
        result.getAndPredicates().addAll(getAndPredicates(left));
        result.getAndPredicates().addAll(getAndPredicates(right));
        return result;
    }
    
    private OrPredicateSegment mergeAndPredicateSegment(final ASTNode left, final ASTNode right) {
        OrPredicateSegment result = new OrPredicateSegment();
        for (AndPredicate eachLeft : getAndPredicates(left)) {
            for (AndPredicate eachRight : getAndPredicates(right)) {
                result.getAndPredicates().add(createAndPredicate(eachLeft, eachRight));
            }
        }
        return result;
    }
    
    private AndPredicate createAndPredicate(final AndPredicate left, final AndPredicate right) {
        AndPredicate result = new AndPredicate();
        result.getPredicates().addAll(left.getPredicates());
        result.getPredicates().addAll(right.getPredicates());
        return result;
    }
    
    private Collection<AndPredicate> getAndPredicates(final ASTNode astNode) {
        if (astNode instanceof OrPredicateSegment) {
            return ((OrPredicateSegment) astNode).getAndPredicates();
        }
        if (astNode instanceof AndPredicate) {
            return Collections.singleton((AndPredicate) astNode);
        }
        AndPredicate andPredicate = new AndPredicate();
        andPredicate.getPredicates().add((PredicateSegment) astNode);
        return Collections.singleton(andPredicate);
    }
    
    // TODO :FIXME, sql case id: insert_with_str_to_date
    private void calculateParameterCount(final Collection<ExprContext> exprContexts) {
        for (ExprContext each : exprContexts) {
            visit(each);
        }
    }
}
