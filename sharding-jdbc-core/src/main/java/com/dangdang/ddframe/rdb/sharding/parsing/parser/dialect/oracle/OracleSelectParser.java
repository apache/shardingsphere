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
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.oracle.OracleKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Keyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.AbstractSQLParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.SelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.exception.SQLParsingUnsupportedException;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.AbstractSelectParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.dql.select.SelectStatement;

import java.util.Collection;
import java.util.Collections;

/**
 * Oracle Select语句解析器.
 *
 * @author zhangliang
 */
public final class OracleSelectParser extends AbstractSelectParser {
    
    public OracleSelectParser(final ShardingRule shardingRule, final AbstractSQLParser sqlParser) {
        super(shardingRule, sqlParser);
    }
    
    @Override
    protected Collection<Keyword> getCustomizedDistinctKeywords() {
        return Collections.<Keyword>singletonList(DefaultKeyword.UNIQUE);
    }
    
    @Override
    protected Keyword[] getSkippedKeywordsBeforeSelectItem() {
        return new Keyword[] {OracleKeyword.CONNECT_BY_ROOT};
    }
    
    @Override
    protected void customizedBetweenWhereAndGroupBy(final SelectStatement selectStatement) {
        skipHierarchicalQueryClause(selectStatement);
    }
    
    private void skipHierarchicalQueryClause(final SelectStatement selectStatement) {
        skipConnect(selectStatement);
        skipStart(selectStatement);
        skipConnect(selectStatement);
    }
    
    private void skipStart(final SelectStatement selectStatement) {
        if (getSqlParser().skipIfEqual(OracleKeyword.START)) {
            getSqlParser().accept(DefaultKeyword.WITH);
            getSqlParser().parseComparisonCondition(getShardingRule(), selectStatement, Collections.<SelectItem>emptyList());
        }
    }
    
    private void skipConnect(final SelectStatement selectStatement) {
        if (getSqlParser().skipIfEqual(OracleKeyword.CONNECT)) {
            getSqlParser().accept(DefaultKeyword.BY);
            getSqlParser().skipIfEqual(OracleKeyword.PRIOR);
            if (getSqlParser().skipIfEqual(OracleKeyword.NOCYCLE)) {
                getSqlParser().skipIfEqual(OracleKeyword.PRIOR);
            }
            getSqlParser().parseComparisonCondition(getShardingRule(), selectStatement, Collections.<SelectItem>emptyList());
        }
    }
    
    @Override
    protected void customizedBetweenGroupByAndOrderBy(final SelectStatement selectStatement) {
        skipModelClause();
    }
    
    private void skipModelClause() {
        if (!getSqlParser().skipIfEqual(OracleKeyword.MODEL)) {
            return;
        }
        skipCellReferenceOptions();
        getSqlParser().skipIfEqual(OracleKeyword.RETURN);
        getSqlParser().skipIfEqual(DefaultKeyword.ALL);
        getSqlParser().skipIfEqual(OracleKeyword.UPDATED);
        getSqlParser().skipIfEqual(OracleKeyword.ROWS);
        while (getSqlParser().skipIfEqual(OracleKeyword.REFERENCE)) {
            getSqlParser().getLexer().nextToken();
            getSqlParser().accept(DefaultKeyword.ON);
            getSqlParser().skipParentheses();
            skipModelColumnClause();
            skipCellReferenceOptions();
        }
        skipMainModelClause();
    }
    
    private void skipCellReferenceOptions() {
        if (getSqlParser().skipIfEqual(OracleKeyword.IGNORE)) {
            getSqlParser().accept(OracleKeyword.NAV);
        } else if (getSqlParser().skipIfEqual(OracleKeyword.KEEP)) {
            getSqlParser().accept(OracleKeyword.NAV);
        }
        if (getSqlParser().skipIfEqual(DefaultKeyword.UNIQUE)) {
            getSqlParser().skipIfEqual(OracleKeyword.DIMENSION, OracleKeyword.SINGLE);
            getSqlParser().skipIfEqual(OracleKeyword.REFERENCE);
        }
    }
    
    private void skipMainModelClause() {
        if (getSqlParser().skipIfEqual(OracleKeyword.MAIN)) {
            getSqlParser().getLexer().nextToken();
        }
        skipQueryPartitionClause();
        getSqlParser().accept(OracleKeyword.DIMENSION);
        getSqlParser().accept(DefaultKeyword.BY);
        getSqlParser().skipParentheses();
        getSqlParser().accept(OracleKeyword.MEASURES);
        getSqlParser().skipParentheses();
        skipCellReferenceOptions();
        skipModelRulesClause();
    }
    
    private void skipModelRulesClause() {
        if (getSqlParser().skipIfEqual(OracleKeyword.RULES)) {
            getSqlParser().skipIfEqual(DefaultKeyword.UPDATE);
            getSqlParser().skipIfEqual(OracleKeyword.UPSERT);
            if (getSqlParser().skipIfEqual(OracleKeyword.AUTOMATIC)) {
                getSqlParser().accept(DefaultKeyword.ORDER);
            } else if (getSqlParser().skipIfEqual(OracleKeyword.SEQUENTIAL)) {
                getSqlParser().accept(DefaultKeyword.ORDER);
            }
        }
        if (getSqlParser().skipIfEqual(DefaultKeyword.ITERATE)) {
            getSqlParser().skipParentheses();
            if (getSqlParser().skipIfEqual(DefaultKeyword.UNTIL)) {
                getSqlParser().skipParentheses();
            }
        }
        getSqlParser().skipParentheses();
    }
    
    private void skipQueryPartitionClause() {
        if (getSqlParser().skipIfEqual(OracleKeyword.PARTITION)) {
            getSqlParser().accept(DefaultKeyword.BY);
            if (getSqlParser().equalAny(Symbol.LEFT_PAREN)) {
                getSqlParser().skipParentheses();
            } else {
                throw new UnsupportedOperationException("Cannot support PARTITION BY without ()");
            }
        }
    }
    
    private void skipModelColumnClause() {
        throw new SQLParsingUnsupportedException(getSqlParser().getLexer().getCurrentToken().getType());
    }
    
    @Override
    protected void customizedSelect(final SelectStatement selectStatement) {
        if (getSqlParser().equalAny(DefaultKeyword.FOR)) {
            skipForUpdate();
        }
        if (selectStatement.getOrderByItems().isEmpty()) {
            parseOrderBy(selectStatement);
        }
    }
    
    private void skipForUpdate() {
        getSqlParser().getLexer().nextToken();
        getSqlParser().accept(DefaultKeyword.UPDATE);
        if (getSqlParser().skipIfEqual(DefaultKeyword.OF)) {
            do {
                getSqlParser().parseExpression();
            } while (getSqlParser().skipIfEqual(Symbol.COMMA));
        }
        if (getSqlParser().equalAny(OracleKeyword.NOWAIT, OracleKeyword.WAIT)) {
            getSqlParser().getLexer().nextToken();
        } else if (getSqlParser().skipIfEqual(OracleKeyword.SKIP)) {
            getSqlParser().accept(OracleKeyword.LOCKED);
        }
    }
    
    @Override
    protected void parseGroupBy(final SelectStatement selectStatement) {
        if (getSqlParser().equalAny(DefaultKeyword.GROUP)) {
            getSqlParser().getLexer().nextToken();
            getSqlParser().accept(DefaultKeyword.BY);
            while (true) {
                if (getSqlParser().equalAny(OracleKeyword.ROLLUP, OracleKeyword.CUBE, OracleKeyword.GROUPING)) {
                    throw new UnsupportedOperationException("Cannot support ROLLUP, CUBE, GROUPING SETS");
                } 
                addGroupByItem(getSqlParser().parseExpression(), selectStatement);
                if (!getSqlParser().equalAny(Symbol.COMMA)) {
                    break;
                }
                getSqlParser().getLexer().nextToken();
            }
            if (getSqlParser().skipIfEqual(DefaultKeyword.HAVING)) {
                throw new UnsupportedOperationException("Cannot support Having");
            }
            selectStatement.setGroupByLastPosition(getSqlParser().getLexer().getCurrentToken().getEndPosition() - getSqlParser().getLexer().getCurrentToken().getLiterals().length());
        }
    }
    
    @Override
    protected void parseTableFactor(final SelectStatement selectStatement) {
        if (getSqlParser().skipIfEqual(OracleKeyword.ONLY)) {
            getSqlParser().skipIfEqual(Symbol.LEFT_PAREN);
            parseQueryTableExpression(selectStatement);
            getSqlParser().skipIfEqual(Symbol.RIGHT_PAREN);
            skipFlashbackQueryClause();
        } else {
            parseQueryTableExpression(selectStatement);
            skipPivotClause();
            skipFlashbackQueryClause();
        }
    }
    
    private void parseQueryTableExpression(final SelectStatement selectStatement) {
        parseTableFactorInternal(selectStatement);
        parseSample();
        skipPartition();
    }
    
    private void parseSample() {
        if (getSqlParser().skipIfEqual(OracleKeyword.SAMPLE)) {
            getSqlParser().skipIfEqual(OracleKeyword.BLOCK);
            getSqlParser().skipParentheses();
            if (getSqlParser().skipIfEqual(OracleKeyword.SEED)) {
                getSqlParser().skipParentheses();
            }
        }
    }
    
    private void skipPartition() {
        skipPartition(OracleKeyword.PARTITION);
        skipPartition(OracleKeyword.SUBPARTITION);
    }
    
    private void skipPartition(final OracleKeyword keyword) {
        if (getSqlParser().skipIfEqual(keyword)) {
            getSqlParser().skipParentheses();
            if (getSqlParser().skipIfEqual(DefaultKeyword.FOR)) {
                getSqlParser().skipParentheses();
            }
        }
    }
    
    private void skipFlashbackQueryClause() {
        if (isFlashbackQueryClauseForVersions() || isFlashbackQueryClauseForAs()) {
            throw new UnsupportedOperationException("Cannot support Flashback Query");
        }
    }
    
    private boolean isFlashbackQueryClauseForVersions() {
        return getSqlParser().skipIfEqual(OracleKeyword.VERSIONS) && getSqlParser().skipIfEqual(DefaultKeyword.BETWEEN);
    }
    
    private boolean isFlashbackQueryClauseForAs() {
        return getSqlParser().skipIfEqual(DefaultKeyword.AS) && getSqlParser().skipIfEqual(DefaultKeyword.OF)
                && (getSqlParser().skipIfEqual(OracleKeyword.SCN) || getSqlParser().skipIfEqual(OracleKeyword.TIMESTAMP));
    }
    
    private void skipPivotClause() {
        if (getSqlParser().skipIfEqual(OracleKeyword.PIVOT)) {
            getSqlParser().skipIfEqual(OracleKeyword.XML);
            getSqlParser().skipParentheses();
        } else if (getSqlParser().skipIfEqual(OracleKeyword.UNPIVOT)) {
            if (getSqlParser().skipIfEqual(OracleKeyword.INCLUDE)) {
                getSqlParser().accept(OracleKeyword.NULLS);
            } else if (getSqlParser().skipIfEqual(OracleKeyword.EXCLUDE)) {
                getSqlParser().accept(OracleKeyword.NULLS);
            }
            getSqlParser().skipParentheses();
        }
    }
    
    @Override
    protected void skipAfterOrderByItem(final SelectStatement selectStatement) {
        if (getSqlParser().skipIfEqual(OracleKeyword.NULLS)) {
            getSqlParser().getLexer().nextToken();
            if (!getSqlParser().skipIfEqual(OracleKeyword.FIRST, OracleKeyword.LAST)) {
                throw new SQLParsingUnsupportedException(getSqlParser().getLexer().getCurrentToken().getType());
            }
        }
    }
}
