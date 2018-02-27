package io.shardingjdbc.core.parsing.parser.dialect.mysql.sql;

import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.dialect.mysql.MySQLKeyword;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.parser.clause.TableReferencesClauseParser;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.statement.ShowStatement;
import io.shardingjdbc.core.parsing.parser.dialect.mysql.statement.ShowType;
import io.shardingjdbc.core.parsing.parser.sql.SQLParser;
import io.shardingjdbc.core.parsing.parser.token.SchemaToken;
import io.shardingjdbc.core.rule.ShardingRule;
import lombok.RequiredArgsConstructor;

/**
 * Show parser for MySQL.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class MySQLShowParser implements SQLParser {
    
    private final LexerEngine lexerEngine;
    
    private final TableReferencesClauseParser tableReferencesClauseParser;
    
    public MySQLShowParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        this.lexerEngine = lexerEngine;
        tableReferencesClauseParser = new TableReferencesClauseParser(shardingRule, lexerEngine);
    }
    
    @Override
    public ShowStatement parse() {
        lexerEngine.nextToken();
        lexerEngine.skipIfEqual(DefaultKeyword.FULL);
        if (lexerEngine.equalAny(MySQLKeyword.DATABASES)) {
            return new ShowStatement(ShowType.DATABASES);
        }
        if (lexerEngine.equalAny(MySQLKeyword.TABLES)) {
            return new ShowStatement(ShowType.TABLES);
        }
        if (lexerEngine.skipIfEqual(MySQLKeyword.COLUMNS, MySQLKeyword.FIELDS)) {
            ShowStatement result = new ShowStatement(ShowType.COLUMNS);
            lexerEngine.skipIfEqual(DefaultKeyword.FROM, DefaultKeyword.IN);
            tableReferencesClauseParser.parseSingleTableWithoutAlias(result);
            lexerEngine.skipIfEqual(DefaultKeyword.FROM, DefaultKeyword.IN);
            int beginPosition = lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length();
            result.getSqlTokens().add(new SchemaToken(beginPosition, lexerEngine.getCurrentToken().getLiterals(), result.getTables().getSingleTableName()));
            return result;
        }
        return new ShowStatement(ShowType.OTHER);
    }
}
