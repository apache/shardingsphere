package com.alibaba.druid.sql.dialect.postgresql.parser;

import com.alibaba.druid.sql.ast.statement.AbstractSQLUpdateStatement;
import com.alibaba.druid.sql.dialect.postgresql.ast.stmt.PGUpdateStatement;
import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.AbstractUpdateParser;
import com.alibaba.druid.sql.parser.SQLExprParser;

import java.util.Set;
import java.util.TreeSet;

/**
 * PostgreSQL Update语句解析器.
 *
 * @author zhangliang
 */
public final class PostgreSQLUpdateParser extends AbstractUpdateParser {
    
    public PostgreSQLUpdateParser(final SQLExprParser exprParser) {
        super(exprParser);
    }
    
    @Override
    protected PGUpdateStatement createUpdateStatement() {
        return new PGUpdateStatement();
    }
    
    @Override
    protected Set<String> getIdentifiersBetweenUpdateAndTable() {
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.add(Token.ONLY.getName());
        return result;
    }
    
    @Override
    protected void parseCustomizedParserBetweenSetAndWhere(final AbstractSQLUpdateStatement updateStatement) {
        if (getLexer().equalToken(Token.FROM)) {
            getLexer().nextToken();
            ((PGUpdateStatement) updateStatement).setFrom(getExprParser().createSelectParser().parseTableSource());
        }
    }
    
    @Override
    protected Set<String> getAppendixIdentifiers() {
        Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        result.add(Token.RETURNING.getName());
        return result;
    }
}
