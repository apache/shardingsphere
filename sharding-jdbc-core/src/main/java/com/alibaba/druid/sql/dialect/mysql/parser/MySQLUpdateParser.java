package com.alibaba.druid.sql.dialect.mysql.parser;

import com.alibaba.druid.sql.lexer.Token;
import com.alibaba.druid.sql.parser.AbstractUpdateParser;
import com.alibaba.druid.sql.parser.SQLExprParser;

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
    protected void skipBetweenUpdateAndTable() {
        while (getExprParser().getLexer().equalToken(Token.LOW_PRIORITY) || getExprParser().getLexer().equalToken(Token.IGNORE)) {
            getExprParser().getLexer().nextToken();
        }
    }
}
