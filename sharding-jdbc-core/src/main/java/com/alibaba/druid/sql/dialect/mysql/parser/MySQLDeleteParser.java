package com.alibaba.druid.sql.dialect.mysql.parser;

import com.alibaba.druid.sql.dialect.mysql.lexer.MySQLKeyword;
import com.alibaba.druid.sql.lexer.DefaultKeyword;
import com.alibaba.druid.sql.parser.AbstractDeleteParser;
import com.alibaba.druid.sql.parser.SQLExprParser;

/**
 * MySQL Delete语句解析器.
 *
 * @author zhangliang
 */
public final class MySQLDeleteParser extends AbstractDeleteParser {
    
    public MySQLDeleteParser(final SQLExprParser exprParser) {
        super(exprParser);
    }
    
    @Override
    protected void skipBetweenDeleteAndTable() {
        while (getExprParser().getLexer().equalToken(MySQLKeyword.LOW_PRIORITY) || getExprParser().getLexer().equalToken(MySQLKeyword.QUICK) || getExprParser().getLexer().equalToken(MySQLKeyword.IGNORE)) {
            getExprParser().getLexer().nextToken();
        }
        getExprParser().getLexer().skipIfEqual(DefaultKeyword.FROM);
    }
}
