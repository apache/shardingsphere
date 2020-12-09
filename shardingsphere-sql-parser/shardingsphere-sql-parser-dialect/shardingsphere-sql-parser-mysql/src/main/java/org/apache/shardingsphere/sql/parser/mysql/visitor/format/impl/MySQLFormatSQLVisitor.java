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

package org.apache.shardingsphere.sql.parser.mysql.visitor.format.impl;

import lombok.Getter;
import lombok.Setter;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementBaseVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AliasContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterCommandListContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterListContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterTableActionsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AlterTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.AssignmentValuesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ColumnNamesContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateDefinitionClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CreateTableOptionsSpaceSeparatedContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.CteClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.DataTypeContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ExplicitTableContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ExprContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.FieldLengthContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.IdentifierContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.LiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.NumberLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.PrecisionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ProjectionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.QueryExpressionBodyContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.QueryExpressionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.QueryExpressionParensContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.QuerySpecificationContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.RowConstructorListContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.SelectContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StandaloneAlterTableActionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StringListContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.StringLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.String_Context;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableElementListContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableNameContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TableValueConstructorContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TemporalLiteralsContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.TypeDatetimePrecisionContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.WhereClauseContext;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser.WithClauseContext;

/**
 * MySQL Format SQL visitor for MySQL.
 */
@Getter
@Setter
public abstract class MySQLFormatSQLVisitor extends MySQLStatementBaseVisitor<String> {

    private StringBuilder result = new StringBuilder();

    private boolean uperCase = true;

    private boolean parameterized = true;

    private int indentCount;

    private int lines;

    private final int projectionsCountOfLine = 3;

    @Override
    public String visitSelect(final SelectContext ctx) {
        if (null != ctx.queryExpression()) {
            visit(ctx.queryExpression());
            if (null != ctx.lockClauseList()) {
                result.append(" ");
                visit(ctx.lockClauseList());
            }
        } else if (null != ctx.queryExpressionParens()) {
            visit(ctx.queryExpressionParens());
        } else {
            visit(ctx.selectWithInto());
        }
        formatPrint(";");
        return result.toString();
    }

    @Override
    public String visitQueryExpression(final QueryExpressionContext ctx) {
        if (null != ctx.withClause()) {
            visit(ctx.withClause());
            formatPrint(" ");
        }
        if (null != ctx.queryExpressionBody()) {
            visit(ctx.queryExpressionBody());
        } else {
            visit(ctx.queryExpressionParens());
        }
        if (null != ctx.orderByClause()) {
            formatPrint(" ");
            visit(ctx.orderByClause());
        }
        if (null != ctx.limitClause()) {
            formatPrint(" ");
            visit(ctx.limitClause());
        }
        return result.toString();
    }

    @Override
    public String visitQueryExpressionParens(final QueryExpressionParensContext ctx) {
        formatPrintln();
        indentCount++;
        formatPrint("(");
        formatPrintln();
        if (null != ctx.queryExpressionParens()) {
            visit(ctx.queryExpressionParens());
        } else {
            visit(ctx.queryExpression());
            if (null != ctx.lockClauseList()) {
                result.append(" ");
                visit(ctx.lockClauseList());
            }
        }
        indentCount--;
        formatPrintln();
        formatPrint(")");
        return result.toString();
    }

    @Override
    public String visitQueryExpressionBody(final QueryExpressionBodyContext ctx) {
        if (1 == ctx.getChildCount()) {
            visit(ctx.queryPrimary());
        } else {
            visit(ctx.queryExpressionParens(0));
            result.append("\nUNION\n");
            if (null != ctx.unionOption()) {
                visit(ctx.unionOption());
                result.append(" ");
            }
            if (null != ctx.queryPrimary()) {
                visit(ctx.queryPrimary());
            } else {
                visit(ctx.queryExpressionParens(1));
            }
        }
        return result.toString();
    }

    @Override
    public String visitQuerySpecification(final QuerySpecificationContext ctx) {
        formatPrint("SELECT ");
        int selectSpecCount = ctx.selectSpecification().size();
        for (int i = 0; i < selectSpecCount; i++) {
            visit(ctx.selectSpecification(i));
            formatPrint(" ");
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
        return result.toString();
    }

    @Override
    public String visitExplicitTable(final ExplicitTableContext ctx) {
        formatPrint("TABLE ");
        visit(ctx.tableName());
        return result.toString();
    }

    @Override
    public String visitTableName(final TableNameContext ctx) {
        if (null != ctx.owner()) {
            formatPrint(ctx.owner().getText());
            formatPrint(".");
        }
        formatPrint(ctx.name().getText());
        return result.toString();
    }

    @Override
    public String visitTableValueConstructor(final TableValueConstructorContext ctx) {
        formatPrint("VALUES ");
        visit(ctx.rowConstructorList());
        return result.toString();
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
        return result.toString();
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
        return result.toString();
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
        return result.toString();
    }

    @Override
    public String visitAlterList(final AlterListContext ctx) {
        int childCount = ctx.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ParseTree child = ctx.getChild(i);
            if (i != 0) {
                if (child instanceof TerminalNode) {
                    formatPrintln(",");
                    continue;
                } else {
                    child.accept(this);
                }
            } else {
                child.accept(this);
            }
        }
        return result.toString();
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
        return result.toString();
    }

    @Override
    public String visitStandaloneAlterTableAction(final StandaloneAlterTableActionContext ctx) {
        if (null != ctx.alterCommandsModifierList()) {
            visit(ctx.alterCommandsModifierList());
            formatPrintln(",");
        }
        visit(ctx.standaloneAlterCommands());
        return result.toString();
    }

    @Override
    public String visitRowConstructorList(final RowConstructorListContext ctx) {
        int rowCount = ctx.assignmentValues().size();
        for (int i = 0; i < rowCount; i++) {
            if (i != 0 && i != rowCount) {
                formatPrint(", ROW");
                visit(ctx.assignmentValues(i));
            } else {
                formatPrint("ROW");
                visit(ctx.assignmentValues(i));
            }
        }
        return result.toString();
    }

    @Override
    public String visitAssignmentValues(final AssignmentValuesContext ctx) {
        formatPrint("(");
        int assignCount = ctx.assignmentValue().size();
        for (int i = 0; i < assignCount; i++) {
            if (i != 0) {
                formatPrint(", ");
                visit(ctx.assignmentValue(i));
            } else {
                visit(ctx.assignmentValue(i));
            }
        }
        formatPrint(")");
        return result.toString();
    }

    @Override
    public String visitWhereClause(final WhereClauseContext ctx) {
        visit(ctx.WHERE());
        formatPrint(" ");
        indentCount++;
        formatPrintln();
        visit(ctx.expr());
        indentCount--;
        return result.toString();
    }

    @Override
    public String visitExpr(final ExprContext ctx) {
        if (null != ctx.logicalOperator()) {
            ExprContext left = ctx.expr(0);
            visit(left);
            formatPrintln();
            ExprContext right = ctx.expr(1);
            formatPrint(ctx.logicalOperator().getText());
            formatPrint(" ");
            visit(right);
        } else if (null != ctx.notOperator()) {
            formatPrint(ctx.notOperator().getText());
            visit(ctx.expr(0));
        } else {
            visitChildren(ctx);
        }
        return result.toString();
    }

    @Override
    public String visitAlias(final AliasContext ctx) {
        formatPrint(ctx.getText());
        return result.toString();
    }

    @Override
    public String visitProjections(final ProjectionsContext ctx) {
        indentCount++;
        if (null != ctx.unqualifiedShorthand()) {
            visit(ctx.unqualifiedShorthand());
            result.append(" ");
        }
        int projectionCount = ctx.projection().size();
        int lineItemCount = 0;
        for (int i = 0; i < projectionCount; i++) {
            if (0 != i) {
                result.append(", ");
                if (lineItemCount >= projectionsCountOfLine) {
                    lineItemCount = 0;
                    formatPrintln();
                }
            }
            visit(ctx.projection(i));
            lineItemCount++;
        }
        indentCount--;
        return result.toString();
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
        return result.toString();
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
        return result.toString();
    }

    @Override
    public String visitTableElementList(final TableElementListContext ctx) {
        int tableElementCount = ctx.tableElement().size();
        for (int i = 0; i < tableElementCount; i++) {
            if (0 == i) {
                visit(ctx.tableElement(i));
            } else {
                formatPrintln(",");
                visit(ctx.tableElement(i));
            }
        }
        return result.toString();
    }

    @Override
    public String visitFieldLength(final FieldLengthContext ctx) {
        formatPrint("(");
        formatPrint(ctx.NUMBER_().getText());
        formatPrint(")");
        return result.toString();
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
        return result.toString();
    }

    @Override
    public String visitDataType(final DataTypeContext ctx) {
        int childCount = ctx.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ParseTree child = ctx.getChild(i);
            if (i != 0 && !(child instanceof FieldLengthContext || child instanceof PrecisionContext || child instanceof StringListContext || child instanceof TypeDatetimePrecisionContext)) {
                formatPrint(" ");
            }
            child.accept(this);
        }
        return result.toString();
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
        return result.toString();
    }

    @Override
    public String visitTerminal(final TerminalNode node) {
        if (isUperCase()) {
            formatPrint(node.getText().toUpperCase());
        } else {
            formatPrint(node.getText().toLowerCase());
        }
        return result.toString();
    }

    @Override
    public String visitIdentifier(final IdentifierContext ctx) {
        formatPrint(ctx.getText());
        return result.toString();
    }

    @Override
    public String visitLiterals(final LiteralsContext ctx) {
        if (parameterized) {
            formatPrint("?");
        } else {
            super.visitLiterals(ctx);
        }
        return result.toString();
    }

    @Override
    public String visitTemporalLiterals(final TemporalLiteralsContext ctx) {
        visit(ctx.getChild(0));
        formatPrint(ctx.SINGLE_QUOTED_TEXT().getText());
        return result.toString();
    }

    @Override
    public String visitStringLiterals(final StringLiteralsContext ctx) {
        if (parameterized) {
            formatPrint("?");
            return result.toString();
        }
        if (null != ctx.string_()) {
            if (null != ctx.UNDERSCORE_CHARSET()) {
                formatPrint(ctx.UNDERSCORE_CHARSET().getText());
            }
            visit(ctx.string_());
        } else {
            visit(ctx.NCHAR_TEXT());
        }
        return result.toString();
    }

    @Override
    public String visitString_(final String_Context ctx) {
        formatPrint(ctx.getText());
        return result.toString();
    }

    @Override
    public String visitNumberLiterals(final NumberLiteralsContext ctx) {
        if (parameterized) {
            formatPrint("?");
        } else {
            formatPrint(ctx.getText());
        }
        return result.toString();
    }

    @Override
    public String visitWithClause(final WithClauseContext ctx) {
        formatPrint("WITH ");
        if (null != ctx.RECURSIVE()) {
            visit(ctx.RECURSIVE());
            result.append(" ");
        }
        for (int i = 0; i < ctx.cteClause().size(); i++) {
            if (i != 0 && i < ctx.cteClause().size() - 1) {
                result.append(", ");
            }
            visit(ctx.cteClause(i));
        }
        if (null != ctx.parent) {
            result.append("\n");
        }
        return result.toString();
    }

    @Override
    public String visitCteClause(final CteClauseContext ctx) {
        visit(ctx.identifier());
        result.append(" ");
        if (null != ctx.columnNames()) {
            visit(ctx.columnNames());
            result.append(" ");
        }
        result.append("AS ");
        visit(ctx.subquery());
        return result.toString();
    }

    @Override
    public String visitColumnNames(final ColumnNamesContext ctx) {
        int columnCount = ctx.columnName().size();
        for (int i = 0; i < columnCount; i++) {
            if (i != 0 && i < columnCount - 1) {
                result.append(", ");
            } else {
                visit(ctx.columnName(i));
            }
        }
        return result.toString();
    }

    @Override
    public String visitChildren(final RuleNode node) {
        String result = defaultResult();

        int childCount = node.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (i != 0) {
                formatPrint(" ");
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
        if (null == result) {
            return;
        }
        result.append(value);
    }

    private void formatPrint(final String text) {
        if (null == result) {
            return;
        }
        result.append(text);
    }

    protected void formatPrintIndent() {
        if (null == result) {
            return;
        }
        for (int i = 0; i < indentCount; ++i) {
            result.append('\t');
        }
    }

    private void formatPrintln() {
        formatPrint('\n');
        lines++;
        formatPrintIndent();
    }

    private void formatPrintln(final String text) {
        formatPrint(text);
        formatPrint('\n');
        lines++;
        formatPrintIndent();
    }
}
