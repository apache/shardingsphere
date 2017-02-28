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

import com.alibaba.druid.sql.ast.SQLDataType;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLAssignItem;
import com.alibaba.druid.sql.ast.statement.SQLCharacterDataType;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlCharExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlExtractExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlIntervalExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlMatchAgainstExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlSelectGroupByExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlUserName;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock.Limit;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUnionQuery;
import com.alibaba.druid.sql.visitor.SQLASTOutputVisitor;

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
        } else if (x.getCollate() != null) {
            print(" COLLATE ");
            print(x.getCollate());
        }
        
        return false;
    }

    @Override
    public void endVisit(Limit x) {

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

    public boolean visit(final SQLVariantRefExpr x) {
        {
            int index = x.getIndex();
            if (index >= 0 && index < getParameters().size()) {
                Object param = this.getParameters().get(index);
                printParameter(param);
                return false;
            }
        }
        String varName = x.getName();
        if (x.isGlobal()) {
            print("@@global.");
        } else {
            if ((!varName.startsWith("@")) && (!varName.equals("?")) && (!varName.startsWith("#")) && (!varName.startsWith("$")) && (!varName.startsWith(":"))) {
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

        if (("CONVERT".equalsIgnoreCase(x.getMethodName())) || "CHAR".equalsIgnoreCase(x.getMethodName())) {
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
            print(x.getSearchModifier().getName());
        }
        print(')');

        return false;
    }

    @Override
    public void endVisit(MySqlSelectQueryBlock x) {

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

    public boolean visit(SQLExprTableSource x) {
        x.getExpr().accept(this);

        if (x.getAlias() != null) {
            print(' ');
            print(x.getAlias());
        }
        return false;
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
}
