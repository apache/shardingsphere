package io.shardingjdbc.core.parsing.parser.dialect.sqlserver.clause;

import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.parsing.lexer.LexerEngine;
import io.shardingjdbc.core.parsing.parser.context.selectitem.SelectItem;
import io.shardingjdbc.core.parsing.parser.clause.WhereClauseParser;
import com.google.common.base.Optional;

import java.util.List;

/**
 * Where clause parser for SQLServer.
 *
 * @author zhangliang
 */
public final class SQLServerWhereClauseParser extends WhereClauseParser {
    
    public SQLServerWhereClauseParser(final LexerEngine lexerEngine) {
        super(DatabaseType.SQLServer, lexerEngine);
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
