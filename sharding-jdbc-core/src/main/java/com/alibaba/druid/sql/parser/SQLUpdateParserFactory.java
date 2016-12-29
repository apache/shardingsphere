package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.dialect.mysql.parser.MySQLUpdateParser;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleUpdateParser;
import com.alibaba.druid.sql.dialect.postgresql.parser.PostgreSQLUpdateParser;
import com.alibaba.druid.sql.dialect.sqlserver.parser.SQLServerUpdateParser;
import com.alibaba.druid.util.JdbcConstants;

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
    public static AbstractUpdateParser newInstance(final SQLExprParser exprParser, final String dbType) {
        switch (dbType) {
            case JdbcConstants.MYSQL :
            case JdbcConstants.MARIADB :
            case JdbcConstants.H2 :
                return new MySQLUpdateParser(exprParser);
            case JdbcConstants.POSTGRESQL :
                return new PostgreSQLUpdateParser(exprParser);
            case JdbcConstants.ORACLE :
                return new OracleUpdateParser(exprParser);
            case JdbcConstants.SQL_SERVER :
                return new SQLServerUpdateParser(exprParser);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database '%s'.", dbType));
        }
    } 
}
