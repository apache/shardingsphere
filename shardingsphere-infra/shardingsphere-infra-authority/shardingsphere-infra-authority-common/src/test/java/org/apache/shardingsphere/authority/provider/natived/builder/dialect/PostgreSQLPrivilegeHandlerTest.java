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
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

public final class PostgreSQLPrivilegeHandlerTest {
    
    @BeforeClass
    public static void setUp() {
        ShardingSphereServiceLoader.register(StoragePrivilegeHandler.class);
    }
    
    @Test
    public void assertDiff() throws SQLException {
        Collection<ShardingSphereUser> newUsers = createUsers();
        newUsers.add(new ShardingSphereUser("postgres2", "", ""));
        DataSource dataSource = mockDataSourceForUsers(newUsers);
        assertDiffUsers(TypedSPIRegistry.getRegisteredService(StoragePrivilegeHandler.class, "PostgreSQL", new Properties()).diff(newUsers, dataSource));
    }
    
    @Test
    public void assertCreate() throws SQLException {
        Collection<ShardingSphereUser> users = createUsers();
        DataSource dataSource = mockDataSourceForUsers(users);
        TypedSPIRegistry.getRegisteredService(StoragePrivilegeHandler.class, "PostgreSQL", new Properties()).create(users, dataSource);
        assertCreateUsers(users, dataSource.getConnection().createStatement());
    }
    
    @Test
    public void assertGrantAll() throws SQLException {
        Collection<ShardingSphereUser> users = createUsers();
        DataSource dataSource = mockDataSourceForUsers(users);
        TypedSPIRegistry.getRegisteredService(StoragePrivilegeHandler.class, "PostgreSQL", new Properties()).grantAll(users, dataSource);
        assertGrantUsersAll(users, dataSource.getConnection().createStatement());
    }
    
    @Test
    public void assertLoad() throws SQLException {
        Collection<ShardingSphereUser> users = createUsers();
        DataSource dataSource = mockDataSource(users);
        assertPrivileges(TypedSPIRegistry.getRegisteredService(StoragePrivilegeHandler.class, "PostgreSQL", new Properties()).load(users, dataSource));
    }
    
    private void assertPrivileges(final Map<ShardingSphereUser, NativePrivileges> actual) {
        assertThat(actual.size(), is(1));
        ShardingSphereUser user = new ShardingSphereUser("postgres", "", "");
        assertThat(actual.get(user).getDatabasePrivileges().getGlobalPrivileges().size(), is(0));
        assertThat(actual.get(user).getDatabasePrivileges().getSpecificPrivileges().size(), is(1));
        Collection<PrivilegeType> expectedSpecificPrivilege = new CopyOnWriteArraySet(Arrays.asList(PrivilegeType.INSERT, PrivilegeType.SELECT, PrivilegeType.UPDATE,
                PrivilegeType.DELETE));
        SchemaPrivileges schemaPrivileges = actual.get(user).getDatabasePrivileges().getSpecificPrivileges().get("db0");
        assertThat(schemaPrivileges.getSpecificPrivileges().get("t_order").hasPrivileges(expectedSpecificPrivilege), is(true));
        assertThat(actual.get(user).getAdministrativePrivileges().getPrivileges().size(), is(4));
        Collection<PrivilegeType> expectedAdminPrivileges = new CopyOnWriteArraySet(
                Arrays.asList(PrivilegeType.SUPER, PrivilegeType.CREATE_ROLE, PrivilegeType.CREATE_DATABASE, PrivilegeType.CAN_LOGIN));
        assertEquals(actual.get(user).getAdministrativePrivileges().getPrivileges(), expectedAdminPrivileges);
    }
    
    private void assertDiffUsers(final Collection<ShardingSphereUser> users) {
        assertThat(users.size(), is(1));
        assertThat(users.iterator().next().getGrantee().getUsername(), is("postgres2"));
    }
    
    private void assertCreateUsers(final Collection<ShardingSphereUser> users, final Statement statement) throws SQLException {
        for (ShardingSphereUser each : users) {
            verify(statement).execute(String.format("CREATE USER %s WITH PASSWORD '%s'", each.getGrantee().getUsername(), each.getPassword()));
        }
    }
    
    private void assertGrantUsersAll(final Collection<ShardingSphereUser> users, final Statement statement) throws SQLException {
        for (ShardingSphereUser each : users) {
            verify(statement).execute(String.format("ALTER USER %s WITH SUPERUSER", each.getGrantee().getUsername()));
        }
    }
    
    private Collection<ShardingSphereUser> createUsers() {
        LinkedList<ShardingSphereUser> result = new LinkedList<>();
        result.add(new ShardingSphereUser("postgres", "", ""));
        return result;
    }
    
    private DataSource mockDataSourceForUsers(final Collection<ShardingSphereUser> users) throws SQLException {
        ResultSet usersResultSet = mockUsersResultSet();
        DataSource result = mock(DataSource.class, RETURNS_DEEP_STUBS);
        Statement statement = mock(Statement.class);
        Connection connection = mock(Connection.class);
        String diffUsersSQL = "SELECT * FROM pg_roles WHERE rolname IN (%s)";
        String userList = users.stream().map(item -> String.format("'%s'", item.getGrantee().getUsername())).collect(Collectors.joining(", "));
        when(statement.executeQuery(String.format(diffUsersSQL, userList))).thenReturn(usersResultSet);
        when(connection.createStatement()).thenReturn(statement);
        when(result.getConnection()).thenReturn(connection);
        return result;
    }
    
    private DataSource mockDataSource(final Collection<ShardingSphereUser> users) throws SQLException {
        ResultSet tablePrivilegeResultSet = mockTablePrivilegeResultSet();
        DataSource result = mock(DataSource.class, RETURNS_DEEP_STUBS);
        String tablePrivilegeSql = "SELECT grantor, grantee, table_catalog, table_name, privilege_type, is_grantable FROM information_schema.table_privileges WHERE grantee IN (%s)";
        String userList = users.stream().map(item -> String.format("'%s'", item.getGrantee().getUsername())).collect(Collectors.joining(", "));
        when(result.getConnection().createStatement().executeQuery(String.format(tablePrivilegeSql, userList))).thenReturn(tablePrivilegeResultSet);
        ResultSet rolePrivilegeResultSet = mockRolePrivilegeResultSet();
        String rolePrivilegeSql = "SELECT * FROM pg_roles WHERE rolname IN (%s)";
        when(result.getConnection().createStatement().executeQuery(String.format(rolePrivilegeSql, userList))).thenReturn(rolePrivilegeResultSet);
        return result;
    }
    
    private ResultSet mockUsersResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, false);
        when(result.getString("rolname")).thenReturn("postgres1", "postgres");
        return result;
    }
    
    private ResultSet mockTablePrivilegeResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class, RETURNS_DEEP_STUBS);
        when(result.next()).thenReturn(true, true, true, true, true, true, true, false);
        when(result.getString("table_catalog")).thenReturn("db0");
        when(result.getString("table_name")).thenReturn("t_order");
        when(result.getString("privilege_type")).thenReturn("INSERT", "SELECT", "UPDATE", "DELETE", "TRUNCATE", "REFERENCES", "TRIGGER");
        when(result.getString("is_grantable")).thenReturn("TRUE", "TRUE", "TRUE", "TRUE", "TRUE", "TRUE", "TRUE");
        when(result.getString("grantee")).thenReturn("postgres");
        return result;
    }
    
    private ResultSet mockRolePrivilegeResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class, RETURNS_DEEP_STUBS);
        when(result.next()).thenReturn(true, false);
        when(result.getString("rolname")).thenReturn("postgres");
        when(result.getBoolean("rolsuper")).thenReturn(true);
        when(result.getBoolean("rolcreaterole")).thenReturn(true);
        when(result.getBoolean("rolcreatedb")).thenReturn(true);
        when(result.getBoolean("rolreplication")).thenReturn(false);
        when(result.getBoolean("rolinherit")).thenReturn(false);
        when(result.getBoolean("rolcanlogin")).thenReturn(true);
        return result;
    }
}
