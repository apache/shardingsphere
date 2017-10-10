package io.shardingjdbc.core.parsing.parser.dialect.oracle.clause;

import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.lexer.dialect.oracle.OracleKeyword;
import io.shardingjdbc.core.parsing.lexer.token.DefaultKeyword;
import io.shardingjdbc.core.parsing.lexer.token.Symbol;
import io.shardingjdbc.core.parsing.parser.clause.TableReferencesClauseParser;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;

/**
 * Table references clause parser for Oracle.
 *
 * @author zhangliang
 */
public final class OracleTableReferencesClauseParser extends TableReferencesClauseParser {
    
    public OracleTableReferencesClauseParser(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        super(shardingRule, lexerEngine);
    }
    
    @Override
    protected void parseTableReference(final SQLStatement sqlStatement, final boolean isSingleTableOnly) {
        if (getLexerEngine().skipIfEqual(OracleKeyword.ONLY)) {
            getLexerEngine().skipIfEqual(Symbol.LEFT_PAREN);
            parseQueryTableExpression(sqlStatement, isSingleTableOnly);
            getLexerEngine().skipIfEqual(Symbol.RIGHT_PAREN);
            parseFlashbackQueryClause();
        } else {
            parseQueryTableExpression(sqlStatement, isSingleTableOnly);
            parsePivotClause(sqlStatement);
            parseFlashbackQueryClause();
        }
    }
    
    private void parseQueryTableExpression(final SQLStatement sqlStatement, final boolean isSingleTableOnly) {
        parseTableFactor(sqlStatement, isSingleTableOnly);
        parseDbLink();
        parsePartitionExtensionClause();
        parseSampleClause();
    }
    
    private void parseDbLink() {
        getLexerEngine().unsupportedIfEqual(Symbol.AT);
    }
    
    private void parsePartitionExtensionClause() {
        getLexerEngine().unsupportedIfEqual(OracleKeyword.PARTITION, OracleKeyword.SUBPARTITION);
    }
    
    private void parseSampleClause() {
        getLexerEngine().unsupportedIfEqual(OracleKeyword.SAMPLE);
    }
    
    private void parseFlashbackQueryClause() {
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
    
    private void parsePivotClause(final SQLStatement sqlStatement) {
        if (getLexerEngine().skipIfEqual(OracleKeyword.PIVOT)) {
            getLexerEngine().skipIfEqual(OracleKeyword.XML);
            getLexerEngine().skipParentheses(sqlStatement);
        } else if (getLexerEngine().skipIfEqual(OracleKeyword.UNPIVOT)) {
            if (getLexerEngine().skipIfEqual(OracleKeyword.INCLUDE)) {
                getLexerEngine().accept(OracleKeyword.NULLS);
            } else if (getLexerEngine().skipIfEqual(OracleKeyword.EXCLUDE)) {
                getLexerEngine().accept(OracleKeyword.NULLS);
            }
            getLexerEngine().skipParentheses(sqlStatement);
        }
    }
}
