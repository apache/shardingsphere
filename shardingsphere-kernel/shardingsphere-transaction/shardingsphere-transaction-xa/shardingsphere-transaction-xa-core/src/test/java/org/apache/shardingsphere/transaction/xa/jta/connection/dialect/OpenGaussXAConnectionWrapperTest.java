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
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.transaction.xa.fixture.DataSourceUtils;
import org.apache.shardingsphere.transaction.xa.jta.datasource.XADataSourceFactory;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@Ignore("openGauss jdbc driver is not import because of absenting from Maven central repository")
public final class OpenGaussXAConnectionWrapperTest {
    
    private static final String BASE_CONNECTION_CLASS = "org.opengauss.core.BaseConnection";
    
    private static final String PG_XA_CONNECTION_CLASS = "org.opengauss.xa.PGXAConnection";
    
    private XADataSource xaDataSource;
    
    @Mock
    private Connection connection;
    
    @Before
    public void setUp() throws SQLException, ClassNotFoundException {
        Object baseConnection = mock(Class.forName(BASE_CONNECTION_CLASS));
        DataSource dataSource = DataSourceUtils.build(HikariDataSource.class, DatabaseTypeRegistry.getActualDatabaseType("openGauss"), "ds1");
        xaDataSource = XADataSourceFactory.build(DatabaseTypeRegistry.getActualDatabaseType("openGauss"), dataSource);
        when(connection.unwrap(any())).thenReturn(baseConnection);
    }
    
    @Test
    public void assertCreateOpenGaussConnection() throws SQLException {
        XAConnection actual = new OpenGaussXAConnectionWrapper().wrap(xaDataSource, connection);
        assertThat(actual.getXAResource(), instanceOf(getPGXAConnectionClass()));
        assertThat(actual.getConnection(), instanceOf(Connection.class));
    }
    
    @SneakyThrows(ClassNotFoundException.class)
    private Class<?> getPGXAConnectionClass() {
        return Class.forName(PG_XA_CONNECTION_CLASS);
    }
}
