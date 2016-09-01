package com.dangdang.ddframe.rdb.sharding.parser.visitor.basic.sqlserver;

import com.alibaba.druid.sql.dialect.sqlserver.ast.stmt.SQLServerInsertStatement;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition.BinaryOperator;
import com.google.common.base.Optional;

/**
 * SQLServer的INSERT语句访问器.
 * 
 * @author CNJUN
 */
public class SQLServerInsertVisitor extends AbstractSQLServerVisitor {
    
    @Override
    public boolean visit(final SQLServerInsertStatement x) {
        getParseContext().setCurrentTable(x.getTableName().toString(), Optional.fromNullable(x.getAlias()));
        if (null == x.getValues()) {
            return super.visit(x);
        }
        for (int i = 0; i < x.getColumns().size(); i++) {
            getParseContext().addCondition(x.getColumns().get(i).toString(), x.getTableName().toString(), BinaryOperator.EQUAL, x.getValues().getValues().get(i), getDatabaseType(), getParameters());
        }
        return super.visit(x);
    }
}
