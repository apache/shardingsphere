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

package org.apache.shardingsphere.sql.parser.mysql.visitor.format;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.sql.parser.api.visitor.format.SQLFormatVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementBaseVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AliasContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterCommandListContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterListContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterTableActionsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AssignmentValuesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ColumnNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CombineClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateTableOptionsSpaceSeparatedContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CteClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DataTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DerivedColumnsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ExprContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FieldLengthContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.IdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.InsertContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.InsertSelectClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.InsertValuesClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.LiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.NumberLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.OnDuplicateKeyClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PartitionNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PrecisionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ProjectionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.QueryExpressionBodyContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.QueryExpressionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.QueryExpressionParensContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.QuerySpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RowConstructorListContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SelectContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SetAssignmentsClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StandaloneAlterTableActionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StringListContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StringLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.String_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SystemVariableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableElementListContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableStatementContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableValueConstructorContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TemporalLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TypeDatetimePrecisionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.UserVariableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.WhereClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.WithClauseContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

/**
 * SQL format visitor for MySQL.
 */
public final class MySQLFormatVisitor extends MySQLStatementBaseVisitor<String> implements SQLFormatVisitor {
    
    private static final Collection<Class<? extends ParserRuleContext>> DATA_TYPE_EXTRA_DESCRIPTION_CONTEXT_CLASSES = new HashSet<>(
            Arrays.asList(FieldLengthContext.class, PrecisionContext.class, StringListContext.class, TypeDatetimePrecisionContext.class));
    
    private final StringBuilder formattedSQL = new StringBuilder(256);
    
    private boolean upperCase = true;
    
    private boolean parameterized = true;
    
    private int projectionsCountOfLine = 3;
    
    private int indentCount;
    
    @Override
    public void init(final Properties props) {
        if (null != props) {
            upperCase = Boolean.parseBoolean(props.getProperty("upperCase", Boolean.TRUE.toString()));
            parameterized = Boolean.parseBoolean(props.getProperty("parameterized", Boolean.TRUE.toString()));
            projectionsCountOfLine = Integer.parseInt(props.getProperty("projectionsCountOfLine", "3"));
        }
    }
    
    @Override
    public String visitSelect(final SelectContext ctx) {
        if (null == ctx.queryExpression()) {
            visit(null == ctx.queryExpressionParens() ? ctx.selectWithInto() : ctx.queryExpressionParens());
        } else {
            visit(ctx.queryExpression());
            if (null != ctx.lockClauseList()) {
                formattedSQL.append(' ');
                visit(ctx.lockClauseList());
            }
        }
        formatPrint(';');
        return formattedSQL.toString();
    }
    
    @Override
    public String visitQueryExpression(final QueryExpressionContext ctx) {
        if (null != ctx.withClause()) {
            visit(ctx.withClause());
            formatPrint(' ');
        }
        visit(null == ctx.queryExpressionBody() ? ctx.queryExpressionParens() : ctx.queryExpressionBody());
        if (null != ctx.orderByClause()) {
            formatPrint(' ');
            visit(ctx.orderByClause());
        }
        if (null != ctx.limitClause()) {
            formatPrint(' ');
            visit(ctx.limitClause());
        }
        return formattedSQL.toString();
    }
    
    @Override
    public String visitQueryExpressionParens(final QueryExpressionParensContext ctx) {
        formatPrintln();
        indentCount++;
        formatPrint('(');
        formatPrintln();
        if (null == ctx.queryExpressionParens()) {
            visit(ctx.queryExpression());
            if (null != ctx.lockClauseList()) {
                formattedSQL.append(' ');
                visit(ctx.lockClauseList());
            }
        } else {
            visit(ctx.queryExpressionParens());
        }
        indentCount--;
        formatPrintln();
        formatPrint(')');
        return formattedSQL.toString();
    }
    
    @Override
    public String visitQueryExpressionBody(final QueryExpressionBodyContext ctx) {
        if (1 == ctx.getChildCount()) {
            visit(ctx.queryPrimary());
        } else if (null == ctx.queryExpressionParens()) {
            visit(ctx.queryExpressionBody());
            visit(ctx.combineClause());
        } else {
            visit(ctx.queryExpressionParens());
            visit(ctx.combineClause());
        }
        return formattedSQL.toString();
    }
    
    @Override
    public String visitCombineClause(final CombineClauseContext ctx) {
        formattedSQL.append("\nUNION\n");
        if (null != ctx.combineOption()) {
            visit(ctx.combineOption());
            formattedSQL.append(' ');
        }
        visit(null == ctx.queryPrimary() ? ctx.queryExpressionParens() : ctx.queryPrimary());
        return formattedSQL.toString();
    }
    
    @Override
    public String visitQuerySpecification(final QuerySpecificationContext ctx) {
        formatPrint("SELECT ");
        int selectSpecCount = ctx.selectSpecification().size();
        for (int i = 0; i < selectSpecCount; i++) {
            visit(ctx.selectSpecification(i));
            formatPrint(' ');
        }
        visit(ctx.projections());
        if (null != ctx.fromClause()) {
            formatPrintln();
            visit(ctx.fromClause());
        }
        if (null != ctx.whereClause()) {
            formatPrintln();
            visit(ctx.whereClause());
        }
        if (null != ctx.groupByClause()) {
            formatPrintln();
            visit(ctx.groupByClause());
        }
        if (null != ctx.havingClause()) {
            formatPrintln();
            visit(ctx.havingClause());
        }
        if (null != ctx.windowClause()) {
            formatPrintln();
            visit(ctx.windowClause());
        }
        return formattedSQL.toString();
    }
    
    @Override
    public String visitTableStatement(final TableStatementContext ctx) {
        formatPrint("TABLE ");
        visit(ctx.tableName());
        return formattedSQL.toString();
    }
    
    @Override
    public String visitInsert(final InsertContext ctx) {
        visit(ctx.INSERT());
        formatPrint(' ');
        visit(ctx.insertSpecification());
        formatPrint(' ');
        if (null != ctx.INTO()) {
            visit(ctx.INTO());
            formatPrint(' ');
        }
        visit(ctx.tableName());
        formatPrint(' ');
        if (null != ctx.partitionNames()) {
            formatPrintln();
            visit(ctx.partitionNames());
        }
        if (null != ctx.insertValuesClause()) {
            visit(ctx.insertValuesClause());
        } else if (null != ctx.insertSelectClause()) {
            visit(ctx.insertSelectClause());
        } else {
            visit(ctx.setAssignmentsClause());
        }
        if (null != ctx.onDuplicateKeyClause()) {
            formatPrintln();
            visit(ctx.onDuplicateKeyClause());
        }
        return formattedSQL.toString();
    }
    
    @Override
    public String visitPartitionNames(final PartitionNamesContext ctx) {
        visit(ctx.PARTITION());
        formatPrintln(" (");
        int identifierCount = ctx.identifier().size();
        for (int i = 0; i < identifierCount; i++) {
            if (0 != i) {
                formatPrint(" ,");
            }
            visit(ctx.identifier(i));
        }
        formatPrint(')');
        return formattedSQL.toString();
    }
    
    @Override
    public String visitInsertValuesClause(final InsertValuesClauseContext ctx) {
        if (null != ctx.LP_()) {
            formatPrint('(');
            if (null != ctx.fields()) {
                visit(ctx.fields());
            }
            formatPrint(')');
        }
        formatPrintln();
        visit(null == ctx.VALUE() ? ctx.VALUES() : ctx.VALUE());
        indentCount++;
        formatPrintln();
        if (!ctx.assignmentValues().isEmpty()) {
            int valueCount = ctx.assignmentValues().size();
            for (int i = 0; i < valueCount; i++) {
                if (0 != i) {
                    formatPrint(',');
                    formatPrintln();
                }
                visit(ctx.assignmentValues(i));
            }
        }
        if (null != ctx.rowConstructorList()) {
            indentCount++;
            visit(ctx.rowConstructorList());
            indentCount--;
        }
        indentCount--;
        if (null != ctx.valueReference()) {
            formatPrintln();
            visit(ctx.valueReference());
        }
        return formattedSQL.toString();
    }
    
    @Override
    public String visitInsertSelectClause(final InsertSelectClauseContext ctx) {
        if (null != ctx.valueReference()) {
            visit(ctx.valueReference());
            formatPrint(' ');
        }
        if (null != ctx.LP_()) {
            formatPrint('(');
            if (null != ctx.fields()) {
                visit(ctx.fields());
            }
            formatPrint(") ");
        }
        formatPrintln();
        visit(ctx.select());
        return formattedSQL.toString();
    }
    
    @Override
    public String visitSetAssignmentsClause(final SetAssignmentsClauseContext ctx) {
        if (null != ctx.valueReference()) {
            visit(ctx.valueReference());
            formatPrint(' ');
        }
        indentCount++;
        visit(ctx.SET());
        formatPrint(' ');
        int assignmentCount = ctx.assignment().size();
        for (int i = 0; i < assignmentCount; i++) {
            if (0 != i) {
                formatPrintln(",");
            }
            visit(ctx.assignment(i));
        }
        indentCount--;
        return formattedSQL.toString();
    }
    
    @Override
    public String visitDerivedColumns(final DerivedColumnsContext ctx) {
        formatPrint("(");
        int aliasCount = ctx.alias().size();
        for (int i = 0; i < aliasCount; i++) {
            if (0 != i) {
                formatPrint(", ");
            }
            visit(ctx.alias(i));
        }
        formatPrint(")");
        return formattedSQL.toString();
    }
    
    @Override
    public String visitOnDuplicateKeyClause(final OnDuplicateKeyClauseContext ctx) {
        visit(ctx.ON());
        formatPrint(" ");
        visit(ctx.DUPLICATE());
        formatPrint(" ");
        visit(ctx.KEY());
        formatPrint(" ");
        visit(ctx.UPDATE());
        formatPrint(" ");
        indentCount++;
        int assignmentCount = ctx.assignment().size();
        for (int i = 0; i < assignmentCount; i++) {
            if (0 != i) {
                formatPrintln();
            }
            visit(ctx.assignment(i));
        }
        indentCount--;
        return formattedSQL.toString();
    }
    
    @Override
    public String visitTableName(final TableNameContext ctx) {
        if (null != ctx.owner()) {
            formatPrint(ctx.owner().getText());
            formatPrint(".");
        }
        formatPrint(ctx.name().getText());
        return formattedSQL.toString();
    }
    
    @Override
    public String visitTableValueConstructor(final TableValueConstructorContext ctx) {
        formatPrint("VALUES ");
        visit(ctx.rowConstructorList());
        return formattedSQL.toString();
    }
    
    @Override
    public String visitAlterTable(final AlterTableContext ctx) {
        visit(ctx.ALTER());
        formatPrint(" ");
        visit(ctx.TABLE());
        formatPrint(" ");
        visit(ctx.tableName());
        if (null != ctx.alterTableActions()) {
            indentCount++;
            formatPrintln();
            visit(ctx.alterTableActions());
            indentCount--;
        } else if (null != ctx.standaloneAlterTableAction()) {
            indentCount++;
            formatPrintln();
            visit(ctx.standaloneAlterTableAction());
            indentCount--;
        }
        return formattedSQL.toString();
    }
    
    @Override
    public String visitAlterTableActions(final AlterTableActionsContext ctx) {
        if (null != ctx.alterCommandList()) {
            visit(ctx.alterCommandList());
            if (null != ctx.alterTablePartitionOptions()) {
                formatPrintln();
                visit(ctx.alterTablePartitionOptions());
            }
        } else {
            visit(ctx.alterTablePartitionOptions());
        }
        return formattedSQL.toString();
    }
    
    @Override
    public String visitAlterCommandList(final AlterCommandListContext ctx) {
        if (null != ctx.alterCommandsModifierList()) {
            visit(ctx.alterCommandsModifierList());
            if (null != ctx.alterList()) {
                formatPrintln(",");
                visit(ctx.alterList());
            }
        } else if (null != ctx.alterList()) {
            visit(ctx.alterList());
        }
        return formattedSQL.toString();
    }
    
    @Override
    public String visitAlterList(final AlterListContext ctx) {
        int childCount = ctx.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ParseTree child = ctx.getChild(i);
            if (i == 0) {
                child.accept(this);
            } else {
                if (child instanceof TerminalNode) {
                    formatPrintln(",");
                } else {
                    child.accept(this);
                }
            }
        }
        return formattedSQL.toString();
    }
    
    @Override
    public String visitCreateTableOptionsSpaceSeparated(final CreateTableOptionsSpaceSeparatedContext ctx) {
        int childCount = ctx.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (0 != i) {
                formatPrintln();
            }
            visit(ctx.getChild(i));
        }
        return formattedSQL.toString();
    }
    
    @Override
    public String visitStandaloneAlterTableAction(final StandaloneAlterTableActionContext ctx) {
        if (null != ctx.alterCommandsModifierList()) {
            visit(ctx.alterCommandsModifierList());
            formatPrintln(",");
        }
        visit(ctx.standaloneAlterCommands());
        return formattedSQL.toString();
    }
    
    @Override
    public String visitRowConstructorList(final RowConstructorListContext ctx) {
        int rowCount = ctx.assignmentValues().size();
        for (int i = 0; i < rowCount; i++) {
            if (0 != i) {
                formatPrintln(",");
            }
            visit(ctx.ROW(i));
            formatPrint(" ");
            visit(ctx.assignmentValues(i));
        }
        return formattedSQL.toString();
    }
    
    @Override
    public String visitAssignmentValues(final AssignmentValuesContext ctx) {
        formatPrint("(");
        int assignCount = ctx.assignmentValue().size();
        for (int i = 0; i < assignCount; i++) {
            if (i != 0) {
                formatPrint(", ");
            }
            visit(ctx.assignmentValue(i));
        }
        formatPrint(")");
        return formattedSQL.toString();
    }
    
    @Override
    public String visitWhereClause(final WhereClauseContext ctx) {
        visit(ctx.WHERE());
        formatPrint(" ");
        indentCount++;
        formatPrintln();
        visit(ctx.expr());
        indentCount--;
        return formattedSQL.toString();
    }
    
    @Override
    public String visitExpr(final ExprContext ctx) {
        if (null != ctx.andOperator()) {
            visitLogicalOperator(ctx, ctx.andOperator().getText());
        } else if (null != ctx.orOperator()) {
            visitLogicalOperator(ctx, ctx.orOperator().getText());
        } else if (null != ctx.notOperator()) {
            formatPrint(ctx.notOperator().getText());
            visit(ctx.expr(0));
        } else {
            visitChildren(ctx);
        }
        return formattedSQL.toString();
    }
    
    private void visitLogicalOperator(final ExprContext ctx, final String operator) {
        ExprContext left = ctx.expr(0);
        visit(left);
        formatPrintln();
        ExprContext right = ctx.expr(1);
        formatPrint(operator);
        formatPrint(" ");
        visit(right);
    }
    
    @Override
    public String visitAlias(final AliasContext ctx) {
        formatPrint(ctx.getText());
        return formattedSQL.toString();
    }
    
    @Override
    public String visitProjections(final ProjectionsContext ctx) {
        indentCount++;
        if (null != ctx.unqualifiedShorthand()) {
            visit(ctx.unqualifiedShorthand());
            formattedSQL.append(' ');
        }
        int projectionCount = ctx.projection().size();
        int lineItemCount = 0;
        for (int i = 0; i < projectionCount; i++) {
            if (0 != i) {
                formattedSQL.append(", ");
                if (lineItemCount >= projectionsCountOfLine) {
                    lineItemCount = 0;
                    formatPrintln();
                }
            }
            visit(ctx.projection(i));
            lineItemCount++;
        }
        indentCount--;
        return formattedSQL.toString();
    }
    
    @Override
    public String visitProjection(final ProjectionContext ctx) {
        if (null != ctx.expr()) {
            visit(ctx.expr());
            formatPrint(" ");
        }
        if (null != ctx.AS()) {
            formatPrint("AS ");
        }
        if (null != ctx.alias()) {
            visit(ctx.alias());
        }
        if (null != ctx.qualifiedShorthand()) {
            visit(ctx.qualifiedShorthand());
        }
        return formattedSQL.toString();
    }
    
    @Override
    public String visitCreateDefinitionClause(final CreateDefinitionClauseContext ctx) {
        indentCount++;
        formatPrint("(");
        formatPrintln();
        visit(ctx.tableElementList());
        formatPrint("\n");
        formatPrint(")");
        indentCount--;
        return formattedSQL.toString();
    }
    
    @Override
    public String visitTableElementList(final TableElementListContext ctx) {
        int tableElementCount = ctx.tableElement().size();
        for (int i = 0; i < tableElementCount; i++) {
            if (0 != i) {
                formatPrintln(",");
            }
            visit(ctx.tableElement(i));
        }
        return formattedSQL.toString();
    }
    
    @Override
    public String visitFieldLength(final FieldLengthContext ctx) {
        formatPrint("(");
        formatPrint(ctx.NUMBER_().getText());
        formatPrint(")");
        return formattedSQL.toString();
    }
    
    @Override
    public String visitPrecision(final PrecisionContext ctx) {
        formatPrint("(");
        formatPrint(ctx.NUMBER_(0).getText());
        formatPrint(", ");
        formatPrint(ctx.NUMBER_(1).getText());
        formatPrint(")");
        return super.visitPrecision(ctx);
    }
    
    @Override
    public String visitTypeDatetimePrecision(final TypeDatetimePrecisionContext ctx) {
        formatPrint("(");
        formatPrint(ctx.NUMBER_().getText());
        formatPrint(")");
        return formattedSQL.toString();
    }
    
    @Override
    public String visitDataType(final DataTypeContext ctx) {
        int childCount = ctx.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ParseTree child = ctx.getChild(i);
            if (0 != i && !DATA_TYPE_EXTRA_DESCRIPTION_CONTEXT_CLASSES.contains(child.getClass())) {
                formatPrint(" ");
            }
            child.accept(this);
        }
        return formattedSQL.toString();
    }
    
    @Override
    public String visitStringList(final StringListContext ctx) {
        int stringCount = ctx.textString().size();
        formatPrint("(");
        for (int i = 0; i < stringCount; i++) {
            if (0 == i) {
                formatPrint(ctx.textString(i).getText());
            } else {
                formatPrintln(",");
                visit(ctx.textString(i));
            }
        }
        formatPrint(")");
        return formattedSQL.toString();
    }
    
    @Override
    public String visitUserVariable(final UserVariableContext ctx) {
        formatPrint("@");
        visit(ctx.textOrIdentifier());
        return formattedSQL.toString();
    }
    
    @Override
    public String visitSystemVariable(final SystemVariableContext ctx) {
        formatPrint("@@");
        if (null != ctx.systemVariableScope) {
            formatPrint(upperCase ? ctx.systemVariableScope.getText().toUpperCase() : ctx.systemVariableScope.getText().toLowerCase());
            formatPrint(".");
        }
        visit(ctx.rvalueSystemVariable().textOrIdentifier());
        if (null != ctx.rvalueSystemVariable().DOT_()) {
            formatPrint(".");
            visit(ctx.rvalueSystemVariable().identifier());
        }
        return formattedSQL.toString();
    }
    
    @Override
    public String visitTerminal(final TerminalNode node) {
        if ("<EOF>".equals(node.getText())) {
            return formattedSQL.toString();
        }
        formatPrint(upperCase ? node.getText().toUpperCase() : node.getText().toLowerCase());
        return formattedSQL.toString();
    }
    
    @Override
    public String visitIdentifier(final IdentifierContext ctx) {
        formatPrint(ctx.getText());
        return formattedSQL.toString();
    }
    
    @Override
    public String visitLiterals(final LiteralsContext ctx) {
        if (parameterized) {
            formatPrint("?");
        } else {
            super.visitLiterals(ctx);
        }
        return formattedSQL.toString();
    }
    
    @Override
    public String visitTemporalLiterals(final TemporalLiteralsContext ctx) {
        visit(ctx.getChild(0));
        formatPrint(ctx.textString().getText());
        return formattedSQL.toString();
    }
    
    @Override
    public String visitStringLiterals(final StringLiteralsContext ctx) {
        if (parameterized) {
            formatPrint("?");
            return formattedSQL.toString();
        }
        if (null == ctx.string_()) {
            visit(ctx.NCHAR_TEXT());
        } else {
            if (null != ctx.UNDERSCORE_CHARSET()) {
                formatPrint(ctx.UNDERSCORE_CHARSET().getText());
            }
            visit(ctx.string_());
        }
        return formattedSQL.toString();
    }
    
    @Override
    public String visitString_(final String_Context ctx) {
        formatPrint(ctx.getText());
        return formattedSQL.toString();
    }
    
    @Override
    public String visitNumberLiterals(final NumberLiteralsContext ctx) {
        formatPrint(parameterized ? "?" : ctx.getText());
        return formattedSQL.toString();
    }
    
    @Override
    public String visitWithClause(final WithClauseContext ctx) {
        formatPrint("WITH ");
        if (null != ctx.RECURSIVE()) {
            visit(ctx.RECURSIVE());
            formattedSQL.append(' ');
        }
        for (int i = 0; i < ctx.cteClause().size(); i++) {
            if (i != 0 && i < ctx.cteClause().size() - 1) {
                formattedSQL.append(", ");
            }
            visit(ctx.cteClause(i));
        }
        if (null != ctx.parent) {
            formattedSQL.append('\n');
        }
        return formattedSQL.toString();
    }
    
    @Override
    public String visitCteClause(final CteClauseContext ctx) {
        visit(ctx.identifier());
        formattedSQL.append(' ');
        if (null != ctx.columnNames()) {
            visit(ctx.columnNames());
            formattedSQL.append(' ');
        }
        formattedSQL.append("AS ");
        visit(ctx.subquery());
        return formattedSQL.toString();
    }
    
    @Override
    public String visitColumnNames(final ColumnNamesContext ctx) {
        int columnCount = ctx.columnName().size();
        for (int i = 0; i < columnCount; i++) {
            if (i != 0 && i < columnCount - 1) {
                formattedSQL.append(", ");
            } else {
                visit(ctx.columnName(i));
            }
        }
        return formattedSQL.toString();
    }
    
    @Override
    public String visitChildren(final RuleNode node) {
        String result = defaultResult();
        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (0 != i) {
                String previousText = node.getChild(i - 1).getText();
                String text = node.getChild(i).getText();
                if (!"(".equals(previousText) && !".".equals(previousText) && !")".equals(text) && !"(".equals(text) && !".".equals(text)) {
                    formatPrint(" ");
                }
            }
            if (!shouldVisitNextChild(node, result)) {
                break;
            }
            ParseTree child = node.getChild(i);
            String childResult = child.accept(this);
            result = aggregateResult(result, childResult);
        }
        return result;
    }
    
    private void formatPrint(final char value) {
        formattedSQL.append(value);
    }
    
    private void formatPrint(final String text) {
        formattedSQL.append(text);
    }
    
    private void formatPrintIndent() {
        for (int i = 0; i < indentCount; ++i) {
            formattedSQL.append('\t');
        }
    }
    
    private void formatPrintln() {
        formatPrint('\n');
        formatPrintIndent();
    }
    
    private void formatPrintln(final String text) {
        formatPrint(text);
        formatPrint('\n');
        formatPrintIndent();
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
