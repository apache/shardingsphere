package com.dangdang.ddframe.rdb.sharding.parsing.parser.clause;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constant.AggregationType;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.AggregationSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.CommonSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.SelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.StarSelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.dql.select.SelectStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.TableToken;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;
import com.google.common.base.Optional;
import lombok.Getter;

import java.util.List;

/**
 * Select list clause parser.
 *
 * @author zhangliang
 */
@Getter
public class SelectListClauseParser implements SQLClauseParser {
    
    private final ShardingRule shardingRule;
    
    private final LexerEngine lexerEngine;
    
    private final AliasClauseParser aliasClauseParser;
    
    public SelectListClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        this.shardingRule = shardingRule;
        this.lexerEngine = lexerEngine;
        aliasClauseParser = new AliasClauseParser(lexerEngine);
    }
    
    /**
     * Parse select list.
     * 
     * @param selectStatement select statement
     * @param items select items
     */
    public void parse(final SelectStatement selectStatement, final List<SelectItem> items) {
        do {
            selectStatement.getItems().add(parseSelectItem(selectStatement));
        } while (lexerEngine.skipIfEqual(Symbol.COMMA));
        selectStatement.setSelectListLastPosition(lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length());
        items.addAll(selectStatement.getItems());
    }
    
    private SelectItem parseSelectItem(final SelectStatement selectStatement) {
        lexerEngine.skipIfEqual(getSkippedKeywordsBeforeSelectItem());
        SelectItem result;
        if (isRowNumberSelectItem()) {
            result = parseRowNumberSelectItem(selectStatement);
        } else if (isStarSelectItem()) {
            selectStatement.setContainStar(true);
            result = parseStarSelectItem();
        } else if (isAggregationSelectItem()) {
            result = parseAggregationSelectItem(selectStatement);
            parseRestSelectItem(selectStatement);
        } else {
            result = new CommonSelectItem(SQLUtil.getExactlyValue(parseCommonSelectItem(selectStatement) + parseRestSelectItem(selectStatement)), aliasClauseParser.parse());
        }
        return result;
    }
    
    protected Keyword[] getSkippedKeywordsBeforeSelectItem() {
        return new Keyword[0];
    }
    
    protected boolean isRowNumberSelectItem() {
        return false;
    }
    
    protected SelectItem parseRowNumberSelectItem(final SelectStatement selectStatement) {
        throw new UnsupportedOperationException("Cannot support special select item.");
    }
    
    private boolean isStarSelectItem() {
        return Symbol.STAR.getLiterals().equals(SQLUtil.getExactlyValue(lexerEngine.getCurrentToken().getLiterals()));
    }
    
    private SelectItem parseStarSelectItem() {
        lexerEngine.nextToken();
        aliasClauseParser.parse();
        return new StarSelectItem(Optional.<String>absent());
    }
    
    private boolean isAggregationSelectItem() {
        return lexerEngine.equalAny(DefaultKeyword.MAX, DefaultKeyword.MIN, DefaultKeyword.SUM, DefaultKeyword.AVG, DefaultKeyword.COUNT);
    }
    
    private SelectItem parseAggregationSelectItem(final SelectStatement selectStatement) {
        AggregationType aggregationType = AggregationType.valueOf(lexerEngine.getCurrentToken().getLiterals().toUpperCase());
        lexerEngine.nextToken();
        return new AggregationSelectItem(aggregationType, lexerEngine.skipParentheses(selectStatement), aliasClauseParser.parse());
    }
    
    private String parseCommonSelectItem(final SelectStatement selectStatement) {
        String literals = lexerEngine.getCurrentToken().getLiterals();
        int position = lexerEngine.getCurrentToken().getEndPosition() - literals.length();
        StringBuilder result = new StringBuilder();
        result.append(literals);
        lexerEngine.nextToken();
        if (lexerEngine.equalAny(Symbol.LEFT_PAREN)) {
            result.append(lexerEngine.skipParentheses(selectStatement));
        } else if (lexerEngine.equalAny(Symbol.DOT)) {
            String tableName = SQLUtil.getExactlyValue(literals);
            if (shardingRule.tryFindTableRule(tableName).isPresent() || shardingRule.findBindingTableRule(tableName).isPresent()) {
                selectStatement.getSqlTokens().add(new TableToken(position, literals));
            }
            result.append(lexerEngine.getCurrentToken().getLiterals());
            lexerEngine.nextToken();
            result.append(lexerEngine.getCurrentToken().getLiterals());
            lexerEngine.nextToken();
        }
        return result.toString();
    }
    
    private String parseRestSelectItem(final SelectStatement selectStatement) {
        StringBuilder result = new StringBuilder();
        while (lexerEngine.equalAny(Symbol.getOperators())) {
            result.append(lexerEngine.getCurrentToken().getLiterals());
            lexerEngine.nextToken();
            result.append(parseCommonSelectItem(selectStatement));
        }
        return result.toString();
    }
}
