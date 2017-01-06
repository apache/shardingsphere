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

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumberExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.parser.result.GeneratedKeyContext;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition.BinaryOperator;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;

import java.util.Collection;
import java.util.List;

/**
 * MySQL的INSERT语句访问器.
 * 
 * @author gaohongtao
 * @author zhangliang
 */
public class MySQLInsertVisitor extends AbstractMySQLVisitor {
    
    @Override
    public boolean visit(final MySqlInsertStatement x) {
        final String tableName = SQLUtil.getExactlyValue(x.getTableSource().getExpr().toString());
        getParseContext().setCurrentTable(tableName, Optional.fromNullable(x.getTableSource().getAlias()));
        if (x.getValuesList().isEmpty()) {
            return super.visit(x);
        }
        Collection<String> autoIncrementColumns = getParseContext().getShardingRule().getAutoIncrementColumns(tableName);
        List<SQLExpr> columns = x.getColumns();
        List<SQLExpr> values = x.getValuesList().get(0).getValues();
        for (int i = 0; i < x.getColumns().size(); i++) {
            String columnName = SQLUtil.getExactlyValue(columns.get(i).toString());
            getParseContext().addCondition(columnName, tableName, BinaryOperator.EQUAL, values.get(i), getDatabaseType(), getParameters());
            if (autoIncrementColumns.contains(columnName)) {
                autoIncrementColumns.remove(columnName);
            }
        }
        if (autoIncrementColumns.isEmpty()) {
            return super.visit(x);
        }
        supplyAutoIncrementColumn(autoIncrementColumns, tableName, columns, values);
        return super.visit(x);
    }
    
    private void supplyAutoIncrementColumn(final Collection<String> autoIncrementColumns, final String tableName, final List<SQLExpr> columns, final List<SQLExpr> values) {
        boolean isPreparedStatement = !getParameters().isEmpty();
        GeneratedKeyContext generatedKeyContext = getParseContext().getParsedResult().getGeneratedKeyContext();
        if (isPreparedStatement) {
            generatedKeyContext.getColumns().addAll(autoIncrementColumns);
        }
        TableRule tableRule = getParseContext().getShardingRule().findTableRule(tableName);
        for (String each : autoIncrementColumns) {
            SQLExpr sqlExpr;
            Object id = tableRule.generateId(each);
            generatedKeyContext.putValue(each, id);
            if (isPreparedStatement) {
                sqlExpr = new SQLVariantRefExpr("?");
                getParameters().add(id);
                ((SQLVariantRefExpr) sqlExpr).setIndex(getParameters().size() - 1);
            } else {
                sqlExpr = (id instanceof Number) ? new SQLNumberExpr((Number) id) : new SQLCharExpr((String) id);
            }
            getParseContext().addCondition(each, tableName, BinaryOperator.EQUAL, sqlExpr, getDatabaseType(), getParameters());
            columns.add(new SQLIdentifierExpr(each));
            values.add(sqlExpr);
        }
    }
}
