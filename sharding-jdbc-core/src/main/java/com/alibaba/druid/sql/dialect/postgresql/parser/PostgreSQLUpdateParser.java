package com.alibaba.druid.sql.dialect.postgresql.parser;

import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.AbstractUpdateParser;
import com.alibaba.druid.sql.parser.SQLExprParser;

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
    protected void skipBetweenUpdateAndTable() {
        getExprParser().getLexer().skipIfEqual(Token.ONLY);
    }
}
