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

import com.microsoft.sqlserver.jdbc.SQLServerXADataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.database.type.DatabaseTypes;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mariadb.jdbc.MariaDbDataSource;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.postgresql.xa.PGXADataSource;

import javax.sql.XADataSource;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class XADataSourceFactoryTest {
    
    @Mock
    private HikariDataSource dataSource;
    
    @Test
    public void assertCreateH2XADataSource() {
        XADataSource xaDataSource = XADataSourceFactory.build(DatabaseTypes.getActualDatabaseType("H2"), dataSource);
        assertThat(xaDataSource, instanceOf(JdbcDataSource.class));
    }

    @Test
    public void assertCreateMariaDBXADataSource() {
        XADataSource xaDataSource = XADataSourceFactory.build(DatabaseTypes.getActualDatabaseType("MariaDB"), dataSource);
        assertThat(xaDataSource, instanceOf(MariaDbDataSource.class));
    }

    @Test
    public void assertCreatePGXADataSource() {
        when(dataSource.getJdbcUrl()).thenReturn("jdbc:postgresql://localhost:5432/db1");
        XADataSource xaDataSource = XADataSourceFactory.build(DatabaseTypes.getActualDatabaseType("PostgreSQL"), dataSource);
        assertThat(xaDataSource, instanceOf(PGXADataSource.class));
    }
    
    @Test
    public void assertCreateMSXADataSource() {
        XADataSource xaDataSource = XADataSourceFactory.build(DatabaseTypes.getActualDatabaseType("SQLServer"), dataSource);
        assertThat(xaDataSource, instanceOf(SQLServerXADataSource.class));
    }
}
