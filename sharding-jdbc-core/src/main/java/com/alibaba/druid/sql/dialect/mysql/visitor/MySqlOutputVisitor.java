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
package com.alibaba.druid.sql.dialect.mysql.visitor;

import com.alibaba.druid.sql.ast.SQLCommentHint;
import com.alibaba.druid.sql.ast.SQLDataType;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.SQLSetQuantifier;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLAssignItem;
import com.alibaba.druid.sql.ast.statement.SQLCharacterDataType;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlForceIndexHint;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlIgnoreIndexHint;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlUseIndexHint;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlCaseStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlCaseStatement.MySqlWhenStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlElseStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlIfStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlIfStatement.MySqlElseIfStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlIterateStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlLeaveStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlLoopStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlParameter;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlParameter.ParameterType;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlRepeatStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlSelectIntoStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.MySqlWhileStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlCharExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlExtractExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlIntervalExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlMatchAgainstExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlOutFileExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlSelectGroupByExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlUserName;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDeleteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlExecuteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlHintStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlPartitioningDef;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlPartitioningDef.InValues;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlPartitioningDef.LessThanValues;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlPrepareStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectGroupBy;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock.Limit;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSetCharSetStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSetNamesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSetPasswordStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSetTransactionStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlTableIndex;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUnionQuery;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUnlockTablesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateTableSource;
import com.alibaba.druid.sql.visitor.SQLASTOutputVisitor;

import java.util.List;

public class MySqlOutputVisitor extends SQLASTOutputVisitor implements MySqlASTVisitor {

    public MySqlOutputVisitor(final Appendable appender) {
        super(appender);
    }

    @Override
    public boolean visit(final SQLSelectQueryBlock select) {
        if (select instanceof MySqlSelectQueryBlock) {
            return visit((MySqlSelectQueryBlock) select);
        }

        return super.visit(select);
    }

    public boolean visit(MySqlSelectQueryBlock x) {
        if (x.getOrderBy() != null) {
            x.getOrderBy().setParent(x);
        }

        print("SELECT ");

        for (int i = 0, size = x.getHintsSize(); i < size; ++i) {
            SQLCommentHint hint = x.getHints().get(i);
            hint.accept(this);
            print(' ');
        }

        if (SQLSetQuantifier.ALL == x.getDistionOption()) {
            print("ALL ");
        } else if (SQLSetQuantifier.DISTINCT == x.getDistionOption()) {
            print("DISTINCT ");
        } else if (SQLSetQuantifier.DISTINCTROW == x.getDistionOption()) {
            print("DISTINCTROW ");
        }

        if (x.isHignPriority()) {
            print("HIGH_PRIORITY ");
        }

        if (x.isStraightJoin()) {
            print("STRAIGHT_JOIN ");
        }

        if (x.isSmallResult()) {
            print("SQL_SMALL_RESULT ");
        }

        if (x.isBigResult()) {
            print("SQL_BIG_RESULT ");
        }

        if (x.isBufferResult()) {
            print("SQL_BUFFER_RESULT ");
        }

        if (x.getCache() != null) {
            if (x.getCache().booleanValue()) {
                print("SQL_CACHE ");
            } else {
                print("SQL_NO_CACHE ");
            }
        }

        if (x.isCalcFoundRows()) {
            print("SQL_CALC_FOUND_ROWS ");
        }

        printSelectList(x.getSelectList());

        if (x.getInto() != null) {
            println();
            print("INTO ");
            x.getInto().accept(this);
        }

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

        if (x.getOrderBy() != null) {
            println();
            x.getOrderBy().accept(this);
        }

        if (x.getLimit() != null) {
            println();
            x.getLimit().accept(this);
        }

        if (x.getProcedureName() != null) {
            print(" PROCEDURE ");
            x.getProcedureName().accept(this);
            if (x.getProcedureArgumentList().size() > 0) {
                print("(");
                printAndAccept(x.getProcedureArgumentList(), ", ");
                print(")");
            }
        }

        if (x.isForUpdate()) {
            println();
            print("FOR UPDATE");
        }

        if (x.isLockInShareMode()) {
            println();
            print("LOCK IN SHARE MODE");
        }

        return false;
    }

    public boolean visit(MySqlSelectQueryBlock.Limit x) {
        print("LIMIT ");
        if (x.getOffset() != null) {
            x.getOffset().accept(this);
            print(", ");
        }
        x.getRowCount().accept(this);

        return false;
    }

    public boolean visit(SQLDataType x) {
        print(x.getName());
        if (x.getArguments().size() > 0) {
            print("(");
            printAndAccept(x.getArguments(), ", ");
            print(")");
        }

        if (Boolean.TRUE == x.getAttribute("UNSIGNED")) {
            print(" UNSIGNED");
        }
        
        if (Boolean.TRUE == x.getAttribute("ZEROFILL")) {
            print(" ZEROFILL");
        }


        if (x instanceof SQLCharacterDataType) {
            SQLCharacterDataType charType = (SQLCharacterDataType) x;
            if (charType.getCharSetName() != null) {
                print(" CHARACTER SET ");
                print(charType.getCharSetName());

                if (charType.getCollate() != null) {
                    print(" COLLATE ");
                    print(charType.getCollate());
                }
            }
        }
        return false;
    }

    public boolean visit(SQLCharacterDataType x) {
        print(x.getName());
        if (x.getArguments().size() > 0) {
            print("(");
            printAndAccept(x.getArguments(), ", ");
            print(")");
        }

        if (x.isHasBinary()) {
            print(" BINARY ");
        }
        
        if (x.getCharSetName() != null) {
            print(" CHARACTER SET ");
            print(x.getCharSetName());
            if (x.getCollate() != null) {
                print(" COLLATE ");
                print(x.getCollate());
            }
        }else if (x.getCollate() != null) {
            print(" COLLATE ");
            print(x.getCollate());
        }
        
        return false;
    }

    @Override
    public void endVisit(Limit x) {

    }

    @Override
    public void endVisit(MySqlTableIndex x) {

    }

    @Override
    public boolean visit(MySqlTableIndex x) {
        print("INDEX");
        if (x.getName() != null) {
            print(" ");
            x.getName().accept(this);
        }

        if (x.getIndexType() != null) {
            print(" USING ");
            print(x.getIndexType());
        }

        print("(");
        for (int i = 0, size = x.getColumns().size(); i < size; ++i) {
            if (i != 0) {
                print(", ");
            }
            x.getColumns().get(i).accept(this);
        }
        print(")");
        return false;
    }

    @Override
    public boolean visit(SQLCharExpr x) {
        print('\'');

        String text = x.getText();
        text = text.replaceAll("'", "''");
        text = text.replace("\\", "\\\\");

        print(text);

        print('\'');
        return false;
    }

    public boolean visit(SQLVariantRefExpr x) {
        {
            int parametersSize = this.getParametersSize();
            int index = x.getIndex();

            if (index >= 0 && index < parametersSize) {
                Object param = this.getParameters().get(index);
                printParameter(param);
                return false;
            }
        }

        String varName = x.getName();
        if (x.isGlobal()) {
            print("@@global.");
        } else {
            if ((!varName.startsWith("@")) // /
                && (!varName.equals("?")) //
                && (!varName.startsWith("#")) //
                && (!varName.startsWith("$")) //
                && (!varName.startsWith(":"))) {
                print("@@");
            }
        }

        for (int i = 0; i < x.getName().length(); ++i) {
            char ch = x.getName().charAt(i);
            if (ch == '\'') {
                if (x.getName().startsWith("@@") && i == 2) {
                    print(ch);
                } else if (x.getName().startsWith("@") && i == 1) {
                    print(ch);
                } else if (i != 0 && i != x.getName().length() - 1) {
                    print("\\'");
                } else {
                    print(ch);
                }
            } else {
                print(ch);
            }
        }

        String collate = (String) x.getAttribute("COLLATE");
        if (collate != null) {
            print(" COLLATE ");
            print(collate);
        }

        return false;
    }

    public boolean visit(SQLMethodInvokeExpr x) {
        if ("SUBSTRING".equalsIgnoreCase(x.getMethodName())) {
            if (x.getOwner() != null) {
                x.getOwner().accept(this);
                print(".");
            }
            print(x.getMethodName());
            print("(");
            printAndAccept(x.getParameters(), ", ");
            SQLExpr from = (SQLExpr) x.getAttribute("FROM");
            if (from != null) {
                print(" FROM ");
                from.accept(this);
            }

            SQLExpr forExpr = (SQLExpr) x.getAttribute("FOR");
            if (forExpr != null) {
                print(" FOR ");
                forExpr.accept(this);
            }
            print(")");

            return false;
        }

        if ("TRIM".equalsIgnoreCase(x.getMethodName())) {
            if (x.getOwner() != null) {
                x.getOwner().accept(this);
                print(".");
            }
            print(x.getMethodName());
            print("(");

            String trimType = (String) x.getAttribute("TRIM_TYPE");
            if (trimType != null) {
                print(trimType);
                print(' ');
            }

            printAndAccept(x.getParameters(), ", ");

            SQLExpr from = (SQLExpr) x.getAttribute("FROM");
            if (from != null) {
                print(" FROM ");
                from.accept(this);
            }

            print(")");

            return false;
        }

        if (("CONVERT".equalsIgnoreCase(x.getMethodName()))||"CHAR".equalsIgnoreCase(x.getMethodName())) {
            if (x.getOwner() != null) {
                x.getOwner().accept(this);
                print(".");
            }
            print(x.getMethodName());
            print("(");
            printAndAccept(x.getParameters(), ", ");

            String charset = (String) x.getAttribute("USING");
            if (charset != null) {
                print(" USING ");
                print(charset);
            }
            print(")");
            return false;
        }

        return super.visit(x);
    }

    @Override
    public void endVisit(MySqlIntervalExpr x) {

    }

    @Override
    public boolean visit(MySqlIntervalExpr x) {
        print("INTERVAL ");
        x.getValue().accept(this);
        print(' ');
        print(x.getUnit().name());
        return false;
    }

    @Override
    public boolean visit(MySqlExtractExpr x) {
        print("EXTRACT(");
        print(x.getUnit().name());
        print(" FROM ");
        x.getValue().accept(this);
        print(')');
        return false;
    }

    @Override
    public void endVisit(MySqlExtractExpr x) {

    }

    @Override
    public void endVisit(MySqlMatchAgainstExpr x) {

    }

    @Override
    public boolean visit(MySqlMatchAgainstExpr x) {
        print("MATCH (");
        printAndAccept(x.getColumns(), ", ");
        print(")");

        print(" AGAINST (");
        x.getAgainst().accept(this);
        if (x.getSearchModifier() != null) {
            print(' ');
            print(x.getSearchModifier().name);
        }
        print(')');

        return false;
    }

    @Override
    public void endVisit(MySqlPrepareStatement x) {
    }

    @Override
    public boolean visit(MySqlPrepareStatement x) {
        print("PREPARE ");
        x.getName().accept(this);
        print(" FROM ");
        x.getFrom().accept(this);
        return false;
    }

    @Override
    public void endVisit(MySqlExecuteStatement x) {

    }

    @Override
    public boolean visit(MySqlExecuteStatement x) {
        print("EXECUTE ");
        x.getStatementName().accept(this);
        if (x.getParameters().size() > 0) {
            print(" USING ");
            printAndAccept(x.getParameters(), ", ");
        }
        return false;
    }

    @Override
    public void endVisit(MySqlDeleteStatement x) {

    }

    @Override
    public boolean visit(MySqlDeleteStatement x) {
        print("DELETE ");
        for (String each : x.getIdentifiersBetweenDeleteAndFrom()) {
            print(each);
            print(" ");
        }
        print("FROM ");
        x.getTableSource().accept(this);
        if (x.getWhere() != null) {
            println();
            incrementIndent();
            print("WHERE ");
            x.getWhere().setParent(x);
            x.getWhere().accept(this);
            decrementIndent();
        }

        if (x.getOrderBy() != null) {
            println();
            x.getOrderBy().accept(this);
        }

        if (x.getLimit() != null) {
            println();
            x.getLimit().accept(this);
        }

        return false;
    }

    @Override
    public void endVisit(MySqlInsertStatement x) {

    }

    @Override
    public boolean visit(final MySqlInsertStatement x) {
        print("INSERT ");
        for (String each : x.getIdentifiersBetweenInsertAndInto()) {
            print(each);
            print(" ");
        }
        print("INTO ");
        x.getTableSource().accept(this);
        for (String each : x.getIdentifiersBetweenTableAndValues()) {
            print(" ");
            print(each);
        }
        if (x.getColumns().size() > 0) {
            incrementIndent();
            print(" (");
            for (int i = 0, size = x.getColumns().size(); i < size; ++i) {
                if (i != 0) {
                    if (i % 5 == 0) {
                        println();
                    }
                    print(", ");
                }

                x.getColumns().get(i).accept(this);
            }
            print(")");
            decrementIndent();
        }

        if (x.getValuesList().size() != 0) {
            println();
            printValuesList(x);
        }

        if (x.getQuery() != null) {
            println();
            x.getQuery().accept(this);
        }
        for (String each : x.getAppendices()) {
            print(" ");
            print(each);
        }
        return false;
    }

    protected void printValuesList(MySqlInsertStatement x) {
        print("VALUES ");
        if (x.getValuesList().size() > 1) {
            incrementIndent();
        }
        for (int i = 0, size = x.getValuesList().size(); i < size; ++i) {
            if (i != 0) {
                print(",");
                println();
            }
            x.getValuesList().get(i).accept(this);
        }
        if (x.getValuesList().size() > 1) {
            decrementIndent();
        }
    }

    @Override
    public void endVisit(MySqlSelectGroupBy x) {

    }

    @Override
    public boolean visit(MySqlSelectGroupBy x) {
        super.visit(x);

        if (x.isRollUp()) {
            print(" WITH ROLLUP");
        }

        return false;
    }

    @Override
    public void endVisit(MySqlSelectQueryBlock x) {

    }

    @Override
    public boolean visit(MySqlOutFileExpr x) {
        print("OUTFILE ");
        x.getFile().accept(this);

        if (x.getCharset() != null) {
            print(" CHARACTER SET ");
            print(x.getCharset());
        }

        if (x.getColumnsTerminatedBy() != null || x.getColumnsEnclosedBy() != null || x.getColumnsEscaped() != null) {
            print(" COLUMNS");
            if (x.getColumnsTerminatedBy() != null) {
                print(" TERMINATED BY ");
                x.getColumnsTerminatedBy().accept(this);
            }

            if (x.getColumnsEnclosedBy() != null) {
                if (x.isColumnsEnclosedOptionally()) {
                    print(" OPTIONALLY");
                }
                print(" ENCLOSED BY ");
                x.getColumnsEnclosedBy().accept(this);
            }

            if (x.getColumnsEscaped() != null) {
                print(" ESCAPED BY ");
                x.getColumnsEscaped().accept(this);
            }
        }

        if (x.getLinesStartingBy() != null || x.getLinesTerminatedBy() != null) {
            print(" LINES");
            if (x.getLinesStartingBy() != null) {
                print(" STARTING BY ");
                x.getLinesStartingBy().accept(this);
            }

            if (x.getLinesTerminatedBy() != null) {
                print(" TERMINATED BY ");
                x.getLinesTerminatedBy().accept(this);
            }
        }

        return false;
    }

    @Override
    public void endVisit(MySqlOutFileExpr x) {

    }

    @Override
    public boolean visit(final MySqlUpdateStatement x) {
        print("UPDATE ");
        for (String each : x.getIdentifiersBetweenUpdateAndTable()) {
            print(each);
            print(" ");
        }
        x.getTableSource().accept(this);
        println();
        print("SET ");
        for (int i = 0, size = x.getItems().size(); i < size; ++i) {
            if (i != 0) {
                print(", ");
            }
            x.getItems().get(i).accept(this);
        }

        if (x.getWhere() != null) {
            println();
            incrementIndent();
            print("WHERE ");
            x.getWhere().setParent(x);
            x.getWhere().accept(this);
            decrementIndent();
        }

        if (x.getOrderBy() != null) {
            println();
            x.getOrderBy().accept(this);
        }

        if (x.getLimit() != null) {
            println();
            x.getLimit().accept(this);
        }
        return false;
    }

    @Override
    public void endVisit(MySqlUpdateStatement x) {

    }

    @Override
    public boolean visit(MySqlSetTransactionStatement x) {
        if (x.getGlobal() == null) {
            print("SET TRANSACTION ");
        } else if (x.getGlobal().booleanValue()) {
            print("SET GLOBAL TRANSACTION ");
        } else {
            print("SET SESSION TRANSACTION ");
        }

        if (x.getIsolationLevel() != null) {
            print("ISOLATION LEVEL ");
            print(x.getIsolationLevel());
        }

        if (x.getAccessModel() != null) {
            print("READ ");
            print(x.getAccessModel());
        }

        return false;
    }

    @Override
    public void endVisit(MySqlSetTransactionStatement x) {
        
    }

    @Override
    public boolean visit(MySqlSetNamesStatement x) {
        print("SET NAMES ");
        if (x.isDefault()) {
            print("DEFAULT");
        } else {
            print(x.getCharSet());
            if (x.getCollate() != null) {
                print(" COLLATE ");
                print(x.getCollate());
            }
        }
        return false;
    }

    @Override
    public void endVisit(MySqlSetNamesStatement x) {

    }

    @Override
    public boolean visit(MySqlSetCharSetStatement x) {
        print("SET CHARACTER SET ");
        if (x.isDefault()) {
            print("DEFAULT");
        } else {
            print(x.getCharSet());
            if (x.getCollate() != null) {
                print(" COLLATE ");
                print(x.getCollate());
            }
        }
        return false;
    }

    @Override
    public void endVisit(MySqlSetCharSetStatement x) {

    }

    @Override
    public boolean visit(MySqlUserName x) {
        print(x.getUserName());
        if (x.getHost() != null) {
            print('@');
            print(x.getHost());
        }
        return false;
    }

    @Override
    public void endVisit(MySqlUserName x) {

    }

    @Override
    public boolean visit(MySqlUnionQuery x) {
        {
            boolean needParen = false;
            if (x.getLeft() instanceof MySqlSelectQueryBlock) {
                MySqlSelectQueryBlock right = (MySqlSelectQueryBlock) x.getLeft();
                if (right.getOrderBy() != null || right.getLimit() != null) {
                    needParen = true;
                }
            }
            if (needParen) {
                print('(');
                x.getLeft().accept(this);
                print(')');
            } else {
                x.getLeft().accept(this);
            }
        }
        println();
        print(x.getOperator().getText());
        println();

        boolean needParen = false;

        if (x.getOrderBy() != null || x.getLimit() != null) {
            needParen = true;
        } else if (x.getRight() instanceof MySqlSelectQueryBlock) {
            MySqlSelectQueryBlock right = (MySqlSelectQueryBlock) x.getRight();
            if (right.getOrderBy() != null || right.getLimit() != null) {
                needParen = true;
            }
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

        if (x.getLimit() != null) {
            println();
            x.getLimit().accept(this);
        }

        return false;
    }

    @Override
    public void endVisit(MySqlUnionQuery x) {

    }

    @Override
    public boolean visit(MySqlUseIndexHint x) {
        print("USE INDEX ");
        if (x.getOption() != null) {
            print("FOR ");
            print(x.getOption().name);
            print(' ');
        }
        print('(');
        printAndAccept(x.getIndexList(), ", ");
        print(')');
        return false;
    }

    @Override
    public void endVisit(MySqlUseIndexHint x) {

    }

    @Override
    public boolean visit(MySqlIgnoreIndexHint x) {
        print("IGNORE INDEX ");
        if (x.getOption() != null) {
            print("FOR ");
            print(x.getOption().name);
            print(' ');
        }
        print('(');
        printAndAccept(x.getIndexList(), ", ");
        print(')');
        return false;
    }

    @Override
    public void endVisit(MySqlIgnoreIndexHint x) {

    }

    public boolean visit(SQLExprTableSource x) {
        x.getExpr().accept(this);

        if (x.getAlias() != null) {
            print(' ');
            print(x.getAlias());
        }

        for (int i = 0; i < x.getHints().size(); ++i) {
            print(' ');
            x.getHints().get(i).accept(this);
        }

        return false;
    }

    @Override
    public boolean visit(MySqlUnlockTablesStatement x) {
        print("UNLOCK TABLES");
        return false;
    }

    @Override
    public void endVisit(MySqlUnlockTablesStatement x) {

    }

    @Override
    public boolean visit(MySqlForceIndexHint x) {
        print("FORCE INDEX ");
        if (x.getOption() != null) {
            print("FOR ");
            print(x.getOption().name);
            print(' ');
        }
        print('(');
        printAndAccept(x.getIndexList(), ", ");
        print(')');
        return false;
    }

    @Override
    public void endVisit(MySqlForceIndexHint x) {

    }
    
    @Override
    public boolean visit(MySqlCharExpr x) {
        print(x.toString());
        return false;
    }

    @Override
    public void endVisit(MySqlCharExpr x) {

    }

    @Override
    public boolean visit(SQLAssignItem x) {
        x.getTarget().accept(this);
        if (!"NAMES".equalsIgnoreCase(x.getTarget().toString())) {
            print(" = ");
        }
        x.getValue().accept(this);
        return false;
    }

    @Override
    public boolean visit(MySqlPartitioningDef x) {
        print("PARTITION ");
        x.getName().accept(this);
        if (x.getValues() != null) {
            print(' ');
            x.getValues().accept(this);
        }

        if (x.getDataDirectory() != null) {
            incrementIndent();
            println();
            print("DATA DIRECTORY ");
            x.getDataDirectory().accept(this);
            decrementIndent();
        }
        if (x.getIndexDirectory() != null) {
            incrementIndent();
            println();
            print("INDEX DIRECTORY ");
            x.getIndexDirectory().accept(this);
            decrementIndent();
        }

        return false;
    }

    @Override
    public void endVisit(MySqlPartitioningDef x) {

    }

    @Override
    public boolean visit(LessThanValues x) {
        print("VALUES LESS THAN (");
        printAndAccept(x.getItems(), ", ");
        print(')');
        return false;
    }

    @Override
    public void endVisit(LessThanValues x) {

    }

    @Override
    public boolean visit(InValues x) {
        print("VALUES IN (");
        printAndAccept(x.getItems(), ", ");
        print(')');
        return false;
    }

    @Override
    public void endVisit(InValues x) {

    }

    protected void visitAggreateRest(SQLAggregateExpr aggregateExpr) {
        {
            SQLOrderBy value = (SQLOrderBy) aggregateExpr.getAttribute("ORDER BY");
            if (value != null) {
                print(" ");
                ((SQLObject) value).accept(this);
            }
        }
        {
            Object value = aggregateExpr.getAttribute("SEPARATOR");
            if (value != null) {
                print(" SEPARATOR ");
                ((SQLObject) value).accept(this);
            }
        }
    }

    @Override
    public boolean visit(MySqlSetPasswordStatement x) {
        print("SET PASSWORD ");

        if (x.getUser() != null) {
            print("FOR ");
            x.getUser().accept(this);
            print(' ');
        }

        print("= ");

        if (x.getPassword() != null) {
            x.getPassword().accept(this);
        }
        return false;
    }

    @Override
    public void endVisit(MySqlSetPasswordStatement x) {

    }

    @Override
    public boolean visit(MySqlHintStatement x) {
        List<SQLCommentHint> hints = x.getHints();

        for (SQLCommentHint hint : hints) {
            hint.accept(this);
        }
        return false;
    }

    @Override
    public void endVisit(MySqlHintStatement x) {
        
    }
    
    @Override
    public boolean visit(MySqlSelectGroupByExpr x) {
        x.getExpr().accept(this);
        if (x.getType() != null) {
            print(" ");
            print(x.getType().name().toUpperCase());
        }

        return false;
    }

    @Override
    public void endVisit(MySqlSelectGroupByExpr x) {

    }

    @Override
    public boolean visit(MySqlParameter x) {
        if (x.getDataType().getName().equalsIgnoreCase("CURSOR")) {
            print("CURSOR ");
            x.getName().accept(this);
            print(" IS");
            incrementIndent();
            println();
            SQLSelect select = ((SQLQueryExpr) x.getDefaultValue()).getSubQuery();
            select.accept(this);
            decrementIndent();

        } else {
            
            if(x.getParamType() == ParameterType.IN) {
                print("IN ");
            } else if(x.getParamType() == ParameterType.OUT) {
                print("OUT ");
            } else if(x.getParamType() == ParameterType.INOUT) {
                print("INOUT ");
            }
            x.getName().accept(this);
            print(" ");

            x.getDataType().accept(this);

            if (x.getDefaultValue() != null) {
                print(" := ");
                x.getDefaultValue().accept(this);
            }
        }

        return false;
    }

    @Override
    public void endVisit(MySqlParameter x) {
        
    }

    @Override
    public boolean visit(MySqlWhileStatement x) {
        if(x.getLabelName()!=null&&!x.getLabelName().equals(""))
            print(x.getLabelName()+": ");
        print("WHILE ");
        x.getCondition().accept(this);
        print(" DO");
        println();
        for (int i = 0, size = x.getStatements().size(); i < size; ++i) {
            SQLStatement item = x.getStatements().get(i);
            item.setParent(x);
            item.accept(this);
            if (i != size - 1) {
                println();
            }
        }
        println();
        print("END WHILE");
        if(x.getLabelName()!=null&&!x.getLabelName().equals(""))
            print(" "+x.getLabelName());
        return false;
    }

    @Override
    public void endVisit(MySqlWhileStatement x) {
        
    }

    @Override
    public boolean visit(MySqlIfStatement x) {
        print("IF ");
        x.getCondition().accept(this);
        print(" THEN");
        println();
        for (int i = 0, size = x.getStatements().size(); i < size; ++i) {
            SQLStatement item = x.getStatements().get(i);
            item.setParent(x);
            item.accept(this);
            if (i != size - 1) {
                println();
            }
        }
        println();
        for (MySqlElseIfStatement iterable_element : x.getElseIfList()) {
            iterable_element.accept(this);
        }
        
        if(x.getElseItem()!=null)
            x.getElseItem().accept(this);
        
        print("END IF");
        return false;
    }

    @Override
    public void endVisit(MySqlIfStatement x) {
        
    }

    @Override
    public boolean visit(MySqlElseIfStatement x) {
        print("ELSE IF ");
        x.getCondition().accept(this);
        print(" THEN");
        println();
        for (int i = 0, size = x.getStatements().size(); i < size; ++i) {
            SQLStatement item = x.getStatements().get(i);
            item.setParent(x);
            item.accept(this);
            if (i != size - 1) {
                println();
            }
        }
        println();
        return false;
    }

    @Override
    public void endVisit(MySqlElseIfStatement x) {
        
    }

    @Override
    public boolean visit(MySqlElseStatement x) {
        print("ELSE ");
        println();
        for (int i = 0, size = x.getStatements().size(); i < size; ++i) {
            SQLStatement item = x.getStatements().get(i);
            item.setParent(x);
            item.accept(this);
            if (i != size - 1) {
                println();
            }
        }
        println();
        return false;
    }

    @Override
    public void endVisit(MySqlElseStatement x) {
        
    }

    @Override
    public boolean visit(MySqlCaseStatement x) {
        
        print("CASE ");
        x.getCondition().accept(this);
        println();
        for (int i = 0; i < x.getWhenList().size(); i++) {
            x.getWhenList().get(i).accept(this);
        }
        if(x.getElseItem()!=null)
            x.getElseItem().accept(this);
        print("END CASE");
        return false;
    }

    @Override
    public void endVisit(MySqlCaseStatement x) {
        
    }

    @Override
    public boolean visit(MySqlSelectIntoStatement x) {
        x.getSelect().accept(this);
        print(" INTO ");
        for (int i = 0; i < x.getVarList().size(); i++) {
            x.getVarList().get(i).accept(this);
            if(i!=x.getVarList().size()-1)
                print(",");
        }
        return false;
    }

    @Override
    public void endVisit(MySqlSelectIntoStatement x) {
        
    }

    @Override
    public boolean visit(MySqlWhenStatement x) {
        print("WHEN ");
        x.getCondition().accept(this);
        println(" THEN");
        for (int i = 0; i < x.getStatements().size(); i++) {
            x.getStatements().get(i).accept(this);
            if (i != x.getStatements().size() - 1) {
                println();
            }
        }
        println();
        return false;
    }

    @Override
    public void endVisit(MySqlWhenStatement x) {
        
    }

    @Override
    public boolean visit(MySqlLoopStatement x) {
        if(x.getLabelName()!=null&&!x.getLabelName().equals(""))
            print(x.getLabelName()+": ");
        print("LOOP ");
        println();
        for (int i = 0, size = x.getStatements().size(); i < size; ++i) {
            SQLStatement item = x.getStatements().get(i);
            item.setParent(x);
            item.accept(this);
            if (i != size - 1) {
                println();
            }
        }
        println();
        print("END LOOP");
        if(x.getLabelName()!=null&&!x.getLabelName().equals(""))
            print(" "+x.getLabelName());
        return false;
    }

    @Override
    public void endVisit(MySqlLoopStatement x) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean visit(MySqlLeaveStatement x) {
        print("LEAVE "+x.getLabelName());
        return false;
    }

    @Override
    public void endVisit(MySqlLeaveStatement x) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean visit(MySqlIterateStatement x) {
        print("ITERATE "+x.getLabelName());
        return false;
    }

    @Override
    public void endVisit(MySqlIterateStatement x) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean visit(MySqlRepeatStatement x) {
        // TODO Auto-generated method stub
        if(x.getLabelName()!=null&&!x.getLabelName().equals(""))
            print(x.getLabelName()+": ");
        print("REPEAT ");
        println();
        for (int i = 0, size = x.getStatements().size(); i < size; ++i) {
            SQLStatement item = x.getStatements().get(i);
            item.setParent(x);
            item.accept(this);
            if (i != size - 1) {
                println();
            }
        }
        println();
        print("UNTIL ");
        x.getCondition().accept(this);
        println();
        print("END REPEAT");
        if(x.getLabelName()!=null&&!x.getLabelName().equals(""))
            print(" "+x.getLabelName());
        return false;
    }

    @Override
    public void endVisit(MySqlRepeatStatement x) {
        
    }

    @Override
    public boolean visit(MySqlUpdateTableSource x) {
        MySqlUpdateStatement update = x.getUpdate();
        if (update != null) {
            update.accept0(this);
        }
        return false;
    }

    @Override
    public void endVisit(MySqlUpdateTableSource x) {

    }
}
