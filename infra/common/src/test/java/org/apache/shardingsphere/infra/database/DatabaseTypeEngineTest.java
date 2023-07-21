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

package org.apache.shardingsphere.infra.database;

import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.mariadb.MariaDBDatabaseType;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.postgresql.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.fixture.FixtureRuleConfiguration;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.wrapper.SQLWrapperException;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DatabaseTypeEngineTest {
    
    @Test
    void assertGetProtocolTypeFromConfiguredProperties() {
        Properties props = PropertiesBuilder.build(new Property(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE.getKey(), "MySQL"));
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(Collections.emptyMap(), Collections.singleton(new FixtureRuleConfiguration()));
        assertThat(DatabaseTypeEngine.getProtocolType("sharding_db", databaseConfig, new ConfigurationProperties(props)), instanceOf(MySQLDatabaseType.class));
        assertThat(DatabaseTypeEngine.getProtocolType(Collections.singletonMap("foo_db", databaseConfig), new ConfigurationProperties(props)), instanceOf(MySQLDatabaseType.class));
    }
    
    @Test
    void assertGetProtocolTypeFromDataSource() throws SQLException {
        DataSource datasource = mockDataSource(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(Collections.singletonMap("foo_ds", datasource), Collections.singleton(new FixtureRuleConfiguration()));
        assertThat(DatabaseTypeEngine.getProtocolType("sharding_db", databaseConfig, new ConfigurationProperties(new Properties())), instanceOf(PostgreSQLDatabaseType.class));
        assertThat(DatabaseTypeEngine.getProtocolType(Collections.singletonMap("foo_db", databaseConfig), new ConfigurationProperties(new Properties())), instanceOf(PostgreSQLDatabaseType.class));
    }
    
    @Test
    void assertGetStorageTypes() throws SQLException {
        DataSource datasource = mockDataSource(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(Collections.singletonMap("foo_db", datasource), Collections.singletonList(new FixtureRuleConfiguration()));
        assertTrue(DatabaseTypeEngine.getStorageTypes("foo_db", databaseConfig).containsKey("foo_db"));
        assertThat(DatabaseTypeEngine.getStorageTypes("foo_db", databaseConfig).get("foo_db"), instanceOf(MySQLDatabaseType.class));
    }
    
    @Test
    void assertGetStorageTypeWithEmptyDataSources() {
        assertThat(DatabaseTypeEngine.getStorageType(Collections.emptyList()).getType(), is("MySQL"));
    }
    
    @Test
    void assertGetStorageTypeWithDataSources() throws SQLException {
        Collection<DataSource> dataSources = Arrays.asList(mockDataSource(TypedSPILoader.getService(DatabaseType.class, "H2")),
                mockDataSource(TypedSPILoader.getService(DatabaseType.class, "H2")));
        assertThat(DatabaseTypeEngine.getStorageType(dataSources).getType(), is("H2"));
    }
    
    @Test
    void assertGetStorageTypeWithDifferentDataSourceTypes() throws SQLException {
        Collection<DataSource> dataSources = Arrays.asList(mockDataSource(TypedSPILoader.getService(DatabaseType.class, "H2")),
                mockDataSource(TypedSPILoader.getService(DatabaseType.class, "MySQL")));
        assertThat(DatabaseTypeEngine.getStorageType(dataSources).getType(), is("H2"));
    }
    
    @Test
    void assertGetStorageTypeWhenGetConnectionError() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(SQLException.class);
        assertThrows(SQLWrapperException.class, () -> DatabaseTypeEngine.getStorageType(Collections.singleton(dataSource)));
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
    
    @Test
    void assertGetTrunkDatabaseTypeWithTrunkDatabaseType() {
        assertThat(DatabaseTypeEngine.getTrunkDatabaseType("MySQL").getType(), is("MySQL"));
    }
    
    @Test
    void assertGetTrunkDatabaseTypeWithBranchDatabaseType() {
        assertThat(DatabaseTypeEngine.getTrunkDatabaseType("H2").getType(), is("MySQL"));
    }
    
    @Test
    void assertGetTrunkDatabaseTypeNameWithTrunkDatabaseType() {
        assertThat(DatabaseTypeEngine.getTrunkDatabaseTypeName(new MySQLDatabaseType()), is("MySQL"));
    }
    
    @Test
    void assertGetTrunkDatabaseTypeNameWithBranchDatabaseType() {
        assertThat(DatabaseTypeEngine.getTrunkDatabaseTypeName(new MariaDBDatabaseType()), is("MySQL"));
    }
    
    @Test
    void assertGetDefaultSchemaName() {
        DatabaseType schemaSupportDatabaseType = TypedSPILoader.getService(DatabaseType.class, "openGauss");
        assertThat(DatabaseTypeEngine.getDefaultSchemaName(schemaSupportDatabaseType, ""), is("public"));
        DatabaseType schemaNoSupportDatabaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        assertThat(DatabaseTypeEngine.getDefaultSchemaName(schemaNoSupportDatabaseType, "MySQL"), is("mysql"));
    }
    
    @Test
    void assertGetBranchDatabaseTypes() {
        Collection<DatabaseType> actual = DatabaseTypeEngine.getTrunkAndBranchDatabaseTypes(Collections.singleton("MySQL"));
        assertTrue(actual.contains(TypedSPILoader.getService(DatabaseType.class, "MySQL")), "MySQL not present");
        assertTrue(actual.contains(TypedSPILoader.getService(DatabaseType.class, "MariaDB")), "MariaDB not present");
    }
}
