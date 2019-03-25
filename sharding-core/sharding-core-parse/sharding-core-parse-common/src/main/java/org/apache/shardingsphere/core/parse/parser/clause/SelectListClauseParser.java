/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.parse.parser.clause;

import com.google.common.base.Optional;
import lombok.Getter;
import org.apache.shardingsphere.core.constant.AggregationType;
import org.apache.shardingsphere.core.parse.antlr.constant.QuoteCharacter;
import org.apache.shardingsphere.core.parse.lexer.LexerEngine;
import org.apache.shardingsphere.core.parse.lexer.token.DefaultKeyword;
import org.apache.shardingsphere.core.parse.lexer.token.Keyword;
import org.apache.shardingsphere.core.parse.lexer.token.Symbol;
import org.apache.shardingsphere.core.parse.parser.clause.expression.AliasExpressionParser;
import org.apache.shardingsphere.core.parse.parser.constant.DerivedAlias;
import org.apache.shardingsphere.core.parse.parser.context.selectitem.AggregationDistinctSelectItem;
import org.apache.shardingsphere.core.parse.parser.context.selectitem.AggregationSelectItem;
import org.apache.shardingsphere.core.parse.parser.context.selectitem.CommonSelectItem;
import org.apache.shardingsphere.core.parse.parser.context.selectitem.DistinctSelectItem;
import org.apache.shardingsphere.core.parse.parser.context.selectitem.SelectItem;
import org.apache.shardingsphere.core.parse.parser.context.selectitem.StarSelectItem;
import org.apache.shardingsphere.core.parse.parser.dialect.ExpressionParserFactory;
import org.apache.shardingsphere.core.parse.parser.sql.dql.select.SelectStatement;
import org.apache.shardingsphere.core.parse.parser.token.AggregationDistinctToken;
import org.apache.shardingsphere.core.parse.parser.token.TableToken;
import org.apache.shardingsphere.core.parse.util.SQLUtil;
import org.apache.shardingsphere.core.rule.ShardingRule;

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
        selectStatement.setSelectListStopIndex(lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length());
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
            if (shardingRule.findTableRule(tableName).isPresent() || shardingRule.isBroadcastTable(tableName) || shardingRule.findBindingTableRule(tableName).isPresent()) {
                selectStatement.addSQLToken(new TableToken(position, 0, SQLUtil.getExactlyValue(literals), QuoteCharacter.getQuoteCharacter(literals)));
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
        int endPosition = lexerEngine.getCurrentToken().getEndPosition();
        lexerEngine.nextToken();
        String innerExpression = lexerEngine.skipParentheses(selectStatement);
        endPosition = endPosition + innerExpression.length();
        return isAggregationDistinctSelectItem(innerExpression) ? getAggregationDistinctSelectItem(selectStatement, aggregationType, beginPosition, endPosition, innerExpression)
                : new AggregationSelectItem(aggregationType, innerExpression, aliasExpressionParser.parseSelectItemAlias());
    }
    
    private SelectItem getAggregationDistinctSelectItem(final SelectStatement selectStatement, 
                                                        final AggregationType aggregationType, final int beginPosition, final int endPosition, final String innerExpression) {
        Optional<String> alias = aliasExpressionParser.parseSelectItemAlias().isPresent() ? aliasExpressionParser.parseSelectItemAlias() 
                : Optional.of(DerivedAlias.AGGREGATION_DISTINCT_DERIVED.getDerivedAlias(selectStatement.getAggregationDistinctSelectItems().size()));
        AggregationDistinctSelectItem result = new AggregationDistinctSelectItem(
                aggregationType, innerExpression, alias, getDistinctColumnName(innerExpression));
        selectStatement.getSQLTokens().add(new AggregationDistinctToken(beginPosition, endPosition - 1, result.getDistinctColumnName(), alias));
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
