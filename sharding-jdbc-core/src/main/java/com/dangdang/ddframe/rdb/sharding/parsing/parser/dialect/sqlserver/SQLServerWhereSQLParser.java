package com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver;

import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.selectitem.SelectItem;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.clause.WhereSQLParser;
import com.google.common.base.Optional;

import java.util.List;

/**
 * SQLServer WHERE语句解析对象.
 *
 * @author zhangliang
 */
public final class SQLServerWhereSQLParser extends WhereSQLParser {
    
    public SQLServerWhereSQLParser(final LexerEngine lexerEngine) {
        super(lexerEngine);
    }
    
    @Override
    protected boolean isRowNumberCondition(final List<SelectItem> items, final String columnLabel) {
        Optional<String> rowNumberAlias = Optional.absent();
        for (SelectItem each : items) {
            if (each.getAlias().isPresent() && "ROW_NUMBER".equalsIgnoreCase(each.getExpression())) {
                rowNumberAlias = each.getAlias();
            }
        }
        return columnLabel.equalsIgnoreCase(rowNumberAlias.orNull());
    }
}
