package io.shardingjdbc.core.parsing.parser.dialect.mysql.sql;

import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.dialect.mysql.MySQLKeyword;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.statement.ShowStatement;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.statement.ShowType;
import io.shardingjdbc.core.parsing.parser.sql.SQLParser;
import lombok.RequiredArgsConstructor;

/**
 * Show parser for MySQL.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class MySQLShowParser implements SQLParser {
    
    private final LexerEngine lexerEngine;
    
    @Override
    public ShowStatement parse() {
        lexerEngine.nextToken();
        if (lexerEngine.equalAny(MySQLKeyword.DATABASES)) {
            return new ShowStatement(ShowType.DATABASES);
        }
        if (lexerEngine.equalAny(MySQLKeyword.TABLES)) {
            return new ShowStatement(ShowType.TABLES);
        }
        return new ShowStatement(ShowType.OTHER);
    }
}
