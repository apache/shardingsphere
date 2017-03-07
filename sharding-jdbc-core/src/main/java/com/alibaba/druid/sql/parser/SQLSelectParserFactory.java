package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.dialect.mysql.parser.MySqlSelectParser;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleSelectParser;
import com.alibaba.druid.sql.dialect.postgresql.parser.PGSelectParser;
import com.alibaba.druid.sql.dialect.sqlserver.parser.SQLServerSelectParser;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;

/**
 * Select语句解析器工厂.
 *
 * @author zhangliang
 */
public class SQLSelectParserFactory {
    
    /**
     * 创建Select语句解析器.
     * 
     * @param exprParser 表达式
     * @param dbType 数据库类型
     * @return Select语句解析器
     */
    public static AbstractSelectParser newInstance(final SQLExprParser exprParser, final DatabaseType dbType) {
        switch (dbType) {
            case H2 :
            case MySQL :
                return new MySqlSelectParser(exprParser);
            case Oracle:
                return new OracleSelectParser(exprParser);
            case SQLServer :
                return new SQLServerSelectParser(exprParser);
            case PostgreSQL :
                return new PGSelectParser(exprParser);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database '%s'.", dbType));
        }
    } 
}
