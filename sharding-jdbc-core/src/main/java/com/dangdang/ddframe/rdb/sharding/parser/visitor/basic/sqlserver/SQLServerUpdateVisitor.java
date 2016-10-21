package com.dangdang.ddframe.rdb.sharding.parser.visitor.basic.sqlserver;

import com.alibaba.druid.sql.dialect.sqlserver.ast.stmt.SQLServerUpdateStatement;
import com.google.common.base.Optional;

/**
 * SQLServer的UPDATE语句访问器.
 * 
 * @author CNJUN
 */
public class SQLServerUpdateVisitor extends AbstractSQLServerVisitor {
    
    @Override
    public boolean visit(final SQLServerUpdateStatement x) {
        getParseContext().setCurrentTable(x.getTableName().toString(), Optional.<String>absent());
        return super.visit(x);
    }
}