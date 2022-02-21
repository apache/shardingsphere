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
import lombok.SneakyThrows;
import org.apache.shardingsphere.transaction.xa.fixture.DataSourceUtils;
import org.apache.shardingsphere.transaction.xa.jta.datasource.XADataSourceFactory;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAResource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class OracleXAConnectionWrapperTest {
    
    private static final short MINIMUM_VERSION_OF_XA_SUPPORTED = 8171;
    
    private XADataSource xaDataSource;
    
    @Mock
    private Connection connection;
    
    @SneakyThrows(ReflectiveOperationException.class)
    @Before
    @Ignore("oracle jdbc driver is not import because of the limitations of license")
    public void setUp() throws SQLException {
        Connection connection = (Connection) mock(Class.forName("oracle.jdbc.internal.OracleConnection"));
        DataSource dataSource = DataSourceUtils.build(HikariDataSource.class, DatabaseTypeRegistry.getActualDatabaseType("Oracle"), "ds1");
        xaDataSource = XADataSourceFactory.build(DatabaseTypeRegistry.getActualDatabaseType("Oracle"), dataSource);
        when(this.connection.unwrap(any())).thenReturn(connection);
        Method getVersionNumberMethod = connection.getClass().getDeclaredMethod("getVersionNumber");
        when(getVersionNumberMethod.invoke(connection)).thenReturn(MINIMUM_VERSION_OF_XA_SUPPORTED);
        Method getLogicalConnectionMethod = connection.getClass().getDeclaredMethod("getLogicalConnection", Class.forName("oracle.jdbc.pool.OraclePooledConnection"), Boolean.TYPE);
        Connection logicalConnection = (Connection) mock(Class.forName("oracle.jdbc.driver.LogicalConnection"));
        when(getLogicalConnectionMethod.invoke(connection, any(), anyBoolean())).thenReturn(logicalConnection);
    }
    
    @Test
    @Ignore("oracle jdbc driver is not import because of the limitations of license")
    public void assertCreateOracleConnection() throws SQLException {
        XAConnection actual = new OracleXAConnectionWrapper().wrap(xaDataSource, connection);
        assertThat(actual.getXAResource(), instanceOf(XAResource.class));
        assertThat(actual.getConnection(), instanceOf(Connection.class));
    }
}
