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

import org.apache.shardingsphere.driver.jdbc.base.AbstractShardingSphereDataSourceForCalciteTest;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class CalcitePrepareStatementTest extends AbstractShardingSphereDataSourceForCalciteTest {

    private static final String SELECT_SQL_BY_ID_ACROSS_SINGLE_TABLES = "select o.*, i.* from t_order_calcite o, t_order_item_calcite i where o.order_id = ? and i.item_id = ?";

    private static final String SELECT_SQL_BY_ID_ACROSS_SINGLE_AND_SHARDING_TABLES =
            "select t_order_calcite.*, t_order_item_calcite_sharding.* "
                    + "from t_order_calcite, t_order_item_calcite_sharding "
                    + "where t_order_calcite.order_id = t_order_item_calcite_sharding.item_id "
                    + "AND t_order_item_calcite_sharding.order_id = ?";

    private static final String SELECT_SQL_BY_ID_ACROSS_SINGLE_AND_SHARDING_TABLES_ALIAS =
            "select o.*, i.* from t_order_calcite o, t_order_item_calcite_sharding i "
                    + "where o.order_id = i.item_id AND i.order_id = ?";

    private static final String SELECT_SQL_BY_ID_ACROSS_SINGLE_AND_SHARDING_TABLES_REWRITE =
            "select t_order_calcite.*, t_order_item_calcite_sharding.* "
                    + "from t_order_calcite, t_order_item_calcite_sharding "
                    + "where t_order_calcite.order_id = t_order_item_calcite_sharding.item_id "
                    + "AND t_order_item_calcite_sharding.remarks = 't_order_item_calcite_sharding' "
                    + "AND t_order_item_calcite_sharding.user_id = ?";

    @Test
    public void assertQueryWithCalciteInSingleTables() throws SQLException {
        ShardingSpherePreparedStatement preparedStatement = (ShardingSpherePreparedStatement) getShardingSphereDataSource().getConnection().prepareStatement(SELECT_SQL_BY_ID_ACROSS_SINGLE_TABLES);
        preparedStatement.setInt(1, 1000);
        preparedStatement.setInt(2, 100000);
        ResultSet resultSet = preparedStatement.executeQuery();
        assertNotNull(resultSet);
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(1000));
        assertThat(resultSet.getInt(2), is(10));
        assertThat(resultSet.getString(3), is("init"));
        assertThat(resultSet.getInt(4), is(100000));
        assertThat(resultSet.getInt(5), is(1000));
        assertThat(resultSet.getInt(6), is(10));
        assertThat(resultSet.getString(7), is("init"));
        assertFalse(resultSet.next());
    }

    @Test
    public void assertQueryWithCalciteInSingleAndShardingTable() throws SQLException {
        ShardingSpherePreparedStatement preparedStatement = (ShardingSpherePreparedStatement) getShardingSphereDataSource()
                .getConnection().prepareStatement(SELECT_SQL_BY_ID_ACROSS_SINGLE_AND_SHARDING_TABLES);
        preparedStatement.setInt(1, 10001);
        ResultSet resultSet = preparedStatement.executeQuery();
        assertNotNull(resultSet);
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(1001));
        assertThat(resultSet.getInt(2), is(11));
        assertThat(resultSet.getString(3), is("init"));
        assertThat(resultSet.getInt(4), is(1001));
        assertThat(resultSet.getInt(5), is(10001));
        assertThat(resultSet.getInt(6), is(11));
        assertThat(resultSet.getString(7), is("init"));
        assertFalse(resultSet.next());
    }

    @Test
    public void assertQueryWithCalciteInSingleAndShardingTableWithAlias() throws SQLException {
        ShardingSpherePreparedStatement preparedStatement = (ShardingSpherePreparedStatement) getShardingSphereDataSource()
                .getConnection().prepareStatement(SELECT_SQL_BY_ID_ACROSS_SINGLE_AND_SHARDING_TABLES_ALIAS);
        preparedStatement.setInt(1, 10001);
        ResultSet resultSet = preparedStatement.executeQuery();
        assertNotNull(resultSet);
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(1001));
        assertThat(resultSet.getInt(2), is(11));
        assertThat(resultSet.getString(3), is("init"));
        assertThat(resultSet.getInt(4), is(1001));
        assertThat(resultSet.getInt(5), is(10001));
        assertThat(resultSet.getInt(6), is(11));
        assertThat(resultSet.getString(7), is("init"));
        assertFalse(resultSet.next());
    }

    @Test
    public void assertQueryWithCalciteInSingleAndShardingTableRewrite() throws SQLException {
        ShardingSpherePreparedStatement preparedStatement = (ShardingSpherePreparedStatement) getShardingSphereDataSource()
                .getConnection().prepareStatement(SELECT_SQL_BY_ID_ACROSS_SINGLE_AND_SHARDING_TABLES_REWRITE);
        preparedStatement.setInt(1, 11);
        ResultSet resultSet = preparedStatement.executeQuery();
        assertNotNull(resultSet);
        assertTrue(resultSet.next());
        assertThat(resultSet.getInt(1), is(1001));
        assertThat(resultSet.getInt(2), is(11));
        assertThat(resultSet.getString(3), is("init"));
        assertThat(resultSet.getInt(4), is(1001));
        assertThat(resultSet.getInt(5), is(10001));
        assertThat(resultSet.getInt(6), is(11));
        assertThat(resultSet.getString(7), is("init"));
        assertThat(resultSet.getString(8), is("t_order_item_calcite_sharding"));
        assertFalse(resultSet.next());
        preparedStatement.setInt(1, 10);
        ResultSet resultSet1 = preparedStatement.executeQuery();
        assertNotNull(resultSet1);
        assertTrue(resultSet1.next());
        assertThat(resultSet1.getInt(1), is(1000));
        assertThat(resultSet1.getInt(2), is(10));
        assertThat(resultSet1.getString(3), is("init"));
        assertThat(resultSet1.getInt(4), is(1000));
        assertThat(resultSet1.getInt(5), is(10000));
        assertThat(resultSet1.getInt(6), is(10));
        assertThat(resultSet1.getString(7), is("init"));
        assertThat(resultSet1.getString(8), is("t_order_item_calcite_sharding"));
        assertFalse(resultSet1.next());
    }
}
