package io.shardingjdbc.core.parsing.parser.dialect;

import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.parser.clause.AliasClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.clause.MySQLAliasClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.oracle.clause.OracleAliasClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.postgresql.clause.PostgreSQLAliasClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.sqlserver.clause.SQLServerAliasClauseParser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Alias clause parser factory.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AliasClauseParserFactory {
    
    /**
     * Create alias clause parser instance.
     * 
     * @param lexerEngine lexical analysis engine.
     * @return alias clause parser instance
     */
    public static AliasClauseParser createInstance(final LexerEngine lexerEngine) {
        switch (lexerEngine.getDatabaseType()) {
            case MySQL:
                return new MySQLAliasClauseParser(lexerEngine);
            case Oracle:
                return new OracleAliasClauseParser(lexerEngine);
            case SQLServer:
                return new SQLServerAliasClauseParser(lexerEngine);
            case PostgreSQL:
                return new PostgreSQLAliasClauseParser(lexerEngine);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database type: %s", lexerEngine.getDatabaseType()));
        }
    }
}
