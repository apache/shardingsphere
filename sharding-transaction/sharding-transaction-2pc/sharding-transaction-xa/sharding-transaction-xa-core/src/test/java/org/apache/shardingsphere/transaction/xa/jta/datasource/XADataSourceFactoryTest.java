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
import org.apache.shardingsphere.core.database.DatabaseTypes;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Test;
import org.mariadb.jdbc.MariaDbDataSource;
import org.postgresql.xa.PGXADataSource;

import javax.sql.XADataSource;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public final class XADataSourceFactoryTest {
    
    @Test
    public void assertCreateH2XADataSource() {
        XADataSource xaDataSource = XADataSourceFactory.build(DatabaseTypes.getActualDatabaseType("H2"));
        assertThat(xaDataSource, instanceOf(JdbcDataSource.class));
    }

    @Test
    public void assertCreateMariaDBXADataSource() {
        XADataSource xaDataSource = XADataSourceFactory.build(DatabaseTypes.getActualDatabaseType("MariaDB"));
        assertThat(xaDataSource, instanceOf(MariaDbDataSource.class));
    }

    @Test
    public void assertCreatePGXADataSource() {
        XADataSource xaDataSource = XADataSourceFactory.build(DatabaseTypes.getActualDatabaseType("PostgreSQL"));
        assertThat(xaDataSource, instanceOf(PGXADataSource.class));
    }
    
    @Test
    public void assertCreateMSXADataSource() {
        XADataSource xaDataSource = XADataSourceFactory.build(DatabaseTypes.getActualDatabaseType("SQLServer"));
        assertThat(xaDataSource, instanceOf(SQLServerXADataSource.class));
    }
}
