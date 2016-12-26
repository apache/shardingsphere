package com.alibaba.druid.sql.dialect.oracle.parser;

import com.alibaba.druid.sql.dialect.oracle.ast.stmt.OracleInsertStatement;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.SQLExprParser;
import com.alibaba.druid.sql.parser.AbstractInsertParser;

import java.util.Set;
import java.util.TreeSet;

/**
 * Oracle Insert语句解析器.
 *
 * @author zhangliang
 */
public final class OracleInsertParser extends AbstractInsertParser {
    
    public OracleInsertParser(final SQLExprParser exprParser) {
        super(exprParser);
    }
    
    @Override
    protected OracleInsertStatement createSQLInsertStatement() {
        OracleInsertStatement result = new OracleInsertStatement();
        result.getHints().addAll(getExprParser().parseHints());
        return result;
    }
    
    protected Set<String> getUnsupportedIdentifiers() {
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.add(Token.ALL.getName());
        result.add(Token.FIRST.getName());
        return result;
    }
    
    @Override
    protected Set<String> getAppendixIdentifiers() {
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.add(Token.RETURNING.getName());
        result.add("LOG");
        return result;
    }
}
