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
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.transaction.xa.fixture.DataSourceUtils;
import org.apache.shardingsphere.transaction.xa.jta.connection.XAConnectionWrapper;
import org.apache.shardingsphere.transaction.xa.jta.datasource.properties.XADataSourceDefinition;
import org.apache.shardingsphere.transaction.xa.jta.datasource.swapper.DataSourceSwapper;
import org.h2.jdbc.JdbcConnection;
import org.h2.jdbcx.JdbcXAConnection;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class H2XAConnectionWrapperTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "H2");
    
    @Test
    void assertWrap() throws SQLException {
        XAConnection actual = DatabaseTypedSPILoader.getService(XAConnectionWrapper.class, databaseType).wrap(createXADataSource(), mockConnection());
        assertThat(actual.getXAResource(), instanceOf(JdbcXAConnection.class));
    }
    
    private XADataSource createXADataSource() {
        DataSource dataSource = DataSourceUtils.build(HikariDataSource.class, databaseType, "foo_ds");
        return new DataSourceSwapper(DatabaseTypedSPILoader.getService(XADataSourceDefinition.class, databaseType)).swap(dataSource);
    }
    
    private Connection mockConnection() throws SQLException {
        Connection result = mock(Connection.class);
        when(result.unwrap(JdbcConnection.class)).thenReturn(mock(JdbcConnection.class));
        return result;
    }
}
