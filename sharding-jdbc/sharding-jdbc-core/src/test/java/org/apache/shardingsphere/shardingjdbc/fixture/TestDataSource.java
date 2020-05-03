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

package org.apache.shardingsphere.shardingjdbc.fixture;

import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.apache.shardingsphere.shardingjdbc.jdbc.adapter.AbstractDataSourceAdapter;
import org.apache.shardingsphere.underlying.common.rule.ShardingSphereRule;
import org.mockito.ArgumentMatchers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@EqualsAndHashCode
public final class TestDataSource extends AbstractDataSourceAdapter {
    
    private final String name;
    
    @Setter
    private boolean throwExceptionWhenClosing;
    
    public TestDataSource(final String name) throws SQLException {
        super(Collections.singletonMap("test", getDataSource()), Collections.singletonList(mock(ShardingSphereRule.class)), new Properties());
        this.name = name;
    }
    
    private static DataSource getDataSource() throws SQLException {
        DataSource result = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(metaData.getTables(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(mock(ResultSet.class));
        when(metaData.getURL()).thenReturn("jdbc:h2:mem:test_ds;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL");
        when(connection.getMetaData()).thenReturn(metaData);
        when(result.getConnection()).thenReturn(connection);
        return result;
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        Connection result = mock(Connection.class);
        DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(metaData.getTables(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(mock(ResultSet.class));
        when(result.getMetaData()).thenReturn(metaData);
        when(result.getMetaData().getURL()).thenReturn("jdbc:h2:mem:demo_ds;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL");
        if (throwExceptionWhenClosing) {
            doThrow(SQLException.class).when(result).close();
        }
        return result;
    }
}
