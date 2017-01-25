package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.dialect.mysql.parser.MySQLUpdateParser;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleUpdateParser;
import com.alibaba.druid.sql.dialect.postgresql.parser.PostgreSQLUpdateParser;
import com.alibaba.druid.sql.dialect.sqlserver.parser.SQLServerUpdateParser;
import com.alibaba.druid.util.JdbcConstants;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;

import java.util.List;

/**
 * Update语句解析器工厂.
 *
 * @author zhangliang
 */
public class SQLUpdateParserFactory {
    
    /**
     * 创建Update语句解析器.
     * 
     * @param shardingRule 分库分表规则配置
     * @param parameters 参数列表
     * @param exprParser 表达式
     * @param dbType 数据库类型
     * @return Update语句解析器
     */
    public static AbstractUpdateParser newInstance(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser, final String dbType) {
        switch (dbType) {
            case JdbcConstants.MYSQL :
            case JdbcConstants.MARIADB :
            case JdbcConstants.H2 :
                return new MySQLUpdateParser(shardingRule, parameters, exprParser);
            case JdbcConstants.POSTGRESQL :
                return new PostgreSQLUpdateParser(shardingRule, parameters, exprParser);
            case JdbcConstants.ORACLE :
                return new OracleUpdateParser(shardingRule, parameters, exprParser);
            case JdbcConstants.SQL_SERVER :
                return new SQLServerUpdateParser(shardingRule, parameters, exprParser);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database '%s'.", dbType));
        }
    } 
}
