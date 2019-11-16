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

package org.apache.shardingsphere.transaction.xa.jta.datasource;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.apache.shardingsphere.transaction.xa.fixture.DataSourceUtils;
import org.apache.shardingsphere.transaction.xa.jta.connection.XATransactionConnection;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class SingleXADataSourceTest {
    
    @Test
    public void assertBuildSingleXADataSourceOfXA() {
        XADataSource xaDataSource = DataSourceUtils.createXADataSource(DatabaseTypes.getActualDatabaseType("H2"), "ds1");
        XATransactionDataSource actual = new XATransactionDataSource(DatabaseTypes.getActualDatabaseType("H2"), "ds1", xaDataSource);
        assertThat(actual.getResourceName(), is("ds1"));
        assertThat(actual.getXaDataSource(), is(xaDataSource));
    }
    
    @Test
    public void assertBuildSingleXADataSourceOfNoneXA() {
        DataSource dataSource = DataSourceUtils.build(HikariDataSource.class, DatabaseTypes.getActualDatabaseType("H2"), "ds1");
        XATransactionDataSource actual = new XATransactionDataSource(DatabaseTypes.getActualDatabaseType("H2"), "ds1", dataSource);
        assertThat(actual.getResourceName(), is("ds1"));
        assertThat(actual.getXaDataSource(), instanceOf(JdbcDataSource.class));
        JdbcDataSource jdbcDataSource = (JdbcDataSource) actual.getXaDataSource();
        assertThat(jdbcDataSource.getUser(), is("root"));
        assertThat(jdbcDataSource.getPassword(), is("root"));
    }
    
    @Test
    public void assertGetXAConnectionOfXA() throws SQLException {
        XADataSource xaDataSource = DataSourceUtils.createXADataSource(DatabaseTypes.getActualDatabaseType("H2"), "ds1");
        XATransactionDataSource transactionDataSource = new XATransactionDataSource(DatabaseTypes.getActualDatabaseType("H2"), "ds1", xaDataSource);
        XATransactionConnection actual = transactionDataSource.getXAConnection();
        assertThat(actual.getTargetConnection(), instanceOf(Connection.class));
    }
    
    @Test
    public void assertGetXAConnectionOfNoneXA() throws SQLException {
        DataSource dataSource = DataSourceUtils.build(HikariDataSource.class, DatabaseTypes.getActualDatabaseType("H2"), "ds1");
        XATransactionDataSource transactionDataSource = new XATransactionDataSource(DatabaseTypes.getActualDatabaseType("H2"), "ds1", dataSource);
        XATransactionConnection actual = transactionDataSource.getXAConnection();
        assertThat(actual.getTargetConnection(), instanceOf(Connection.class));
    }
}
