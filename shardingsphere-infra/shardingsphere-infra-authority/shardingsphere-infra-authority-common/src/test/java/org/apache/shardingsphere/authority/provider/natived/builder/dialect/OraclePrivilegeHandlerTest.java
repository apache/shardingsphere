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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class OraclePrivilegeHandlerTest {
    
    @BeforeClass
    public static void setUp() {
        ShardingSphereServiceLoader.register(StoragePrivilegeHandler.class);
    }
    
    @Test
    public void assertDiff() throws SQLException {
        Collection<ShardingSphereUser> newUsers = createUsers();
        newUsers.add(new ShardingSphereUser("user", "", ""));
        DataSource dataSource = mockDataSourceForUsers(newUsers);
        Collection<ShardingSphereUser> result = TypedSPIRegistry.getRegisteredService(StoragePrivilegeHandler.class, "Oracle", new Properties()).diff(newUsers, dataSource);
        assertDiffUsers(result);
    }
    
    @Test
    public void assertCreate() throws SQLException {
        Collection<ShardingSphereUser> users = createUsers();
        DataSource dataSource = mockDataSourceForUsers(users);
        TypedSPIRegistry.getRegisteredService(StoragePrivilegeHandler.class, "Oracle", new Properties()).create(users, dataSource);
        assertCreateUsers(users, dataSource.getConnection().createStatement());
    }
    
    @Test
    public void assertGrantAll() throws SQLException {
        Collection<ShardingSphereUser> users = createUsers();
        DataSource dataSource = mockDataSourceForUsers(users);
        TypedSPIRegistry.getRegisteredService(StoragePrivilegeHandler.class, "Oracle", new Properties()).grantAll(users, dataSource);
        assertGrantUsersAll(users, dataSource.getConnection().createStatement());
    }
    
    @Test
    public void assertLoad() throws SQLException {
        Collection<ShardingSphereUser> users = createUsers();
        DataSource dataSource = mockDataSource(users);
        assertPrivileges(TypedSPIRegistry.getRegisteredService(StoragePrivilegeHandler.class, "Oracle", new Properties()).load(users, dataSource));
    }

    private void assertCreateUsers(final Collection<ShardingSphereUser> users, final Statement statement) throws SQLException {
        for (ShardingSphereUser each : users) {
            verify(statement).execute(String.format("CREATE USER %s IDENTIFIED BY %s", each.getGrantee().getUsername(), each.getPassword()));
        }
    }

    private void assertDiffUsers(final Collection<ShardingSphereUser> users) {
        assertThat(users.size(), is(1));
        assertThat(users.iterator().next().getGrantee().getUsername(), is("user"));
    }

    private void assertGrantUsersAll(final Collection<ShardingSphereUser> users, final Statement statement) throws SQLException {
        for (ShardingSphereUser each : users) {
            verify(statement).execute(String.format("GRANT ALL PRIVILEGES TO %s", each.getGrantee().getUsername()));
        }
    }
    
    private void assertPrivileges(final Map<ShardingSphereUser, NativePrivileges> actual) {
        assertThat(actual.size(), is(1));
        ShardingSphereUser user = new ShardingSphereUser("admin", "", "");
        assertThat(actual.get(user).getDatabasePrivileges().getGlobalPrivileges().size(), is(0));
        assertThat(actual.get(user).getDatabasePrivileges().getSpecificPrivileges().size(), is(1));
        Collection<PrivilegeType> expectedSpecificPrivilege = new CopyOnWriteArraySet(Arrays.asList(PrivilegeType.INSERT, PrivilegeType.SELECT, PrivilegeType.UPDATE));
        SchemaPrivileges schemaPrivileges = actual.get(user).getDatabasePrivileges().getSpecificPrivileges().get("sys");
        assertThat(schemaPrivileges.getSpecificPrivileges().get("t_order").hasPrivileges(expectedSpecificPrivilege), is(true));
        assertThat(actual.get(user).getAdministrativePrivileges().getPrivileges().size(), is(3));
        Collection<PrivilegeType> expectedAdminPrivileges = new CopyOnWriteArraySet(Arrays.asList(PrivilegeType.SUPER, PrivilegeType.CREATE_ROLE, PrivilegeType.CREATE_TABLESPACE));
        assertEquals(actual.get(user).getAdministrativePrivileges().getPrivileges(), expectedAdminPrivileges);
    }

    private Collection<ShardingSphereUser> createUsers() {
        LinkedList<ShardingSphereUser> result = new LinkedList<>();
        result.add(new ShardingSphereUser("admin", "", ""));
        return result;
    }

    private DataSource mockDataSource(final Collection<ShardingSphereUser> users) throws SQLException {
        ResultSet sysPrivilegeResultSet = mockSysPrivilegeResultSet();
        DataSource result = mock(DataSource.class, RETURNS_DEEP_STUBS);
        String sysPrivilegeSql = "SELECT * FROM DBA_SYS_PRIVS WHERE GRANTEE IN (%s)";
        String userList = users.stream().map(item -> String.format("'%s'", item.getGrantee().getUsername())).collect(Collectors.joining(", "));
        when(result.getConnection().createStatement().executeQuery(String.format(sysPrivilegeSql, userList))).thenReturn(sysPrivilegeResultSet);
        ResultSet tabPrivilegeResultSet = mockTabPrivilegeResultSet();
        String tabPrivilegeSql = "SELECT GRANTEE, TABLE_SCHEMA, TABLE_NAME, PRIVILEGE, GRANTABLE, INHERITED FROM ALL_TAB_PRIVS WHERE GRANTEE IN (%s)";
        when(result.getConnection().createStatement().executeQuery(String.format(tabPrivilegeSql, userList))).thenReturn(tabPrivilegeResultSet);
        return result;
    }

    private DataSource mockDataSourceForUsers(final Collection<ShardingSphereUser> users) throws SQLException {
        ResultSet usersResultSet = mockUsersResultSet();
        DataSource result = mock(DataSource.class, RETURNS_DEEP_STUBS);
        Statement statement = mock(Statement.class);
        Connection connection = mock(Connection.class);
        String diffUsersSQL = "SELECT * FROM DBA_SYS_PRIVS WHERE GRANTEE IN (%s)";
        String userList = users.stream().map(item -> String.format("'%s'", item.getGrantee().getUsername())).collect(Collectors.joining(", "));
        when(statement.executeQuery(String.format(diffUsersSQL, userList))).thenReturn(usersResultSet);
        when(connection.createStatement()).thenReturn(statement);
        when(result.getConnection()).thenReturn(connection);
        return result;
    }
    
    private ResultSet mockSysPrivilegeResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class, RETURNS_DEEP_STUBS);
        when(result.next()).thenReturn(true, true, true, false);
        when(result.getString("GRANTEE")).thenReturn("admin");
        when(result.getString("PRIVILEGE")).thenReturn("SYSDBA", "CREATE ROLE", "CREATE TABLESPACE");
        return result;
    }

    private ResultSet mockTabPrivilegeResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class, RETURNS_DEEP_STUBS);
        when(result.next()).thenReturn(true, true, true, true, false);
        when(result.getString("TABLE_SCHEMA")).thenReturn("sys");
        when(result.getString("TABLE_NAME")).thenReturn("t_order");
        when(result.getString("PRIVILEGE")).thenReturn("SELECT", "INSERT", "DELETE", "UPDATE");
        when(result.getString("GRANTABLE")).thenReturn("YES", "YES", "FALSE", "YES");
        when(result.getString("GRANTEE")).thenReturn("admin");
        return result;
    }

    private ResultSet mockUsersResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("GRANTEE")).thenReturn("admin");
        return result;
    }
}
