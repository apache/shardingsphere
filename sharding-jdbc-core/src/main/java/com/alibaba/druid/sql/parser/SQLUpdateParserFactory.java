package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.dialect.mysql.parser.MySQLUpdateParser;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleUpdateParser;
import com.alibaba.druid.sql.dialect.postgresql.parser.PostgreSQLUpdateParser;
import com.alibaba.druid.sql.dialect.sqlserver.parser.SQLServerUpdateParser;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;

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
    public static AbstractUpdateParser newInstance(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser, final DatabaseType dbType) {
        switch (dbType) {
            case H2 :
            case MySQL :
                return new MySQLUpdateParser(shardingRule, parameters, exprParser);
            case Oracle:
                return new OracleUpdateParser(shardingRule, parameters, exprParser);
            case SQLServer:
                return new SQLServerUpdateParser(shardingRule, parameters, exprParser);
            case PostgreSQL:
                return new PostgreSQLUpdateParser(shardingRule, parameters, exprParser);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database '%s'.", dbType));
        }
    } 
}
