package com.dangdang.ddframe.rdb.sharding.parsing.parser.sql;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.table.Table;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatement;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.token.TableToken;
import com.dangdang.ddframe.rdb.sharding.util.SQLUtil;

/**
 * Table解析对象.
 *
 * @author zhangliang
 */
public class TableSQLParser implements SQLParser {
    
    private final LexerEngine lexerEngine;
    
    private final AliasSQLParser aliasSQLParser;
    
    public TableSQLParser(final LexerEngine lexerEngine) {
        this.lexerEngine = lexerEngine;
        aliasSQLParser = new AliasSQLParser(lexerEngine);
    }
    
    /**
     * 解析单表.
     *
     * @param sqlStatement SQL语句对象
     */
    public void parseSingleTable(final SQLStatement sqlStatement) {
        boolean hasParentheses = false;
        if (lexerEngine.skipIfEqual(Symbol.LEFT_PAREN)) {
            if (lexerEngine.equalAny(DefaultKeyword.SELECT)) {
                throw new UnsupportedOperationException("Cannot support subquery");
            }
            hasParentheses = true;
        }
        Table table;
        final int beginPosition = lexerEngine.getCurrentToken().getEndPosition() - lexerEngine.getCurrentToken().getLiterals().length();
        String literals = lexerEngine.getCurrentToken().getLiterals();
        lexerEngine.nextToken();
        if (lexerEngine.skipIfEqual(Symbol.DOT)) {
            lexerEngine.nextToken();
            if (hasParentheses) {
                lexerEngine.accept(Symbol.RIGHT_PAREN);
            }
            table = new Table(SQLUtil.getExactlyValue(literals), aliasSQLParser.parse());
        } else {
            if (hasParentheses) {
                lexerEngine.accept(Symbol.RIGHT_PAREN);
            }
            table = new Table(SQLUtil.getExactlyValue(literals), aliasSQLParser.parse());
        }
        if (skipJoin()) {
            throw new UnsupportedOperationException("Cannot support Multiple-Table.");
        }
        sqlStatement.getSqlTokens().add(new TableToken(beginPosition, literals));
        sqlStatement.getTables().add(table);
    }
    
    /**
     * 跳过表关联.
     *
     * @return 是否表关联.
     */
    public boolean skipJoin() {
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
}
