package io.shardingjdbc.core.parsing.parser.dialect.mysql.sql;

import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.parser.clause.TableReferencesClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.statement.DescStatement;
import io.shardingjdbc.core.parsing.parser.sql.SQLParser;
import io.shardingjdbc.core.rule.ShardingRule;

/**
 * Desc parser for MySQL.
 *
 * @author zhangliang
 */
public final class MySQLDescParser implements SQLParser {
    
    private final LexerEngine lexerEngine;
    
    private final TableReferencesClauseParser tableReferencesClauseParser;
    
    public MySQLDescParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        this.lexerEngine = lexerEngine;
        tableReferencesClauseParser = new TableReferencesClauseParser(shardingRule, lexerEngine);
    }
    
    @Override
    public DescStatement parse() {
        lexerEngine.nextToken();
        DescStatement result = new DescStatement();
        tableReferencesClauseParser.parseSingleTableWithoutAlias(result);
        return result;
    }
}
