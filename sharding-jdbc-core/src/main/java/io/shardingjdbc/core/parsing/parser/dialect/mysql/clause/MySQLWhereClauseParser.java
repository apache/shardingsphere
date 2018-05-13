package io.shardingjdbc.core.parsing.parser.dialect.mysql.clause;

import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.dialect.mysql.MySQLKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Keyword;
import io.shardingjdbc.core.parsing.parser.clause.WhereClauseParser;

/**
 * Where clause parser for MySQL.
 *
 * @author zhangliang
 */
public final class MySQLWhereClauseParser extends WhereClauseParser {
    
    public MySQLWhereClauseParser(final LexerEngine lexerEngine) {
        super(DatabaseType.MySQL, lexerEngine);
    }
    
    @Override
    protected Keyword[] getCustomizedOtherConditionOperators() {
        return new Keyword[] {MySQLKeyword.REGEXP};
    }
}
