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

package org.apache.shardingsphere.infra.database.type;

import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MariaDBDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OracleDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.SQL92DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.SQLServerDatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.fixture.FixtureDatabaseType;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DatabaseTypeFactoryTest {
    
    @Test
    public void assertGetInstance() {
        assertThat(DatabaseTypeFactory.getInstance("FIXTURE"), instanceOf(FixtureDatabaseType.class));
    }
    
    @Test
    public void assertGetInstances() {
        Collection<DatabaseType> actual = DatabaseTypeFactory.getInstances();
        assertThat(actual.size(), is(9));
        Iterator<DatabaseType> iterator = actual.iterator();
        assertThat(iterator.next(), instanceOf(FixtureDatabaseType.class));
        assertThat(iterator.next(), instanceOf(SQL92DatabaseType.class));
        assertThat(iterator.next(), instanceOf(MySQLDatabaseType.class));
        assertThat(iterator.next(), instanceOf(MariaDBDatabaseType.class));
        assertThat(iterator.next(), instanceOf(PostgreSQLDatabaseType.class));
        assertThat(iterator.next(), instanceOf(OpenGaussDatabaseType.class));
        assertThat(iterator.next(), instanceOf(OracleDatabaseType.class));
        assertThat(iterator.next(), instanceOf(SQLServerDatabaseType.class));
        assertThat(iterator.next(), instanceOf(H2DatabaseType.class));
    }
    
    @Test
    public void assertGetDatabaseTypeWithEmptyDataSources() {
        assertThat(DatabaseTypeEngine.getDatabaseType(Collections.emptyList()).getType(), is("MySQL"));
    }
    
    @Test
    public void assertGetDatabaseTypeWithDataSources() throws SQLException {
        Collection<DataSource> dataSources = Arrays.asList(mockDataSource(DatabaseTypeFactory.getInstance("H2")), mockDataSource(DatabaseTypeFactory.getInstance("H2")));
        assertThat(DatabaseTypeEngine.getDatabaseType(dataSources).getType(), is("H2"));
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertGetDatabaseTypeWithDifferentDataSourceTypes() throws SQLException {
        Collection<DataSource> dataSources = Arrays.asList(mockDataSource(DatabaseTypeFactory.getInstance("H2")), mockDataSource(DatabaseTypeFactory.getInstance("MySQL")));
        DatabaseTypeEngine.getDatabaseType(dataSources);
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertGetDatabaseTypeWhenGetConnectionError() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(SQLException.class);
        DatabaseTypeEngine.getDatabaseType(Collections.singleton(dataSource));
    }
    
    @Test
    public void assertGetDatabaseTypeWithRecognizedURL() {
        assertThat(DatabaseTypeEngine.getDatabaseType("jdbc:mysql://localhost:3306/test").getType(), is("MySQL"));
    }
    
    @Test
    public void assertGetDatabaseTypeWithUnrecognizedURL() {
        assertThat(DatabaseTypeEngine.getDatabaseType("jdbc:sqlite:test").getType(), is("SQL92"));
    }
    
    private DataSource mockDataSource(final DatabaseType databaseType) throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getURL()).thenReturn(getURL(databaseType));
        return new MockedDataSource(connection);
    }
    
    private String getURL(final DatabaseType databaseType) {
        switch (databaseType.getType()) {
            case "H2":
                return "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL";
            case "MySQL":
                return "jdbc:mysql://localhost:3306/test";
            case "PostgreSQL":
                return "jdbc:postgresql://localhost:5432/test";
            default:
                throw new IllegalStateException("Unexpected value: " + databaseType.getType());
        }
    }
}
