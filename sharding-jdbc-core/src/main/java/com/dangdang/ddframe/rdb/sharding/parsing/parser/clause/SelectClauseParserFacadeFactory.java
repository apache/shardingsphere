package com.dangdang.ddframe.rdb.sharding.parsing.parser.clause;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import com.dangdang.ddframe.rdb.sharding.parsing.lexer.LexerEngine;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.MySQLDistinctClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.MySQLGroupByClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.MySQLOrderByClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.MySQLSelectRestClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.mysql.MySQLTableClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.OracleDistinctClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.OracleGroupByClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.OracleOrderByClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.OracleSelectListClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.OracleTableClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.oracle.OracleWhereClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql.PostgreSQLOrderByClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.postgresql.PostgreSQLSelectRestClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.SQLServerOrderByClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.SQLServerSelectListClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.SQLServerTableClauseParser;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.dialect.sqlserver.SQLServerWhereClauseParser;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * SELECT从句解析器门面工厂.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SelectClauseParserFacadeFactory {
    
    /**
     * 创建SELECT从句解析器门面对象.
     * 
     * @param dbType 数据库类型
     * @param shardingRule 分库分表规则配置
     * @param lexerEngine 词法解析器引擎
     * @return SELECT从句解析器门面对象
     */
    public static SelectClauseParserFacade newInstance(final DatabaseType dbType, final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        switch (dbType) {
            case H2:
            case MySQL:
                return newInstanceForMySQL(shardingRule, lexerEngine);
            case Oracle:
                return newInstanceForOracle(shardingRule, lexerEngine);
            case SQLServer:
                return newInstanceForSQLServer(shardingRule, lexerEngine);
            case PostgreSQL:
                return newInstanceForPostgreSQL(shardingRule, lexerEngine);
            default:
                throw new UnsupportedOperationException(String.format("Cannot support database [%s].", dbType));
        }
    }
    
    private static SelectClauseParserFacade newInstanceForMySQL(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        return new SelectClauseParserFacade(new MySQLDistinctClauseParser(lexerEngine), new SelectListClauseParser(shardingRule, lexerEngine), 
                new MySQLTableClauseParser(shardingRule, lexerEngine), new WhereClauseParser(lexerEngine), new MySQLGroupByClauseParser(lexerEngine), 
                new HavingClauseParser(lexerEngine), new MySQLOrderByClauseParser(lexerEngine), new MySQLSelectRestClauseParser(lexerEngine));
    }
    
    private static SelectClauseParserFacade newInstanceForOracle(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        return new SelectClauseParserFacade(new OracleDistinctClauseParser(lexerEngine), new OracleSelectListClauseParser(shardingRule, lexerEngine),
                new OracleTableClauseParser(shardingRule, lexerEngine), new OracleWhereClauseParser(lexerEngine), new OracleGroupByClauseParser(lexerEngine),
                new HavingClauseParser(lexerEngine), new OracleOrderByClauseParser(lexerEngine), new SelectRestClauseParser(lexerEngine));
    }
    
    private static SelectClauseParserFacade newInstanceForSQLServer(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        return new SelectClauseParserFacade(new DistinctClauseParser(lexerEngine), new SQLServerSelectListClauseParser(shardingRule, lexerEngine),
                new SQLServerTableClauseParser(shardingRule, lexerEngine), new SQLServerWhereClauseParser(lexerEngine), new GroupByClauseParser(lexerEngine),
                new HavingClauseParser(lexerEngine), new SQLServerOrderByClauseParser(lexerEngine), new SelectRestClauseParser(lexerEngine));
    }
    
    private static SelectClauseParserFacade newInstanceForPostgreSQL(final ShardingRule shardingRule, final LexerEngine lexerEngine) {
        return new SelectClauseParserFacade(new DistinctClauseParser(lexerEngine), new SelectListClauseParser(shardingRule, lexerEngine), 
                new TableClauseParser(shardingRule, lexerEngine), new WhereClauseParser(lexerEngine), 
                new GroupByClauseParser(lexerEngine), new HavingClauseParser(lexerEngine), new PostgreSQLOrderByClauseParser(lexerEngine), new PostgreSQLSelectRestClauseParser(lexerEngine));
    }
}
