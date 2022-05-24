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

import com.mysql.jdbc.jdbc2.optional.JDBC4MysqlXAConnection;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.transaction.xa.fixture.DataBaseTypeFixTure;
import org.apache.shardingsphere.transaction.xa.fixture.DataSourceUtils;
import org.apache.shardingsphere.transaction.xa.fixture.XAConnectionWrapperFixTure;
import org.apache.shardingsphere.transaction.xa.fixture.XADataSourceDefinitionFixTure;
import org.apache.shardingsphere.transaction.xa.jta.datasource.swapper.DataSourceSwapper;
import org.junit.Test;

import javax.sql.DataSource;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class XAConnectionWrapperFactoryTest {

    private final DatabaseType databaseType = new DataBaseTypeFixTure();


    @Test
    public void assertGetInstance() throws SQLException {
        XAConnectionWrapper xaConnectionWrapperFixTrue = new XAConnectionWrapperFixTure();
        XAConnection actual = xaConnectionWrapperFixTrue.wrap(createXADataSource(), mockConnection());
        assertThat(actual.getXAResource(), instanceOf(JDBC4MysqlXAConnection.class));
    }

    private XADataSource createXADataSource() {
        DataSource dataSource = DataSourceUtils.build(HikariDataSource.class, databaseType, "foo_ds");
        XADataSourceDefinitionFixTure xaDataSourceDefinitionFixTure = new XADataSourceDefinitionFixTure();
        return new DataSourceSwapper(xaDataSourceDefinitionFixTure).swap(dataSource);
    }

    private Connection mockConnection() throws SQLException {
        Connection result = mock(Connection.class);
        when(result.unwrap(com.mysql.jdbc.Connection.class)).thenReturn(mock(com.mysql.jdbc.Connection.class));
        return result;
    }
}
