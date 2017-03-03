package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.dialect.mysql.parser.MySqlExprParser;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleExprParser;
import com.alibaba.druid.sql.dialect.postgresql.parser.PGExprParser;
import com.alibaba.druid.sql.dialect.sqlserver.parser.SQLServerExprParser;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.NoneTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.constants.DatabaseType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractStatementParserTest {
    
    protected final SQLStatementParser getSqlStatementParser(final DatabaseType dbType, final String actualSQL) {
        ShardingRule shardingRule = createShardingRule();
        List<Object> parameters = Collections.emptyList();
        SQLExprParser sqlExprParser;
        switch (dbType) {
            case H2:
            case MySQL:
                sqlExprParser = new MySqlExprParser(shardingRule, parameters, actualSQL);
                break;
            case Oracle:
                sqlExprParser = new OracleExprParser(shardingRule, parameters, actualSQL);
                break;
            case SQLServer:
                sqlExprParser = new SQLServerExprParser(shardingRule, parameters, actualSQL);
                break;
            case PostgreSQL:
                sqlExprParser = new PGExprParser(shardingRule, parameters, actualSQL);
                break;
            default:
                throw new UnsupportedOperationException(dbType.name());
        }
        return new SQLStatementParser(dbType, shardingRule, parameters, sqlExprParser);
    }
    
    protected final ShardingRule createShardingRule() {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        try {
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.getMetaData()).thenReturn(databaseMetaData);
            when(databaseMetaData.getDatabaseProductName()).thenReturn("H2");
        } catch (final SQLException ex) {
            throw new RuntimeException(ex);
        }
        Map<String, DataSource> dataSourceMap = new HashMap<>(1);
        dataSourceMap.put("ds", dataSource);
        DataSourceRule dataSourceRule = new DataSourceRule(dataSourceMap);
        TableRule tableRule = TableRule.builder("TABLE_XXX").actualTables(Arrays.asList("table_0", "table_1", "table_2")).dataSourceRule(dataSourceRule)
                .tableShardingStrategy(new TableShardingStrategy(Arrays.asList("field1", "field2", "field3", "field4", "field5", "field6", "field7"), new NoneTableShardingAlgorithm())).build();
        return ShardingRule.builder().dataSourceRule(dataSourceRule).tableRules(Collections.singletonList(tableRule)).build();
    }
}
