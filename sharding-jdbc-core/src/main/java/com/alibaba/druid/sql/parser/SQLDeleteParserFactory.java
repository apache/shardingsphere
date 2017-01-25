package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.dialect.mysql.parser.MySQLDeleteParser;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleDeleteParser;
import com.alibaba.druid.sql.dialect.postgresql.parser.PostgreSQLDeleteParser;
import com.alibaba.druid.sql.dialect.sqlserver.parser.SQLServerDeleteParser;
import com.alibaba.druid.util.JdbcConstants;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;

import java.util.List;

/**
 * Delete语句解析器工厂.
 *
 * @author zhangliang
 */
public class SQLDeleteParserFactory {
    
    /**
     * 创建Delete语句解析器.
     * 
     * @param shardingRule 分库分表规则配置
     * @param parameters 参数列表
     * @param exprParser 表达式
     * @param dbType 数据库类型
     * @return Delete语句解析器
     */
    public static AbstractDeleteParser newInstance(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser, final String dbType) {
        switch (dbType) {
            case JdbcConstants.MYSQL :
            case JdbcConstants.MARIADB :
            case JdbcConstants.H2 :
                return new MySQLDeleteParser(shardingRule, parameters, exprParser);
            case JdbcConstants.POSTGRESQL :
                return new PostgreSQLDeleteParser(shardingRule, parameters, exprParser);
            case JdbcConstants.ORACLE :
                return new OracleDeleteParser(shardingRule, parameters, exprParser);
            case JdbcConstants.SQL_SERVER :
                return new SQLServerDeleteParser(shardingRule, parameters, exprParser);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database '%s'.", dbType));
        }
    } 
}
