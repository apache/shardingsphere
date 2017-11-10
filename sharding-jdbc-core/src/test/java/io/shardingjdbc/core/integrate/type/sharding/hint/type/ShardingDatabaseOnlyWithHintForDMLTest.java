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

package io.shardingjdbc.core.integrate.type.sharding.hint.type;

import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.constant.SQLType;
import io.shardingjdbc.core.integrate.sql.DatabaseTestSQL;
import io.shardingjdbc.core.integrate.type.sharding.hint.base.AbstractShardingDatabaseOnlyWithHintTest;
import io.shardingjdbc.core.integrate.type.sharding.hint.helper.HintShardingValueHelper;
import io.shardingjdbc.core.jdbc.core.connection.ShardingConnection;
import io.shardingjdbc.core.jdbc.core.datasource.ShardingDataSource;
import org.dbunit.DatabaseUnitException;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

import static io.shardingjdbc.core.common.util.SQLPlaceholderUtil.replacePreparedStatement;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ShardingDatabaseOnlyWithHintForDMLTest extends AbstractShardingDatabaseOnlyWithHintTest {
    
    private Map<DatabaseType, ShardingDataSource> shardingDataSources;
    
    @Before
    public void init() throws SQLException {
        shardingDataSources = initShardingDataSources();
    }
    
    @Test
    public void assertInsertWithAllPlaceholders() throws SQLException, DatabaseUnitException {
        for (Map.Entry<DatabaseType, ShardingDataSource> each : shardingDataSources.entrySet()) {
            for (int i = 1; i <= 10; i++) {
                try (HintShardingValueHelper helper = new HintShardingValueHelper(i, i);
                     Connection connection = each.getValue().getConnection()) {
                    PreparedStatement preparedStatement = connection.prepareStatement(DatabaseTestSQL.INSERT_ORDER_WITH_ALL_PLACEHOLDERS_SQL);
                    preparedStatement.setInt(1, i);
                    preparedStatement.setInt(2, i);
                    preparedStatement.setString(3, "insert");
                    preparedStatement.executeUpdate();
                }
            }
            assertDataSet(each.getValue().getConnection(), each.getKey(), "insert", "insert");
        }
    }
    
    @Test
    public void assertInsertWithoutPlaceholder() throws SQLException, DatabaseUnitException {
        for (Map.Entry<DatabaseType, ShardingDataSource> each : shardingDataSources.entrySet()) {
            for (int i = 1; i <= 10; i++) {
                try (HintShardingValueHelper helper = new HintShardingValueHelper(i, i);
                     Connection connection = each.getValue().getConnection()) {
                    PreparedStatement preparedStatement = connection.prepareStatement(String.format(DatabaseTestSQL.INSERT_WITHOUT_PLACEHOLDER_SQL, i, i));
                    preparedStatement.executeUpdate();
                }
            }
            assertDataSet(each.getValue().getConnection(), each.getKey(), "insert", "insert");
        }
    }
    
    @Test
    public void assertInsertWithPartialPlaceholders() throws SQLException, DatabaseUnitException {
        for (Map.Entry<DatabaseType, ShardingDataSource> each : shardingDataSources.entrySet()) {
            for (int i = 1; i <= 10; i++) {
                try (HintShardingValueHelper helper = new HintShardingValueHelper(i, i);
                     Connection connection = each.getValue().getConnection()) {
                    PreparedStatement preparedStatement = connection.prepareStatement(String.format(DatabaseTestSQL.INSERT_WITH_PARTIAL_PLACEHOLDERS_SQL, i, i));
                    preparedStatement.setString(1, "insert");
                    preparedStatement.executeUpdate();
                }
            }
            assertDataSet(each.getValue().getConnection(), each.getKey(), "insert", "insert");
        }
    }
    
    @Test
    public void assertUpdateWithoutAlias() throws SQLException, DatabaseUnitException {
        for (Map.Entry<DatabaseType, ShardingDataSource> each : shardingDataSources.entrySet()) {
            updateWithoutAlias(each);
            assertDataSet(each.getValue().getConnection(), each.getKey(), "update", "updated");
        }
    }
    
    private void updateWithoutAlias(final Map.Entry<DatabaseType, ShardingDataSource> dataSourceEntry) throws SQLException {
        for (int i = 10; i < 30; i++) {
            for (int j = 0; j < 2; j++) {
                try (HintShardingValueHelper helper = new HintShardingValueHelper(i, i * 100 + j);
                     Connection connection = dataSourceEntry.getValue().getConnection()) {
                    PreparedStatement preparedStatement = connection.prepareStatement(replacePreparedStatement(DatabaseTestSQL.UPDATE_WITHOUT_ALIAS_SQL));
                    preparedStatement.setString(1, "updated");
                    preparedStatement.setInt(2, i * 100 + j);
                    preparedStatement.setInt(3, i);
                    assertThat(preparedStatement.executeUpdate(), is(1));
                }
            }
        }
    }
    
    @Test
    public void assertUpdateWithAlias() throws SQLException, DatabaseUnitException {
        for (Map.Entry<DatabaseType, ShardingDataSource> each : shardingDataSources.entrySet()) {
            if (DatabaseType.H2 == each.getKey() || DatabaseType.MySQL == each.getKey()) {
                updateWithAlias(each);
            }
        }
    }
    
    private void updateWithAlias(final Map.Entry<DatabaseType, ShardingDataSource> dataSourceEntry) throws SQLException, DatabaseUnitException {
        for (int i = 10; i < 30; i++) {
            for (int j = 0; j < 2; j++) {
                try (HintShardingValueHelper helper = new HintShardingValueHelper(i, i * 100 + j);
                     Connection connection = dataSourceEntry.getValue().getConnection()) {
                    PreparedStatement preparedStatement = connection.prepareStatement(DatabaseTestSQL.UPDATE_WITH_ALIAS_SQL);
                    preparedStatement.setString(1, "updated");
                    preparedStatement.setInt(2, i * 100 + j);
                    preparedStatement.setInt(3, i);
                    assertThat(preparedStatement.executeUpdate(), is(1));
                }
            }
        }
        assertDataSet(dataSourceEntry.getValue().getConnection(), dataSourceEntry.getKey(), "update", "updated");
    }
    
    @Test
    public void assertDeleteWithoutAlias() throws SQLException, DatabaseUnitException {
        for (Map.Entry<DatabaseType, ShardingDataSource> each : shardingDataSources.entrySet()) {
            deleteWithoutAlias(each);
            assertDataSet(each.getValue().getConnection(), each.getKey(), "delete", "init");
        }
    }
    
    private void deleteWithoutAlias(final Map.Entry<DatabaseType, ShardingDataSource> dataSourceEntry) throws SQLException {
        for (int i = 10; i < 30; i++) {
            for (int j = 0; j < 2; j++) {
                try (HintShardingValueHelper helper = new HintShardingValueHelper(i, i * 100 + j);
                     Connection connection = dataSourceEntry.getValue().getConnection()) {
                    PreparedStatement preparedStatement = connection.prepareStatement(replacePreparedStatement(DatabaseTestSQL.DELETE_WITHOUT_ALIAS_SQL));
                    preparedStatement.setInt(1, i * 100 + j);
                    preparedStatement.setInt(2, i);
                    preparedStatement.setString(3, "init");
                    assertThat(preparedStatement.executeUpdate(), is(1));
                }
            }
        }
    }
    
    private void assertDataSet(final ShardingConnection connection, final DatabaseType type, final String expectedDataSetPattern, final String status) throws SQLException, DatabaseUnitException {
        for (int i = 0; i < 10; i++) {
            assertDataSet(String.format("integrate/dataset/sharding/hint/expect/%s/db_%s.xml", expectedDataSetPattern, i),
                    connection.getConnection(String.format("dataSource_db_%s", i), SQLType.DQL),
                    replacePreparedStatement(DatabaseTestSQL.ASSERT_SELECT_WITH_STATUS_SQL), type, status);
        }
    }
}
