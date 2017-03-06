package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.dialect.mysql.parser.MySQLDeleteParser;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleDeleteParser;
import com.alibaba.druid.sql.dialect.postgresql.parser.PostgreSQLDeleteParser;
import com.alibaba.druid.sql.dialect.sqlserver.parser.SQLServerDeleteParser;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;

/**
 * Delete语句解析器工厂.
 *
 * @author zhangliang
 */
public class SQLDeleteParserFactory {
    
    /**
     * 创建Delete语句解析器.
     * 
     * @param exprParser 表达式
     * @param dbType 数据库类型
     * @return Delete语句解析器
     */
    public static AbstractDeleteParser newInstance(final SQLExprParser exprParser, final DatabaseType dbType) {
        switch (dbType) {
            case H2 :
            case MySQL :
                return new MySQLDeleteParser(exprParser);
            case Oracle:
                return new OracleDeleteParser(exprParser);
            case SQLServer:
                return new SQLServerDeleteParser(exprParser);
            case PostgreSQL:
                return new PostgreSQLDeleteParser(exprParser);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database '%s'.", dbType));
        }
    } 
}
