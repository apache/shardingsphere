package com.dangdang.ddframe.rdb.sharding.parser.visitor.basic.sqlserver;

import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.google.common.base.Optional;

/**
 * SQLServer的DELETE语句访问器.
 * 
 * @author CNJUN
 */
public class SQLServerDeleteVisitor extends AbstractSQLServerVisitor {
    
    @Override
    public boolean visit(final SQLDeleteStatement x) {
        getParseContext().setCurrentTable(x.getTableName().toString(), Optional.fromNullable(x.getAlias()));
        return super.visit(x);
    }
}
