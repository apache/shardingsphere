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

package org.apache.shardingsphere.driver.api;

import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingSphereDataSourceFactoryTest {
    
    @Test
    public void assertCreateDataSourceWithShardingRuleAndProperties() throws SQLException {
        Properties props = new Properties();
        ShardingSphereDataSource dataSource = (ShardingSphereDataSource) ShardingSphereDataSourceFactory.createDataSource(
                getDataSourceMap(), Collections.singleton(createShardingRuleConfiguration()), props);
        assertThat(dataSource.getSchemaContexts().getProps().getProps(), is(props));
    }
    
    private Map<String, DataSource> getDataSourceMap() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        Statement statement = mock(Statement.class);
        ResultSet resultSet = mock(ResultSet.class);
        when(statement.getResultSet()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(statement.getConnection()).thenReturn(connection);
        when(statement.getConnection().getMetaData().getTables(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);
        when(statement.getConnection().getMetaData().getURL()).thenReturn("jdbc:h2:mem:demo_ds;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        when(statement.getConnection().getMetaData().getColumns(null, null, "table_0", "%")).thenReturn(mock(ResultSet.class));
        when(statement.getConnection().getMetaData().getPrimaryKeys(null, null, "table_0")).thenReturn(mock(ResultSet.class));
        when(statement.getConnection().getMetaData().getIndexInfo(null, null, "table_0", false, false)).thenReturn(mock(ResultSet.class));
        Map<String, DataSource> result = new HashMap<>(1);
        result.put("ds", dataSource);
        return result;
    }
    
    private ShardingRuleConfiguration createShardingRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(new ShardingTableRuleConfiguration("logicTable", "ds.table_${0..2}"));
        return result;
    }
}
