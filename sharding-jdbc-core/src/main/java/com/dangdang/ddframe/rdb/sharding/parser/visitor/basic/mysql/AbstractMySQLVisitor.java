/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
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
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.parser.visitor.basic.mysql;

import com.alibaba.druid.sql.ast.SQLHint;
import com.alibaba.druid.sql.ast.expr.SQLBetweenExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlOutputVisitor;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition.BinaryOperator;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.SQLBuilder;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Table;
import com.dangdang.ddframe.rdb.sharding.parser.visitor.ParseContext;
import com.dangdang.ddframe.rdb.sharding.parser.visitor.SQLVisitor;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;

import java.util.Arrays;
import java.util.Collections;

/**
 * MySQL解析基础访问器.
 * 
 * @author zhangliang
 */
public abstract class AbstractMySQLVisitor extends MySqlOutputVisitor implements SQLVisitor {
    
    private ParseContext parseContext;
    
    private int parseContextIndex;
    
    protected AbstractMySQLVisitor() {
        super(new SQLBuilder());
        setPrettyFormat(false);
        parseContext = new ParseContext(parseContextIndex);
    }
    
    @Override
    public final DatabaseType getDatabaseType() {
        return DatabaseType.MySQL;
    }
    
    @Override
    public final ParseContext getParseContext() {
        return parseContext;
    }
    
    protected final void stepInQuery() {
        if (0 == parseContextIndex) {
            parseContextIndex++;
            return;
        }
        ParseContext parseContext = new ParseContext(parseContextIndex++);
        parseContext.setShardingColumns(this.parseContext.getShardingColumns());
        parseContext.setParentParseContext(this.parseContext);
        this.parseContext.getSubParseContext().add(parseContext);
        this.parseContext = parseContext;
    }
    
    protected final void stepOutQuery() {
        if (null == parseContext.getParentParseContext()) {
            return;
        }
        parseContext = parseContext.getParentParseContext();
    }
    
    @Override
    public final SQLBuilder getSQLBuilder() {
        return (SQLBuilder) appender;
    }
    
    @Override
    public final void printToken(final String token) {
        getSQLBuilder().appendToken(SQLUtil.getExactlyValue(token));
    }
    
    /**
     * 父类使用<tt>@@</tt>代替<tt>?</tt>,此处直接输出参数占位符<tt>?</tt>
     * 
     * @param x 变量表达式
     * @return false 终止遍历AST
     */
    @Override
    public final boolean visit(final SQLVariantRefExpr x) {
        print(x.getName());
        return false;
    }
    
    @Override
    public final boolean visit(final SQLExprTableSource x) {
        if ("dual".equalsIgnoreCase(SQLUtil.getExactlyValue(x.getExpr().toString()))) {
            return super.visit(x);
        }
        return visit(x, getParseContext().addTable(x));
    }
    
    private boolean visit(final SQLExprTableSource x, final Table table) {
        printToken(table.getName());
        if (table.getAlias().isPresent()) {
            print(' ');
            print(table.getAlias().get());
        }
        for (SQLHint each : x.getHints()) {
            print(' ');
            each.accept(this);
        }
        return false;
    }
    
    /**
     * 将表名替换成占位符.
     * 
     * <p>
     * 1. 如果二元表达式使用别名, 如: 
     * {@code FROM order o WHERE o.column_name = 't' }, 则Column中的tableName为o.
     * </p>
     * 
     * <p>
     * 2. 如果二元表达式使用表名, 如: 
     * {@code FROM order WHERE order.column_name = 't' }, 则Column中的tableName为order.
     * </p>
     * 
     * @param x SQL属性表达式
     * @return true表示继续遍历AST, false表示终止遍历AST
     */
    @Override
    // TODO SELECT [别名.xxx]的情况，目前都是替换成token，解析之后应该替换回去
    public final boolean visit(final SQLPropertyExpr x) {
        if (!(x.getParent() instanceof SQLBinaryOpExpr) && !(x.getParent() instanceof SQLSelectItem)) {
            return super.visit(x);
        }
        if (!(x.getOwner() instanceof SQLIdentifierExpr)) {
            return super.visit(x);
        }
        String tableOrAliasName = ((SQLIdentifierExpr) x.getOwner()).getLowerName();
        if (getParseContext().isBinaryOperateWithAlias(x, tableOrAliasName)) {
            return super.visit(x);
        }
        printToken(tableOrAliasName);
        print(".");
        print(x.getName());
        return false;
    }
    
    @Override
    public boolean visit(final SQLBinaryOpExpr x) {
        switch (x.getOperator()) {
            case BooleanOr: 
                parseContext.setHasOrCondition(true);
                break;
            case Equality: 
                parseContext.addCondition(x.getLeft(), BinaryOperator.EQUAL, Collections.singletonList(x.getRight()), getDatabaseType(), getParameters());
                parseContext.addCondition(x.getRight(), BinaryOperator.EQUAL, Collections.singletonList(x.getLeft()), getDatabaseType(), getParameters());
                break;
            default:
                break;
        }
        return super.visit(x);
    }
    
    @Override
    public boolean visit(final SQLInListExpr x) {
        if (!x.isNot()) {
            parseContext.addCondition(x.getExpr(), BinaryOperator.IN, x.getTargetList(), getDatabaseType(), getParameters());
        }
        return super.visit(x);
    }
    
    @Override
    public boolean visit(final SQLBetweenExpr x) {
        parseContext.addCondition(x.getTestExpr(), BinaryOperator.BETWEEN, Arrays.asList(x.getBeginExpr(), x.getEndExpr()), getDatabaseType(), getParameters());
        return super.visit(x);
    }
}
