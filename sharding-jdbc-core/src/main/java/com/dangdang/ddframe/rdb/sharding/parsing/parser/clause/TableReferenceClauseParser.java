package com.dangdang.ddframe.rdb.sharding.parsing.parser.clause;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.table.Table;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.SQLStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.dql.select.SelectStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.TableToken;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.Getter;

/**
 * 表从句解析器.
 *
 * @author zhangliang
 */
public class TableReferenceClauseParser implements SQLClauseParser {
    
    private final ShardingRule shardingRule;
    
    @Getter
    private final LexerEngine lexerEngine;
    
    private final AliasClauseParser aliasClauseParser;
    
    private final ExpressionClauseParser expressionClauseParser;
    
    public TableReferenceClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        this.shardingRule = shardingRule;
        this.lexerEngine = lexerEngine;
        aliasClauseParser = new AliasClauseParser(lexerEngine);
        expressionClauseParser = new ExpressionClauseParser(lexerEngine);
    }
    
    /**
     * 解析单表.
     *
     * @param sqlStatement SQL语句对象
     */
    public void parseSingleTable(final SQLStatement sqlStatement) {
        lexerEngine.skipAll(DefaultKeyword.AS);
        final int beginPosition = lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length();
        String literals = lexerEngine.getCurrentToken().getLiterals();
        lexerEngine.nextToken();
        if (lexerEngine.equalAny(Symbol.DOT)) {
            throw new UnsupportedOperationException("Cannot support SQL for `schema.table`");
        }
        String tableName = SQLUtil.getExactlyValue(literals);
        Optional<String> alias = aliasClauseParser.parse();
        sqlStatement.getSqlTokens().add(new TableToken(beginPosition, literals));
        sqlStatement.getTables().add(new Table(tableName, alias));
        if (skipJoin()) {
            throw new UnsupportedOperationException("Cannot support Multiple-Table.");
        }
    }
    
    /**
     * 解析表.
     * 
     * @param selectStatement Select SQL语句对象
     */
    public void parseTableFactor(final SelectStatement selectStatement) {
        parseTableFactorInternal(selectStatement);
    }
    
    protected final void parseTableFactorInternal(final SelectStatement selectStatement) {
        lexerEngine.skipAll(DefaultKeyword.AS);
        final int beginPosition = lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length();
        String literals = lexerEngine.getCurrentToken().getLiterals();
        lexerEngine.nextToken();
        if (lexerEngine.equalAny(Symbol.DOT)) {
            throw new UnsupportedOperationException("Cannot support SQL for `schema.table`");
        }
        String tableName = SQLUtil.getExactlyValue(literals);
        Optional<String> alias = aliasClauseParser.parse();
        if (shardingRule.tryFindTableRule(tableName).isPresent() || shardingRule.findBindingTableRule(tableName).isPresent()) {
            selectStatement.getSqlTokens().add(new TableToken(beginPosition, literals));
            selectStatement.getTables().add(new Table(tableName, alias));
        }
        parseJoinTable(selectStatement);
        afterParseTableFactor(selectStatement);
    }
    
    private void parseJoinTable(final SelectStatement selectStatement) {
        beforeParseJoinTable(selectStatement);
        if (skipJoin()) {
            if (lexerEngine.equalAny(Symbol.LEFT_PAREN)) {
                throw new UnsupportedOperationException("Cannot support sub query for join table.");
            }
            parseTableFactor(selectStatement);
            parseJoinTable(selectStatement);
            if (lexerEngine.skipIfEqual(DefaultKeyword.ON)) {
                do {
                    expressionClauseParser.parse(selectStatement);
                    lexerEngine.accept(Symbol.EQ);
                    expressionClauseParser.parse(selectStatement);
                } while (lexerEngine.skipIfEqual(DefaultKeyword.AND));
            } else if (lexerEngine.skipIfEqual(DefaultKeyword.USING)) {
                lexerEngine.skipParentheses(selectStatement);
            }
            parseJoinTable(selectStatement);
        }
    }
    
    protected void beforeParseJoinTable(final SelectStatement selectStatement) {
    }
    
    private boolean skipJoin() {
        if (lexerEngine.skipIfEqual(DefaultKeyword.LEFT, DefaultKeyword.RIGHT, DefaultKeyword.FULL)) {
            lexerEngine.skipIfEqual(DefaultKeyword.OUTER);
            lexerEngine.accept(DefaultKeyword.JOIN);
            return true;
        } else if (lexerEngine.skipIfEqual(DefaultKeyword.INNER)) {
            lexerEngine.accept(DefaultKeyword.JOIN);
            return true;
        } else if (lexerEngine.skipIfEqual(DefaultKeyword.JOIN, Symbol.COMMA, DefaultKeyword.STRAIGHT_JOIN)) {
            return true;
        } else if (lexerEngine.skipIfEqual(DefaultKeyword.CROSS)) {
            if (lexerEngine.skipIfEqual(DefaultKeyword.JOIN, DefaultKeyword.APPLY)) {
                return true;
            }
        } else if (lexerEngine.skipIfEqual(DefaultKeyword.OUTER)) {
            if (lexerEngine.skipIfEqual(DefaultKeyword.APPLY)) {
                return true;
            }
        }
        return false;
    }
    
    protected void afterParseTableFactor(final SelectStatement selectStatement) {
    }
}
