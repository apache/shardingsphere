package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.clause;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.dialect.oracle.OracleKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.TableReferenceClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.dql.select.SelectStatement;

/**
 * Oracle 表从句解析器.
 *
 * @author zhangliang
 */
public final class OracleTableReferenceClauseParser extends TableReferenceClauseParser {
    
    public OracleTableReferenceClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine);
    }
    
    @Override
    public void parseTableFactor(final SelectStatement selectStatement) {
        if (getLexerEngine().skipIfEqual(OracleKeyword.ONLY)) {
            getLexerEngine().skipIfEqual(Symbol.LEFT_PAREN);
            parseTableFactorInternal(selectStatement);
            getLexerEngine().skipIfEqual(Symbol.RIGHT_PAREN);
            skipFlashbackQueryClause();
        } else {
            parseTableFactorInternal(selectStatement);
            skipPivotClause(selectStatement);
            skipFlashbackQueryClause();
        }
    }
    
    @Override
    protected void afterParseTableFactor(final SelectStatement selectStatement) {
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
}
