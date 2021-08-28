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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class FederatePrepareStatementTest extends AbstractShardingSphereDataSourceForFederateTest {

    private static final String SELECT_SQL_BY_ID_ACROSS_SINGLE_AND_SHARDING_TABLES_ALIAS =
            "select o.*, i.* from t_order_federate o, t_order_item_federate_sharding i "
                    + "where o.order_id = i.item_id AND i.order_id = ?";

    private static final String SELECT_SQL_BY_ID_ACROSS_SINGLE_AND_SHARDING_TABLES_REWRITE =
            "select t_order_federate.*, t_order_item_federate_sharding.* "
                    + "from t_order_federate, t_order_item_federate_sharding "
                    + "where t_order_federate.order_id = t_order_item_federate_sharding.item_id "
                    + "AND t_order_item_federate_sharding.remarks = 't_order_item_federate_sharding' "
                    + "AND t_order_item_federate_sharding.user_id = ?";

    private static final String SELECT_SQL_BY_ID_ACROSS_SINGLE_TABLES_WITH_ENCRYPT =
            "select t_user_encrypt_federate.user_id, t_user_encrypt_federate.pwd, t_user_info.information from t_user_encrypt_federate, t_user_info "
                    + "where t_user_encrypt_federate.user_id = t_user_info.user_id and t_user_encrypt_federate.user_id > ? ";

    @Test
    public void assertQueryWithFederateInSingleAndShardingTableWithAliasByExecuteQuery() throws SQLException {
        assertQueryWithFederateInSingleAndShardingTableWithAlias(true);
    }
    
    @Test
    public void assertQueryWithFederateInSingleAndShardingTableWithAliasByExecute() throws SQLException {
        assertQueryWithFederateInSingleAndShardingTableWithAlias(false);
    }
    
    public void assertQueryWithFederateInSingleAndShardingTableWithAlias(final boolean executeQuery) throws SQLException {
        ShardingSpherePreparedStatement preparedStatement = (ShardingSpherePreparedStatement) getShardingSphereDataSource()
                .getConnection().prepareStatement(SELECT_SQL_BY_ID_ACROSS_SINGLE_AND_SHARDING_TABLES_ALIAS);
        preparedStatement.setInt(1, 10001);
        ResultSet resultSet = getResultSet(preparedStatement, executeQuery);
        assertNotNull(resultSet);
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
        ShardingSpherePreparedStatement preparedStatement = (ShardingSpherePreparedStatement) getShardingSphereDataSource()
                .getConnection().prepareStatement(SELECT_SQL_BY_ID_ACROSS_SINGLE_AND_SHARDING_TABLES_REWRITE);
        preparedStatement.setInt(1, 11);
        ResultSet resultSet = getResultSet(preparedStatement, executeQuery);
        assertNotNull(resultSet);
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(1001));
        assertThat(resultSet.getInt(2), is(11));
        assertThat(resultSet.getString(3), is("init"));
        assertThat(resultSet.getInt(4), is(1001));
        assertThat(resultSet.getInt(5), is(10001));
        assertFalse(resultSet.next());
        preparedStatement.setInt(1, 10);
        ResultSet resultSet1 = getResultSet(preparedStatement, executeQuery);
        assertNotNull(resultSet1);
        assertTrue(resultSet1.next());
        assertThat(resultSet1.getInt(1), is(1000));
        assertThat(resultSet1.getInt(2), is(10));
        assertThat(resultSet1.getString(3), is("init"));
        assertThat(resultSet1.getInt(4), is(1000));
        assertThat(resultSet1.getInt(5), is(10000));
        assertFalse(resultSet1.next());
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
        ShardingSpherePreparedStatement preparedStatement = (ShardingSpherePreparedStatement) getShardingSphereDataSource()
                .getConnection().prepareStatement(SELECT_SQL_BY_ID_ACROSS_SINGLE_TABLES_WITH_ENCRYPT);
        preparedStatement.setInt(1, 1);
        ResultSet resultSet = getResultSet(preparedStatement, executeQuery);
        assertNotNull(resultSet);
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

}
