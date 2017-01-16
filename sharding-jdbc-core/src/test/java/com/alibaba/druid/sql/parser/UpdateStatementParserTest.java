package com.alibaba.druid.sql.parser;

import com.alibaba.druid.sql.ast.statement.AbstractSQLUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.dialect.oracle.parser.OracleStatementParser;
import com.alibaba.druid.sql.dialect.postgresql.parser.PGSQLStatementParser;
import com.alibaba.druid.sql.dialect.sqlserver.parser.SQLServerStatementParser;
import com.alibaba.druid.util.JdbcConstants;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.NoneTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition;
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

public final class UpdateStatementParserTest {
    
    @Test
    public void parseWithoutParameter() throws SQLException {
        MySqlStatementParser statementParser = new MySqlStatementParser(createShardingRule(), Collections.emptyList(), 
                "UPDATE TABLE_XXX xxx SET field1=2 WHERE field4<10 AND field1=1 AND field5>10 AND field2 IN (1, 3) AND field6<=10 AND field3 BETWEEN 5 AND 20 AND field7>=10");
        MySqlUpdateStatement updateStatement = (MySqlUpdateStatement) statementParser.parseStatement();
        assertThat(updateStatement.getDbType(), is(JdbcConstants.MYSQL));
        assertThat(updateStatement.getSqlContext().getTable().getName(), is("TABLE_XXX"));
        assertThat(updateStatement.getSqlContext().getTable().getAlias().get(), is("xxx"));
        Iterator<Condition> conditions = updateStatement.getSqlContext().getConditionContexts().iterator().next().getAllConditions().iterator();
        Condition condition = conditions.next();
        assertThat(condition.getColumn().getTableName(), is("TABLE_XXX"));
        assertThat(condition.getColumn().getColumnName(), is("field1"));
        assertThat(condition.getOperator(), is(Condition.BinaryOperator.EQUAL));
        assertThat(condition.getValues().size(), is(1));
        assertThat(condition.getValues().get(0), is((Comparable) 1));
        condition = conditions.next();
        assertThat(condition.getColumn().getTableName(), is("TABLE_XXX"));
        assertThat(condition.getColumn().getColumnName(), is("field2"));
        assertThat(condition.getOperator(), is(Condition.BinaryOperator.IN));
        assertThat(condition.getValues().size(), is(2));
        assertThat(condition.getValues().get(0), is((Comparable) 1));
        assertThat(condition.getValues().get(1), is((Comparable) 3));
        condition = conditions.next();
        assertThat(condition.getColumn().getTableName(), is("TABLE_XXX"));
        assertThat(condition.getColumn().getColumnName(), is("field3"));
        assertThat(condition.getOperator(), is(Condition.BinaryOperator.BETWEEN));
        assertThat(condition.getValues().size(), is(2));
        assertThat(condition.getValues().get(0), is((Comparable) 5));
        assertThat(condition.getValues().get(1), is((Comparable) 20));
        assertFalse(conditions.hasNext());
        assertThat(updateStatement.getSqlContext().getSqlBuilder().toString(), is(
                "UPDATE [Token(TABLE_XXX)] AS xxx SET field1=2 WHERE field4<10 AND field1=1 AND field5>10 AND field2 IN (1, 3) AND field6<=10 AND field3 BETWEEN 5 AND 20 AND field7>=10"));
    }
    
    @Test
    public void parseWithParameter() throws SQLException {
        MySqlStatementParser statementParser = new MySqlStatementParser(createShardingRule(), Arrays.<Object>asList(2, 10, 1, 10, 1, 3, 10, 5, 20, 10),
                "UPDATE TABLE_XXX AS xxx SET field1=? WHERE field4<? AND xxx.field1=? AND field5>? AND xxx.field2 IN (?, ?) AND field6<=? AND xxx.field3 BETWEEN ? AND ? AND field7>=?");
        MySqlUpdateStatement updateStatement = (MySqlUpdateStatement) statementParser.parseStatement();
        assertThat(updateStatement.getDbType(), is(JdbcConstants.MYSQL));
        assertThat(updateStatement.getSqlContext().getTable().getName(), is("TABLE_XXX"));
        assertThat(updateStatement.getSqlContext().getTable().getAlias().get(), is("xxx"));
        Iterator<Condition> conditions = updateStatement.getSqlContext().getConditionContexts().iterator().next().getAllConditions().iterator();
        Condition condition = conditions.next();
        assertThat(condition.getColumn().getTableName(), is("TABLE_XXX"));
        assertThat(condition.getColumn().getColumnName(), is("field1"));
        assertThat(condition.getOperator(), is(Condition.BinaryOperator.EQUAL));
        assertThat(condition.getValues().size(), is(1));
        assertThat(condition.getValues().get(0), is((Comparable) 1));
        condition = conditions.next();
        assertThat(condition.getColumn().getTableName(), is("TABLE_XXX"));
        assertThat(condition.getColumn().getColumnName(), is("field2"));
        assertThat(condition.getOperator(), is(Condition.BinaryOperator.IN));
        assertThat(condition.getValues().size(), is(2));
        assertThat(condition.getValues().get(0), is((Comparable) 1));
        assertThat(condition.getValues().get(1), is((Comparable) 3));
        condition = conditions.next();
        assertThat(condition.getColumn().getTableName(), is("TABLE_XXX"));
        assertThat(condition.getColumn().getColumnName(), is("field3"));
        assertThat(condition.getOperator(), is(Condition.BinaryOperator.BETWEEN));
        assertThat(condition.getValues().size(), is(2));
        assertThat(condition.getValues().get(0), is((Comparable) 5));
        assertThat(condition.getValues().get(1), is((Comparable) 20));
        assertFalse(conditions.hasNext());
        assertThat(updateStatement.getSqlContext().getSqlBuilder().toString(), is(
                "UPDATE [Token(TABLE_XXX)] AS xxx SET field1=? WHERE field4<? AND xxx.field1=? AND field5>? AND xxx.field2 IN (?, ?) AND field6<=? AND xxx.field3 BETWEEN ? AND ? AND field7>=?"));
    }
    
    @Test(expected = ParserUnsupportedException.class)
    public void parseWithOr() throws SQLException {
        new MySqlStatementParser(createShardingRule(), Collections.emptyList(), "UPDATE TABLE_XXX SET field1=1 WHERE field1<1 AND (field1 >2 OR field2 =1)").parseStatement();
    }
    
    @Test
    public void parseWithSpecialSyntax() throws SQLException {
        parseWithSpecialSyntax(JdbcConstants.MYSQL, "UPDATE LOW_PRIORITY IGNORE TABLE_XXX SET field1=1 WHERE field1=1 ORDER BY field1 LIMIT 10",
                "UPDATE LOW_PRIORITY IGNORE [Token(TABLE_XXX)] SET field1=1 WHERE field1=1 ORDER BY field1 LIMIT 10");
        parseWithSpecialSyntax(JdbcConstants.ORACLE, "UPDATE /*+ index(field1) */ ONLY TABLE_XXX SET field1=1 WHERE field1=1 RETURN * LOG ERRORS INTO TABLE_LOG", 
                "UPDATE /*+ index(field1) */ ONLY [Token(TABLE_XXX)] SET field1=1 WHERE field1=1 RETURN * LOG ERRORS INTO TABLE_LOG");
        parseWithSpecialSyntax(JdbcConstants.ORACLE, "UPDATE /*+ index(field1) */ ONLY TABLE_XXX SET field1=1 WHERE field1=1 RETURNING *",
                "UPDATE /*+ index(field1) */ ONLY [Token(TABLE_XXX)] SET field1=1 WHERE field1=1 RETURNING *");
        parseWithSpecialSyntax(JdbcConstants.ORACLE, "UPDATE /*+ index(field1) */ ONLY TABLE_XXX SET field1=1 WHERE field1=1 LOG ERRORS INTO TABLE_LOG",
                "UPDATE /*+ index(field1) */ ONLY [Token(TABLE_XXX)] SET field1=1 WHERE field1=1 LOG ERRORS INTO TABLE_LOG");
        parseWithSpecialSyntax(JdbcConstants.SQL_SERVER, "UPDATE TOP(10) TABLE_XXX SET field1=1 OUTPUT (inserted.field1) WHERE field1=1",
                "UPDATE TOP(10) [Token(TABLE_XXX)] SET field1=1 OUTPUT (inserted.field1) WHERE field1=1");
        parseWithSpecialSyntax(JdbcConstants.POSTGRESQL, "UPDATE ONLY TABLE_XXX SET field1=1 WHERE field1=1 RETURNING *",
                "UPDATE ONLY [Token(TABLE_XXX)] SET field1=1 WHERE field1=1 RETURNING *");
    }
    
    private void parseWithSpecialSyntax(final String dbType, final String actualSQL, final String expectedSQL) throws SQLException {
        SQLStatementParser statementParser = null;
        if (dbType.equalsIgnoreCase(JdbcConstants.MYSQL)) {
            statementParser = new MySqlStatementParser(createShardingRule(), Collections.emptyList(), actualSQL);
        } else if (dbType.equalsIgnoreCase(JdbcConstants.ORACLE)) {
            statementParser = new OracleStatementParser(createShardingRule(), Collections.emptyList(), actualSQL);
        } else if (dbType.equalsIgnoreCase(JdbcConstants.SQL_SERVER)) {
            statementParser = new SQLServerStatementParser(createShardingRule(), Collections.emptyList(), actualSQL);
        } else if (dbType.equalsIgnoreCase(JdbcConstants.POSTGRESQL)) {
            statementParser = new PGSQLStatementParser(createShardingRule(), Collections.emptyList(), actualSQL);
        }
        AbstractSQLUpdateStatement updateStatement = (AbstractSQLUpdateStatement) statementParser.parseStatement();
        assertThat(updateStatement.getDbType(), is(dbType));
        assertThat(updateStatement.getSqlContext().getTable().getName(), is("TABLE_XXX"));
        assertFalse(updateStatement.getSqlContext().getTable().getAlias().isPresent());
        Iterator<Condition> conditions = updateStatement.getSqlContext().getConditionContexts().iterator().next().getAllConditions().iterator();
        Condition condition = conditions.next();
        assertThat(condition.getColumn().getTableName(), is("TABLE_XXX"));
        assertThat(condition.getColumn().getColumnName(), is("field1"));
        assertThat(condition.getOperator(), is(Condition.BinaryOperator.EQUAL));
        assertThat(condition.getValues().size(), is(1));
        assertThat(condition.getValues().get(0), is((Comparable) 1));
        assertFalse(conditions.hasNext());
        assertThat(updateStatement.getSqlContext().getSqlBuilder().toString(), is(expectedSQL));
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
                .tableShardingStrategy(new TableShardingStrategy(Arrays.asList("field1", "field2", "field3", "field4", "field5", "field6", "field7"), new NoneTableShardingAlgorithm())).build();
        return ShardingRule.builder().dataSourceRule(dataSourceRule).tableRules(Collections.singletonList(tableRule)).build();
    }
}
