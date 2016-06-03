/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.integrate.dbtbl.statically.pstatement;

import com.dangdang.ddframe.rdb.integrate.dbtbl.common.pstatement.AbstractShardingBothForPStatementWithDMLTest;
import com.dangdang.ddframe.rdb.integrate.dbtbl.statically.StaticShardingBothHelper;
import com.dangdang.ddframe.rdb.sharding.jdbc.ShardingDataSource;
import org.dbunit.DatabaseUnitException;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class StaticShardingBothForPStatementWithDMLTest extends AbstractShardingBothForPStatementWithDMLTest {
    
    @Override
    protected ShardingDataSource getShardingDataSource() {
        return StaticShardingBothHelper.getShardingDataSource(createDataSourceMap("dataSource_%s"));
    }
    
    @Test
    public void assertUpdateWithoutShardingValue() throws SQLException, DatabaseUnitException {
        String sql = "UPDATE `t_order` SET `status` = ? WHERE `status` = ?";
        try (Connection connection = getShardingDataSource().getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, "updated");
            preparedStatement.setString(2, "init");
            assertThat(preparedStatement.executeUpdate(), is(100));
        }
        assertDataSet("update", "updated");
    }
    
    @Test
    public void assertDeleteWithoutShardingValue() throws SQLException, DatabaseUnitException {
        String sql = "DELETE `t_order` WHERE `status` = ?";
        try (Connection connection = getShardingDataSource().getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, "init");
            assertThat(preparedStatement.executeUpdate(), is(100));
        }
        assertDataSet("delete", "init");
    }
}
