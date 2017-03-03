package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.dialect.mysql.parser.MySQLDeleteParser;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleDeleteParser;
import com.alibaba.druid.sql.dialect.postgresql.parser.PostgreSQLDeleteParser;
import com.alibaba.druid.sql.dialect.sqlserver.parser.SQLServerDeleteParser;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;

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
    public static AbstractDeleteParser newInstance(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser, final DatabaseType dbType) {
        switch (dbType) {
            case H2 :
            case MySQL :
                return new MySQLDeleteParser(shardingRule, parameters, exprParser);
            case Oracle:
                return new OracleDeleteParser(shardingRule, parameters, exprParser);
            case SQLServer:
                return new SQLServerDeleteParser(shardingRule, parameters, exprParser);
            case PostgreSQL:
                return new PostgreSQLDeleteParser(shardingRule, parameters, exprParser);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database '%s'.", dbType));
        }
    } 
}
