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

import lombok.AccessLevel;
import lombok.Getter;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementBaseVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser;

/**
 * MySQL Format SQL visitor for MySQL.
 */
@Getter(AccessLevel.PROTECTED)
public abstract class MySQLFormatSQLVisitor extends MySQLStatementBaseVisitor<String> {

    private StringBuilder result = new StringBuilder();

    private final boolean uperCase = true;

    private int indentCount;

    private int lines;

    private final int projectionsCountOfLine = 3;

    @Override
    public String visitSelect(final MySQLStatementParser.SelectContext ctx) {
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
        formartPrint(";");
        return result.toString();
    }

    @Override
    public String visitQueryExpression(final MySQLStatementParser.QueryExpressionContext ctx) {
        if (null != ctx.withClause()) {
            visit(ctx.withClause());
            formartPrint(" ");
        }
        if (null != ctx.queryExpressionBody()) {
            visit(ctx.queryExpressionBody());
        } else {
            visit(ctx.queryExpressionParens());
        }
        if (null != ctx.orderByClause()) {
            formartPrint(" ");
            visit(ctx.orderByClause());
        }
        if (null != ctx.limitClause()) {
            formartPrint(" ");
            visit(ctx.limitClause());
        }
        return result.toString();
    }

    @Override
    public String visitQueryExpressionParens(final MySQLStatementParser.QueryExpressionParensContext ctx) {
        formartPrintln();
        this.indentCount++;
        formartPrint("(");
        formartPrintln();
        if (null != ctx.queryExpressionParens()) {
            visit(ctx.queryExpressionParens());
        } else {
            visit(ctx.queryExpression());
            if (null != ctx.lockClauseList()) {
                result.append(" ");
                visit(ctx.lockClauseList());
            }
        }
        this.indentCount--;
        formartPrintln();
        formartPrint(")");
        return result.toString();
    }

    @Override
    public String visitQueryExpressionBody(final MySQLStatementParser.QueryExpressionBodyContext ctx) {
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
    public String visitQuerySpecification(final MySQLStatementParser.QuerySpecificationContext ctx) {
        formartPrint("SELECT ");
        int selectSpecCount = ctx.selectSpecification().size();
        for (int i = 0; i < selectSpecCount; i++) {
            visit(ctx.selectSpecification(i));
            formartPrint(" ");
        }
        visit(ctx.projections());
        if (null != ctx.fromClause()) {
            formartPrintln();
            visit(ctx.fromClause());
        }
        if (null != ctx.whereClause()) {
            formartPrintln();
            visit(ctx.whereClause());
        }
        if (null != ctx.groupByClause()) {
            formartPrintln();
            visit(ctx.groupByClause());
        }
        if (null != ctx.havingClause()) {
            formartPrintln();
            visit(ctx.havingClause());
        }
        if (null != ctx.windowClause()) {
            formartPrintln();
            visit(ctx.windowClause());
        }
        return result.toString();
    }

    @Override
    public String visitExplicitTable(final MySQLStatementParser.ExplicitTableContext ctx) {
        formartPrint("TABLE ");
        visit(ctx.tableName());
        return result.toString();
    }

    @Override
    public String visitTableName(final MySQLStatementParser.TableNameContext ctx) {
        if (null != ctx.owner()) {
            formartPrint(ctx.owner().getText());
            formartPrint(".");
        }
        formartPrint(ctx.name().getText());
        return result.toString();
    }

    @Override
    public String visitTableValueConstructor(final MySQLStatementParser.TableValueConstructorContext ctx) {
        formartPrint("VALUES ");
        visit(ctx.rowConstructorList());
        return result.toString();
    }

    @Override
    public String visitRowConstructorList(final MySQLStatementParser.RowConstructorListContext ctx) {
        int rowCount = ctx.assignmentValues().size();
        for (int i = 0; i < rowCount; i++) {
            if (i != 0 && i != rowCount) {
                formartPrint(", ROW");
                visit(ctx.assignmentValues(i));

            } else {
                formartPrint("ROW");
                visit(ctx.assignmentValues(i));
            }
        }
        return result.toString();
    }

    @Override
    public String visitAssignmentValues(final MySQLStatementParser.AssignmentValuesContext ctx) {
        formartPrint("(");
        int assignCount = ctx.assignmentValue().size();
        for (int i = 0; i < assignCount; i++) {
            if (i != 0) {
                formartPrint(", ");
                visit(ctx.assignmentValue(i));
            } else {
                visit(ctx.assignmentValue(i));
            }
        }
        formartPrint(")");
        return result.toString();
    }

    @Override
    public String visitWhereClause(final MySQLStatementParser.WhereClauseContext ctx) {
        visit(ctx.WHERE());
        formartPrint(" ");
        this.indentCount++;
        formartPrintln();
        visit(ctx.expr());
        this.indentCount--;
        return result.toString();
    }

    @Override
    public String visitExpr(final MySQLStatementParser.ExprContext ctx) {
        if (null != ctx.logicalOperator()) {
            MySQLStatementParser.ExprContext left = ctx.expr(0);
            visit(left);
            formartPrintln();
            MySQLStatementParser.ExprContext right = ctx.expr(1);
            formartPrint(ctx.logicalOperator().getText());
            visit(right);
        } else if (null != ctx.notOperator()) {
            formartPrint(ctx.notOperator().getText());
            visit(ctx.expr(0));
        } else {
            visitChildren(ctx);
        }
        return result.toString();
    }

    @Override
    public String visitAlias(final MySQLStatementParser.AliasContext ctx) {
        formartPrint(ctx.getText());
        return result.toString();
    }

    @Override
    public String visitProjections(final MySQLStatementParser.ProjectionsContext ctx) {
        this.indentCount++;
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
                    formartPrintln();
                }
            }
            visit(ctx.projection(i));
            lineItemCount++;
        }
        this.indentCount--;
        return result.toString();
    }

    @Override
    public String visitProjection(final MySQLStatementParser.ProjectionContext ctx) {
        if (null != ctx.expr()) {
            visit(ctx.expr());
            formartPrint(" ");
        }
        if (null != ctx.AS()) {
            formartPrint("AS ");
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
    public String visitTerminal(final TerminalNode node) {
        if (isUperCase()) {
            formartPrint(node.getText().toUpperCase());
        } else {
            formartPrint(node.getText().toLowerCase());
        }
        return result.toString();
    }

    @Override
    public String visitIdentifier(final MySQLStatementParser.IdentifierContext ctx) {
        formartPrint(ctx.getText());
        return result.toString();
    }

    @Override
    public String visitLiterals(final MySQLStatementParser.LiteralsContext ctx) {
        formartPrint("?");
        return result.toString();
    }

    @Override
    public String visitStringLiterals(final MySQLStatementParser.StringLiteralsContext ctx) {
        formartPrint("?");
        return result.toString();
    }

    @Override
    public String visitNumberLiterals(final MySQLStatementParser.NumberLiteralsContext ctx) {
        formartPrint("?");
        return result.toString();
    }

    @Override
    public String visitWithClause(final MySQLStatementParser.WithClauseContext ctx) {
        formartPrint("WITH ");
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
    public String visitCteClause(final MySQLStatementParser.CteClauseContext ctx) {
        visit(ctx.ignoredIdentifier());
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
    public String visitIgnoredIdentifier(final MySQLStatementParser.IgnoredIdentifierContext ctx) {
        visit(ctx.identifier(0));
        if (null != ctx.DOT_()) {
            visit(ctx.DOT_());
            visit(ctx.identifier(1));
        }
        return result.toString();
    }

    @Override
    public String visitColumnNames(final MySQLStatementParser.ColumnNamesContext ctx) {
        if (null != ctx.LP_()) {
            visit(ctx.LP_());
        }
        int columnCount = ctx.columnName().size();
        for (int i = 0; i < columnCount; i++) {
            if (i != 0 && i < columnCount - 1) {
                result.append(", ");
            } else {
                visit(ctx.columnName(i));
            }
        }
        if (null != ctx.RP_()) {
            visit(ctx.RP_());
        }
        return result.toString();
    }

    @Override
    public String visitColumnName(final MySQLStatementParser.ColumnNameContext ctx) {
        if (null != ctx.owner()) {
            visit(ctx.owner());
            visit(ctx.DOT_(0));
        }
        visit(ctx.name());
        return result.toString();
    }

    @Override
    public String visitChildren(final RuleNode node) {
        String result = defaultResult();

        int n = node.getChildCount();
        for (int i = 0; i < n; i++) {
            if (i != 0) {
                formartPrint(" ");
            }
            if (!shouldVisitNextChild(node, result)) {
                break;
            }

            ParseTree c = node.getChild(i);
            String childResult = c.accept(this);
            result = aggregateResult(result, childResult);
        }
        return result;
    }

    private void formartPrint(final char value) {
        if (null == this.result) {
            return;
        }
        this.result.append(value);
    }

    private void formartPrint(final String text) {
        if (null == this.result) {
            return;
        }
        result.append(text);
    }

    protected void formartPrintIndent() {
        if (null == this.result) {
            return;
        }
        for (int i = 0; i < this.indentCount; ++i) {
            this.result.append('\t');
        }
    }

    private void formartPrintln() {
        formartPrint('\n');
        lines++;
        formartPrintIndent();
    }
}
