package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.dialect.mysql.parser.MySqlSelectParser;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleSelectParser;
import com.alibaba.druid.sql.dialect.postgresql.parser.PGSelectParser;
import com.alibaba.druid.sql.dialect.sqlserver.parser.SQLServerSelectParser;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;

import java.util.List;

/**
 * Select语句解析器工厂.
 *
 * @author zhangliang
 */
public class SQLSelectParserFactory {
    
    /**
     * 创建Select语句解析器.
     * 
     * @param shardingRule 分库分表规则配置
     * @param parameters 参数列表
     * @param exprParser 表达式
     * @param dbType 数据库类型
     * @return Select语句解析器
     */
    public static AbstractSelectParser newInstance(final ShardingRule shardingRule, final List<Object> parameters, final SQLExprParser exprParser, final DatabaseType dbType) {
        switch (dbType) {
            case H2 :
            case MySQL :
                return new MySqlSelectParser(shardingRule, parameters, exprParser);
            case Oracle:
                return new OracleSelectParser(shardingRule, parameters, exprParser);
            case SQLServer :
                return new SQLServerSelectParser(shardingRule, parameters, exprParser);
            case PostgreSQL :
                return new PGSelectParser(shardingRule, parameters, exprParser);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database '%s'.", dbType));
        }
    } 
}
