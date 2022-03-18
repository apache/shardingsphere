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
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.transaction.xa.fixture.DataSourceUtils;
import org.apache.shardingsphere.transaction.xa.jta.connection.XAConnectionWrapperFactory;
import org.apache.shardingsphere.transaction.xa.jta.datasource.XADataSourceFactory;
import org.junit.Test;
import org.opengauss.core.BaseConnection;
import org.opengauss.xa.PGXAConnection;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class OpenGaussXAConnectionWrapperTest {
    
    private final DatabaseType databaseType = DatabaseTypeRegistry.getActualDatabaseType("openGauss");
    
    @Test
    public void assertWrap() throws SQLException {
        XAConnection actual = XAConnectionWrapperFactory.newInstance(databaseType).wrap(createXADataSource(), mockConnection());
        assertThat(actual.getXAResource(), instanceOf(PGXAConnection.class));
    }
    
    private XADataSource createXADataSource() {
        DataSource dataSource = DataSourceUtils.build(HikariDataSource.class, databaseType, "foo_ds");
        return XADataSourceFactory.build(databaseType, dataSource);
    }
    
    private Connection mockConnection() throws SQLException {
        Connection result = mock(Connection.class);
        when(result.unwrap(BaseConnection.class)).thenReturn(mock(BaseConnection.class));
        return result;
    }
}
