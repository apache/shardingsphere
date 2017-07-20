/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.integrate.single;

import com.dangdang.ddframe.rdb.sharding.constant.DatabaseType;
import org.dbunit.DatabaseUnitException;
import org.junit.Test;

import java.sql.SQLException;

public final class SingleResultSetWithPreparedStatementTest extends AbstractSingleResultSetDBUnitTest {
    
    @Test
    public void assertSelectWithRowCountAndOffset() throws SQLException, DatabaseUnitException {
        if (DatabaseType.MySQL == currentDbType()) {
            String sql = "SELECT o.* FROM t_order o WHERE o.user_id = ? ORDER BY o.order_id limit ?, ?";
            String expectedDataSetFile = "integrate/dataset/single/expect/SelectWithLimit.xml";
            assertDataSet(expectedDataSetFile, getShardingDataSource().getConnection(), "t_order", sql, 10, 2, 4);
            assertDataSet("integrate/dataset/Empty.xml", getShardingDataSource().getConnection(), "t_order",
                    sql, 10, 4, 4);
        }
    }
}
