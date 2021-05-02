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

package org.apache.shardingsphere.authority.provider.natived.builder.dialect;

import org.apache.shardingsphere.authority.model.PrivilegeType;
import org.apache.shardingsphere.authority.provider.natived.builder.StoragePrivilegeHandler;
import org.apache.shardingsphere.authority.provider.natived.model.privilege.NativePrivileges;
import org.apache.shardingsphere.authority.provider.natived.model.privilege.database.SchemaPrivileges;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.DatabaseMetaData;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

public final class SQLServerPrivilegeHandlerTest {
    
    @BeforeClass
    public static void setUp() {
        ShardingSphereServiceLoader.register(StoragePrivilegeHandler.class);
    }
    
    @Test
    public void assertDiff() throws SQLException {
        Collection<ShardingSphereUser> newUsers = createUsers();
        newUsers.add(new ShardingSphereUser("testUser2", "", ""));
        DataSource dataSource = mockDataSourceForUsers(newUsers);
        assertDiffUsers(TypedSPIRegistry.getRegisteredService(StoragePrivilegeHandler.class, "SQLServer", new Properties()).diff(newUsers, dataSource));
    }
    
    @Test
    public void assertCreate() throws SQLException {
        Collection<ShardingSphereUser> users = createUsers();
        DataSource dataSource = mockDataSourceForUsers(users);
        TypedSPIRegistry.getRegisteredService(StoragePrivilegeHandler.class, "SQLServer", new Properties()).create(users, dataSource);
        assertCreateUsers(users, dataSource.getConnection().createStatement());
    }
    
    @Test
    public void assertGrantAll() throws SQLException {
        Collection<ShardingSphereUser> users = createUsers();
        DataSource dataSource = mockDataSourceForUsers(users);
        TypedSPIRegistry.getRegisteredService(StoragePrivilegeHandler.class, "SQLServer", new Properties()).grantAll(users, dataSource);
        assertGrantUsersAll(users, dataSource.getConnection().createStatement());
    }
    
    @Test
    public void assertLoad() throws SQLException {
        Collection<ShardingSphereUser> users = createUsers();
        DataSource dataSource = mockDataSource(users);
        assertPrivileges(TypedSPIRegistry.getRegisteredService(StoragePrivilegeHandler.class, "SQLServer", new Properties()).load(users, dataSource));
    }
    
    private void assertPrivileges(final Map<ShardingSphereUser, NativePrivileges> actual) {
        assertThat(actual.size(), is(2));
        ShardingSphereUser dbo = new ShardingSphereUser("dbo", "", "");
        assertThat(actual.get(dbo).getAdministrativePrivileges().getPrivileges().size(), is(2));
        Collection<PrivilegeType> expectedAdminPrivileges = new CopyOnWriteArraySet<>(Arrays.asList(PrivilegeType.CONNECT, PrivilegeType.SHUTDOWN));
        assertThat(actual.get(dbo).getAdministrativePrivileges().getPrivileges(), is(expectedAdminPrivileges));

        Collection<PrivilegeType> expectedSpecificPrivilege = new CopyOnWriteArraySet(Arrays.asList(PrivilegeType.INSERT, PrivilegeType.SELECT, PrivilegeType.UPDATE,
                PrivilegeType.DELETE));
        SchemaPrivileges schemaPrivileges = actual.get(dbo).getDatabasePrivileges().getSpecificPrivileges().get("db0");
        assertThat(schemaPrivileges.getSpecificPrivileges().get("t_order").hasPrivileges(expectedSpecificPrivilege), is(true));
        
        ShardingSphereUser testUser = new ShardingSphereUser("testUser", "", "");
        assertThat(actual.get(testUser).getAdministrativePrivileges().getPrivileges().size(), is(0));
        assertThat(actual.get(testUser).getDatabasePrivileges().getGlobalPrivileges().size(), is(0));
        assertThat(actual.get(testUser).getDatabasePrivileges().getSpecificPrivileges().size(), is(0));
    }
    
    private Collection<ShardingSphereUser> createUsers() {
        LinkedList<ShardingSphereUser> result = new LinkedList<>();
        result.add(new ShardingSphereUser("dbo", "password", ""));
        result.add(new ShardingSphereUser("testUser", "password", ""));
        return result;
    }
    
    private DataSource mockDataSource(final Collection<ShardingSphereUser> users) throws SQLException {
        ResultSet globalPrivilegeResultSet = mockGlobalPrivilegeResultSet();
        DataSource result = mock(DataSource.class, RETURNS_DEEP_STUBS);
        String userList = users.stream().map(item -> String.format("'%s'", item.getGrantee().getUsername())).collect(Collectors.joining(", "));
        String globalPrivilegeSql = "SELECT pr.name AS GRANTEE, pe.state_desc AS STATE, pe.permission_name AS PRIVILEGE_TYPE"
                + "FROM sys.server_principals AS pr JOIN sys.server_permissions AS pe"
                + "ON pe.grantee_principal_id = pr.principal_id WHERE pr.name IN (%s) GROUP BY pr.name, pe.state_desc, pe.permission_name";
        when(result.getConnection().createStatement().executeQuery(String.format(globalPrivilegeSql, userList))).thenReturn(globalPrivilegeResultSet);
        ResultSet schemaPrivilegeResultSet = mockSchemaPrivilegeResultSet();
        String schemaPrivilegeSql = "SELECT pr.name AS GRANTEE, pe.state_desc AS STATE, pe.permission_name AS PRIVILEGE_TYPE, o.name AS DB"
                + "FROM sys.database_principals AS pr JOIN sys.database_permissions AS pe"
                + "ON pe.grantee_principal_id = pr.principal_id JOIN sys.objects AS o"
                + "ON pe.major_id = o.object_id WHERE pr.name IN (%s) GROUP BY pr.name, pe.state_desc, pe.permission_name, o.name";
        when(result.getConnection().createStatement().executeQuery(String.format(schemaPrivilegeSql, userList))).thenReturn(schemaPrivilegeResultSet);
        ResultSet tablePrivilegeResultSet = mockTablePrivilegeResultSet();
        String tablePrivilegeSql = "SELECT GRANTOR, GRANTEE, TABLE_CATALOG, TABLE_SCHEMA, TABLE_NAME, PRIVILEGE_TYPE, IS_GRANTABLE from INFORMATION_SCHEMA.TABLE_PRIVILEGES WHERE GRANTEE IN (%s)";
        when(result.getConnection().createStatement().executeQuery(String.format(tablePrivilegeSql, userList))).thenReturn(tablePrivilegeResultSet);
        return result;
    }
    
    private DataSource mockDataSourceForUsers(final Collection<ShardingSphereUser> users) throws SQLException {
        ResultSet usersResultSet = mockUsersResultSet();
        DataSource result = mock(DataSource.class, RETURNS_DEEP_STUBS);
        Statement statement = mock(Statement.class);
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        String diffUsersSQL = "SELECT pr.name AS GRANTEE, pe.state_desc AS STATE, pe.permission_name AS PRIVILEGE_TYPE"
                + "FROM sys.server_principals AS pr JOIN sys.server_permissions AS pe"
                + "ON pe.grantee_principal_id = pr.principal_id WHERE pr.name IN (%s) GROUP BY pr.name, pe.state_desc, pe.permission_name";
        String userList = users.stream().map(item -> String.format("'%s'", item.getGrantee().getUsername())).collect(Collectors.joining(", "));
        when(statement.executeQuery(String.format(diffUsersSQL, userList))).thenReturn(usersResultSet);
        when(connection.createStatement()).thenReturn(statement);
        when(result.getConnection()).thenReturn(connection);
        when(statement.getConnection()).thenReturn(connection);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getURL()).thenReturn("jdbc:sqlserver://127.0.0.1;DatabaseName=ds_0");
        return result;
    }
    
    private ResultSet mockUsersResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, false);
        when(result.getString("GRANTEE")).thenReturn("dbo", "testUser");
        return result;
    }
    
    private ResultSet mockGlobalPrivilegeResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class, RETURNS_DEEP_STUBS);
        when(result.next()).thenReturn(true, true, false);
        when(result.getString("STATE")).thenReturn("GRANT", "GRANT");
        when(result.getString("GRANTEE")).thenReturn("dbo", "dbo");
        when(result.getString("PRIVILEGE_TYPE")).thenReturn("CONNECT", "SHUTDOWN");
        return result;
    }
    
    private ResultSet mockTablePrivilegeResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class, RETURNS_DEEP_STUBS);
        when(result.next()).thenReturn(true, true, true, true, true, true, true, false);
        when(result.getString("TABLE_CATALOG")).thenReturn("db0");
        when(result.getString("TABLE_NAME")).thenReturn("t_order");
        when(result.getString("PRIVILEGE_TYPE")).thenReturn("INSERT", "SELECT", "UPDATE", "DELETE", "REFERENCES");
        when(result.getString("IS_GRANTABLE")).thenReturn("YES", "YES", "YES", "YES", "YES", "YES", "YES");
        when(result.getString("GRANTEE")).thenReturn("dbo");
        return result;
    }
    
    private ResultSet mockSchemaPrivilegeResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class, RETURNS_DEEP_STUBS);
        when(result.next()).thenReturn(true, false);
        when(result.getString("STATE")).thenReturn("GRANT");
        when(result.getString("GRANTEE")).thenReturn("dbo");
        when(result.getString("PRIVILEGE_TYPE")).thenReturn("CONNECT");
        when(result.getString("DB")).thenReturn("t_order");
        return result;
    }
    
    private void assertDiffUsers(final Collection<ShardingSphereUser> users) {
        assertThat(users.size(), is(1));
        assertThat(users.iterator().next().getGrantee().getUsername(), is("testUser2"));
    }
    
    private void assertCreateUsers(final Collection<ShardingSphereUser> users, final Statement statement) throws SQLException {
        for (ShardingSphereUser each : users) {
            StringBuilder result = new StringBuilder();
            result.append(String.format("CREATE LOGIN %s WITH PASSWORD = '%s';", each.getGrantee().getUsername(), each.getPassword())).append("\n");
            result.append("GO").append("\n");
            result.append(String.format("CREATE USER %s FOR LOGIN %s;\n", each.getGrantee().getUsername(), each.getGrantee().getUsername()));
            result.append("GO");
            verify(statement).execute(result.toString());
        }
    }
    
    private void assertGrantUsersAll(final Collection<ShardingSphereUser> users, final Statement statement) throws SQLException {
        String databaseName = getDatabaseName(statement.getConnection().getMetaData().getURL());
        for (ShardingSphereUser each : users) {
            verify(statement).execute(String.format("GRANT CONTROL ON DATABASE::%s TO %s", databaseName, each.getGrantee().getUsername()));
        }
    }
    
    private String getDatabaseName(final String url) {
        if (url.contains("?")) {
            return url.substring(url.indexOf("DatabaseName=") + 1, url.indexOf("?"));
        }
        return url.substring(url.indexOf("DatabaseName=") + 1);
    }
}
