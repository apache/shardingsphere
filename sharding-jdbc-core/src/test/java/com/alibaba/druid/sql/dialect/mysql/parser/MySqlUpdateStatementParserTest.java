package com.alibaba.druid.sql.dialect.mysql.parser;

import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import com.alibaba.druid.sql.parser.ParserUnsupportedException;
import com.alibaba.druid.util.JdbcConstants;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.NoneTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class MySqlUpdateStatementParserTest {
    
    @Test
    public void parseStatementWithUpdateSingleTable() throws SQLException {
        MySqlStatementParser statementParser = new MySqlStatementParser(createShardingRule(), Collections.emptyList(), 
                "UPDATE LOW_PRIORITY IGNORE TABLE_XXX xxx SET field1=1 WHERE xxx.field1=1 ORDER BY field1 LIMIT 10");
        MySqlUpdateStatement updateStatement = (MySqlUpdateStatement) statementParser.parseStatement();
        assertThat(updateStatement.getDbType(), is(JdbcConstants.MYSQL));
        assertThat(updateStatement.getTableSource().toString(), is("TABLE_XXX"));
        assertThat(updateStatement.getTableSource().getAlias(), is("xxx"));
        assertThat(updateStatement.getItems().size(), is(1));
        assertThat(updateStatement.getItems().get(0).getColumn().toString(), is("field1"));
        assertThat(updateStatement.getItems().get(0).getValue().toString(), is("1"));
        assertThat(updateStatement.getOrderBy().getItems().size(), is(1));
        assertThat(updateStatement.getOrderBy().getItems().get(0).getExpr().toString(), is("field1"));
        assertThat(((SQLIntegerExpr) updateStatement.getLimit().getRowCount()).getNumber().intValue(), is(10));
        assertThat(updateStatement.getIdentifiersBetweenUpdateAndTable().size(), is(2));
        assertThat(updateStatement.getIdentifiersBetweenUpdateAndTable().get(0), is("LOW_PRIORITY"));
        assertThat(updateStatement.getIdentifiersBetweenUpdateAndTable().get(1), is("IGNORE"));
        // TODO use SQLBuilder
        // assertThat(updateStatement.toString(), is("UPDATE LOW_PRIORITY IGNORE TABLE_XXX xxx\nSET field1 = 1\nWHERE xxx.field1 = 1\nORDER BY field1\nLIMIT 10"));
    }
    
    @Test(expected = ParserUnsupportedException.class)
    public void parseStatementWithUpdateSingleTableForComplicatedWhere() {
        MySqlStatementParser statementParser = new MySqlStatementParser(null, Collections.emptyList(), "UPDATE TABLE_XXX xxx SET field1=1,field2=2 WHERE field1<1 AND (field1 >2 OR field2 =1)");
        statementParser.parseStatement();
    }
    
    private ShardingRule createShardingRule() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getDatabaseProductName()).thenReturn("H2");
        Map<String, DataSource> dataSourceMap = new HashMap<>(1);
        dataSourceMap.put("ds", dataSource);
        DataSourceRule dataSourceRule = new DataSourceRule(dataSourceMap);
        TableRule tableRule = TableRule.builder("TABLE_XXX").actualTables(Arrays.asList("table_0", "table_1", "table_2")).dataSourceRule(dataSourceRule)
                .tableShardingStrategy(new TableShardingStrategy("field1", new NoneTableShardingAlgorithm())).build();
        return ShardingRule.builder().dataSourceRule(dataSourceRule).tableRules(Collections.singletonList(tableRule)).build();
    }
}
