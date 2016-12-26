package com.alibaba.druid.sql.dialect.sqlserver.parser;

import com.alibaba.druid.sql.dialect.sqlserver.ast.stmt.SQLServerInsertStatement;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.AbstractInsertParser;

import java.util.Set;
import java.util.TreeSet;

/**
 * SQLServer Insert语句解析器.
 *
 * @author zhangliang
 */
public final class SQLServerInsertParser extends AbstractInsertParser {
    
    public SQLServerInsertParser(final SQLExprParser exprParser) {
        super(exprParser);
    }
    
    @Override
    protected SQLServerInsertStatement createSQLInsertStatement() {
        return new SQLServerInsertStatement();
    }
    
    @Override
    protected Set<String> getIdentifiersBetweenIntoAndTable() {
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.add("OUTPUT");
        return result;
    }
    
    @Override
    protected Set<String> getAppendixIdentifiers() {
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.add(Token.DEFAULT.getName());
        return result;
    }
}
