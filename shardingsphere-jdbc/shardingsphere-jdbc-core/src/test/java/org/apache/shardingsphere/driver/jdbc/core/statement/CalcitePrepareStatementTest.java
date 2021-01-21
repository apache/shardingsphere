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

import org.apache.shardingsphere.driver.common.base.AbstractShardingSphereDataSourceForCalciteTest;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class CalcitePrepareStatementTest extends AbstractShardingSphereDataSourceForCalciteTest {

    private static final String SELECT_SQL_BY_ID = "select o.*, i.* from t_order_calcite o, t_order_item_calcite i where o.order_id = ? and i.item_id = ?";

    @Test
    public void assertQueryWithCalciteInSingleTables() throws SQLException {
        ShardingSpherePreparedStatement preparedStatement = (ShardingSpherePreparedStatement) getShardingSphereDataSource().getConnection().prepareStatement(SELECT_SQL_BY_ID);
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
}
