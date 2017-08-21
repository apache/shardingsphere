package com.dangdang.ddframe.rdb.sharding.parsing.parser.sql;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.DefaultKeyword;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.token.Symbol;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.CommonParser;
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
    
    private final CommonParser commonParser;
    
    private final AliasSQLParser aliasSQLParser;
    
    public TableSQLParser(final CommonParser commonParser) {
        this.commonParser = commonParser;
        aliasSQLParser = new AliasSQLParser(commonParser);
    }
    
    /**
     * 解析单表.
     *
     * @param sqlStatement SQL语句对象
     */
    public void parseSingleTable(final SQLStatement sqlStatement) {
        boolean hasParentheses = false;
        if (commonParser.skipIfEqual(Symbol.LEFT_PAREN)) {
            if (commonParser.equalAny(DefaultKeyword.SELECT)) {
                throw new UnsupportedOperationException("Cannot support subquery");
            }
            hasParentheses = true;
        }
        Table table;
        final int beginPosition = commonParser.getLexer().getCurrentToken().getEndPosition() - commonParser.getLexer().getCurrentToken().getLiterals().length();
        String literals = commonParser.getLexer().getCurrentToken().getLiterals();
        commonParser.getLexer().nextToken();
        if (commonParser.skipIfEqual(Symbol.DOT)) {
            commonParser.getLexer().nextToken();
            if (hasParentheses) {
                commonParser.accept(Symbol.RIGHT_PAREN);
            }
            table = new Table(SQLUtil.getExactlyValue(literals), aliasSQLParser.parse());
        } else {
            if (hasParentheses) {
                commonParser.accept(Symbol.RIGHT_PAREN);
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
        if (commonParser.skipIfEqual(DefaultKeyword.LEFT, DefaultKeyword.RIGHT, DefaultKeyword.FULL)) {
            commonParser.skipIfEqual(DefaultKeyword.OUTER);
            commonParser.accept(DefaultKeyword.JOIN);
            return true;
        } else if (commonParser.skipIfEqual(DefaultKeyword.INNER)) {
            commonParser.accept(DefaultKeyword.JOIN);
            return true;
        } else if (commonParser.skipIfEqual(DefaultKeyword.JOIN, Symbol.COMMA, DefaultKeyword.STRAIGHT_JOIN)) {
            return true;
        } else if (commonParser.skipIfEqual(DefaultKeyword.CROSS)) {
            if (commonParser.skipIfEqual(DefaultKeyword.JOIN, DefaultKeyword.APPLY)) {
                return true;
            }
        } else if (commonParser.skipIfEqual(DefaultKeyword.OUTER)) {
            if (commonParser.skipIfEqual(DefaultKeyword.APPLY)) {
                return true;
            }
        }
        return false;
    }
}
