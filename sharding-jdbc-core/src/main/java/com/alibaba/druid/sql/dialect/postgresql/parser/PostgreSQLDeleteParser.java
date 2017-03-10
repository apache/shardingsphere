package com.alibaba.druid.sql.dialect.postgresql.parser;

import com.alibaba.druid.sql.dialect.postgresql.lexer.PostgreSQLKeyword;
import com.alibaba.druid.sql.lexer.DefaultKeyword;
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
        getExprParser().getLexer().skipIfEqual(DefaultKeyword.FROM);
        getExprParser().getLexer().skipIfEqual(PostgreSQLKeyword.ONLY);
    }
}
