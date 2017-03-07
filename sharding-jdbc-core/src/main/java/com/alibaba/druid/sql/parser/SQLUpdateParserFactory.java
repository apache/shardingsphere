package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.dialect.mysql.parser.MySQLUpdateParser;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleUpdateParser;
import com.alibaba.druid.sql.dialect.postgresql.parser.PostgreSQLUpdateParser;
import com.alibaba.druid.sql.dialect.sqlserver.parser.SQLServerUpdateParser;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;

/**
 * Update语句解析器工厂.
 *
 * @author zhangliang
 */
public class SQLUpdateParserFactory {
    
    /**
     * 创建Update语句解析器.
     * 
     * @param exprParser 表达式
     * @param dbType 数据库类型
     * @return Update语句解析器
     */
    public static AbstractUpdateParser newInstance(final SQLExprParser exprParser, final DatabaseType dbType) {
        switch (dbType) {
            case H2 :
            case MySQL :
                return new MySQLUpdateParser(exprParser);
            case Oracle:
                return new OracleUpdateParser(exprParser);
            case SQLServer:
                return new SQLServerUpdateParser(exprParser);
            case PostgreSQL:
                return new PostgreSQLUpdateParser(exprParser);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database '%s'.", dbType));
        }
    } 
}
