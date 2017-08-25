package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.clause;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.sqlserver.SQLServerKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.OrderByClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.SelectListClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.CommonSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.SelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.dql.select.SelectStatement;

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
