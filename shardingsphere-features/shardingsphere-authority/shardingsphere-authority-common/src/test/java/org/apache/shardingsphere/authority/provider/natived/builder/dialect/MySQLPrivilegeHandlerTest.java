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

import org.apache.shardingsphere.authority.provider.natived.builder.StoragePrivilegeHandler;
import org.apache.shardingsphere.authority.model.PrivilegeType;
import org.apache.shardingsphere.authority.provider.natived.model.privilege.NativePrivileges;
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
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class MySQLPrivilegeHandlerTest {
    
    @BeforeClass
    public static void setUp() {
        ShardingSphereServiceLoader.register(StoragePrivilegeHandler.class);
    }
    
    @Test
    public void assertDiff() throws SQLException {
        Collection<ShardingSphereUser> newUsers = createUsers();
        newUsers.add(new ShardingSphereUser("root", "", "127.0.0.2"));
        DataSource dataSource = mockDataSourceForUsers(newUsers);
        assertDiffUsers(TypedSPIRegistry.getRegisteredService(StoragePrivilegeHandler.class, "MySQL", new Properties()).diff(newUsers, dataSource));
    }
    
    @Test
    public void assertCreate() throws SQLException {
        Collection<ShardingSphereUser> users = createUsers();
        DataSource dataSource = mockDataSourceForUsers(users);
        TypedSPIRegistry.getRegisteredService(StoragePrivilegeHandler.class, "MySQL", new Properties()).create(users, dataSource);
        assertCreateUsers(users, dataSource.getConnection().createStatement());
    }
    
    @Test
    public void assertGrantAll() throws SQLException {
        Collection<ShardingSphereUser> users = createUsers();
        DataSource dataSource = mockDataSourceForUsers(users);
        TypedSPIRegistry.getRegisteredService(StoragePrivilegeHandler.class, "MySQL", new Properties()).grantAll(users, dataSource);
        assertGrantUsersAll(users, dataSource.getConnection().createStatement());
    }
    
    @Test
    public void assertLoad() throws SQLException {
        Collection<ShardingSphereUser> users = createUsers();
        DataSource dataSource = mockDataSourceForPrivileges(users);
        assertPrivileges(TypedSPIRegistry.getRegisteredService(StoragePrivilegeHandler.class, "MySQL", new Properties()).load(users, dataSource));
    }
    
    private Collection<ShardingSphereUser> createUsers() {
        LinkedList<ShardingSphereUser> result = new LinkedList<>();
        result.add(new ShardingSphereUser("root", "", "localhost"));
        result.add(new ShardingSphereUser("mysql.sys", "", "localhost"));
        return result;
    }
    
    private DataSource mockDataSourceForUsers(final Collection<ShardingSphereUser> users) throws SQLException {
        ResultSet usersResultSet = mockUsersResultSet();
        DataSource result = mock(DataSource.class, RETURNS_DEEP_STUBS);
        Statement statement = mock(Statement.class);
        Connection connection = mock(Connection.class);
        String diffUsersSQL = "SELECT * FROM mysql.user WHERE (user, host) in (%s)";
        String useHostTuples = users.stream().map(item -> String.format("('%s', '%s')", item.getGrantee().getUsername(), item.getGrantee().getHostname())).collect(Collectors.joining(", "));
        when(statement.executeQuery(String.format(diffUsersSQL, useHostTuples))).thenReturn(usersResultSet);
        when(connection.createStatement()).thenReturn(statement);
        when(result.getConnection()).thenReturn(connection);
        return result;
    }
    
    private DataSource mockDataSourceForPrivileges(final Collection<ShardingSphereUser> users) throws SQLException {
        ResultSet globalPrivilegeResultSet = mockGlobalPrivilegeResultSet();
        ResultSet schemaPrivilegeResultSet = mockSchemaPrivilegeResultSet();
        ResultSet tablePrivilegeResultSet = mockTablePrivilegeResultSet();
        DataSource result = mock(DataSource.class, RETURNS_DEEP_STUBS);
        String globalPrivilegeSQL = "SELECT * FROM mysql.user WHERE (user, host) in (%s)";
        String schemaPrivilegeSQL = "SELECT * FROM mysql.db WHERE (user, host) in (%s)";
        String tablePrivilegeSQL = "SELECT Db, Table_name, Table_priv FROM mysql.tables_priv WHERE (user, host) in (%s)";
        String useHostTuples = users.stream().map(item -> String.format("('%s', '%s')", item.getGrantee().getUsername(), item.getGrantee().getHostname())).collect(Collectors.joining(", "));
        when(result.getConnection().createStatement().executeQuery(String.format(globalPrivilegeSQL, useHostTuples))).thenReturn(globalPrivilegeResultSet);
        when(result.getConnection().createStatement().executeQuery(String.format(schemaPrivilegeSQL, useHostTuples))).thenReturn(schemaPrivilegeResultSet);
        when(result.getConnection().createStatement().executeQuery(String.format(tablePrivilegeSQL, useHostTuples))).thenReturn(tablePrivilegeResultSet);
        return result;
    }
    
    private ResultSet mockUsersResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, false);
        when(result.getString("user")).thenReturn("root", "mysql.sys");
        when(result.getString("host")).thenReturn("localhost", "localhost");
        return result;
    }
    
    private ResultSet mockGlobalPrivilegeResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, false);
        when(result.getBoolean("Super_priv")).thenReturn(true, false);
        when(result.getBoolean("Reload_priv")).thenReturn(true, false);
        when(result.getBoolean("Shutdown_priv")).thenReturn(true, false);
        when(result.getBoolean("Process_priv")).thenReturn(false, false);
        when(result.getBoolean("File_priv")).thenReturn(false, false);
        when(result.getBoolean("Show_db_priv")).thenReturn(false, false);
        when(result.getBoolean("Repl_slave_priv")).thenReturn(false, false);
        when(result.getBoolean("Repl_client_priv")).thenReturn(false, false);
        when(result.getBoolean("Create_user_priv")).thenReturn(false, false);
        when(result.getBoolean("Create_tablespace_priv")).thenReturn(false, false);
        when(result.getBoolean("Select_priv")).thenReturn(true, false);
        when(result.getBoolean("Insert_priv")).thenReturn(true, false);
        when(result.getBoolean("Update_priv")).thenReturn(true, false);
        when(result.getBoolean("Delete_priv")).thenReturn(true, false);
        when(result.getBoolean("Create_priv")).thenReturn(true, false);
        when(result.getBoolean("Alter_priv")).thenReturn(true, false);
        when(result.getBoolean("Drop_priv")).thenReturn(false, false);
        when(result.getBoolean("Grant_priv")).thenReturn(false, false);
        when(result.getBoolean("Index_priv")).thenReturn(false, false);
        when(result.getBoolean("References_priv")).thenReturn(false, false);
        when(result.getBoolean("Create_tmp_table_priv")).thenReturn(false, false);
        when(result.getBoolean("Lock_tables_priv")).thenReturn(false, false);
        when(result.getBoolean("Execute_priv")).thenReturn(false, false);
        when(result.getBoolean("Create_view_priv")).thenReturn(false, false);
        when(result.getBoolean("Show_view_priv")).thenReturn(false, false);
        when(result.getBoolean("Create_routine_priv")).thenReturn(false, false);
        when(result.getBoolean("Alter_routine_priv")).thenReturn(false, false);
        when(result.getBoolean("Event_priv")).thenReturn(false, false);
        when(result.getBoolean("Trigger_priv")).thenReturn(false, false);
        when(result.getString("user")).thenReturn("root", "mysql.sys");
        when(result.getString("host")).thenReturn("localhost", "localhost");
        return result;
    }
    
    private ResultSet mockSchemaPrivilegeResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("Db")).thenReturn("sys");
        when(result.getBoolean("Select_priv")).thenReturn(false);
        when(result.getBoolean("Insert_priv")).thenReturn(false);
        when(result.getBoolean("Update_priv")).thenReturn(false);
        when(result.getBoolean("Delete_priv")).thenReturn(false);
        when(result.getBoolean("Create_priv")).thenReturn(false);
        when(result.getBoolean("Alter_priv")).thenReturn(false);
        when(result.getBoolean("Drop_priv")).thenReturn(false);
        when(result.getBoolean("Grant_priv")).thenReturn(false);
        when(result.getBoolean("Index_priv")).thenReturn(false);
        when(result.getBoolean("References_priv")).thenReturn(false);
        when(result.getBoolean("Create_tmp_table_priv")).thenReturn(false);
        when(result.getBoolean("Lock_tables_priv")).thenReturn(false);
        when(result.getBoolean("Execute_priv")).thenReturn(false);
        when(result.getBoolean("Create_view_priv")).thenReturn(false);
        when(result.getBoolean("Show_view_priv")).thenReturn(false);
        when(result.getBoolean("Create_routine_priv")).thenReturn(false);
        when(result.getBoolean("Alter_routine_priv")).thenReturn(false);
        when(result.getBoolean("Event_priv")).thenReturn(false);
        when(result.getBoolean("Trigger_priv")).thenReturn(true);
        when(result.getString("user")).thenReturn("mysql.sys");
        when(result.getString("host")).thenReturn("localhost");
        return result;
    }
    
    private ResultSet mockTablePrivilegeResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class, RETURNS_DEEP_STUBS);
        when(result.next()).thenReturn(true, false);
        when(result.getString("Db")).thenReturn("sys");
        when(result.getString("Table_name")).thenReturn("sys_config");
        when(result.getArray("Table_priv").getArray()).thenReturn(new String[]{"Select"});
        when(result.getString("user")).thenReturn("mysql.sys");
        when(result.getString("host")).thenReturn("localhost");
        return result;
    }
    
    private void assertDiffUsers(final Collection<ShardingSphereUser> users) {
        assertThat(users.size(), is(1));
        assertThat(users.iterator().next().getGrantee().getUsername(), is("root"));
        assertThat(users.iterator().next().getGrantee().getHostname(), is("127.0.0.2"));
    }
    
    private void assertCreateUsers(final Collection<ShardingSphereUser> users, final Statement statement) throws SQLException {
        String createUsers = users.stream().map(each -> String.format("'%s'@'%s' IDENTIFIED BY '%s'",
                each.getGrantee().getUsername(), each.getGrantee().getHostname(), each.getPassword())).collect(Collectors.joining(", "));
        verify(statement).execute(String.format("CREATE USER %s", createUsers));
    }
    
    private void assertGrantUsersAll(final Collection<ShardingSphereUser> users, final Statement statement) throws SQLException {
        String grantUsers = users.stream().map(each -> String.format("'%s'@'%s'",
                each.getGrantee().getUsername(), each.getGrantee().getHostname())).collect(Collectors.joining(", "));
        verify(statement).execute(String.format("GRANT ALL ON *.* TO %s", grantUsers));
    }
    
    private void assertPrivileges(final Map<ShardingSphereUser, NativePrivileges> actual) {
        assertThat(actual.size(), is(2));
        ShardingSphereUser root = new ShardingSphereUser("root", "", "localhost");
        assertThat(actual.get(root).getAdministrativePrivileges().getPrivileges().size(), is(3));
        Collection<PrivilegeType> expectedAdminPrivileges = new CopyOnWriteArraySet<>(Arrays.asList(PrivilegeType.SUPER, PrivilegeType.RELOAD, PrivilegeType.SHUTDOWN));
        assertThat(actual.get(root).getAdministrativePrivileges().getPrivileges(), is(expectedAdminPrivileges));
        Collection<PrivilegeType> expectedDatabasePrivileges = new CopyOnWriteArraySet<>(
                Arrays.asList(PrivilegeType.SELECT, PrivilegeType.INSERT, PrivilegeType.UPDATE, PrivilegeType.DELETE, PrivilegeType.CREATE, PrivilegeType.ALTER));
        assertThat(actual.get(root).getDatabasePrivileges().getGlobalPrivileges().size(), is(6));
        assertThat(actual.get(root).getDatabasePrivileges().getGlobalPrivileges(), is(expectedDatabasePrivileges));
        ShardingSphereUser sys = new ShardingSphereUser("mysql.sys", "", "localhost");
        assertThat(actual.get(sys).getAdministrativePrivileges().getPrivileges().size(), is(0));
        assertThat(actual.get(sys).getDatabasePrivileges().getGlobalPrivileges().size(), is(0));
        assertThat(actual.get(sys).getDatabasePrivileges().getSpecificPrivileges().size(), is(1));
    }
}
