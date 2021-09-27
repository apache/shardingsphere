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
}
