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
import lombok.Setter;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementBaseVisitor;
import org.apache.shardingsphere.sql.parser.autogen.MySQLStatementParser;

/**
 * MySQL Format SQL visitor for MySQL.
 */
@Getter(AccessLevel.PROTECTED)
@Setter
public abstract class MySQLFormatSQLVisitor extends MySQLStatementBaseVisitor<Boolean> {
    @Getter
    private StringBuilder result = new StringBuilder();
    private int indentCount = 0;
    private int lines = 0;
    private int rowConstructorNumberOfLine = 5;

    @Override
    public Boolean visitSelect(final MySQLStatementParser.SelectContext ctx) {
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
        return true;
    }

    @Override
    public Boolean visitQueryExpression(final MySQLStatementParser.QueryExpressionContext ctx) {
        if (null != ctx.withClause()) {
            visit(ctx.withClause());
            result.append(" ");
        }
        if (null != ctx.queryExpressionBody()) {
            visit(ctx.queryExpressionBody());
        } else {
            visit(ctx.queryExpressionParens());
        }
        if (null != ctx.orderByClause()) {
            result.append(" ");
            visit(ctx.orderByClause());
        }
        if (null != ctx.limitClause()) {
            result.append(" ");
            visit(ctx.limitClause());
        }
        return true;
    }

    @Override
    public Boolean visitQueryExpressionParens(final MySQLStatementParser.QueryExpressionParensContext ctx) {
        result.append("(");
        if (null != ctx.queryExpressionParens()) {
            visit(ctx.queryExpressionParens());
        } else {
            visit(ctx.queryExpression());
            if (null != ctx.lockClauseList()) {
                result.append(" ");
                visit(ctx.lockClauseList());
            }
        }
        result.append(")");
        return true;
    }

    @Override
    public Boolean visitQueryExpressionBody(final MySQLStatementParser.QueryExpressionBodyContext ctx) {
        if (1 == ctx.getChildCount()) {
            visit(ctx.queryPrimary());
        } else {
            visit(ctx.queryExpressionParens(0));
            result.append("\nUNION\n" );
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
        return true;
    }

    @Override
    public Boolean visitQuerySpecification(final MySQLStatementParser.QuerySpecificationContext ctx) {
        print("SELECT ");
        int selectSpecCount = ctx.selectSpecification().size();
        for (int i = 0; i < selectSpecCount; i++) {
            visit(ctx.selectSpecification(i));
            print(" ");
        }
        visit(ctx.projections());
        if (null != ctx.fromClause()) {
            println();
            visit(ctx.fromClause());
        }
        if (null != ctx.whereClause()) {
            println();
            visit(ctx.whereClause());
        }
        if (null != ctx.groupByClause()) {
            println();
            visit(ctx.groupByClause());
        }
        if (null != ctx.havingClause()) {
            println();
            visit(ctx.havingClause());
        }
        if (null != ctx.windowClause()) {
            println();
            visit(ctx.windowClause());
        }
        return true;
    }

    @Override
    public Boolean visitExplicitTable(final MySQLStatementParser.ExplicitTableContext ctx) {
        print("TABLE ");
        visit(ctx.tableName());
        return true;
    }

    @Override
    public Boolean visitTableName(final MySQLStatementParser.TableNameContext ctx) {
        if (null != ctx.owner()) {
            visit(ctx.owner());
            print(".");
            visit(ctx.name());
        }
        return true;
    }

    @Override
    public Boolean visitTableValueConstructor(final MySQLStatementParser.TableValueConstructorContext ctx) {
        print("VALUES ");
        visit(ctx.rowConstructorList());
        return true;
    }

    @Override
    public Boolean visitRowConstructorList(final MySQLStatementParser.RowConstructorListContext ctx) {
        int rowCount = ctx.assignmentValues().size();
        for (int i = 0; i < rowCount; i++) {
            if (i != 0 && i != rowCount) {
                print(", ROW");
                visit(ctx.assignmentValues(i));

            } else {
                print("ROW");
                visit(ctx.assignmentValues(i));
            }
        }
        return true;
    }

    @Override
    public Boolean visitAssignmentValues(final MySQLStatementParser.AssignmentValuesContext ctx) {
        print("(");
        int assignCount = ctx.assignmentValue().size();
        for (int i = 0; i < assignCount; i++) {
            if (i != 0) {
                print(", ");
                visit(ctx.assignmentValue(i));
            } else {
                visit(ctx.assignmentValue(i));
            }
        }
        print(")");
        return true;
    }

    @Override
    public Boolean visitWhereClause(final MySQLStatementParser.WhereClauseContext ctx) {
        print("WHERE ");
        visit(ctx.expr());
        return true;
    }

    @Override
    public Boolean visitExpr(final MySQLStatementParser.ExprContext ctx) {
        print("expr not support yet");
        return true;
    }

    @Override
    public Boolean visitProjections(final MySQLStatementParser.ProjectionsContext ctx) {
        if (null != ctx.unqualifiedShorthand()) {
            visit(ctx.unqualifiedShorthand());
            result.append(" ");
        }
        int projectionCount = ctx.projection().size();
        for (int i = 0; i < projectionCount; i++) {
            if (0 != i) {
                result.append(", ");
            }
            visit(ctx.projection(i));
        }
        return true;
    }

    @Override
    public Boolean visitTerminal(final TerminalNode node) {
        result.append(node.getText().toUpperCase());
        return true;
    }

    @Override
    public Boolean visitWithClause(final MySQLStatementParser.WithClauseContext ctx) {
        result.append("WITH").append(" ");
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
        return true;
    }

    @Override
    public Boolean visitCteClause(final MySQLStatementParser.CteClauseContext ctx) {
        visit(ctx.ignoredIdentifier());
        result.append(" ");
        if (null != ctx.columnNames()) {
            visit(ctx.columnNames());
            result.append(" ");
        }
        result.append("AS ");
        visit(ctx.subquery());
        return true;
    }

    @Override
    public Boolean visitIgnoredIdentifier(final MySQLStatementParser.IgnoredIdentifierContext ctx) {
        visit(ctx.identifier(0));
        if (null != ctx.DOT_()) {
            visit(ctx.DOT_());
            visit(ctx.identifier(1));
        }
        return true;
    }

    @Override
    public Boolean visitColumnNames(final MySQLStatementParser.ColumnNamesContext ctx) {
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
        return true;
    }

    @Override
    public Boolean visitColumnName(final MySQLStatementParser.ColumnNameContext ctx) {
        if (null != ctx.owner()) {
            visit(ctx.owner());
            visit(ctx.DOT_());
        }
        visit(ctx.name());
        return true;
    }

    public void print(char value) {
        if (this.result == null) {
            return;
        }
        this.result.append(value);
    }

    protected void printIndent() {
        if (this.result == null) {
            return;
        }
        for (int i = 0; i < this.indentCount; ++i) {
            this.result.append('\t');
        }
    }

    public void println() {
        print('\n');
        lines++;
        printIndent();
    }

    public void println(String text) {
        print(text);
        println();
    }

    public void print(String text) {
        if (this.result == null) {
            return;
        }
        result.append(text);
    }
}
