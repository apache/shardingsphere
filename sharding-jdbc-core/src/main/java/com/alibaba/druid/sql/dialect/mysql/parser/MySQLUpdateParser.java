package com.alibaba.druid.sql.dialect.mysql.parser;

import com.alibaba.druid.sql.ast.statement.AbstractSQLUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import com.alibaba.druid.sql.parser.AbstractUpdateParser;
import com.alibaba.druid.sql.parser.SQLExprParser;

import java.util.Set;
import java.util.TreeSet;

/**
 * MySQL Update语句解析器.
 *
 * @author zhangliang
 */
public final class MySQLUpdateParser extends AbstractUpdateParser {
    
    public MySQLUpdateParser(final SQLExprParser exprParser) {
        super(exprParser);
    }
    
    @Override
    protected MySqlUpdateStatement createUpdateStatement() {
        return new MySqlUpdateStatement();
    }
    
    @Override
    protected Set<String> getIdentifiersBetweenUpdateAndTable() {
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.add(MySqlKeyword.LOW_PRIORITY);
        result.add(MySqlKeyword.IGNORE);
        return result;
    }
    
    @Override
    protected void parseCustomizedParserAfterWhere(final AbstractSQLUpdateStatement updateStatement) {
        ((MySqlUpdateStatement) updateStatement).setOrderBy(getExprParser().parseOrderBy());
        ((MySqlUpdateStatement) updateStatement).setLimit(((MySqlExprParser) getExprParser()).parseLimit());
    }
}
