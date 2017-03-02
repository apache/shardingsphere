package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.context.InsertSQLContext;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleStatementParser;
import com.alibaba.druid.util.JdbcConstants;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.NoneTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.id.generator.fixture.IncrementIdGenerator;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition;
import com.google.common.collect.Lists;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class InsertStatementParserTest extends AbstractStatementParserTest {
    
    @Test
    public void parseWithoutParameter() throws SQLException {
        MySqlStatementParser statementParser = new MySqlStatementParser(createShardingRule(), Collections.emptyList(), "INSERT INTO `TABLE_XXX` (`field1`, `field2`) VALUES (10, 1)");
        InsertSQLContext sqlContext = (InsertSQLContext) statementParser.parseStatement();
        assertInsertStatement(sqlContext);
        assertThat(sqlContext.toSqlBuilder().toString(), is("INSERT INTO [Token(TABLE_XXX)] (`field1`, `field2`) VALUES (10, 1)"));
    }
    
    @Test
    public void parseWithParameter() {
        MySqlStatementParser statementParser = new MySqlStatementParser(createShardingRule(), Arrays.<Object>asList(10, 1), "INSERT INTO TABLE_XXX (field1, field2) VALUES (?, ?)");
        InsertSQLContext sqlContext = (InsertSQLContext) statementParser.parseStatement();
        assertInsertStatement(sqlContext);
        assertThat(sqlContext.toSqlBuilder().toString(), is("INSERT INTO [Token(TABLE_XXX)] (field1, field2) VALUES (?, ?)"));
    }
    
    @Test
    public void parseWithAutoIncrementColumnsWithoutParameter() throws SQLException {
        MySqlStatementParser statementParser = new MySqlStatementParser(createShardingRuleWithAutoIncrementColumns(), Collections.emptyList(), "INSERT INTO `TABLE_XXX` (`field1`) VALUES (10)");
        InsertSQLContext sqlContext = (InsertSQLContext) statementParser.parseStatement();
        assertInsertStatement(sqlContext);
        assertThat(sqlContext.toSqlBuilder().toString(), is("INSERT INTO [Token(TABLE_XXX)] (`field1`, field2) VALUES (10, 1)"));
    }
    
    @Test
    public void parseWithAutoIncrementColumnsWithParameter() throws SQLException {
        MySqlStatementParser statementParser = new MySqlStatementParser(createShardingRuleWithAutoIncrementColumns(), Lists.<Object>newArrayList(10), "INSERT INTO `TABLE_XXX` (`field1`) VALUES (?)");
        InsertSQLContext sqlContext = (InsertSQLContext) statementParser.parseStatement();
        assertInsertStatement(sqlContext);
        assertThat(sqlContext.toSqlBuilder().toString(), is("INSERT INTO [Token(TABLE_XXX)] (`field1`, field2) VALUES (?, ?)"));
    }
    
    private void assertInsertStatement(final InsertSQLContext sqlContext) {
        assertThat(sqlContext.getTables().get(0).getName(), is("TABLE_XXX"));
        assertThat(sqlContext.getConditionContexts().size(), is(1));
        Iterator<Condition> conditions = sqlContext.getConditionContexts().iterator().next().getAllConditions().iterator();
        Condition condition1 = conditions.next();
        assertThat(condition1.getColumn().getColumnName(), is("field1"));
        assertThat(condition1.getColumn().getTableName(), is("TABLE_XXX"));
        assertThat(condition1.getOperator(), is(Condition.BinaryOperator.EQUAL));
        assertThat(condition1.getValues().size(), is(1));
        assertThat(condition1.getValues().get(0), is((Comparable) 10));
        Condition condition2 = conditions.next();
        assertThat(condition2.getColumn().getColumnName(), is("field2"));
        assertThat(condition2.getColumn().getTableName(), is("TABLE_XXX"));
        assertThat(condition2.getOperator(), is(Condition.BinaryOperator.EQUAL));
        assertThat(condition2.getValues().size(), is(1));
        assertThat(condition2.getValues().get(0), is((Comparable) 1));
        assertFalse(conditions.hasNext());
    }
    
    private ShardingRule createShardingRuleWithAutoIncrementColumns() {
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
                .tableShardingStrategy(new TableShardingStrategy(Arrays.asList("field1", "field2", "field3", "field4", "field5", "field6", "field7"), new NoneTableShardingAlgorithm()))
                .autoIncrementColumns("field1").autoIncrementColumns("field2").build();
        return ShardingRule.builder().dataSourceRule(dataSourceRule).tableRules(Collections.singletonList(tableRule)).idGenerator(IncrementIdGenerator.class).build();
    }
    
    @Test
    public void parseWithSpecialSyntax() {
        parseWithSpecialSyntax(JdbcConstants.MYSQL, "INSERT LOW_PRIORITY IGNORE INTO `TABLE_XXX` PARTITION (partition1,partition2) (`field1`) VALUE (1)", 
                "INSERT LOW_PRIORITY IGNORE INTO [Token(TABLE_XXX)] PARTITION (partition1,partition2) (`field1`) VALUE (1)");
        parseWithSpecialSyntax(JdbcConstants.MYSQL, "INSERT INTO TABLE_XXX SET field1=1", "INSERT INTO [Token(TABLE_XXX)] SET field1=1");
        // TODO
//         parseWithSpecialSyntax(JdbcConstants.MYSQL, "INSERT INTO TABLE_XXX (field1) SELECT field1 FROM TABLE_XXX2 ON DUPLICATE KEY UPDATE field1=field1+1", 
//                 "INSERT INTO [Token(TABLE_XXX)] (field1) SELECT field1 FROM TABLE_XXX2 ON DUPLICATE KEY UPDATE field1=field1+1");
        parseWithSpecialSyntax(JdbcConstants.ORACLE, "INSERT /*+ index(field1) */ INTO TABLE_XXX (`field1`) VALUES (1) RETURNING field1*2 LOG ERRORS INTO TABLE_LOG",
                "INSERT /*+ index(field1) */ INTO [Token(TABLE_XXX)] (`field1`) VALUES (1) RETURNING field1*2 LOG ERRORS INTO TABLE_LOG");
        /* // TODO 不支持
        parseWithSpecialSyntax(JdbcConstants.SQL_SERVER, 
                "WITH field_query (field1, field2) AS (SELECT field1, field2 FROM TABLE_XXX AS xxx GROUP BY field1) INSERT TOP(10) INTO OUTPUT TABLE_XXX (`field1`) VALUES (1)", 
                "WITH field_query (field1, field2) AS (SELECT field1, field2 FROM TABLE_XXX AS xxx GROUP BY field1) INSERT TOP(10) INTO OUTPUT [Token(TABLE_XXX)] (`field1`) VALUES (1)");
        */
        parseWithSpecialSyntax(JdbcConstants.POSTGRESQL, 
                "WITH RECURSIVE field_query (field1, field2) AS (SELECT field1, field2 FROM TABLE_XXX AS xxx ORDER BY field1 DESC) INSERT INTO TABLE_XXX (field1) VALUES (1) RETURNING id",
                "WITH RECURSIVE field_query (field1, field2) AS (SELECT field1, field2 FROM TABLE_XXX AS xxx ORDER BY field1 DESC) INSERT INTO [Token(TABLE_XXX)] (field1) VALUES (1) RETURNING id");
        parseWithSpecialSyntax(JdbcConstants.POSTGRESQL,
                "WITH field1_query AS (SELECT field1 FROM TABLE_XXX), field2_query AS (SELECT field2 FROM TABLE_XXX) INSERT INTO TABLE_XXX (field1) VALUES (1) RETURNING *",
                "WITH field1_query AS (SELECT field1 FROM TABLE_XXX), field2_query AS (SELECT field2 FROM TABLE_XXX) INSERT INTO [Token(TABLE_XXX)] (field1) VALUES (1) RETURNING *");
    }
    
    private void parseWithSpecialSyntax(final String dbType, final String actualSQL, final String expectedSQL) {
        InsertSQLContext sqlContext = (InsertSQLContext) getSqlStatementParser(dbType, actualSQL).parseStatement();
        assertThat(sqlContext.getTables().get(0).getName(), is("TABLE_XXX"));
        assertFalse(sqlContext.getTables().get(0).getAlias().isPresent());
        Iterator<Condition> conditions = sqlContext.getConditionContexts().iterator().next().getAllConditions().iterator();
        Condition condition = conditions.next();
        assertThat(condition.getColumn().getTableName(), is("TABLE_XXX"));
        assertThat(condition.getColumn().getColumnName(), is("field1"));
        assertThat(condition.getOperator(), is(Condition.BinaryOperator.EQUAL));
        assertThat(condition.getValues().size(), is(1));
        assertThat(condition.getValues().get(0), is((Comparable) 1));
        assertFalse(conditions.hasNext());
        assertThat(sqlContext.toSqlBuilder().toString(), is(expectedSQL));
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void parseMultipleInsertForMySQL() {
        new MySqlStatementParser(createShardingRule(), Collections.emptyList(), "INSERT INTO TABLE_XXX (`field1`, `field2`) VALUES (1, 'value_char'), (2, 'value_char')").parseStatement();
    }
    
    @Test(expected = ParserUnsupportedException.class)
    public void parseInsertAllForOracle() {
        new OracleStatementParser(createShardingRule(), Collections.emptyList(), "INSERT ALL INTO TABLE_XXX (field1) VALUES (field1) SELECT field1 FROM TABLE_XXX2").parseStatement();
    }
    
    @Test(expected = ParserUnsupportedException.class)
    public void parseInsertFirstForOracle() {
        new OracleStatementParser(createShardingRule(), Collections.emptyList(), "INSERT FIRST INTO TABLE_XXX (field1) VALUES (field1) SELECT field1 FROM TABLE_XXX2").parseStatement();
    }
}
