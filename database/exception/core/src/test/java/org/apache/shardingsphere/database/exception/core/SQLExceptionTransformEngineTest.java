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

package org.apache.shardingsphere.database.exception.core;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.SQLDialectException;
import org.apache.shardingsphere.database.exception.core.exception.protocol.DatabaseProtocolException;
import org.apache.shardingsphere.infra.exception.external.server.ShardingSphereServerException;
import org.apache.shardingsphere.infra.exception.external.sql.ShardingSphereSQLException;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SQLExceptionTransformEngineTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertToSQLExceptionWithSQLException() {
        SQLException cause = new SQLException("");
        assertThat(SQLExceptionTransformEngine.toSQLException(cause, databaseType), is(cause));
    }
    
    @Test
    void assertToSQLExceptionWithShardingSphereSQLException() {
        ShardingSphereSQLException cause = mock(ShardingSphereSQLException.class);
        SQLException expected = new SQLException("");
        when(cause.toSQLException()).thenReturn(expected);
        assertThat(SQLExceptionTransformEngine.toSQLException(cause, databaseType), is(expected));
    }
    
    @Test
    void assertToSQLExceptionWithDatabaseProtocolException() {
        DatabaseProtocolException cause = mock(DatabaseProtocolException.class);
        when(cause.getMessage()).thenReturn("No reason");
        SQLException actual = SQLExceptionTransformEngine.toSQLException(cause, databaseType);
        assertThat(actual.getSQLState(), is("HY000"));
        assertThat(actual.getErrorCode(), is(30002));
        assertThat(actual.getMessage(), is("Database protocol exception: No reason"));
    }
    
    @Test
    void assertToSQLExceptionWithSQLDialectException() {
        assertThat(SQLExceptionTransformEngine.toSQLException(mock(SQLDialectException.class), databaseType).getMessage(), is("Dialect exception"));
    }
    
    @Test
    void assertToSQLExceptionWithShardingSphereServerException() {
        ShardingSphereServerException cause = mock(ShardingSphereServerException.class);
        when(cause.getMessage()).thenReturn("No reason");
        SQLException actual = SQLExceptionTransformEngine.toSQLException(cause, databaseType);
        assertThat(actual.getSQLState(), is("HY000"));
        assertThat(actual.getErrorCode(), is(30004));
        assertThat(actual.getMessage(), is("Server exception." + System.lineSeparator() + "More details: " + cause));
    }
    
    @Test
    void assertToSQLExceptionWithOtherException() {
        SQLException actual = SQLExceptionTransformEngine.toSQLException(new Exception("No reason"), databaseType);
        assertThat(actual.getSQLState(), is("HY000"));
        assertThat(actual.getErrorCode(), is(30000));
        assertThat(actual.getMessage(), is("Unknown exception." + System.lineSeparator() + "More details: java.lang.Exception: No reason"));
    }
}
