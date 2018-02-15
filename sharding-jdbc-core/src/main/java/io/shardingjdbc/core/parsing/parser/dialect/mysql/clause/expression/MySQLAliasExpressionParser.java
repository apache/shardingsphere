package io.shardingjdbc.core.parsing.parser.dialect.mysql.clause.expression;

import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.dialect.mysql.MySQLKeyword;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.lexer.token.TokenType;
import io.shardingjdbc.core.parsing.parser.clause.expression.AliasExpressionParser;

/**
 * Alias clause parser for MySQL.
 *
 * @author zhangliang
 */
public final class MySQLAliasExpressionParser extends AliasExpressionParser {
    
    public MySQLAliasExpressionParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected TokenType[] getCustomizedAvailableKeywordsForSelectItemAlias() {
        return new TokenType[] {
            DefaultKeyword.VIEW, DefaultKeyword.TABLESPACE, DefaultKeyword.FUNCTION, DefaultKeyword.SEQUENCE, DefaultKeyword.OF, DefaultKeyword.DO, DefaultKeyword.NO, DefaultKeyword.WITHOUT, 
            DefaultKeyword.TEMPORARY, DefaultKeyword.TEMP, DefaultKeyword.COMMENT, DefaultKeyword.AFTER, DefaultKeyword.INSTEAD, DefaultKeyword.ROW, DefaultKeyword.STATEMENT, 
            DefaultKeyword.EXECUTE, DefaultKeyword.MODIFY, DefaultKeyword.ENABLE, DefaultKeyword.DISABLE, DefaultKeyword.VALIDATE, DefaultKeyword.USER, DefaultKeyword.IDENTIFIED, 
            DefaultKeyword.TRUNCATE, DefaultKeyword.END, DefaultKeyword.FULL, DefaultKeyword.CLOSE, DefaultKeyword.CAST, DefaultKeyword.ESCAPE, DefaultKeyword.SOME, DefaultKeyword.UNTIL, 
            DefaultKeyword.OPEN, DefaultKeyword.OVER, DefaultKeyword.PASSWORD, DefaultKeyword.LOCAL, DefaultKeyword.GLOBAL, DefaultKeyword.STORAGE, DefaultKeyword.DATA, 
            DefaultKeyword.COALESCE, DefaultKeyword.VARCHAR2, DefaultKeyword.DATE, DefaultKeyword.TIME, DefaultKeyword.BOOLEAN, DefaultKeyword.ANY, DefaultKeyword.GREATEST, 
            DefaultKeyword.LEAST, DefaultKeyword.POSITION, DefaultKeyword.SUBSTRING, DefaultKeyword.TRIM, DefaultKeyword.BEGIN,
            MySQLKeyword.OFFSET, MySQLKeyword.VALUE, MySQLKeyword.QUICK, MySQLKeyword.CACHE, MySQLKeyword.SQL_CACHE, MySQLKeyword.SQL_NO_CACHE, MySQLKeyword.SQL_BUFFER_RESULT, 
            MySQLKeyword.FIRST, MySQLKeyword.ALGORITHM, MySQLKeyword.DISCARD, MySQLKeyword.IMPORT, MySQLKeyword.VALIDATION, MySQLKeyword.REORGANIZE, MySQLKeyword.EXCHANGE, MySQLKeyword.REBUILD, 
            MySQLKeyword.REPAIR, MySQLKeyword.REMOVE, MySQLKeyword.UPGRADE, MySQLKeyword.KEY_BLOCK_SIZE, MySQLKeyword.AUTO_INCREMENT, MySQLKeyword.AVG_ROW_LENGTH, MySQLKeyword.CHECKSUM, 
            MySQLKeyword.COMPRESSION, MySQLKeyword.CONNECTION, MySQLKeyword.DIRECTORY, MySQLKeyword.DELAY_KEY_WRITE, MySQLKeyword.ENCRYPTION, MySQLKeyword.ENGINE, MySQLKeyword.INSERT_METHOD, 
            MySQLKeyword.MAX_ROWS, MySQLKeyword.MIN_ROWS, MySQLKeyword.PACK_KEYS, MySQLKeyword.ROW_FORMAT, MySQLKeyword.DYNAMIC, MySQLKeyword.FIXED, MySQLKeyword.COMPRESSED, MySQLKeyword.REDUNDANT, 
            MySQLKeyword.COMPACT, MySQLKeyword.STATS_AUTO_RECALC, MySQLKeyword.STATS_PERSISTENT, MySQLKeyword.STATS_SAMPLE_PAGES, MySQLKeyword.DISK, MySQLKeyword.MEMORY, MySQLKeyword.ROLLUP,
        };
    }
    
    @Override
    protected TokenType[] getCustomizedAvailableKeywordsForTableAlias() {
        return new TokenType[]{
            DefaultKeyword.FUNCTION, DefaultKeyword.COMMENT, DefaultKeyword.ROW, DefaultKeyword.EXECUTE, DefaultKeyword.MODIFY, 
            DefaultKeyword.VALIDATE, DefaultKeyword.USER, DefaultKeyword.IDENTIFIED, DefaultKeyword.END, DefaultKeyword.CLOSE, DefaultKeyword.CAST, 
            DefaultKeyword.ESCAPE, DefaultKeyword.SOME, DefaultKeyword.OPEN, DefaultKeyword.OVER, DefaultKeyword.COALESCE, DefaultKeyword.VARCHAR2, 
            DefaultKeyword.DATE, DefaultKeyword.ANY, DefaultKeyword.BEGIN,
            MySQLKeyword.OFFSET, MySQLKeyword.QUICK, MySQLKeyword.CACHE, MySQLKeyword.SQL_CACHE, MySQLKeyword.SQL_NO_CACHE, MySQLKeyword.SQL_BUFFER_RESULT, 
            MySQLKeyword.FIRST, MySQLKeyword.ALGORITHM, MySQLKeyword.DISCARD, MySQLKeyword.IMPORT, MySQLKeyword.VALIDATION, MySQLKeyword.REORGANIZE, MySQLKeyword.EXCHANGE, MySQLKeyword.REBUILD, 
            MySQLKeyword.REPAIR, MySQLKeyword.KEY_BLOCK_SIZE, MySQLKeyword.AUTO_INCREMENT, MySQLKeyword.AVG_ROW_LENGTH, MySQLKeyword.CHECKSUM, 
            MySQLKeyword.COMPRESSION, MySQLKeyword.CONNECTION, MySQLKeyword.DIRECTORY, MySQLKeyword.DELAY_KEY_WRITE, MySQLKeyword.ENCRYPTION, MySQLKeyword.ENGINE, MySQLKeyword.INSERT_METHOD, 
            MySQLKeyword.MAX_ROWS, MySQLKeyword.MIN_ROWS, MySQLKeyword.PACK_KEYS, MySQLKeyword.ROW_FORMAT, MySQLKeyword.DYNAMIC, MySQLKeyword.FIXED, MySQLKeyword.COMPRESSED, MySQLKeyword.REDUNDANT, 
            MySQLKeyword.COMPACT, MySQLKeyword.STATS_AUTO_RECALC, MySQLKeyword.STATS_PERSISTENT, MySQLKeyword.STATS_SAMPLE_PAGES, MySQLKeyword.DISK, MySQLKeyword.MEMORY, MySQLKeyword.ROLLUP,
        };
    }
}
