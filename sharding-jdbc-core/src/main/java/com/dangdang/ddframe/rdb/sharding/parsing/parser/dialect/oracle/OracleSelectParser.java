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
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.oracle.OracleKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.SelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.DistinctSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.AbstractSelectParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.SelectStatement;

import java.util.Collections;

/**
 * Oracle Select语句解析器.
 *
 * @author zhangliang
 */
public final class OracleSelectParser extends AbstractSelectParser {
    
    private final DistinctSQLParser distinctSQLParser;
    
    public OracleSelectParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine, new OracleWhereSQLParser(lexerEngine));
        distinctSQLParser = new OracleDistinctSQLParser(lexerEngine);
    }
    
    @Override
    protected void parseInternal(final SelectStatement selectStatement) {
        distinctSQLParser.parse();
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
        if (!getLexerEngine().skipIfEqual(OracleKeyword.START)) {
            return;
        }
        getLexerEngine().accept(DefaultKeyword.WITH);
        getWhereSQLParser().parseComparisonCondition(getShardingRule(), selectStatement, Collections.<SelectItem>emptyList());
    }
    
    private void skipConnect(final SelectStatement selectStatement) {
        if (!getLexerEngine().skipIfEqual(OracleKeyword.CONNECT)) {
            return;
        }
        getLexerEngine().accept(DefaultKeyword.BY);
        getLexerEngine().skipIfEqual(OracleKeyword.PRIOR);
        if (getLexerEngine().skipIfEqual(OracleKeyword.NOCYCLE)) {
            getLexerEngine().skipIfEqual(OracleKeyword.PRIOR);
        }
        getWhereSQLParser().parseComparisonCondition(getShardingRule(), selectStatement, Collections.<SelectItem>emptyList());
    }
    
    private void skipModelClause(final SelectStatement selectStatement) {
        if (!getLexerEngine().skipIfEqual(OracleKeyword.MODEL)) {
            return;
        }
        skipCellReferenceOptions();
        getLexerEngine().skipIfEqual(OracleKeyword.RETURN);
        getLexerEngine().skipIfEqual(DefaultKeyword.ALL);
        getLexerEngine().skipIfEqual(OracleKeyword.UPDATED);
        getLexerEngine().skipIfEqual(OracleKeyword.ROWS);
        while (getLexerEngine().skipIfEqual(OracleKeyword.REFERENCE)) {
            getLexerEngine().nextToken();
            getLexerEngine().accept(DefaultKeyword.ON);
            getLexerEngine().skipParentheses(selectStatement);
            skipModelColumnClause();
            skipCellReferenceOptions();
        }
        skipMainModelClause(selectStatement);
    }
    
    private void skipCellReferenceOptions() {
        if (getLexerEngine().skipIfEqual(OracleKeyword.IGNORE)) {
            getLexerEngine().accept(OracleKeyword.NAV);
        } else if (getLexerEngine().skipIfEqual(OracleKeyword.KEEP)) {
            getLexerEngine().accept(OracleKeyword.NAV);
        }
        if (getLexerEngine().skipIfEqual(DefaultKeyword.UNIQUE)) {
            getLexerEngine().skipIfEqual(OracleKeyword.DIMENSION, OracleKeyword.SINGLE);
            getLexerEngine().skipIfEqual(OracleKeyword.REFERENCE);
        }
    }
    
    private void skipMainModelClause(final SelectStatement selectStatement) {
        if (getLexerEngine().skipIfEqual(OracleKeyword.MAIN)) {
            getLexerEngine().nextToken();
        }
        skipQueryPartitionClause(selectStatement);
        getLexerEngine().accept(OracleKeyword.DIMENSION);
        getLexerEngine().accept(DefaultKeyword.BY);
        getLexerEngine().skipParentheses(selectStatement);
        getLexerEngine().accept(OracleKeyword.MEASURES);
        getLexerEngine().skipParentheses(selectStatement);
        skipCellReferenceOptions();
        skipModelRulesClause(selectStatement);
    }
    
    private void skipModelRulesClause(final SelectStatement selectStatement) {
        if (getLexerEngine().skipIfEqual(OracleKeyword.RULES)) {
            getLexerEngine().skipIfEqual(DefaultKeyword.UPDATE);
            getLexerEngine().skipIfEqual(OracleKeyword.UPSERT);
            if (getLexerEngine().skipIfEqual(OracleKeyword.AUTOMATIC)) {
                getLexerEngine().accept(DefaultKeyword.ORDER);
            } else if (getLexerEngine().skipIfEqual(OracleKeyword.SEQUENTIAL)) {
                getLexerEngine().accept(DefaultKeyword.ORDER);
            }
        }
        if (getLexerEngine().skipIfEqual(DefaultKeyword.ITERATE)) {
            getLexerEngine().skipParentheses(selectStatement);
            if (getLexerEngine().skipIfEqual(DefaultKeyword.UNTIL)) {
                getLexerEngine().skipParentheses(selectStatement);
            }
        }
        getLexerEngine().skipParentheses(selectStatement);
    }
    
    private void skipQueryPartitionClause(final SelectStatement selectStatement) {
        if (!getLexerEngine().skipIfEqual(OracleKeyword.PARTITION)) {
            return;
        }
        getLexerEngine().accept(DefaultKeyword.BY);
        if (!getLexerEngine().equalAny(Symbol.LEFT_PAREN)) {
            throw new UnsupportedOperationException("Cannot support PARTITION BY without ()");
        }
        getLexerEngine().skipParentheses(selectStatement);
    }
    
    private void skipModelColumnClause() {
        throw new SQLParsingUnsupportedException(getLexerEngine().getCurrentToken().getType());
    }
    
    private void skipFor(final SelectStatement selectStatement) {
        if (!getLexerEngine().skipIfEqual(DefaultKeyword.FOR)) {
            return;
        }
        getLexerEngine().accept(DefaultKeyword.UPDATE);
        if (getLexerEngine().skipIfEqual(DefaultKeyword.OF)) {
            do {
                getExpressionSQLParser().parse(selectStatement);
            } while (getLexerEngine().skipIfEqual(Symbol.COMMA));
        }
        if (getLexerEngine().equalAny(OracleKeyword.NOWAIT, OracleKeyword.WAIT)) {
            getLexerEngine().nextToken();
        } else if (getLexerEngine().skipIfEqual(OracleKeyword.SKIP)) {
            getLexerEngine().accept(OracleKeyword.LOCKED);
        }
    }
    
    @Override
    protected void parseTableFactor(final SelectStatement selectStatement) {
        if (getLexerEngine().skipIfEqual(OracleKeyword.ONLY)) {
            getLexerEngine().skipIfEqual(Symbol.LEFT_PAREN);
            parseQueryTableExpression(selectStatement);
            getLexerEngine().skipIfEqual(Symbol.RIGHT_PAREN);
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
        if (!getLexerEngine().skipIfEqual(OracleKeyword.SAMPLE)) {
            return;
        }
        getLexerEngine().skipIfEqual(OracleKeyword.BLOCK);
        getLexerEngine().skipParentheses(selectStatement);
        if (getLexerEngine().skipIfEqual(OracleKeyword.SEED)) {
            getLexerEngine().skipParentheses(selectStatement);
        }
    }
    
    private void skipPartition(final SelectStatement selectStatement) {
        skipPartition(selectStatement, OracleKeyword.PARTITION);
        skipPartition(selectStatement, OracleKeyword.SUBPARTITION);
    }
    
    private void skipPartition(final SelectStatement selectStatement, final OracleKeyword keyword) {
        if (!getLexerEngine().skipIfEqual(keyword)) {
            return;
        }
        getLexerEngine().skipParentheses(selectStatement);
        if (getLexerEngine().skipIfEqual(DefaultKeyword.FOR)) {
            getLexerEngine().skipParentheses(selectStatement);
        }
    }
    
    private void skipFlashbackQueryClause() {
        if (isFlashbackQueryClauseForVersions() || isFlashbackQueryClauseForAs()) {
            throw new UnsupportedOperationException("Cannot support Flashback Query");
        }
    }
    
    private boolean isFlashbackQueryClauseForVersions() {
        return getLexerEngine().skipIfEqual(OracleKeyword.VERSIONS) && getLexerEngine().skipIfEqual(DefaultKeyword.BETWEEN);
    }
    
    private boolean isFlashbackQueryClauseForAs() {
        return getLexerEngine().skipIfEqual(DefaultKeyword.AS) && getLexerEngine().skipIfEqual(DefaultKeyword.OF)
                && (getLexerEngine().skipIfEqual(OracleKeyword.SCN) || getLexerEngine().skipIfEqual(OracleKeyword.TIMESTAMP));
    }
    
    private void skipPivotClause(final SelectStatement selectStatement) {
        if (getLexerEngine().skipIfEqual(OracleKeyword.PIVOT)) {
            getLexerEngine().skipIfEqual(OracleKeyword.XML);
            getLexerEngine().skipParentheses(selectStatement);
        } else if (getLexerEngine().skipIfEqual(OracleKeyword.UNPIVOT)) {
            if (getLexerEngine().skipIfEqual(OracleKeyword.INCLUDE)) {
                getLexerEngine().accept(OracleKeyword.NULLS);
            } else if (getLexerEngine().skipIfEqual(OracleKeyword.EXCLUDE)) {
                getLexerEngine().accept(OracleKeyword.NULLS);
            }
            getLexerEngine().skipParentheses(selectStatement);
        }
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
        if (!getLexerEngine().skipIfEqual(OracleKeyword.NULLS)) {
            return OrderType.ASC;
        }
        if (getLexerEngine().skipIfEqual(OracleKeyword.FIRST)) {
            return OrderType.ASC;
        }
        if (getLexerEngine().skipIfEqual(OracleKeyword.LAST)) {
            return OrderType.DESC;
        }
        throw new SQLParsingException(getLexerEngine());
    }
}
