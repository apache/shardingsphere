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

package io.shardingjdbc.core.parsing.parser.sql;

import com.google.common.eventbus.Subscribe;
import io.shardingjdbc.core.common.base.AbstractShardingJDBCDatabaseAndTableTest;
import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.executor.event.DMLExecutionEvent;
import io.shardingjdbc.core.executor.event.EventExecutionType;
import io.shardingjdbc.core.integrate.sql.DatabaseTestSQL;
import io.shardingjdbc.core.jdbc.util.JDBCTestSQL;
import io.shardingjdbc.core.util.EventBusInstance;
import org.junit.Test;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static io.shardingjdbc.core.common.util.SQLPlaceholderUtil.replacePreparedStatement;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public final class OrShardingPreparedStatementTest extends AbstractShardingJDBCDatabaseAndTableTest {
    
    public OrShardingPreparedStatementTest(final DatabaseType databaseType) {
        super(databaseType);
    }
    
    @Test
    public void assertExecuteQueryWithParameter() throws SQLException {
        try (
                Connection connection = getShardingDataSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(DatabaseTestSQL.SELECT_WITH_OR_SQL)) {
            preparedStatement.setString(1, "11");
            preparedStatement.setLong(2,1100L);
            ResultSet resultSet = preparedStatement.executeQuery();
            int totalCount=0;
            while (resultSet.next()){
                totalCount++;
                assertThat(resultSet.getString(2), is("11"));
            }
            assertThat(totalCount,is(2));
        }
    }


    @Test
    public void assertExecuteQueryWithNoShardingColumn() throws SQLException {
        try (
                Connection connection = getShardingDataSource().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(DatabaseTestSQL.SELECT_WITH_OR_SQL_WITH_NO_SHARDING_COLUMN)) {
            preparedStatement.setString(1, "11");
            preparedStatement.setString(2,"init");
            ResultSet resultSet = preparedStatement.executeQuery();
            int totalCount=0;
            while (resultSet.next()){
                totalCount++;
                assertThat(resultSet.getString(3), is("init"));
            }
            assertThat(totalCount,is(4));
        }
    }
    

}
