package io.shardingjdbc.core.parsing.parser.dialect;

import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.parser.clause.expression.AliasExpressionParser;
import io.shardingjdbc.core.parsing.parser.clause.expression.BasicExpressionParser;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.clause.expression.MySQLAliasExpressionParser;
import io.shardingjdbc.core.parsing.parser.dialect.oracle.clause.expression.OracleAliasExpressionParser;
import io.shardingjdbc.core.parsing.parser.dialect.postgresql.clause.expression.PostgreSQLAliasExpressionParser;
import io.shardingjdbc.core.parsing.parser.dialect.sqlserver.clause.expression.SQLServerAliasExpressionParser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Expression parser factory.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExpressionParserFactory {
    
    /**
     * Create alias parser instance.
     * 
     * @param lexerEngine lexical analysis engine.
     * @return alias parser instance
     */
    public static AliasExpressionParser createAliasExpressionParser(final LexerEngine lexerEngine) {
        switch (lexerEngine.getDatabaseType()) {
            case MySQL:
                return new MySQLAliasExpressionParser(lexerEngine);
            case Oracle:
                return new OracleAliasExpressionParser(lexerEngine);
            case SQLServer:
                return new SQLServerAliasExpressionParser(lexerEngine);
            case PostgreSQL:
                return new PostgreSQLAliasExpressionParser(lexerEngine);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database type: %s", lexerEngine.getDatabaseType()));
        }
    }
    
    /**
     * Create expression parser instance.
     *
     * @param lexerEngine lexical analysis engine.
     * @return expression parser instance
     */
    public static BasicExpressionParser createBasicExpressionParser(final LexerEngine lexerEngine) {
        return new BasicExpressionParser(lexerEngine);
    }
}
