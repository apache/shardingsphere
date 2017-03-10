package com.alibaba.druid.sql.dialect.mysql.parser;

import com.alibaba.druid.sql.dialect.mysql.lexer.MySQLKeyword;
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
        while (getExprParser().getLexer().equalToken(MySQLKeyword.LOW_PRIORITY) || getExprParser().getLexer().equalToken(MySQLKeyword.IGNORE)) {
            getExprParser().getLexer().nextToken();
        }
    }
}
