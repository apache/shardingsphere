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

import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.dialect.MariaDBDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.fixture.FixtureRuleConfiguration;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DatabaseTypeEngineTest {
    
    @Test
    public void assertGetH2DatabaseType() throws SQLException {
        DataSource datasource = mockDataSource(DatabaseTypeFactory.getInstance("H2"));
        Collection<DataSource> dataSources = Collections.singleton(datasource);
        assertThat(DatabaseTypeEngine.getDatabaseType(dataSources).getType(), is("H2"));
    }
    
    @Test
    public void assertGetMariaDBDatabaseType() throws SQLException {
        DataSource datasource = mockDataSource(DatabaseTypeFactory.getInstance("MariaDB"));
        Collection<DataSource> dataSources = Collections.singleton(datasource);
        assertThat(DatabaseTypeEngine.getDatabaseType(dataSources).getType(), is("MariaDB"));
    }
    
    @Test
    public void assertGetMySQLDatabaseType() throws SQLException {
        DataSource datasource = mockDataSource(DatabaseTypeFactory.getInstance("MySQL"));
        Collection<DataSource> dataSources = Collections.singleton(datasource);
        assertThat(DatabaseTypeEngine.getDatabaseType(dataSources).getType(), is("MySQL"));
    }
    
    @Test
    public void assertGetOracleDatabaseType() throws SQLException {
        DataSource datasource = mockDataSource(DatabaseTypeFactory.getInstance("Oracle"));
        Collection<DataSource> dataSources = Collections.singleton(datasource);
        assertThat(DatabaseTypeEngine.getDatabaseType(dataSources).getType(), is("Oracle"));
    }
    
    @Test
    public void assertGetPostgreSQLDatabaseType() throws SQLException {
        DataSource datasource = mockDataSource(DatabaseTypeFactory.getInstance("PostgreSQL"));
        Collection<DataSource> dataSources = Collections.singleton(datasource);
        assertThat(DatabaseTypeEngine.getDatabaseType(dataSources).getType(), is("PostgreSQL"));
    }
    
    @Test
    public void assertGetSQL92DatabaseType() throws SQLException {
        DataSource datasource = mockDataSource(DatabaseTypeFactory.getInstance("SQL92"));
        Collection<DataSource> dataSources = Collections.singleton(datasource);
        assertThat(DatabaseTypeEngine.getDatabaseType(dataSources).getType(), is("SQL92"));
    }
    
    @Test
    public void assertGetSQLServerDatabaseType() throws SQLException {
        DataSource datasource = mockDataSource(DatabaseTypeFactory.getInstance("SQLServer"));
        Collection<DataSource> dataSources = Collections.singleton(datasource);
        assertThat(DatabaseTypeEngine.getDatabaseType(dataSources).getType(), is("SQLServer"));
    }
    
    @Test
    public void assertGetDatabaseTypeWithEmptyDataSources() {
        assertThat(DatabaseTypeEngine.getDatabaseType(Collections.emptyList()).getType(), is("MySQL"));
    }
    
    @Test
    public void assertGetDatabaseTypeFromSameDataSources() throws SQLException {
        DataSource datasource1 = mockDataSource(DatabaseTypeFactory.getInstance("MySQL"));
        DataSource datasource2 = mockDataSource(DatabaseTypeFactory.getInstance("MySQL"));
        Collection<DataSource> sameDataSources = Arrays.asList(datasource1, datasource2);
        assertThat(DatabaseTypeEngine.getDatabaseType(sameDataSources).getType(), is("MySQL"));
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertGetDatabaseTypeFromDifferentDataSources() throws SQLException {
        DataSource datasource1 = mockDataSource(DatabaseTypeFactory.getInstance("H2"));
        DataSource datasource2 = mockDataSource(DatabaseTypeFactory.getInstance("Oracle"));
        Collection<DataSource> differentDataSources = Arrays.asList(datasource1, datasource2);
        DatabaseTypeEngine.getDatabaseType(differentDataSources);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void assertCantGetConnectionFromDataSource() throws SQLException {
        DataSource mockDataSource = mock(DataSource.class);
        when(mockDataSource.getConnection()).thenThrow(SQLException.class);
        DatabaseTypeEngine.getDatabaseType(Collections.singleton(mockDataSource));
    }
    
    private DataSource mockDataSource(final DatabaseType databaseType) throws SQLException {
        DataSource result = mock(DataSource.class);
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(result.getConnection()).thenReturn(connection);
        String url;
        switch (databaseType.getType()) {
            case "H2":
                url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL";
                break;
            case "MariaDB":
                url = "jdbc:mariadb://localhost:3306/test";
                break;
            case "MySQL":
                url = "jdbc:mysql://localhost:3306/test";
                break;
            case "Oracle":
                url = "jdbc:oracle:oci:@127.0.0.1/test";
                break;
            case "PostgreSQL":
                url = "jdbc:postgresql://localhost:5432/test";
                break;
            case "SQL92":
                url = "jdbc:jtds:sqlserver://127.0.0.1;DatabaseName=test";
                break;
            case "SQLServer":
                url = "jdbc:microsoft:sqlserver://127.0.0.1;DatabaseName=test";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + databaseType.getType());
        }
        when(connection.getMetaData().getURL()).thenReturn(url);
        return result;
    }
    
    @Test
    public void assertGetDatabaseTypeByStandardURL() {
        assertThat(DatabaseTypeEngine.getDatabaseType("jdbc:mysql://localhost:3306/test").getType(), is("MySQL"));
    }
    
    @Test
    public void assertGetDatabaseTypeByURLAlias() {
        assertThat(DatabaseTypeEngine.getDatabaseType("jdbc:mysqlx://localhost:3306/test").getType(), is("MySQL"));
    }
    
    @Test
    public void assertGetDatabaseTypeSQL92() {
        assertThat(DatabaseTypeEngine.getDatabaseType("jdbc:sqlite:test").getType(), is("SQL92"));
    }
    
    @Test
    public void assertGetTrunkDatabaseTypeWithTrunkDatabaseType() {
        assertThat(DatabaseTypeEngine.getTrunkDatabaseType("MySQL").getType(), is("MySQL"));
    }
    
    @Test
    public void assertGetTrunkDatabaseTypeWithBranchDatabaseType() {
        assertThat(DatabaseTypeEngine.getTrunkDatabaseType("H2").getType(), is("MySQL"));
    }
    
    @Test
    public void assertGetTrunkDatabaseTypeNameWithTrunkDatabaseType() {
        assertThat(DatabaseTypeEngine.getTrunkDatabaseTypeName(new MySQLDatabaseType()), is("MySQL"));
    }
    
    @Test
    public void assertGetTrunkDatabaseTypeNameWithBranchDatabaseType() {
        assertThat(DatabaseTypeEngine.getTrunkDatabaseTypeName(new MariaDBDatabaseType()), is("MySQL"));
    }
    
    @Test
    public void assertGetProtocolType() {
        Properties props = new Properties();
        props.setProperty(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE.getKey(), "H2");
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(Collections.emptyMap(), Collections.singleton(new FixtureRuleConfiguration()));
        assertThat(DatabaseTypeEngine.getProtocolType(Collections.singletonMap("logic_db", databaseConfig), new ConfigurationProperties(props)), instanceOf(MySQLDatabaseType.class));
    }
    
    @Test
    public void assertGetStorageType() throws SQLException {
        DataSource datasource = mockDataSource(DatabaseTypeFactory.getInstance("MySQL"));
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(Collections.singletonMap("", datasource), Collections.singletonList(new FixtureRuleConfiguration()));
        assertThat(DatabaseTypeEngine.getStorageType(Collections.singletonMap("logic_db", databaseConfig)), instanceOf(MySQLDatabaseType.class));
    }
    
    @Test
    public void assertGetDefaultSchemaName() {
        DatabaseType schemaSupportDatabaseType = DatabaseTypeFactory.getInstance("openGauss");
        assertThat(DatabaseTypeEngine.getDefaultSchemaName(schemaSupportDatabaseType, ""), is("public"));
        DatabaseType schemaNoSupportDatabaseType = DatabaseTypeFactory.getInstance("MySQL");
        assertThat(DatabaseTypeEngine.getDefaultSchemaName(schemaNoSupportDatabaseType, "MySQL"), is("MySQL"));
    }
}
