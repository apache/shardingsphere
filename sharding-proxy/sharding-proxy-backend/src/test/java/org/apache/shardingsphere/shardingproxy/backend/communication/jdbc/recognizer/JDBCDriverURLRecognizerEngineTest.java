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

package org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.recognizer;

import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.spi.database.H2DatabaseType;
import org.apache.shardingsphere.core.spi.database.MySQLDatabaseType;
import org.apache.shardingsphere.core.spi.database.OracleDatabaseType;
import org.apache.shardingsphere.core.spi.database.PostgreSQLDatabaseType;
import org.apache.shardingsphere.core.spi.database.SQLServerDatabaseType;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class JDBCDriverURLRecognizerEngineTest {
    
    @Test
    public void assertGetDriverClassName() {
        assertThat(JDBCDriverURLRecognizerEngine.getDriverClassName("jdbc:h2:xxx"), is("org.h2.Driver"));
    }
    
    @Test(expected = ShardingException.class)
    public void assertGetDriverClassNameFailure() {
        JDBCDriverURLRecognizerEngine.getDriverClassName("xxx");
    }
    
    @Test
    public void assertGetDatabaseTypeForMySQL() {
        assertThat(JDBCDriverURLRecognizerEngine.getDatabaseType("jdbc:mysql:xxx"), instanceOf(MySQLDatabaseType.class));
    }
    
    @Test
    public void assertGetDatabaseTypeForPostgreSQL() {
        assertThat(JDBCDriverURLRecognizerEngine.getDatabaseType("jdbc:postgresql:xxx"), instanceOf(PostgreSQLDatabaseType.class));
    }
    
    @Test
    public void assertGetDatabaseTypeForOracle() {
        assertThat(JDBCDriverURLRecognizerEngine.getDatabaseType("jdbc:oracle:xxx"), instanceOf(OracleDatabaseType.class));
    }
    
    @Test
    public void assertGetDatabaseTypeForSQLServer() {
        assertThat(JDBCDriverURLRecognizerEngine.getDatabaseType("jdbc:sqlserver:xxx"), instanceOf(SQLServerDatabaseType.class));
    }
    
    @Test
    public void assertGetDatabaseTypeForH2() {
        assertThat(JDBCDriverURLRecognizerEngine.getDatabaseType("jdbc:h2:xxx"), instanceOf(H2DatabaseType.class));
    }
    
    @Test(expected = ShardingException.class)
    public void assertGetDatabaseTypeFailure() {
        JDBCDriverURLRecognizerEngine.getDatabaseType("xxx");
    }
}
