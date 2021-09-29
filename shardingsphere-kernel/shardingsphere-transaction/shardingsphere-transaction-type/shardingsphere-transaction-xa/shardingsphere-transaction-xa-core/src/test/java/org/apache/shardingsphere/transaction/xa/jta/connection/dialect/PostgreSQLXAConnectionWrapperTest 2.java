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

package org.apache.shardingsphere.transaction.xa.jta.connection.dialect;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.transaction.xa.fixture.DataSourceUtils;
import org.apache.shardingsphere.transaction.xa.jta.datasource.XADataSourceFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.postgresql.core.BaseConnection;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAResource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLXAConnectionWrapperTest {
    
    private XADataSource xaDataSource;
    
    @Mock
    private Connection connection;
    
    @Before
    public void setUp() throws SQLException, ClassNotFoundException {
        BaseConnection connection = (BaseConnection) mock(Class.forName("org.postgresql.core.BaseConnection"));
        DataSource dataSource = DataSourceUtils.build(HikariDataSource.class, DatabaseTypeRegistry.getActualDatabaseType("PostgreSQL"), "ds1");
        xaDataSource = XADataSourceFactory.build(DatabaseTypeRegistry.getActualDatabaseType("PostgreSQL"), dataSource);
        when(this.connection.unwrap(any())).thenReturn(connection);
        
    }
    
    @Test
    public void assertCreatePostgreSQLConnection() throws SQLException {
        XAConnection actual = new PostgreSQLXAConnectionWrapper().wrap(xaDataSource, connection);
        assertThat(actual.getXAResource(), instanceOf(XAResource.class));
        assertThat(actual.getConnection(), instanceOf(Connection.class));
    }
}
