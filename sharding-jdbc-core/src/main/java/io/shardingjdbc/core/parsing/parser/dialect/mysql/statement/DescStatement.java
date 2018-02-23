package io.shardingjdbc.core.parsing.parser.dialect.mysql.statement;

import io.shardingjdbc.core.constant.SQLType;
import io.shardingjdbc.core.parsing.parser.sql.AbstractSQLStatement;
import lombok.Getter;

/**
 * Desc statement.
 *
 * @author zhangliang
 */
@Getter
public final class DescStatement extends AbstractSQLStatement {
    
    public DescStatement() {
        super(SQLType.OTHER);
    }
}
