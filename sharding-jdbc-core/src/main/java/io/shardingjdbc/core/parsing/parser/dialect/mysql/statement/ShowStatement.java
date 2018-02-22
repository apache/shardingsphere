package io.shardingjdbc.core.parsing.parser.dialect.mysql.statement;

import io.shardingjdbc.core.constant.SQLType;
import io.shardingjdbc.core.parsing.parser.sql.AbstractSQLStatement;
import lombok.Getter;

/**
 * Show statement.
 *
 * @author zhangliang
 */
@Getter
public final class ShowStatement extends AbstractSQLStatement {
    
    private final ShowType showType;
    
    public ShowStatement(final ShowType showType) {
        super(SQLType.OTHER);
        this.showType = showType;
    }
}
