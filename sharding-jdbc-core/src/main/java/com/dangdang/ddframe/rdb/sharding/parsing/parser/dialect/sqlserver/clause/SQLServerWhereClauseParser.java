package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.clause;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.SelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.WhereClauseParser;
import com.google.common.base.Optional;

import java.util.List;

/**
 * Where clause parser for SQLServer.
 *
 * @author zhangliang
 */
public final class SQLServerWhereClauseParser extends WhereClauseParser {
    
    public SQLServerWhereClauseParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected boolean isRowNumberCondition(final List<SelectItem> items, final String columnLabel) {
        Optional<String> rowNumberAlias = Optional.absent();
        for (SelectItem each : items) {
            if (each.getAlias().isPresent() && "ROW_NUMBER".equalsIgnoreCase(each.getExpression())) {
                rowNumberAlias = each.getAlias();
                break;
            }
        }
        return columnLabel.equalsIgnoreCase(rowNumberAlias.orNull());
    }
}
