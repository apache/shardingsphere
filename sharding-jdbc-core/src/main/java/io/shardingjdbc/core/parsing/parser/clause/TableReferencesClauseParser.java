package io.shardingjdbc.core.parsing.parser.clause;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Keyword;
import io.shardingjdbc.core.parsing.lexer.token.Symbol;
import io.shardingjdbc.core.parsing.parser.context.table.Table;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;
import io.shardingjdbc.core.parsing.parser.token.TableToken;
import io.shardingjdbc.core.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.Getter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Table references clause parser.
 *
 * @author zhangliang
 */
public class TableReferencesClauseParser implements SQLClauseParser {
    
    private final ShardingRule shardingRule;
    
    @Getter
    private final LexerEngine lexerEngine;
    
    private final AliasClauseParser aliasClauseParser;
    
    private final ExpressionClauseParser expressionClauseParser;
    
    public TableReferencesClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        this.shardingRule = shardingRule;
        this.lexerEngine = lexerEngine;
        aliasClauseParser = new AliasClauseParser(lexerEngine);
        expressionClauseParser = new ExpressionClauseParser(lexerEngine);
    }
    
    /**
     * Parse table references.
     *
     * @param sqlStatement SQL statement
     * @param isSingleTableOnly is parse single table only
     */
    public final void parse(final SQLStatement sqlStatement, final boolean isSingleTableOnly) {
        do {
            parseTableReference(sqlStatement, isSingleTableOnly);
        } while (lexerEngine.skipIfEqual(Symbol.COMMA));
    }
    
    protected void parseTableReference(final SQLStatement sqlStatement, final boolean isSingleTableOnly) {
        parseTableFactor(sqlStatement, isSingleTableOnly);
    }
    
    protected final void parseTableFactor(final SQLStatement sqlStatement, final boolean isSingleTableOnly) {
        final int beginPosition = lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length();
        String literals = lexerEngine.getCurrentToken().getLiterals();
        lexerEngine.nextToken();
        if (lexerEngine.equalAny(Symbol.DOT)) {
            throw new UnsupportedOperationException("Cannot support SQL for `schema.table`");
        }
        String tableName = SQLUtil.getExactlyValue(literals);
        Optional<String> alias = aliasClauseParser.parse();
        if (isSingleTableOnly || shardingRule.tryFindTableRule(tableName).isPresent() || shardingRule.findBindingTableRule(tableName).isPresent()
                || shardingRule.getDataSourceMap().containsKey(shardingRule.getDefaultDataSourceName())) {
            sqlStatement.getSqlTokens().add(new TableToken(beginPosition, literals));
            sqlStatement.getTables().add(new Table(tableName, alias));
        }
        parseJoinTable(sqlStatement);
        if (isSingleTableOnly && !sqlStatement.getTables().isSingleTable()) {
            throw new UnsupportedOperationException("Cannot support Multiple-Table.");
        }
    }
    
    private void parseJoinTable(final SQLStatement sqlStatement) {
        while (parseJoinType()) {
            if (lexerEngine.equalAny(Symbol.LEFT_PAREN)) {
                throw new UnsupportedOperationException("Cannot support sub query for join table.");
            }
            parseTableFactor(sqlStatement, false);
            parseJoinCondition(sqlStatement);
        }
    }
    
    private boolean parseJoinType() {
        List<Keyword> joinTypeKeywords = new LinkedList<>();
        joinTypeKeywords.addAll(Arrays.asList(
                DefaultKeyword.INNER, DefaultKeyword.OUTER, DefaultKeyword.LEFT, DefaultKeyword.RIGHT, DefaultKeyword.FULL, DefaultKeyword.CROSS, DefaultKeyword.NATURAL, DefaultKeyword.JOIN));
        joinTypeKeywords.addAll(Arrays.asList(getKeywordsForJoinType()));
        Keyword[] joinTypeKeywordArrays = joinTypeKeywords.toArray(new Keyword[joinTypeKeywords.size()]);
        if (!lexerEngine.equalAny(joinTypeKeywordArrays)) {
            return false;
        }
        lexerEngine.skipAll(joinTypeKeywordArrays);
        return true;
    }
    
    protected Keyword[] getKeywordsForJoinType() {
        return new Keyword[0];
    }
    
    private void parseJoinCondition(final SQLStatement sqlStatement) {
        if (lexerEngine.skipIfEqual(DefaultKeyword.ON)) {
            do {
                expressionClauseParser.parse(sqlStatement);
                lexerEngine.accept(Symbol.EQ);
                expressionClauseParser.parse(sqlStatement);
            } while (lexerEngine.skipIfEqual(DefaultKeyword.AND));
        } else if (lexerEngine.skipIfEqual(DefaultKeyword.USING)) {
            lexerEngine.skipParentheses(sqlStatement);
        }
    }
}
