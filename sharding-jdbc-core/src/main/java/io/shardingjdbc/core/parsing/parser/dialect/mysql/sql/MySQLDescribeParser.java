package io.shardingjdbc.core.parsing.parser.dialect.mysql.sql;

import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.parser.clause.TableReferencesClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.statement.DescribeStatement;
import io.shardingjdbc.core.parsing.parser.sql.dal.describe.AbstractDescribeParser;
import io.shardingjdbc.core.rule.ShardingRule;

/**
 * Describe parser for MySQL.
 *
 * @author zhangliang
 */
public final class MySQLDescribeParser extends AbstractDescribeParser {
    
    private final LexerEngine lexerEngine;
    
    private final TableReferencesClauseParser tableReferencesClauseParser;
    
    public MySQLDescribeParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        this.lexerEngine = lexerEngine;
        tableReferencesClauseParser = new TableReferencesClauseParser(shardingRule, lexerEngine);
    }
    
    @Override
    public DescribeStatement parse() {
        lexerEngine.nextToken();
        DescribeStatement result = new DescribeStatement();
        tableReferencesClauseParser.parseSingleTableWithoutAlias(result);
        return result;
    }
}
