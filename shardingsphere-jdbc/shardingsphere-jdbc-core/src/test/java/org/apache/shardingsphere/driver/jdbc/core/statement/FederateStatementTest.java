/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.driver.jdbc.core.statement;

import org.apache.shardingsphere.driver.jdbc.base.AbstractShardingSphereDataSourceForFederateTest;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class FederateStatementTest extends AbstractShardingSphereDataSourceForFederateTest {

    private static final String SELECT_SQL_BY_ID_ACROSS_SINGLE_AND_SHARDING_TABLES =
            "select t_order_federate.*, t_order_item_federate_sharding.* from t_order_federate, "
                    + "t_order_item_federate_sharding where t_order_federate.order_id = "
                    + "t_order_item_federate_sharding.item_id";

    private static final String SELECT_SQL_BY_ID_ACROSS_SINGLE_AND_SHARDING_TABLES_ALIAS = "select o.*, i.* from"
            + " t_order_federate o, t_order_item_federate_sharding i where o.order_id = i.item_id";

    private static final String SELECT_SQL_BY_ID_ACROSS_SINGLE_AND_SHARDING_TABLES_REWRITE =
            "select t_order_federate.*, t_order_item_federate_sharding.* from "
                    + "t_order_federate, t_order_item_federate_sharding "
                    + "where t_order_federate.order_id = t_order_item_federate_sharding.item_id "
                    + "AND t_order_item_federate_sharding.remarks = 't_order_item_federate_sharding'";

    private static final String SELECT_SQL_BY_ID_ACROSS_SINGLE_AND_SHARDING_TABLES_ORDER_BY =
            "select t_order_federate.* from t_order_federate, t_order_item_federate_sharding "
                    + "where t_order_federate.order_id = t_order_item_federate_sharding.item_id "
                    + "ORDER BY t_order_item_federate_sharding.user_id";

    private static final String SELECT_SQL_BY_ID_ACROSS_SINGLE_TABLES_WITH_ENCRYPT =
            "select t_user_encrypt_federate.user_id, t_user_encrypt_federate.pwd, t_user_info.information from t_user_encrypt_federate, t_user_info "
            + "where t_user_encrypt_federate.user_id = t_user_info.user_id ";

    private static final String SELECT_SQL_BY_ID_ACROSS_SINGLE_AND_SHARDING_TABLES_WITH_ENCRYPT =
            "select t_user_encrypt_federate_sharding.user_id, t_user_encrypt_federate_sharding.pwd, t_user_info.information from t_user_encrypt_federate_sharding, t_user_info "
                    + "where t_user_encrypt_federate_sharding.user_id = t_user_info.user_id ";

    private static final String SELECT_SQL_BY_ID_ACROSS_TWO_SHARDING_TABLES =
            "select o.order_id_sharding, i.order_id from t_order_federate_sharding o, t_order_item_federate_sharding i "
                    + "where o.order_id_sharding = i.item_id";
    
    private static final String SELECT_HAVING_SQL_FOR_SHARDING_TABLE =
            "SELECT user_id, SUM(order_id_sharding) FROM t_order_federate_sharding GROUP BY user_id HAVING SUM(order_id_sharding) > 1000";
    
    private static final String SELECT_SUBQUEY_AGGREGATION_SQL_FOR_SHARDING_TABLE =
            "SELECT (SELECT MAX(user_id) FROM t_order_federate_sharding) max_user_id, order_id_sharding, status FROM t_order_federate_sharding WHERE order_id_sharding > 1100";
    
    private static final String SELECT_PARTIAL_DISTINCT_AGGREGATION_SQL_FOR_SHARDING_TABLE =
            "SELECT SUM(DISTINCT user_id), SUM(order_id_sharding) FROM t_order_federate_sharding WHERE order_id_sharding > 1000";
    
    @Test
    public void assertQueryWithFederateInSingleAndShardingTableByExecuteQuery() throws SQLException {
        assertQueryWithFederateInSingleAndShardingTable(true);
    }
    
    @Test
    public void assertQueryWithFederateInSingleAndShardingTableByExecute() throws SQLException {
        assertQueryWithFederateInSingleAndShardingTable(false);
    }
    
    private void assertQueryWithFederateInSingleAndShardingTable(final boolean executeQuery) throws SQLException {
        ShardingSphereStatement statement = (ShardingSphereStatement) getShardingSphereDataSource().getConnection().createStatement();
        ResultSet resultSet = getResultSet(statement, SELECT_SQL_BY_ID_ACROSS_SINGLE_AND_SHARDING_TABLES, executeQuery);
        assertNotNull(resultSet);
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(1000));
        assertThat(resultSet.getInt(2), is(10));
        assertThat(resultSet.getString(3), is("init"));
        assertThat(resultSet.getInt(4), is(1000));
        assertThat(resultSet.getInt(5), is(10000));
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(1001));
        assertThat(resultSet.getInt(2), is(11));
        assertThat(resultSet.getString(3), is("init"));
        assertThat(resultSet.getInt(4), is(1001));
        assertThat(resultSet.getInt(5), is(10001));
        assertFalse(resultSet.next());
    }
    
    @Test
    public void assertQueryWithFederateInSingleAndShardingTableWithAliasByExecuteQuery() throws SQLException {
        assertQueryWithFederateInSingleAndShardingTableWithAlias(true);
    }
    
    @Test
    public void assertQueryWithFederateInSingleAndShardingTableWithAliasByExecute() throws SQLException {
        assertQueryWithFederateInSingleAndShardingTableWithAlias(false);
    }
    
    private void assertQueryWithFederateInSingleAndShardingTableWithAlias(final boolean executeQuery) throws SQLException {
        ShardingSphereStatement statement = (ShardingSphereStatement) getShardingSphereDataSource().getConnection().createStatement();
        ResultSet resultSet = getResultSet(statement, SELECT_SQL_BY_ID_ACROSS_SINGLE_AND_SHARDING_TABLES_ALIAS, executeQuery);
        assertNotNull(resultSet);
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(1000));
        assertThat(resultSet.getInt(2), is(10));
        assertThat(resultSet.getString(3), is("init"));
        assertThat(resultSet.getInt(4), is(1000));
        assertThat(resultSet.getInt(5), is(10000));
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(1001));
        assertThat(resultSet.getInt(2), is(11));
        assertThat(resultSet.getString(3), is("init"));
        assertThat(resultSet.getInt(4), is(1001));
        assertThat(resultSet.getInt(5), is(10001));
        assertFalse(resultSet.next());
    }
    
    @Test
    public void assertQueryWithFederateInSingleAndShardingTableRewriteByExecuteQuery() throws SQLException {
        assertQueryWithFederateInSingleAndShardingTableRewrite(true);
    }
    
    @Test
    public void assertQueryWithFederateInSingleAndShardingTableRewriteByExecute() throws SQLException {
        assertQueryWithFederateInSingleAndShardingTableRewrite(false);
    }
    
    private void assertQueryWithFederateInSingleAndShardingTableRewrite(final boolean executeQuery) throws SQLException {
        ShardingSphereStatement statement = (ShardingSphereStatement) getShardingSphereDataSource().getConnection().createStatement();
        ResultSet resultSet = getResultSet(statement, SELECT_SQL_BY_ID_ACROSS_SINGLE_AND_SHARDING_TABLES_REWRITE, executeQuery);
        assertNotNull(resultSet);
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(1000));
        assertThat(resultSet.getInt(2), is(10));
        assertThat(resultSet.getString(3), is("init"));
        assertThat(resultSet.getInt(4), is(1000));
        assertThat(resultSet.getInt(5), is(10000));
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(1001));
        assertThat(resultSet.getInt(2), is(11));
        assertThat(resultSet.getString(3), is("init"));
        assertThat(resultSet.getInt(4), is(1001));
        assertThat(resultSet.getInt(5), is(10001));
        assertFalse(resultSet.next());
    }
    
    @Test
    public void assertQueryWithFederateInSingleAndShardingTableOrderByByExecuteQuery() throws SQLException {
        assertQueryWithFederateInSingleAndShardingTableOrderBy(true);   
    }
    
    @Test
    public void assertQueryWithFederateInSingleAndShardingTableOrderByByExecute() throws SQLException {
        assertQueryWithFederateInSingleAndShardingTableOrderBy(false);
    }
    
    private void assertQueryWithFederateInSingleAndShardingTableOrderBy(final boolean executeQuery) throws SQLException {
        ShardingSphereStatement statement = (ShardingSphereStatement) getShardingSphereDataSource().getConnection().createStatement();
        ResultSet resultSet = getResultSet(statement, SELECT_SQL_BY_ID_ACROSS_SINGLE_AND_SHARDING_TABLES_ORDER_BY, executeQuery);
        assertNotNull(resultSet);
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(1000));
        assertThat(resultSet.getInt(2), is(10));
        assertThat(resultSet.getString(3), is("init"));
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(1001));
        assertThat(resultSet.getInt(2), is(11));
        assertThat(resultSet.getString(3), is("init"));
        assertFalse(resultSet.next());
    }
    
    @Test
    public void assertQueryWithFederateInSingleTablesWithEncryptRuleByExecuteQuery() throws SQLException {
        assertQueryWithFederateInSingleTablesWithEncryptRule(true);
    }
    
    @Test
    public void assertQueryWithFederateInSingleTablesWithEncryptRuleByExecute() throws SQLException {
        assertQueryWithFederateInSingleTablesWithEncryptRule(false);
    }
    
    private void assertQueryWithFederateInSingleTablesWithEncryptRule(final boolean executeQuery) throws SQLException {
        ShardingSphereStatement statement = (ShardingSphereStatement) getShardingSphereDataSource().getConnection().createStatement();
        ResultSet resultSet = getResultSet(statement, SELECT_SQL_BY_ID_ACROSS_SINGLE_TABLES_WITH_ENCRYPT, executeQuery);
        assertNotNull(resultSet);
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(0));
        assertThat(resultSet.getString(2), is("decryptValue"));
        assertThat(resultSet.getString(3), is("description0"));
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(1));
        assertThat(resultSet.getString(2), is("decryptValue"));
        assertThat(resultSet.getString(3), is("description1"));
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(2));
        assertThat(resultSet.getString(2), is("decryptValue"));
        assertThat(resultSet.getString(3), is("description2"));
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(3));
        assertThat(resultSet.getString(2), is("decryptValue"));
        assertThat(resultSet.getString(3), is("description3"));
        assertFalse(resultSet.next());
    }
    
    @Test
    public void assertQueryWithFederateInSingleAndShardingTablesWithEncryptRuleByExecuteQuery() throws SQLException {
        assertQueryWithFederateInSingleAndShardingTablesWithEncryptRule(true);
    }
    
    @Test
    public void assertQueryWithFederateInSingleAndShardingTablesWithEncryptRuleByExecute() throws SQLException {
        assertQueryWithFederateInSingleAndShardingTablesWithEncryptRule(false);
    }
    
    private void assertQueryWithFederateInSingleAndShardingTablesWithEncryptRule(final boolean executeQuery) throws SQLException {
        ShardingSphereStatement statement = (ShardingSphereStatement) getShardingSphereDataSource().getConnection().createStatement();
        ResultSet resultSet = getResultSet(statement, SELECT_SQL_BY_ID_ACROSS_SINGLE_AND_SHARDING_TABLES_WITH_ENCRYPT, executeQuery);
        assertNotNull(resultSet);
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(0));
        assertThat(resultSet.getString(2), is("decryptValue"));
        assertThat(resultSet.getString(3), is("description0"));
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(1));
        assertThat(resultSet.getString(2), is("decryptValue"));
        assertThat(resultSet.getString(3), is("description1"));
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(2));
        assertThat(resultSet.getString(2), is("decryptValue"));
        assertThat(resultSet.getString(3), is("description2"));
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(3));
        assertThat(resultSet.getString(2), is("decryptValue"));
        assertThat(resultSet.getString(3), is("description3"));
        assertFalse(resultSet.next());
    }
    
    @Test
    public void assertQueryWithFederateBetweenTwoShardingTablesByExecuteQuery() throws SQLException {
        assertQueryWithFederateBetweenTwoShardingTables(true);   
    }
    
    @Test
    public void assertQueryWithFederateBetweenTwoShardingTablesByExecute() throws SQLException {
        assertQueryWithFederateBetweenTwoShardingTables(false);
    }
    
    private void assertQueryWithFederateBetweenTwoShardingTables(final boolean executeQuery) throws SQLException {
        ShardingSphereStatement statement = (ShardingSphereStatement) getShardingSphereDataSource().getConnection().createStatement();
        ResultSet resultSet = getResultSet(statement, SELECT_SQL_BY_ID_ACROSS_TWO_SHARDING_TABLES, executeQuery);
        assertNotNull(resultSet);
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(1010));
        assertThat(resultSet.getInt(2), is(10001));
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(1011));
        assertThat(resultSet.getInt(2), is(10001));
        assertFalse(resultSet.next());
    }
    
    @Test
    public void assertHavingForShardingTableWithFederateByExecuteQuery() throws SQLException {
        assertHavingForShardingTableWithFederate(true);
    }
    
    @Test
    public void assertHavingForShardingTableWithFederateByExecute() throws SQLException {
        assertHavingForShardingTableWithFederate(false);
    }
    
    private void assertHavingForShardingTableWithFederate(final boolean executeQuery) throws SQLException {
        Statement statement = getShardingSphereDataSource().getConnection().createStatement();
        ResultSet resultSet = getResultSet(statement, SELECT_HAVING_SQL_FOR_SHARDING_TABLE, executeQuery);
        assertNotNull(resultSet);
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(10));
        assertThat(resultSet.getInt(2), is(2110));
        assertNotNull(resultSet);
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(11));
        assertThat(resultSet.getInt(2), is(2112));
        assertFalse(resultSet.next());
    }
    
    @Test
    public void assertSubqueyAggregationForShardingTableWithFederateByExecuteQuery() throws SQLException {
        assertSubqueyAggregationForShardingTableWithFederate(true);
    }
    
    @Test
    public void assertSubqueyAggregationForShardingTableWithFederateByExecute() throws SQLException {
        assertSubqueyAggregationForShardingTableWithFederate(false);
    }
    
    private void assertSubqueyAggregationForShardingTableWithFederate(final boolean executeQuery) throws SQLException {
        Statement statement = getShardingSphereDataSource().getConnection().createStatement();
        ResultSet resultSet = getResultSet(statement, SELECT_SUBQUEY_AGGREGATION_SQL_FOR_SHARDING_TABLE, executeQuery);
        assertNotNull(resultSet);
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(11));
        assertThat(resultSet.getInt(2), is(1101));
        assertThat(resultSet.getString(3), is("init"));
        assertNotNull(resultSet);
    }
    
    @Test
    public void assertPartialDistinctAggregationForShardingTableWithFederateByExecuteQuery() throws SQLException {
        assertPartialDistinctAggregationForShardingTableWithFederate(true);
    }
    
    @Test
    public void assertPartialDistinctAggregationForShardingTableWithFederateByExecute() throws SQLException {
        assertPartialDistinctAggregationForShardingTableWithFederate(false);
    }
    
    private void assertPartialDistinctAggregationForShardingTableWithFederate(final boolean executeQuery) throws SQLException {
        Statement statement = getShardingSphereDataSource().getConnection().createStatement();
        ResultSet resultSet = getResultSet(statement, SELECT_PARTIAL_DISTINCT_AGGREGATION_SQL_FOR_SHARDING_TABLE, executeQuery);
        assertNotNull(resultSet);
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(21));
        assertThat(resultSet.getInt(2), is(4222));
        assertNotNull(resultSet);
    }
}
