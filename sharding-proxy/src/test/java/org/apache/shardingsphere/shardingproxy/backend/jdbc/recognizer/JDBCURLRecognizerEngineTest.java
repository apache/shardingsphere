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

package org.apache.shardingsphere.shardingproxy.backend.jdbc.recognizer;

import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class JDBCURLRecognizerEngineTest {
    
    @Test
    public void assertGetDriverClassName() {
        assertThat(JDBCURLRecognizerEngine.getDriverClassName("jdbc:h2:xxx"), is("org.h2.Driver"));
    }
    
    @Test(expected = ShardingException.class)
    public void assertGetDriverClassNameFailure() {
        JDBCURLRecognizerEngine.getDriverClassName("xxx");
    }
    
    @Test
    public void assertGetDatabaseTypeForMySQL() {
        assertThat(JDBCURLRecognizerEngine.getDatabaseType("jdbc:mysql:xxx"), is(DatabaseType.MySQL));
    }
    
    @Test
    public void assertGetDatabaseTypeForPostgreSQL() {
        assertThat(JDBCURLRecognizerEngine.getDatabaseType("jdbc:postgresql:xxx"), is(DatabaseType.PostgreSQL));
    }
    
    @Test
    public void assertGetDatabaseTypeForOracle() {
        assertThat(JDBCURLRecognizerEngine.getDatabaseType("jdbc:oracle:xxx"), is(DatabaseType.Oracle));
    }
    
    @Test
    public void assertGetDatabaseTypeForSQLServer() {
        assertThat(JDBCURLRecognizerEngine.getDatabaseType("jdbc:sqlserver:xxx"), is(DatabaseType.SQLServer));
    }
    
    @Test
    public void assertGetDatabaseTypeForH2() {
        assertThat(JDBCURLRecognizerEngine.getDatabaseType("jdbc:h2:xxx"), is(DatabaseType.H2));
    }
    
    @Test(expected = ShardingException.class)
    public void assertGetDatabaseTypeFailure() {
        JDBCURLRecognizerEngine.getDatabaseType("xxx");
    }
}
