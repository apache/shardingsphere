/*
 * Copyright 1999-2101 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.druid.sql.visitor;

import com.alibaba.druid.sql.ast.SQLCommentHint;
import com.alibaba.druid.sql.ast.SQLDataType;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.SQLOver;
import com.alibaba.druid.sql.ast.SQLSetQuantifier;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLAllColumnExpr;
import com.alibaba.druid.sql.ast.expr.SQLAllExpr;
import com.alibaba.druid.sql.ast.expr.SQLAnyExpr;
import com.alibaba.druid.sql.ast.expr.SQLArrayExpr;
import com.alibaba.druid.sql.ast.expr.SQLBetweenExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLBooleanExpr;
import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.alibaba.druid.sql.ast.expr.SQLCastExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLCurrentOfCursorExpr;
import com.alibaba.druid.sql.ast.expr.SQLDefaultExpr;
import com.alibaba.druid.sql.ast.expr.SQLExistsExpr;
import com.alibaba.druid.sql.ast.expr.SQLHexExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLListExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLNCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLNotExpr;
import com.alibaba.druid.sql.ast.expr.SQLNullExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumberExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLSomeExpr;
import com.alibaba.druid.sql.ast.expr.SQLTimestampExpr;
import com.alibaba.druid.sql.ast.expr.SQLUnaryExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.AbstractSQLInsertStatement.ValuesClause;
import com.alibaba.druid.sql.ast.statement.SQLAssignItem;
import com.alibaba.druid.sql.ast.statement.SQLCharacterDataType;
import com.alibaba.druid.sql.ast.statement.SQLCommentStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprHint;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource.JoinType;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLSetStatement;
import com.alibaba.druid.sql.ast.statement.SQLSubqueryTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.sql.ast.statement.SQLUnionQueryTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUpdateSetItem;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.ast.statement.SQLWithSubqueryClause;
import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@RequiredArgsConstructor
@Getter
@Setter
public class SQLASTOutputVisitor extends SQLASTVisitorAdapter implements PrintableVisitor {
    
    private final int selectListNumberOfLine = 5;
    
    private final Appendable appender;
    
    private List<Object> parameters = new LinkedList<>();
    
    @Getter(value = AccessLevel.NONE)
    @Setter(value = AccessLevel.NONE)
    private int indentCount;
    
    private boolean prettyFormat = true;
    
    public void decrementIndent() {
        indentCount -= 1;
    }
    
    public void incrementIndent() {
        indentCount += 1;
    }
    
    public void print(final char value) {
        try {
            appender.append(value);
        } catch (final IOException ex) {
            throw new RuntimeException("println error", ex);
        }
    }
    
    public void print(final int value) {
        print(Integer.toString(value));
    }
    
    public void print(final Date date) {
        SimpleDateFormat dateFormat;
        if (date instanceof java.sql.Timestamp) {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        } else {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        }
        print("'" + dateFormat.format(date) + "'");
    }
    
    public void print(final String text) {
        try {
            this.appender.append(text);
        } catch (final IOException ex) {
            throw new RuntimeException("println error", ex);
        }
    }

    protected void printAlias(final String alias) {
        if (!Strings.isNullOrEmpty(alias)) {
            print(" ");
            print(alias);
        }
    }
    
    protected void printAndAccept(final List<? extends SQLObject> nodes, final String seperator) {
        int count = 0;
        for (SQLObject each : nodes) {
            if (0 != count) {
                print(seperator);
            }
            each.accept(this);
            count++;
        }
    }
    
    protected void printSelectList(final List<SQLSelectItem> selectList) {
        incrementIndent();
        int count = 0;
        for (SQLSelectItem each : selectList) {
            if (0 != count) {
                if (0 == count % selectListNumberOfLine) {
                    println();
                }
                print(", ");
            }
            each.accept(this);
            count++;
        }
        decrementIndent();
    }
    
    protected void printlnAndAccept(final List<? extends SQLObject> nodes, final String seperator) {
        int count = 0;
        for (SQLObject each : nodes) {
            if (0 != count) {
                println(seperator);
            }
            each.accept(this);
            count++;
        }
    }
    
    public void printIndent() {
        for (int i = 0; i < indentCount; i++) {
            print("\t");
        }
    }
    
    public void println() {
        if (!prettyFormat) {
            print(' ');
            return;
        }
        print("\n");
        printIndent();
    }
    
    public void println(final String text) {
        print(text);
        println();
    }
    
    public boolean visit(final SQLBetweenExpr x) {
        x.getTestExpr().accept(this);
        if (x.isNot()) {
            print(" NOT BETWEEN ");
        } else {
            print(" BETWEEN ");
        }
        x.getBeginExpr().accept(this);
        print(" AND ");
        x.getEndExpr().accept(this);
        return false;
    }
    
    public boolean visit(final SQLBinaryOpExpr x) {
        SQLObject parent = x.getParent();
        boolean isRoot = parent instanceof SQLSelectQueryBlock;
        boolean relational = x.getOperator() == SQLBinaryOperator.BooleanAnd || x.getOperator() == SQLBinaryOperator.BooleanOr;
        if (isRoot && relational) {
            incrementIndent();
        }
        List<SQLExpr> groupList = new ArrayList<>();
        SQLExpr left = x.getLeft();
        while (true) {
            if (left instanceof SQLBinaryOpExpr && ((SQLBinaryOpExpr) left).getOperator() == x.getOperator()) {
                SQLBinaryOpExpr binaryLeft = (SQLBinaryOpExpr) left;
                groupList.add(binaryLeft.getRight());
                left = binaryLeft.getLeft();
            } else {
                groupList.add(left);
                break;
            }
        }
        for (int i = groupList.size() - 1; i >= 0; --i) {
            SQLExpr item = groupList.get(i);
            visitBinaryLeft(item, x.getOperator());
            if (relational) {
                println();
            } else {
                print(" ");
            }
            print(x.getOperator().getName());
            print(" ");
        }
        visitorBinaryRight(x);
        if (isRoot && relational) {
            decrementIndent();
        }
        return false;
    }
    
    private void visitorBinaryRight(final SQLBinaryOpExpr x) {
        if (x.getRight() instanceof SQLBinaryOpExpr) {
            SQLBinaryOpExpr right = (SQLBinaryOpExpr) x.getRight();
            boolean rightRational = right.getOperator() == SQLBinaryOperator.BooleanAnd || right.getOperator() == SQLBinaryOperator.BooleanOr;
            if (right.getOperator().getPriority() >= x.getOperator().getPriority()) {
                if (rightRational) {
                    incrementIndent();
                }
                print('(');
                right.accept(this);
                print(')');
                if (rightRational) {
                    decrementIndent();
                }
            } else {
                right.accept(this);
            }
        } else {
            x.getRight().accept(this);
        }
    }
    
    private void visitBinaryLeft(final SQLExpr left, final SQLBinaryOperator op) {
        if (left instanceof SQLBinaryOpExpr) {
            SQLBinaryOpExpr binaryLeft = (SQLBinaryOpExpr) left;
            boolean leftRational = binaryLeft.getOperator() == SQLBinaryOperator.BooleanAnd || binaryLeft.getOperator() == SQLBinaryOperator.BooleanOr;
            if (binaryLeft.getOperator().getPriority() > op.getPriority()) {
                if (leftRational) {
                    incrementIndent();
                }
                print('(');
                left.accept(this);
                print(')');
                if (leftRational) {
                    decrementIndent();
                }
            } else {
                left.accept(this);
            }
        } else {
            left.accept(this);
        }
    }
    
    public boolean visit(final SQLCaseExpr x) {
        print("CASE ");
        if (x.getValueExpr() != null) {
            x.getValueExpr().accept(this);
            print(" ");
        }
        printAndAccept(x.getItems(), " ");
        if (x.getElseExpr() != null) {
            print(" ELSE ");
            x.getElseExpr().accept(this);
        }
        print(" END");
        return false;
    }
    
    public boolean visit(final SQLCaseExpr.Item x) {
        print("WHEN ");
        x.getConditionExpr().accept(this);
        print(" THEN ");
        x.getValueExpr().accept(this);
        return false;
    }
    
    public boolean visit(final SQLCastExpr x) {
        print("CAST(");
        x.getExpr().accept(this);
        print(" AS ");
        x.getDataType().accept(this);
        print(")");
        return false;
    }
    
    public boolean visit(final SQLCharExpr x) {
        if (x.getText() == null) {
            print("NULL");
        } else {
            print("'");
            print(x.getText().replaceAll("'", "''"));
            print("'");
        }
        return false;
    }

    public boolean visit(SQLDataType x) {
        print(x.getName());
        if (x.getArguments().size() > 0) {
            print("(");
            printAndAccept(x.getArguments(), ", ");
            print(")");
        }

        return false;
    }

    public boolean visit(SQLCharacterDataType x) {
        visit((SQLDataType) x);
        return false;
    }

    public boolean visit(SQLExistsExpr x) {
        if (x.isNot()) {
            print("NOT EXISTS (");
        } else {
            print("EXISTS (");
        }
        incrementIndent();
        x.getSubQuery().accept(this);
        decrementIndent();
        print(")");
        return false;
    }

    public boolean visit(SQLIdentifierExpr x) {
        print(x.getSimpleName());
        return false;
    }

    public boolean visit(SQLInListExpr x) {
        x.getExpr().accept(this);

        if (x.isNot()) {
            print(" NOT IN (");
        } else {
            print(" IN (");
        }
        final List<SQLExpr> list = x.getTargetList();
        boolean printLn = false;
        if (list.size() > 5) {
            printLn = true;
            for (SQLExpr each : list) {
                if (!(each instanceof SQLCharExpr)) {
                    printLn = false;
                    break;
                }
            }
        }
        if (printLn) {
            incrementIndent();
            println();
            for (int i = 0, size = list.size(); i < size; ++i) {
                if (i != 0) {
                    print(", ");
                    println();
                }
                list.get(i).accept(this);
            }
            decrementIndent();
            println();
        } else {
            printAndAccept(x.getTargetList(), ", ");
        }

        print(')');
        return false;
    }

    public boolean visit(SQLIntegerExpr x) {
        return SQLASTOutputVisitorUtils.visit(this, x);
    }

    public boolean visit(SQLMethodInvokeExpr x) {
        if (x.getOwner() != null) {
            x.getOwner().accept(this);
            print(".");
        }
        printFunctionName(x.getMethodName());
        print("(");
        printAndAccept(x.getParameters(), ", ");
        print(")");
        return false;
    }
    
    protected void printFunctionName(String name) {
        print(name);
    }

    public boolean visit(SQLAggregateExpr x) {
        print(x.getMethodName());
        print("(");

        if (x.getOption() != null) {
            print(x.getOption().toString());
            print(' ');
        }

        printAndAccept(x.getArguments(), ", ");

        visitAggreateRest(x);

        print(")");

        if (x.getWithinGroup() != null) {
            print(" WITHIN GROUP (");
            x.getWithinGroup().accept(this);
            print(")");
        }

        if (x.getOver() != null) {
            print(" ");
            x.getOver().accept(this);
        }
        return false;
    }

    protected void visitAggreateRest(SQLAggregateExpr aggregateExpr) {

    }

    public boolean visit(SQLAllColumnExpr x) {
        print("*");
        return true;
    }

    public boolean visit(SQLNCharExpr x) {
        if ((x.getText() == null) || (x.getText().length() == 0)) {
            print("NULL");
        } else {
            print("N'");
            print(x.getText().replace("'", "''"));
            print("'");
        }
        return false;
    }

    public boolean visit(SQLNotExpr x) {
        print("NOT ");
        SQLExpr expr = x.getExpr();

        boolean needQuote = false;

        if (expr instanceof SQLBinaryOpExpr) {
            SQLBinaryOpExpr binaryOpExpr = (SQLBinaryOpExpr) expr;
            needQuote = binaryOpExpr.getOperator().isLogical();
        }

        if (needQuote) {
            print('(');
        }
        expr.accept(this);

        if (needQuote) {
            print(')');
        }
        return false;
    }

    public boolean visit(SQLNullExpr x) {
        print("NULL");
        return false;
    }

    public boolean visit(SQLNumberExpr x) {
        return SQLASTOutputVisitorUtils.visit(this, x);
    }

    public boolean visit(SQLPropertyExpr x) {
        x.getOwner().accept(this);
        print(".");
        print(x.getSimpleName());
        return false;
    }

    public boolean visit(SQLQueryExpr x) {
        SQLObject parent = x.getParent();
        if (parent instanceof SQLSelect) {
            parent = parent.getParent();
        }

        if (parent instanceof SQLStatement) {
            incrementIndent();

            println();
            x.getSubQuery().accept(this);

            decrementIndent();
        } else if (parent instanceof ValuesClause) {
            println();
            print("(");
            x.getSubQuery().accept(this);
            print(")");
            println();
        } else {
            print("(");
            incrementIndent();
            println();
            x.getSubQuery().accept(this);
            println();
            decrementIndent();
            print(")");
        }
        return false;
    }

    public boolean visit(SQLSelectGroupByClause x) {
        if (x.getItems().size() > 0) {
            print("GROUP BY ");
            printAndAccept(x.getItems(), ", ");
        }

        if (x.getHaving() != null) {
            println();
            print("HAVING ");
            x.getHaving().accept(this);
        }
        return false;
    }

    public boolean visit(SQLSelect x) {
        x.getQuery().setParent(x);

        if (x.getWithSubQuery() != null) {
            x.getWithSubQuery().accept(this);
            println();
        }

        x.getQuery().accept(this);

        if (x.getOrderBy() != null) {
            println();
            x.getOrderBy().accept(this);
        }

        if (!x.getHints().isEmpty()) {
            printAndAccept(x.getHints(), "");
        }

        return false;
    }

    public boolean visit(SQLSelectQueryBlock x) {
        print("SELECT ");
        if (SQLSetQuantifier.ALL == x.getDistionOption()) {
            print("ALL ");
        } else if (SQLSetQuantifier.DISTINCT == x.getDistionOption()) {
            print("DISTINCT ");
        } else if (SQLSetQuantifier.UNIQUE == x.getDistionOption()) {
            print("UNIQUE ");
        }

        printSelectList(x.getSelectList());

        if (x.getFrom() != null) {
            println();
            print("FROM ");
            x.getFrom().accept(this);
        }

        if (x.getWhere() != null) {
            println();
            print("WHERE ");
            x.getWhere().setParent(x);
            x.getWhere().accept(this);
        }

        if (x.getGroupBy() != null) {
            println();
            x.getGroupBy().accept(this);
        }

        return false;
    }

    public boolean visit(SQLSelectItem x) {
        if (x.isConnectByRoot()) {
            print("CONNECT_BY_ROOT ");
        }
        x.getExpr().accept(this);

        String alias = x.getAlias();
        if (alias != null && alias.length() > 0) {
            print(" AS ");
            if (alias.indexOf(' ') == -1 || alias.charAt(0) == '"' || alias.charAt(0) == '\'') {
                print(alias);
            } else {
                print('"');
                print(alias);
                print('"');
            }
        }
        return false;
    }

    public boolean visit(SQLOrderBy x) {
        if (!x.getItems().isEmpty()) {
            print("ORDER BY ");
            printAndAccept(x.getItems(), ", ");
        }
        return false;
    }

    public boolean visit(SQLSelectOrderByItem x) {
        x.getExpr().accept(this);
        if (x.getType() != null) {
            print(" ");
            print(x.getType().name().toUpperCase());
        }

        if (x.getCollate() != null) {
            print(" COLLATE ");
            print(x.getCollate());
        }

        return false;
    }

    public boolean visit(SQLExprTableSource x) {
        x.getExpr().accept(this);
        
        if (x.getAlias() != null) {
            print(' ');
            print(x.getAlias());
        }
        return false;
    }

    public boolean visit(SQLSelectStatement stmt) {
        SQLSelect select = stmt.getSelect();

        select.accept(this);

        return false;
    }

    public boolean visit(SQLVariantRefExpr x) {
        int index = x.getIndex();

        if (parameters == null || index >= parameters.size()) {
            print(x.getName());
            return false;
        }

        Object param = parameters.get(index);
        printParameter(param);
        return false;
    }

    public void printParameter(Object param) {
        if (param == null) {
            print("NULL");
            return;
        }

        if (param instanceof Number || param instanceof Boolean) {
            print(param.toString());
            return;
        }

        if (param instanceof String) {
            SQLCharExpr charExpr = new SQLCharExpr((String) param);
            visit(charExpr);
            return;
        }

        if (param instanceof Date) {
            print((Date) param);
            return;
        }

        if (param instanceof InputStream) {
            print("'<InputStream>");
            return;
        }

        if (param instanceof Reader) {
            print("'<Reader>");
            return;
        }

        if (param instanceof Blob) {
            print("'<Blob>");
            return;
        }

        if (param instanceof NClob) {
            print("'<NClob>");
            return;
        }

        if (param instanceof Clob) {
            print("'<Clob>");
            return;
        }

        print("'" + param.getClass().getName() + "'");
    }
    

    public boolean visit(SQLCurrentOfCursorExpr x) {
        print("CURRENT OF ");
        x.getCursorName().accept(this);
        return false;
    }

    public boolean visit(SQLUpdateSetItem x) {
        x.getColumn().accept(this);
        print(" = ");
        x.getValue().accept(this);
        return false;
    }

    // TODO tobe removed
    public boolean visit(SQLUpdateStatement x) {
        return false;
    }

    @Override
    public boolean visit(SQLUnionQuery x) {
        x.getLeft().accept(this);
        println();
        print(x.getOperator().getText());
        println();

        boolean needParen = false;

        if (x.getOrderBy() != null) {
            needParen = true;
        }

        if (needParen) {
            print('(');
            x.getRight().accept(this);
            print(')');
        } else {
            x.getRight().accept(this);
        }

        if (x.getOrderBy() != null) {
            println();
            x.getOrderBy().accept(this);
        }

        return false;
    }

    @Override
    public boolean visit(SQLUnaryExpr x) {
        print(x.getOperator().getText());
        SQLExpr expr = x.getExpr();
        switch (x.getOperator()) {
            case BINARY:
            case Prior:
            case ConnectByRoot:
                print(' ');
                expr.accept(this);
                return false;
            default:
                break;
        }

        if (expr instanceof SQLBinaryOpExpr) {
            print('(');
            expr.accept(this);
            print(')');
        } else if (expr instanceof SQLUnaryExpr) {
            print('(');
            expr.accept(this);
            print(')');
        } else {
            expr.accept(this);
        }
        return false;
    }

    @Override
    public boolean visit(SQLHexExpr x) {
        print("0x");
        print(x.getHex());

        String charset = (String) x.getAttribute("USING");
        if (charset != null) {
            print(" USING ");
            print(charset);
        }

        return false;
    }

    @Override
    public boolean visit(SQLSetStatement x) {
        print("SET ");
        printAndAccept(x.getItems(), ", ");

        if (x.getHints() != null && x.getHints().size() > 0) {
            print(" ");
            printAndAccept(x.getHints(), " ");
        }

        return false;
    }

    @Override
    public boolean visit(SQLAssignItem x) {
        x.getTarget().accept(this);
        print(" = ");
        x.getValue().accept(this);
        return false;
    }

    @Override
    public boolean visit(SQLJoinTableSource x) {
        x.getLeft().accept(this);
        incrementIndent();

        if (x.getJoinType() == JoinType.COMMA) {
            print(",");
        } else {
            println();
            printJoinType(x.getJoinType());
        }
        print(" ");
        x.getRight().accept(this);

        if (x.getCondition() != null) {
            incrementIndent();
            print(" ON ");
            x.getCondition().accept(this);
            decrementIndent();
        }

        if (x.getUsing().size() > 0) {
            print(" USING (");
            printAndAccept(x.getUsing(), ", ");
            print(")");
        }

        if (x.getAlias() != null) {
            print(" AS ");
            print(x.getAlias());
        }

        decrementIndent();

        return false;
    }

    protected void printJoinType(JoinType joinType) {
        print(joinType.getName());
    }

    @Override
    public boolean visit(SQLSomeExpr x) {
        print("SOME (");

        incrementIndent();
        x.getSubQuery().accept(this);
        decrementIndent();
        print(")");
        return false;
    }

    @Override
    public boolean visit(SQLAnyExpr x) {
        print("ANY (");

        incrementIndent();
        x.getSubQuery().accept(this);
        decrementIndent();
        print(")");
        return false;
    }

    @Override
    public boolean visit(SQLAllExpr x) {
        print("ALL (");

        incrementIndent();
        x.getSubQuery().accept(this);
        decrementIndent();
        print(")");
        return false;
    }

    @Override
    public boolean visit(SQLInSubQueryExpr x) {
        x.getExpr().accept(this);
        if (x.isNot()) {
            print(" NOT IN (");
        } else {
            print(" IN (");
        }

        incrementIndent();
        x.getSubQuery().accept(this);
        decrementIndent();
        print(")");

        return false;
    }

    @Override
    public boolean visit(SQLListExpr x) {
        print("(");
        printAndAccept(x.getItems(), ", ");
        print(")");

        return false;
    }

    @Override
    public boolean visit(SQLSubqueryTableSource x) {
        print("(");
        incrementIndent();
        x.getSelect().accept(this);
        println();
        decrementIndent();
        print(")");

        if (x.getAlias() != null) {
            print(' ');
            print(x.getAlias());
        }

        return false;
    }

    @Override
    public boolean visit(SQLDefaultExpr x) {
        print("DEFAULT");
        return false;
    }

    @Override
    public void endVisit(SQLCommentStatement x) {

    }

    @Override
    public boolean visit(SQLCommentStatement x) {
        print("COMMENT ON ");
        if (x.getType() != null) {
            print(x.getType().name());
            print(" ");
        }
        x.getOn().accept(this);

        print(" IS ");
        x.getComment().accept(this);

        return false;
    }

    public boolean visit(SQLCommentHint x) {
        print("/*");
        print(x.getText());
        print("*/");
        return false;
    }

    @Override
    public boolean visit(SQLOver x) {
        print("OVER (");
        if (x.getPartitionBy().size() > 0) {
            print("PARTITION BY ");
            printAndAccept(x.getPartitionBy(), ", ");
            print(' ');
        }
        if (x.getOrderBy() != null) {
            x.getOrderBy().accept(this);
        }
        print(")");
        return false;
    }

    @Override
    public boolean visit(SQLWithSubqueryClause x) {
        print("WITH");
        if (x.getRecursive() == Boolean.TRUE) {
            print(" RECURSIVE");
        }
        incrementIndent();
        println();
        printlnAndAccept(x.getEntries(), ", ");
        decrementIndent();
        return false;
    }

    @Override
    public boolean visit(SQLWithSubqueryClause.Entry x) {
        x.getName().accept(this);

        if (x.getColumns().size() > 0) {
            print(" (");
            printAndAccept(x.getColumns(), ", ");
            print(")");
        }
        println();
        print("AS");
        println();
        print("(");
        incrementIndent();
        println();
        x.getSubQuery().accept(this);
        decrementIndent();
        println();
        print(")");

        return false;
    }

    @Override
    public boolean visit(SQLExprHint x) {
        x.getExpr().accept(this);
        return false;
    }

    public boolean visit(SQLBooleanExpr x) {
        print(Boolean.toString(x.isValue()).toLowerCase());

        return false;
    }

    public void endVisit(SQLBooleanExpr x) {
    }

    @Override
    public boolean visit(SQLUnionQueryTableSource x) {
        print("(");
        incrementIndent();
        println();
        x.getUnion().accept(this);
        decrementIndent();
        println();
        print(")");

        if (x.getAlias() != null) {
            print(' ');
            print(x.getAlias());
        }

        return false;
    }

    @Override
    public boolean visit(SQLTimestampExpr x) {
        print("TIMESTAMP ");

        if (x.isWithTimeZone()) {
            print(" WITH TIME ZONE ");
        }

        print('\'');
        print(x.getLiteral());
        print('\'');

        if (x.getTimeZone() != null) {
            print(" AT TIME ZONE '");
            print(x.getTimeZone());
            print('\'');
        }

        return false;
    }

    @Override
    public boolean visit(SQLBinaryExpr x) {
        print("b'");
        print(x.getValue());
        print('\'');

        return false;
    }

    @Override
    public boolean visit(SQLArrayExpr x) {
        x.getExpr().accept(this);
        print("[");
        printAndAccept(x.getValues(), ", ");
        print("]");
        return false;
    }
}
