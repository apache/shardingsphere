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

package org.apache.shardingsphere.authority.provider.natived.builder;

import org.apache.shardingsphere.authority.model.PrivilegeType;
import org.apache.shardingsphere.authority.provider.natived.model.privilege.NativePrivileges;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class StoragePrivilegeBuilderTest {
    
    @Test
    public void assertBuildInCache() {
        Collection<ShardingSphereMetaData> metaDataList = new LinkedList<>();
        Collection<ShardingSphereUser> users = new LinkedList<>();
        ShardingSphereUser root = new ShardingSphereUser("root", "", "localhost");
        users.add(root);
        Map<ShardingSphereUser, NativePrivileges> result = StoragePrivilegeBuilder.build(metaDataList, users);
        assertThat(result.size(), is(1));
        assertTrue(result.get(root).hasPrivileges(Collections.singletonList(PrivilegeType.SUPER)));
    }
    
    @Test
    public void assertBuildPrivilegesInStorage() throws SQLException {
        Collection<ShardingSphereUser> users = new LinkedList<>();
        ShardingSphereUser root = new ShardingSphereUser("root", "", "localhost");
        users.add(root);
        ShardingSphereMetaData metaData = mockShardingSphereMetaData(users);
        Map<ShardingSphereUser, NativePrivileges> result = StoragePrivilegeBuilder.build(Collections.singletonList(metaData), users);
        assertThat(result.size(), is(1));
        Collection<PrivilegeType> expected = new LinkedList<>();
        expected.add(PrivilegeType.SUPER);
        expected.add(PrivilegeType.SELECT);
        expected.add(PrivilegeType.INSERT);
        expected.add(PrivilegeType.UPDATE);
        expected.add(PrivilegeType.RELOAD);
        expected.add(PrivilegeType.SHUTDOWN);
        assertTrue(result.get(root).hasPrivileges(expected));
    }
    
    private ShardingSphereMetaData mockShardingSphereMetaData(final Collection<ShardingSphereUser> users) throws SQLException {
        ShardingSphereMetaData result = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        DataSource dataSource = mockDataSourceForPrivileges(users);
        Collection<DataSource> dataSourceList = Collections.singletonList(dataSource);
        when(result.getResource().getAllInstanceDataSources()).thenReturn(dataSourceList);
        when(result.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
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
        when(result.getConnection().getMetaData().getURL()).thenReturn("jdbc:mysql://localhost:3306/test");
        return result;
    }
    
    private ResultSet mockGlobalPrivilegeResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, true, false, true, true, false);
        when(result.getObject("Super_priv")).thenReturn(true, false, true, false);
        when(result.getObject("Reload_priv")).thenReturn(true, false, true, false);
        when(result.getObject("Shutdown_priv")).thenReturn(true, false, true, false);
        when(result.getObject("Process_priv")).thenReturn(false, false, false, false);
        when(result.getObject("File_priv")).thenReturn(false, false, false, false);
        when(result.getObject("Show_db_priv")).thenReturn(false, false, false, false);
        when(result.getObject("Repl_slave_priv")).thenReturn(false, false, false, false);
        when(result.getObject("Repl_client_priv")).thenReturn(false, false, false, false);
        when(result.getObject("Create_user_priv")).thenReturn(false, false, false, false);
        when(result.getObject("Create_tablespace_priv")).thenReturn(false, false, false, false);
        when(result.getObject("Select_priv")).thenReturn(true, false, true, false);
        when(result.getObject("Insert_priv")).thenReturn(true, false, true, false);
        when(result.getObject("Update_priv")).thenReturn(true, false, true, false);
        when(result.getObject("Delete_priv")).thenReturn(true, false, true, false);
        when(result.getObject("Create_priv")).thenReturn(true, false, true, false);
        when(result.getObject("Alter_priv")).thenReturn(true, false, true, false);
        when(result.getObject("Drop_priv")).thenReturn(false, false, false, false);
        when(result.getObject("Grant_priv")).thenReturn(false, false, false, false);
        when(result.getObject("Index_priv")).thenReturn(false, false, false, false);
        when(result.getObject("References_priv")).thenReturn(false, false, false, false);
        when(result.getObject("Create_tmp_table_priv")).thenReturn(false, false, false, false);
        when(result.getObject("Lock_tables_priv")).thenReturn(false, false, false, false);
        when(result.getObject("Execute_priv")).thenReturn(false, false, false, false);
        when(result.getObject("Create_view_priv")).thenReturn(false, false, false, false);
        when(result.getObject("Show_view_priv")).thenReturn(false, false, false, false);
        when(result.getObject("Create_routine_priv")).thenReturn(false, false, false, false);
        when(result.getObject("Alter_routine_priv")).thenReturn(false, false, false, false);
        when(result.getObject("Event_priv")).thenReturn(false, false, false, false);
        when(result.getObject("Trigger_priv")).thenReturn(false, false, false, false);
        when(result.getString("user")).thenReturn("root", "mysql.sys", "root", "mysql.sys");
        when(result.getString("host")).thenReturn("localhost");
        return result;
    }
    
    private ResultSet mockSchemaPrivilegeResultSet() throws SQLException {
        ResultSet result = mock(ResultSet.class);
        when(result.next()).thenReturn(true, false);
        when(result.getString("Db")).thenReturn("sys");
        when(result.getObject("Select_priv")).thenReturn(false);
        when(result.getObject("Insert_priv")).thenReturn(false);
        when(result.getObject("Update_priv")).thenReturn(false);
        when(result.getObject("Delete_priv")).thenReturn(false);
        when(result.getObject("Create_priv")).thenReturn(false);
        when(result.getObject("Alter_priv")).thenReturn(false);
        when(result.getObject("Drop_priv")).thenReturn(false);
        when(result.getObject("Grant_priv")).thenReturn(false);
        when(result.getObject("Index_priv")).thenReturn(false);
        when(result.getObject("References_priv")).thenReturn(false);
        when(result.getObject("Create_tmp_table_priv")).thenReturn(false);
        when(result.getObject("Lock_tables_priv")).thenReturn(false);
        when(result.getObject("Execute_priv")).thenReturn(false);
        when(result.getObject("Create_view_priv")).thenReturn(false);
        when(result.getObject("Show_view_priv")).thenReturn(false);
        when(result.getObject("Create_routine_priv")).thenReturn(false);
        when(result.getObject("Alter_routine_priv")).thenReturn(false);
        when(result.getObject("Event_priv")).thenReturn(false);
        when(result.getObject("Trigger_priv")).thenReturn(true);
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
}
