package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.clause;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.mysql.MySQLKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.WhereClauseParser;

/**
 * Where clause parser for MySQL.
 *
 * @author zhangliang
 */
public final class MySQLWhereClauseParser extends WhereClauseParser {
    
    public MySQLWhereClauseParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected Keyword[] getCustomizedOtherConditionOperators() {
        return new Keyword[] {MySQLKeyword.REGEXP};
    }
}
