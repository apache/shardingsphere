package com.alibaba.druid.sql.dialect.postgresql.parser;

import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGInsertStatement;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.AbstractInsertParser;

import java.util.Set;
import java.util.TreeSet;

/**
 * PostgreSQL Insert语句解析器.
 *
 * @author zhangliang
 */
public final class PostgreSQLInsertParser extends AbstractInsertParser {
    
    public PostgreSQLInsertParser(final SQLExprParser exprParser) {
        super(exprParser);
    }
    
    @Override
    protected PGInsertStatement createSQLInsertStatement() {
        return new PGInsertStatement();
    }
    
    @Override
    protected Set<String> getAppendixIdentifiers() {
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.add(Token.DEFAULT.getName());
        result.add(Token.RETURNING.getName());
        return result;
    }
}
