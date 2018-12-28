/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.parser.clause;

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.AggregationType;
import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.token.DefaultKeyword;
import io.shardingsphere.core.parsing.lexer.token.Keyword;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.core.parsing.parser.clause.expression.AliasExpressionParser;
import io.shardingsphere.core.parsing.parser.constant.DerivedAlias;
import io.shardingsphere.core.parsing.parser.context.selectitem.AggregationDistinctSelectItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.AggregationSelectItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.CommonSelectItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.DistinctSelectItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.SelectItem;
import io.shardingsphere.core.parsing.parser.context.selectitem.StarSelectItem;
import io.shardingsphere.core.parsing.parser.dialect.ExpressionParserFactory;
import io.shardingsphere.core.parsing.parser.sql.dql.select.SelectStatement;
import io.shardingsphere.core.parsing.parser.token.AggregationDistinctToken;
import io.shardingsphere.core.parsing.parser.token.TableToken;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.util.SQLUtil;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Select list clause parser.
 *
 * @author zhangliang
 * @author panjuan
 */
@Getter
public abstract class SelectListClauseParser implements SQLClauseParser {
    
    private final ShardingRule shardingRule;
    
    private final LexerEngine lexerEngine;
    
    private final AliasExpressionParser aliasExpressionParser;
    
    public SelectListClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        this.shardingRule = shardingRule;
        this.lexerEngine = lexerEngine;
        aliasExpressionParser = ExpressionParserFactory.createAliasExpressionParser(lexerEngine);
    }
    
    /**
     * Parse select list.
     *
     * @param selectStatement select statement
     * @param items           select items
     */
    public void parse(final SelectStatement selectStatement, final List<SelectItem> items) {
        do {
            selectStatement.getItems().addAll(parseSelectItems(selectStatement));
        } while (lexerEngine.skipIfEqual(Symbol.COMMA));
        selectStatement.setSelectListLastPosition(lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length());
        items.addAll(selectStatement.getItems());
    }
    
    private Collection<SelectItem> parseSelectItems(final SelectStatement selectStatement) {
        lexerEngine.skipIfEqual(getSkippedKeywordsBeforeSelectItem());
        Collection<SelectItem> result = getSelectItems(selectStatement);
        reviseDistinctSelectItems(selectStatement, result);
        return result;
    }
    
    private Collection<SelectItem> getSelectItems(final SelectStatement selectStatement) {
        final Collection<SelectItem> result = new LinkedList<>();
        if (isRowNumberSelectItem()) {
            result.add(parseRowNumberSelectItem(selectStatement));
        } else if (isDistinctSelectItem()) {
            result.add(parseDistinctSelectItem(selectStatement));
            addStarSelectItem(result);
            parseRestSelectItem(selectStatement);
        } else if (isStarSelectItem()) {
            selectStatement.setContainStar(true);
            result.add(parseStarSelectItem());
        } else if (isAggregationSelectItem()) {
            result.add(parseAggregationSelectItem(selectStatement));
            parseRestSelectItem(selectStatement);
        } else {
            result.add(parseCommonOrStarSelectItem(selectStatement));
        }
        return result;
    }
    
    private void addStarSelectItem(final Collection<SelectItem> result) {
        if (isStarSelectItem()) {
            result.add(parseStarSelectItem());
        }
    }
    
    private void reviseDistinctSelectItems(final SelectStatement selectStatement, final Collection<SelectItem> selectItems) {
        for (SelectItem each : selectItems) {
            if (selectStatement.getDistinctSelectItem().isPresent() && !(each instanceof StarSelectItem)) {
                selectStatement.getDistinctSelectItem().get().getDistinctColumnNames().add(each.getAlias().isPresent() ? each.getAlias().get() : each.getExpression());
            }
        }
    }
    
    protected abstract Keyword[] getSkippedKeywordsBeforeSelectItem();
    
    protected abstract boolean isRowNumberSelectItem();
    
    protected abstract SelectItem parseRowNumberSelectItem(SelectStatement selectStatement);
    
    private boolean isDistinctSelectItem() {
        return lexerEngine.equalAny(DefaultKeyword.DISTINCT);
    }
    
    private SelectItem parseDistinctSelectItem(final SelectStatement selectStatement) {
        lexerEngine.nextToken();
        String distinctColumnName = lexerEngine.getCurrentToken().getLiterals();
        if (Symbol.STAR == lexerEngine.getCurrentToken().getType()) {
            return new DistinctSelectItem(Collections.<String>emptySet(), aliasExpressionParser.parseSelectItemAlias());
        }
        lexerEngine.nextToken();
        Set<String> distinctColumnNames = new LinkedHashSet<>();
        distinctColumnNames.add(SQLUtil.getExactlyValue(distinctColumnName + parseRestSelectItem(selectStatement)));
        return new DistinctSelectItem(distinctColumnNames, aliasExpressionParser.parseSelectItemAlias());
    }
    
    private boolean isStarSelectItem() {
        return Symbol.STAR.getLiterals().equals(SQLUtil.getExactlyValue(lexerEngine.getCurrentToken().getLiterals()));
    }
    
    private SelectItem parseStarSelectItem() {
        lexerEngine.nextToken();
        aliasExpressionParser.parseSelectItemAlias();
        return new StarSelectItem();
    }
    
    private SelectItem parseStarSelectItem(final String owner) {
        lexerEngine.nextToken();
        aliasExpressionParser.parseSelectItemAlias();
        return new StarSelectItem(owner);
    }
    
    private SelectItem parseCommonOrStarSelectItem(final SelectStatement selectStatement) {
        String literals = lexerEngine.getCurrentToken().getLiterals();
        int position = lexerEngine.getCurrentToken().getEndPosition() - literals.length();
        StringBuilder result = new StringBuilder();
        result.append(literals);
        lexerEngine.nextToken();
        if (lexerEngine.equalAny(Symbol.LEFT_PAREN)) {
            result.append(lexerEngine.skipParentheses(selectStatement));
        } else if (lexerEngine.equalAny(Symbol.DOT)) {
            String tableName = SQLUtil.getExactlyValue(literals);
            if (shardingRule.findTableRuleByLogicTable(tableName).isPresent() || shardingRule.isBroadcastTable(tableName) || shardingRule.findBindingTableRule(tableName).isPresent()) {
                selectStatement.addSQLToken(new TableToken(position, 0, literals));
            }
            result.append(lexerEngine.getCurrentToken().getLiterals());
            lexerEngine.nextToken();
            if (lexerEngine.equalAny(Symbol.STAR)) {
                return parseStarSelectItem(literals);
            }
            result.append(lexerEngine.getCurrentToken().getLiterals());
            lexerEngine.nextToken();
        }
        return new CommonSelectItem(SQLUtil.getExactlyValue(result
                + parseRestSelectItem(selectStatement)), aliasExpressionParser.parseSelectItemAlias());
        
    }
    
    private boolean isAggregationSelectItem() {
        return lexerEngine.equalAny(DefaultKeyword.MAX, DefaultKeyword.MIN, DefaultKeyword.SUM, DefaultKeyword.AVG, DefaultKeyword.COUNT);
    }
    
    private SelectItem parseAggregationSelectItem(final SelectStatement selectStatement) {
        AggregationType aggregationType = AggregationType.valueOf(lexerEngine.getCurrentToken().getLiterals().toUpperCase());
        int beginPosition = lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length();
        lexerEngine.nextToken();
        String innerExpression = lexerEngine.skipParentheses(selectStatement);
        return isAggregationDistinctSelectItem(innerExpression) ? getAggregationDistinctSelectItem(selectStatement, aggregationType, beginPosition, innerExpression)
                : new AggregationSelectItem(aggregationType, innerExpression, aliasExpressionParser.parseSelectItemAlias());
    }
    
    private SelectItem getAggregationDistinctSelectItem(final SelectStatement selectStatement, final AggregationType aggregationType, final int beginPosition, final String innerExpression) {
        Optional<String> alias = aliasExpressionParser.parseSelectItemAlias();
        Optional<String> autoAlias = Optional.absent();
        if (!alias.isPresent()) {
            autoAlias = Optional.of(DerivedAlias.AGGREGATION_DISTINCT_DERIVED.getDerivedAlias(selectStatement.getAggregationDistinctSelectItems().size()));
            alias = autoAlias;
        }
        AggregationDistinctSelectItem result = new AggregationDistinctSelectItem(
                aggregationType, innerExpression, alias, getDistinctColumnName(innerExpression));
        
        selectStatement.getSQLTokens().add(new AggregationDistinctToken(beginPosition, SQLUtil.getExactlyValue(aggregationType.name() + innerExpression), result.getDistinctColumnName(), autoAlias));
        return result;
    }
    
    // TODO :panjuan does not use pattern to check
    private boolean isAggregationDistinctSelectItem(final String innerExpression) {
        String pattern = "\\(\\s*DISTINCT\\s+.*\\)";
        return Pattern.matches(pattern, innerExpression.toUpperCase());
    }
    
    // TODO :panjuan parse distinct column name
    private String getDistinctColumnName(final String innerExpression) {
        Pattern pattern = Pattern.compile("\\(\\s*DISTINCT\\s+(\\S+)\\s*\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(innerExpression);
        String result = "";
        if (matcher.find()) {
            result = matcher.group(1);
        }
        return result;
    }
    
    private String parseRestSelectItem(final SelectStatement selectStatement) {
        StringBuilder result = new StringBuilder();
        while (lexerEngine.equalAny(Symbol.getOperators())) {
            result.append(lexerEngine.getCurrentToken().getLiterals());
            lexerEngine.nextToken();
            SelectItem selectItem = parseCommonOrStarSelectItem(selectStatement);
            result.append(selectItem.getExpression());
        }
        return result.toString();
    }
}
