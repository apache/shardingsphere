package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.dialect.mysql.parser.MySQLInsertParser;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleInsertParser;
import com.alibaba.druid.sql.dialect.postgresql.parser.PostgreSQLInsertParser;
import com.alibaba.druid.sql.dialect.sqlserver.parser.SQLServerInsertParser;
import com.alibaba.druid.util.JdbcConstants;

/**
 * Insert语句解析器工厂.
 *
 * @author zhangliang
 */
public class SQLInsertParserFactory {
    
    /**
     * 创建Insert语句解析器.
     * 
     * @param exprParser 表达式
     * @param dbType 数据库类型
     * @return Insert语句解析器
     */
    public static AbstractInsertParser newInstance(final SQLExprParser exprParser, final String dbType) {
        switch (dbType) {
            case JdbcConstants.MYSQL :
            case JdbcConstants.MARIADB :
            case JdbcConstants.H2 :
                return new MySQLInsertParser(exprParser);
            case JdbcConstants.POSTGRESQL :
                return new PostgreSQLInsertParser(exprParser);
            case JdbcConstants.ORACLE :
                return new OracleInsertParser(exprParser);
            case JdbcConstants.SQL_SERVER :
                return new SQLServerInsertParser(exprParser);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database '%s'.", dbType));
        }
    } 
}
