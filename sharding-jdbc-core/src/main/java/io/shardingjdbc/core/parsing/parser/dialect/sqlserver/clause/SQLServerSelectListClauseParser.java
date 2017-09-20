package io.shardingjdbc.core.parsing.parser.dialect.sqlserver.clause;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.dialect.sqlserver.SQLServerKeyword;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Symbol;
import io.shardingjdbc.core.parsing.parser.clause.OrderByClauseParser;
import io.shardingjdbc.core.parsing.parser.clause.SelectListClauseParser;
import io.shardingjdbc.core.parsing.parser.context.selectitem.CommonSelectItem;
import io.shardingjdbc.core.parsing.parser.context.selectitem.SelectItem;
import io.shardingjdbc.core.parsing.parser.sql.dql.select.SelectStatement;

/**
 * Select list clause parser for SQLServer.
 *
 * @author zhangliang
 */
public final class SQLServerSelectListClauseParser extends SelectListClauseParser {
    
    private OrderByClauseParser orderByClauseParser;
    
    public SQLServerSelectListClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine);
        orderByClauseParser = new SQLServerOrderByClauseParser(lexerEngine);
    }
    
    @Override
    protected boolean isRowNumberSelectItem() {
        return getLexerEngine().skipIfEqual(SQLServerKeyword.ROW_NUMBER);
    }
    
    @Override
    protected SelectItem parseRowNumberSelectItem(final SelectStatement selectStatement) {
        getLexerEngine().skipParentheses(selectStatement);
        getLexerEngine().accept(DefaultKeyword.OVER);
        getLexerEngine().accept(Symbol.LEFT_PAREN);
        getLexerEngine().unsupportedIfEqual(SQLServerKeyword.PARTITION);
        orderByClauseParser.parse(selectStatement);
        getLexerEngine().accept(Symbol.RIGHT_PAREN);
        return new CommonSelectItem(SQLServerKeyword.ROW_NUMBER.name(), getAliasClauseParser().parse());
    }
}
