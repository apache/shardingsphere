/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constant.OrderType;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.oracle.OracleKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.CommonParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.SelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.AbstractSelectParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.SelectStatement;

import java.util.Collections;

/**
 * Oracle Select语句解析器.
 *
 * @author zhangliang
 */
public final class OracleSelectParser extends AbstractSelectParser {
    
    public OracleSelectParser(final ShardingRule shardingRule, final CommonParser commonParser) {
        super(shardingRule, commonParser, new OracleWhereSQLParser(commonParser));
    }
    
    @Override
    protected void parseInternal(final SelectStatement selectStatement) {
        parseDistinct();
        parseSelectList(selectStatement);
        parseFrom(selectStatement);
        parseWhere(selectStatement);
        skipHierarchicalQueryClause(selectStatement);
        parseGroupBy(selectStatement);
        parseHaving();
        skipModelClause(selectStatement);
        parseOrderBy(selectStatement);
        skipFor(selectStatement);
        parseRest();
    }
    
    private void skipHierarchicalQueryClause(final SelectStatement selectStatement) {
        skipConnect(selectStatement);
        skipStart(selectStatement);
        skipConnect(selectStatement);
    }
    
    private void skipStart(final SelectStatement selectStatement) {
        if (!getCommonParser().skipIfEqual(OracleKeyword.START)) {
            return;
        }
        getCommonParser().accept(DefaultKeyword.WITH);
        getWhereSQLParser().parseComparisonCondition(getShardingRule(), selectStatement, Collections.<SelectItem>emptyList());
    }
    
    private void skipConnect(final SelectStatement selectStatement) {
        if (!getCommonParser().skipIfEqual(OracleKeyword.CONNECT)) {
            return;
        }
        getCommonParser().accept(DefaultKeyword.BY);
        getCommonParser().skipIfEqual(OracleKeyword.PRIOR);
        if (getCommonParser().skipIfEqual(OracleKeyword.NOCYCLE)) {
            getCommonParser().skipIfEqual(OracleKeyword.PRIOR);
        }
        getWhereSQLParser().parseComparisonCondition(getShardingRule(), selectStatement, Collections.<SelectItem>emptyList());
    }
    
    private void skipModelClause(final SelectStatement selectStatement) {
        if (!getCommonParser().skipIfEqual(OracleKeyword.MODEL)) {
            return;
        }
        skipCellReferenceOptions();
        getCommonParser().skipIfEqual(OracleKeyword.RETURN);
        getCommonParser().skipIfEqual(DefaultKeyword.ALL);
        getCommonParser().skipIfEqual(OracleKeyword.UPDATED);
        getCommonParser().skipIfEqual(OracleKeyword.ROWS);
        while (getCommonParser().skipIfEqual(OracleKeyword.REFERENCE)) {
            getCommonParser().getLexer().nextToken();
            getCommonParser().accept(DefaultKeyword.ON);
            getCommonParser().skipParentheses(selectStatement);
            skipModelColumnClause();
            skipCellReferenceOptions();
        }
        skipMainModelClause(selectStatement);
    }
    
    private void skipCellReferenceOptions() {
        if (getCommonParser().skipIfEqual(OracleKeyword.IGNORE)) {
            getCommonParser().accept(OracleKeyword.NAV);
        } else if (getCommonParser().skipIfEqual(OracleKeyword.KEEP)) {
            getCommonParser().accept(OracleKeyword.NAV);
        }
        if (getCommonParser().skipIfEqual(DefaultKeyword.UNIQUE)) {
            getCommonParser().skipIfEqual(OracleKeyword.DIMENSION, OracleKeyword.SINGLE);
            getCommonParser().skipIfEqual(OracleKeyword.REFERENCE);
        }
    }
    
    private void skipMainModelClause(final SelectStatement selectStatement) {
        if (getCommonParser().skipIfEqual(OracleKeyword.MAIN)) {
            getCommonParser().getLexer().nextToken();
        }
        skipQueryPartitionClause(selectStatement);
        getCommonParser().accept(OracleKeyword.DIMENSION);
        getCommonParser().accept(DefaultKeyword.BY);
        getCommonParser().skipParentheses(selectStatement);
        getCommonParser().accept(OracleKeyword.MEASURES);
        getCommonParser().skipParentheses(selectStatement);
        skipCellReferenceOptions();
        skipModelRulesClause(selectStatement);
    }
    
    private void skipModelRulesClause(final SelectStatement selectStatement) {
        if (getCommonParser().skipIfEqual(OracleKeyword.RULES)) {
            getCommonParser().skipIfEqual(DefaultKeyword.UPDATE);
            getCommonParser().skipIfEqual(OracleKeyword.UPSERT);
            if (getCommonParser().skipIfEqual(OracleKeyword.AUTOMATIC)) {
                getCommonParser().accept(DefaultKeyword.ORDER);
            } else if (getCommonParser().skipIfEqual(OracleKeyword.SEQUENTIAL)) {
                getCommonParser().accept(DefaultKeyword.ORDER);
            }
        }
        if (getCommonParser().skipIfEqual(DefaultKeyword.ITERATE)) {
            getCommonParser().skipParentheses(selectStatement);
            if (getCommonParser().skipIfEqual(DefaultKeyword.UNTIL)) {
                getCommonParser().skipParentheses(selectStatement);
            }
        }
        getCommonParser().skipParentheses(selectStatement);
    }
    
    private void skipQueryPartitionClause(final SelectStatement selectStatement) {
        if (!getCommonParser().skipIfEqual(OracleKeyword.PARTITION)) {
            return;
        }
        getCommonParser().accept(DefaultKeyword.BY);
        if (!getCommonParser().equalAny(Symbol.LEFT_PAREN)) {
            throw new UnsupportedOperationException("Cannot support PARTITION BY without ()");
        }
        getCommonParser().skipParentheses(selectStatement);
    }
    
    private void skipModelColumnClause() {
        throw new SQLParsingUnsupportedException(getCommonParser().getLexer().getCurrentToken().getType());
    }
    
    private void skipFor(final SelectStatement selectStatement) {
        if (!getCommonParser().skipIfEqual(DefaultKeyword.FOR)) {
            return;
        }
        getCommonParser().accept(DefaultKeyword.UPDATE);
        if (getCommonParser().skipIfEqual(DefaultKeyword.OF)) {
            do {
                getExpressionSQLParser().parse(selectStatement);
            } while (getCommonParser().skipIfEqual(Symbol.COMMA));
        }
        if (getCommonParser().equalAny(OracleKeyword.NOWAIT, OracleKeyword.WAIT)) {
            getCommonParser().getLexer().nextToken();
        } else if (getCommonParser().skipIfEqual(OracleKeyword.SKIP)) {
            getCommonParser().accept(OracleKeyword.LOCKED);
        }
    }
    
    @Override
    protected void parseTableFactor(final SelectStatement selectStatement) {
        if (getCommonParser().skipIfEqual(OracleKeyword.ONLY)) {
            getCommonParser().skipIfEqual(Symbol.LEFT_PAREN);
            parseQueryTableExpression(selectStatement);
            getCommonParser().skipIfEqual(Symbol.RIGHT_PAREN);
            skipFlashbackQueryClause();
        } else {
            parseQueryTableExpression(selectStatement);
            skipPivotClause(selectStatement);
            skipFlashbackQueryClause();
        }
    }
    
    private void parseQueryTableExpression(final SelectStatement selectStatement) {
        parseTableFactorInternal(selectStatement);
        parseSample(selectStatement);
        skipPartition(selectStatement);
    }
    
    private void parseSample(final SelectStatement selectStatement) {
        if (!getCommonParser().skipIfEqual(OracleKeyword.SAMPLE)) {
            return;
        }
        getCommonParser().skipIfEqual(OracleKeyword.BLOCK);
        getCommonParser().skipParentheses(selectStatement);
        if (getCommonParser().skipIfEqual(OracleKeyword.SEED)) {
            getCommonParser().skipParentheses(selectStatement);
        }
    }
    
    private void skipPartition(final SelectStatement selectStatement) {
        skipPartition(selectStatement, OracleKeyword.PARTITION);
        skipPartition(selectStatement, OracleKeyword.SUBPARTITION);
    }
    
    private void skipPartition(final SelectStatement selectStatement, final OracleKeyword keyword) {
        if (!getCommonParser().skipIfEqual(keyword)) {
            return;
        }
        getCommonParser().skipParentheses(selectStatement);
        if (getCommonParser().skipIfEqual(DefaultKeyword.FOR)) {
            getCommonParser().skipParentheses(selectStatement);
        }
    }
    
    private void skipFlashbackQueryClause() {
        if (isFlashbackQueryClauseForVersions() || isFlashbackQueryClauseForAs()) {
            throw new UnsupportedOperationException("Cannot support Flashback Query");
        }
    }
    
    private boolean isFlashbackQueryClauseForVersions() {
        return getCommonParser().skipIfEqual(OracleKeyword.VERSIONS) && getCommonParser().skipIfEqual(DefaultKeyword.BETWEEN);
    }
    
    private boolean isFlashbackQueryClauseForAs() {
        return getCommonParser().skipIfEqual(DefaultKeyword.AS) && getCommonParser().skipIfEqual(DefaultKeyword.OF)
                && (getCommonParser().skipIfEqual(OracleKeyword.SCN) || getCommonParser().skipIfEqual(OracleKeyword.TIMESTAMP));
    }
    
    private void skipPivotClause(final SelectStatement selectStatement) {
        if (getCommonParser().skipIfEqual(OracleKeyword.PIVOT)) {
            getCommonParser().skipIfEqual(OracleKeyword.XML);
            getCommonParser().skipParentheses(selectStatement);
        } else if (getCommonParser().skipIfEqual(OracleKeyword.UNPIVOT)) {
            if (getCommonParser().skipIfEqual(OracleKeyword.INCLUDE)) {
                getCommonParser().accept(OracleKeyword.NULLS);
            } else if (getCommonParser().skipIfEqual(OracleKeyword.EXCLUDE)) {
                getCommonParser().accept(OracleKeyword.NULLS);
            }
            getCommonParser().skipParentheses(selectStatement);
        }
    }
    
    @Override
    protected Keyword[] getSynonymousKeywordsForDistinct() {
        return new Keyword[] {DefaultKeyword.UNIQUE};
    }
    
    @Override
    protected Keyword[] getSkippedKeywordsBeforeSelectItem() {
        return new Keyword[] {OracleKeyword.CONNECT_BY_ROOT};
    }
    
    @Override
    protected Keyword[] getUnsupportedKeywordBeforeGroupByItem() {
        return new Keyword[] {OracleKeyword.ROLLUP, OracleKeyword.CUBE, OracleKeyword.GROUPING};
    }
    
    @Override
    protected OrderType getNullOrderType() {
        if (!getCommonParser().skipIfEqual(OracleKeyword.NULLS)) {
            return OrderType.ASC;
        }
        if (getCommonParser().skipIfEqual(OracleKeyword.FIRST)) {
            return OrderType.ASC;
        }
        if (getCommonParser().skipIfEqual(OracleKeyword.LAST)) {
            return OrderType.DESC;
        }
        throw new SQLParsingException(getCommonParser().getLexer());
    }
}
