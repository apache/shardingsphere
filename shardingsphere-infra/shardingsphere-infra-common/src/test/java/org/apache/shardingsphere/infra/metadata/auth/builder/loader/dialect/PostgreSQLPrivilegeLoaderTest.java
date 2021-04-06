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

package org.apache.shardingsphere.infra.metadata.auth.builder.loader.dialect;

import org.apache.shardingsphere.infra.metadata.auth.builder.loader.PrivilegeLoader;
import org.apache.shardingsphere.infra.metadata.auth.model.privilege.PrivilegeType;
import org.apache.shardingsphere.infra.metadata.auth.model.privilege.ShardingSpherePrivilege;
import org.apache.shardingsphere.infra.metadata.auth.model.privilege.database.SchemaPrivilege;
import org.apache.shardingsphere.infra.metadata.auth.model.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class PostgreSQLPrivilegeLoaderTest {

    @BeforeClass
    public static void setUp() {
        ShardingSphereServiceLoader.register(PrivilegeLoader.class);
    }

    @Test
    public void assertLoad() throws SQLException {
        Collection<ShardingSphereUser> users = createUsers();
        DataSource dataSource = mockDataSource(users);
        assertPrivilege(getPrivilegeLoader().load(users, dataSource));
    }

    private void assertPrivilege(final Map<ShardingSphereUser, ShardingSpherePrivilege> actual) {
        assertThat(actual.size(), is(1));
        ShardingSphereUser user = new ShardingSphereUser("postgres", "", "");
        assertThat(actual.get(user).getDatabasePrivilege().getGlobalPrivileges().size(), is(0));
        assertThat(actual.get(user).getDatabasePrivilege().getSpecificPrivileges().size(), is(1));
        Collection<PrivilegeType> expectedSpecificPrivilege = new CopyOnWriteArraySet(Arrays.asList(PrivilegeType.INSERT, PrivilegeType.SELECT, PrivilegeType.UPDATE,
                PrivilegeType.DELETE));
        SchemaPrivilege schemaPrivilege = actual.get(user).getDatabasePrivilege().getSpecificPrivileges().get("db0");
        assertThat(schemaPrivilege.getSpecificPrivileges().get("t_order").hasPrivileges(expectedSpecificPrivilege), is(true));
        assertThat(actual.get(user).getAdministrativePrivilege().getPrivileges().size(), is(4));
        Collection<PrivilegeType> expectedAdministrativePrivilege = new CopyOnWriteArraySet(Arrays.asList(PrivilegeType.SUPER, PrivilegeType.CREATE_ROLE,
                PrivilegeType.CREATE_DATABASE, PrivilegeType.CAN_LOGIN));
        assertEquals(actual.get(user).getAdministrativePrivilege().getPrivileges(), expectedAdministrativePrivilege);
    }

    private Collection<ShardingSphereUser> createUsers() {
        LinkedList<ShardingSphereUser> users = new LinkedList<>();
        users.add(new ShardingSphereUser("postgres", "", ""));
        return users;
    }

    private DataSource mockDataSource(final Collection<ShardingSphereUser> users) throws SQLException {
        ResultSet tablePrivilegeResultSet = mockTablePrivilegeResultSet();
        DataSource dataSource = mock(DataSource.class, RETURNS_DEEP_STUBS);
        String tablePrivilegeSql = "SELECT grantor, grantee, table_catalog, table_name, privilege_type, is_grantable from information_schema.table_privileges WHERE grantee IN (%s)";
        String userList = users.stream().map(item -> String.format("'%s'", item.getGrantee().getUsername(), item.getGrantee().getHostname())).collect(Collectors.joining(", "));
        when(dataSource.getConnection().createStatement().executeQuery(String.format(tablePrivilegeSql, userList))).thenReturn(tablePrivilegeResultSet);
        ResultSet rolePrivilegeResultSet = mockRolePrivilegeResultSet();
        String rolePrivilegeSql = "select * from pg_roles WHERE rolname IN (%s)";
        when(dataSource.getConnection().createStatement().executeQuery(String.format(rolePrivilegeSql, userList))).thenReturn(rolePrivilegeResultSet);
        return dataSource;
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

    private PrivilegeLoader getPrivilegeLoader() {
        for (PrivilegeLoader each : ShardingSphereServiceLoader.getSingletonServiceInstances(PrivilegeLoader.class)) {
            if ("PostgreSQL".equals(each.getDatabaseType())) {
                return each;
            }
        }
        throw new IllegalStateException("Can not find PostgreSQLPrivilegeLoader");
    }
}
