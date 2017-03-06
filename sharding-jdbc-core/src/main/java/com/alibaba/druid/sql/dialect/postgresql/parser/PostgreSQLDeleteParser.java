package com.alibaba.druid.sql.dialect.postgresql.parser;

import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.AbstractDeleteParser;
import com.alibaba.druid.sql.parser.SQLExprParser;

/**
 * PostgreSQL Delete语句解析器.
 *
 * @author zhangliang
 */
public final class PostgreSQLDeleteParser extends AbstractDeleteParser {
    
    public PostgreSQLDeleteParser(final SQLExprParser exprParser) {
        super(exprParser);
    }
    
    @Override
    protected void skipBetweenDeleteAndTable() {
        getExprParser().getLexer().skipIfEqual(Token.FROM);
        getExprParser().getLexer().skipIfEqual(Token.ONLY);
    }
}
