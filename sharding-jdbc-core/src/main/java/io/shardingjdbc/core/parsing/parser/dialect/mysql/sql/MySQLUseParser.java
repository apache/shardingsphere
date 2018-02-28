package io.shardingjdbc.core.parsing.parser.dialect.mysql.sql;

import io.shardingjdbc.core.parsing.parser.dialect.mysql.statement.UseStatement;
import io.shardingjdbc.core.parsing.parser.sql.SQLParser;
import io.shardingjdbc.core.parsing.parser.sql.dal.DALStatement;

/**
 * Use parser for MySQL.
 *
 * @author zhangliang
 */
public final class MySQLUseParser implements SQLParser {
    
    @Override
    public DALStatement parse() {
        return new UseStatement();
    }
}
