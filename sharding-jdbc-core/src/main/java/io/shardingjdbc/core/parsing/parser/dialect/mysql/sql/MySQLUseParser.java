package io.shardingjdbc.core.parsing.parser.dialect.mysql.sql;

import io.shardingjdbc.core.parsing.parser.dialect.mysql.statement.UseStatement;
import io.shardingjdbc.core.parsing.parser.sql.dal.use.AbstractUseParser;

/**
 * Use parser for MySQL.
 *
 * @author zhangliang
 */
public final class MySQLUseParser extends AbstractUseParser {
    
    @Override
    public UseStatement parse() {
        return new UseStatement();
    }
}
