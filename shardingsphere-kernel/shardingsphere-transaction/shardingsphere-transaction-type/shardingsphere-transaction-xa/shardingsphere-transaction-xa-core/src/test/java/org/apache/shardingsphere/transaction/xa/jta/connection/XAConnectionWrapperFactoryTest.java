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

package org.apache.shardingsphere.transaction.xa.jta.connection;

import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.spi.exception.ServiceProviderNotFoundException;
import org.h2.jdbcx.JdbcXAConnection;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mariadb.jdbc.MariaXaConnection;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.postgresql.xa.PGXAConnection;

import javax.sql.XADataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public final class XAConnectionWrapperFactoryTest {
    
    @Mock
    private XADataSource xaDataSource;
    
    @Mock
    private Connection connection;
    
    @Test(expected = Exception.class)
    // TODO assert fail
    public void assertCreateMySQLXAConnection() throws SQLException {
        XAConnectionWrapperFactory.newInstance(DatabaseTypeRegistry.getActualDatabaseType("MySQL")).wrap(xaDataSource, connection);
    }

    @Test(expected = Exception.class)
    public void assertCreateMariaDBXAConnection() throws SQLException {
        assertThat(XAConnectionWrapperFactory.newInstance(DatabaseTypeRegistry.getActualDatabaseType("MariaDB")).wrap(xaDataSource, connection), instanceOf(MariaXaConnection.class));
    }

    @Test
    public void assertCreatePostgreSQLXAConnection() throws SQLException {
        assertThat(XAConnectionWrapperFactory.newInstance(DatabaseTypeRegistry.getActualDatabaseType("PostgreSQL")).wrap(xaDataSource, connection), instanceOf(PGXAConnection.class));
    }
    
    @Test
    @Ignore("openGauss jdbc driver is not import because of absenting from Maven central repository")
    public void assertCreateOpenGaussXAConnection() throws ClassNotFoundException, SQLException {
        Class<?> pgXAConnectionClass = Class.forName("org.opengauss.xa.PGXAConnection");
        assertThat(XAConnectionWrapperFactory.newInstance(DatabaseTypeRegistry.getActualDatabaseType("openGauss")).wrap(xaDataSource, connection), instanceOf(pgXAConnectionClass));
    }
    
    @Test
    public void assertCreateH2XAConnection() throws SQLException {
        assertThat(XAConnectionWrapperFactory.newInstance(DatabaseTypeRegistry.getActualDatabaseType("H2")).wrap(xaDataSource, connection), instanceOf(JdbcXAConnection.class));
    }
    
    @Test
    @Ignore("oracle jdbc driver is not import because of the limitations of license")
    public void assertCreateOracleXAConnection() throws ClassNotFoundException, SQLException {
        Class<?> clazz = Class.forName("oracle.jdbc.xa.client.OracleXAConnection");
        assertThat(XAConnectionWrapperFactory.newInstance(DatabaseTypeRegistry.getActualDatabaseType("Oracle")).wrap(xaDataSource, connection), instanceOf(clazz));
    }
    
    @Test(expected = ServiceProviderNotFoundException.class)
    public void assertCreateUnknownXAConnection() throws SQLException {
        XAConnectionWrapperFactory.newInstance(DatabaseTypeRegistry.getActualDatabaseType("SQL92")).wrap(xaDataSource, connection);
    }
}
