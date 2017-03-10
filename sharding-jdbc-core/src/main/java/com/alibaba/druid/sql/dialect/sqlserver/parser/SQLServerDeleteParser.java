package com.alibaba.druid.sql.dialect.sqlserver.parser;

import com.alibaba.druid.sql.lexer.DefaultKeyword;
import com.alibaba.druid.sql.parser.AbstractDeleteParser;
import com.alibaba.druid.sql.parser.SQLExprParser;

/**
 * SQLServer Delete语句解析器.
 *
 * @author zhangliang
 */
public final class SQLServerDeleteParser extends AbstractDeleteParser {
    
    public SQLServerDeleteParser(final SQLExprParser exprParser) {
        super(exprParser);
    }
    
    @Override
    protected void skipBetweenDeleteAndTable() {
        ((SQLServerExprParser) getExprParser()).parseTop();
        ((SQLServerExprParser) getExprParser()).skipOutput();
        getExprParser().getLexer().skipIfEqual(DefaultKeyword.FROM);
    }
}
