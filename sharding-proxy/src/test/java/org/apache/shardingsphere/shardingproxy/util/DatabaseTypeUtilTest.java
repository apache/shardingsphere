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

package org.apache.shardingsphere.shardingproxy.util;

import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class DatabaseTypeUtilTest {
    
    @Test
    public void assertGetDatabaseTypeForMySQL() {
        assertThat(DatabaseTypeUtil.getDatabaseType("jdbc:mysql://db.mysql:3306/test?serverTimezone=UTC&useSSL=false"), is(DatabaseType.MySQL));
    }
    
    @Test
    public void assertGetDatabaseTypeForPostgreSQL() {
        assertThat(DatabaseTypeUtil.getDatabaseType("jdbc:postgresql://db.psql:5432/postgres"), is(DatabaseType.PostgreSQL));
    }
    
    @Test
    public void assertGetDatabaseTypeForOracle() {
        assertThat(DatabaseTypeUtil.getDatabaseType("jdbc:oracle:thin:@db.oracle:1521:test"), is(DatabaseType.Oracle));
    }
    
    @Test
    public void assertGetDatabaseTypeForSQLServer() {
        assertThat(DatabaseTypeUtil.getDatabaseType("jdbc:sqlserver://db.mssql:1433;DatabaseName=test"), is(DatabaseType.SQLServer));
    }
    
    @Test
    public void assertGetDatabaseTypeForH2() {
        assertThat(DatabaseTypeUtil.getDatabaseType("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"), is(DatabaseType.H2));
    }
    
    @Test(expected = ShardingException.class)
    public void assertGetDatabaseTypeFailure() {
        DatabaseTypeUtil.getDatabaseType("xxx");
    }
    
    @Test
    public void assertGetDriverClassNameForMySQL() {
        assertThat(DatabaseTypeUtil.getDriverClassName("jdbc:mysql://db.mysql:3306/test?serverTimezone=UTC&useSSL=false"), is("com.mysql.jdbc.Driver"));
    }
    
    @Test
    public void assertGetDriverClassNameForPostgreSQL() {
        assertThat(DatabaseTypeUtil.getDriverClassName("jdbc:postgresql://db.psql:5432/postgres"), is("org.postgresql.Driver"));
    }
    
    @Test
    public void assertGetDriverClassNameForOracle() {
        assertThat(DatabaseTypeUtil.getDriverClassName("jdbc:oracle:thin:@db.oracle:1521:test"), is("oracle.jdbc.driver.OracleDriver"));
    }
    
    @Test
    public void assertGetDriverClassNameForSQLServer() {
        assertThat(DatabaseTypeUtil.getDriverClassName("jdbc:sqlserver://db.mssql:1433;DatabaseName=test"), is("com.microsoft.sqlserver.jdbc.SQLServerDriver"));
    }
    
    @Test
    public void assertGetDriverClassNameForH2() {
        assertThat(DatabaseTypeUtil.getDriverClassName("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL"), is("org.h2.Driver"));
    }
    
    @Test(expected = ShardingException.class)
    public void assertGetDriverClassNameFailure() {
        DatabaseTypeUtil.getDriverClassName("xxx");
    }
}
