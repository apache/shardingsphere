package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.dialect.mysql.parser.MySQLInsertParser;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleInsertParser;
import com.alibaba.druid.sql.dialect.postgresql.parser.PostgreSQLInsertParser;
import com.alibaba.druid.sql.dialect.sqlserver.parser.SQLServerInsertParser;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;

import java.util.List;

/**
 * Insert语句解析器工厂.
 *
 * @author zhangliang
 */
public class SQLInsertParserFactory {
    
    /**
     * 创建Insert语句解析器.
     * 
     * @param shardingRule 分库分表规则配置
     * @param parameters 参数列表
     * @param exprParser 表达式
     * @param dbType 数据库类型
     * @return Insert语句解析器
     */
    public static AbstractInsertParser newInstance(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser, final DatabaseType dbType) {
        switch (dbType) {
            case H2 :
            case MySQL :
                return new MySQLInsertParser(shardingRule, parameters, exprParser);
            case Oracle:
                return new OracleInsertParser(shardingRule, parameters, exprParser);
            case SQLServer:
                return new SQLServerInsertParser(shardingRule, parameters, exprParser);
            case PostgreSQL:
                return new PostgreSQLInsertParser(shardingRule, parameters, exprParser);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database '%s'.", dbType));
        }
    } 
}
