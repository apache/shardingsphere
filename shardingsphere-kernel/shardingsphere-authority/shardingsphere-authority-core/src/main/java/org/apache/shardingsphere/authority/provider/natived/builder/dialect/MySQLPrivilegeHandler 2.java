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

import org.apache.shardingsphere.authority.provider.natived.model.privilege.database.SchemaPrivileges;
import org.apache.shardingsphere.authority.provider.natived.model.privilege.database.TablePrivileges;
import org.apache.shardingsphere.authority.provider.natived.builder.StoragePrivilegeHandler;
import org.apache.shardingsphere.authority.model.PrivilegeType;
import org.apache.shardingsphere.authority.provider.natived.model.privilege.NativePrivileges;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MySQL privilege handler.
 */
public final class MySQLPrivilegeHandler implements StoragePrivilegeHandler {
    
    private static final String CREATE_USER_SQL = "CREATE USER %s";
    
    private static final String GRANT_ALL_SQL = "GRANT ALL ON *.* TO %s";
    
    private static final String GLOBAL_PRIVILEGE_SQL = "SELECT * FROM mysql.user WHERE (user, host) in (%s)";
    
    private static final String SCHEMA_PRIVILEGE_SQL = "SELECT * FROM mysql.db WHERE (user, host) in (%s)";
    
    private static final String TABLE_PRIVILEGE_SQL = "SELECT Db, Table_name, Table_priv FROM mysql.tables_priv WHERE (user, host) in (%s)";
    
    @Override
    public Collection<ShardingSphereUser> diff(final Collection<ShardingSphereUser> users, final DataSource dataSource) throws SQLException {
        Collection<Grantee> grantees = new LinkedList<>();
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(getGlobalPrivilegesSQL(users))
        ) {
            while (resultSet.next()) {
                grantees.add(new Grantee(resultSet.getString("user"), resultSet.getString("host")));
            }
        }
        return users.stream().filter(each -> !grantees.contains(each.getGrantee())).collect(Collectors.toList());
    }
    
    @Override
    public void create(final Collection<ShardingSphereUser> users, final DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(getCreateUsersSQL(users));
        }
    }
    
    private String getCreateUsersSQL(final Collection<ShardingSphereUser> users) {
        String createUsers = users.stream().map(each -> String.format("'%s'@'%s' IDENTIFIED BY '%s'",
                each.getGrantee().getUsername(), each.getGrantee().getHostname(), each.getPassword())).collect(Collectors.joining(", "));
        return String.format(CREATE_USER_SQL, createUsers);
    }
    
    @Override
    public void grantAll(final Collection<ShardingSphereUser> users, final DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute(getGrantAllSQL(users));
        }
    }
    
    private String getGrantAllSQL(final Collection<ShardingSphereUser> users) {
        String grantUsers = users.stream().map(each -> String.format("'%s'@'%s'",
                each.getGrantee().getUsername(), each.getGrantee().getHostname())).collect(Collectors.joining(", "));
        return String.format(GRANT_ALL_SQL, grantUsers);
    }
    
    @Override
    public Map<ShardingSphereUser, NativePrivileges> load(final Collection<ShardingSphereUser> users, final DataSource dataSource) throws SQLException {
        Map<ShardingSphereUser, NativePrivileges> result = new LinkedHashMap<>();
        users.forEach(user -> result.put(user, new NativePrivileges()));
        fillGlobalPrivileges(result, dataSource, users);
        fillSchemaPrivileges(result, dataSource, users);
        fillTablePrivileges(result, dataSource, users);
        return result;
    }
    
    private void fillGlobalPrivileges(final Map<ShardingSphereUser, NativePrivileges> userPrivilegeMap, 
                                      final DataSource dataSource, final Collection<ShardingSphereUser> users) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(getGlobalPrivilegesSQL(users))
        ) {
            while (resultSet.next()) {
                fillGlobalPrivileges(userPrivilegeMap, resultSet);
            }
        }
    }
    
    private void fillGlobalPrivileges(final Map<ShardingSphereUser, NativePrivileges> userPrivilegeMap, final ResultSet resultSet) throws SQLException {
        Optional<ShardingSphereUser> user = findShardingSphereUser(userPrivilegeMap, resultSet);
        if (user.isPresent()) {
            userPrivilegeMap.get(user.get()).getAdministrativePrivileges().getPrivileges().addAll(loadAdministrativePrivileges(resultSet));
            userPrivilegeMap.get(user.get()).getDatabasePrivileges().getGlobalPrivileges().addAll(loadDatabaseGlobalPrivileges(resultSet));
        }
    }
    
    private void fillSchemaPrivileges(final Map<ShardingSphereUser, NativePrivileges> userPrivilegeMap, 
                                      final DataSource dataSource, final Collection<ShardingSphereUser> users) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(getSchemaPrivilegesSQL(users))
        ) {
            while (resultSet.next()) {
                fillSchemaPrivileges(userPrivilegeMap, resultSet);
            }
        }
    }
    
    private void fillSchemaPrivileges(final Map<ShardingSphereUser, NativePrivileges> userPrivilegeMap, final ResultSet resultSet) throws SQLException {
        Optional<ShardingSphereUser> user = findShardingSphereUser(userPrivilegeMap, resultSet);
        if (user.isPresent()) {
            String db = resultSet.getString("Db");
            SchemaPrivileges schemaPrivileges = new SchemaPrivileges(db);
            schemaPrivileges.getGlobalPrivileges().addAll(loadDatabaseGlobalPrivileges(resultSet));
            userPrivilegeMap.get(user.get()).getDatabasePrivileges().getSpecificPrivileges().put(db, schemaPrivileges);
        }
    }
    
    private void fillTablePrivileges(final Map<ShardingSphereUser, NativePrivileges> userPrivilegeMap, 
                                     final DataSource dataSource, final Collection<ShardingSphereUser> users) throws SQLException {
        try (
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(getTablePrivilegesSQL(users))
        ) {
            while (resultSet.next()) {
                fillTablePrivileges(userPrivilegeMap, resultSet);
            }
        }
    }
    
    private void fillTablePrivileges(final Map<ShardingSphereUser, NativePrivileges> userPrivilegeMap, final ResultSet resultSet) throws SQLException {
        Optional<ShardingSphereUser> user = findShardingSphereUser(userPrivilegeMap, resultSet);
        if (user.isPresent()) {
            String db = resultSet.getString("Db");
            String tableName = resultSet.getString("Table_name");
            TablePrivileges tablePrivileges = new TablePrivileges(tableName, getPrivileges((String[]) resultSet.getArray("Table_priv").getArray()));
            NativePrivileges privileges = userPrivilegeMap.get(user.get());
            if (!privileges.getDatabasePrivileges().getSpecificPrivileges().containsKey(db)) {
                privileges.getDatabasePrivileges().getSpecificPrivileges().put(db, new SchemaPrivileges(db));
            }
            privileges.getDatabasePrivileges().getSpecificPrivileges().get(db).getSpecificPrivileges().put(tableName, tablePrivileges);
        }
    }
    
    private String getGlobalPrivilegesSQL(final Collection<ShardingSphereUser> users) {
        String userHostTuples = users.stream().map(each -> String.format("('%s', '%s')", each.getGrantee().getUsername(), each.getGrantee().getHostname())).collect(Collectors.joining(", "));
        return String.format(GLOBAL_PRIVILEGE_SQL, userHostTuples);
    }
    
    private String getSchemaPrivilegesSQL(final Collection<ShardingSphereUser> users) {
        String userHostTuples = users.stream().map(each -> String.format("('%s', '%s')", each.getGrantee().getUsername(), each.getGrantee().getHostname()))
                .collect(Collectors.joining(", "));
        return String.format(SCHEMA_PRIVILEGE_SQL, userHostTuples);
    }
    
    private String getTablePrivilegesSQL(final Collection<ShardingSphereUser> users) {
        String userHostTuples = users.stream().map(each -> String.format("('%s', '%s')", each.getGrantee().getUsername(), each.getGrantee().getHostname()))
                .collect(Collectors.joining(", "));
        return String.format(TABLE_PRIVILEGE_SQL, userHostTuples);
    }
    
    private Optional<ShardingSphereUser> findShardingSphereUser(final Map<ShardingSphereUser, NativePrivileges> privileges, final ResultSet resultSet) throws SQLException {
        Grantee grantee = new Grantee(resultSet.getString("user"), resultSet.getString("host"));
        return privileges.keySet().stream().filter(each -> each.getGrantee().equals(grantee)).findFirst();
    }
    
    private Collection<PrivilegeType> getPrivileges(final String[] privileges) {
        return Arrays.stream(privileges).map(this::getPrivilegeType).collect(Collectors.toSet());
    }
    
    private PrivilegeType getPrivilegeType(final String privilege) {
        switch (privilege) {
            case "Select":
                return PrivilegeType.SELECT;
            case "Insert":
                return PrivilegeType.INSERT;
            case "Update":
                return PrivilegeType.UPDATE;
            case "Delete":
                return PrivilegeType.DELETE;
            case "Create":
                return PrivilegeType.CREATE;
            case "Alter":
                return PrivilegeType.ALTER;
            case "Drop":
                return PrivilegeType.DROP;
            case "Grant":
                return PrivilegeType.GRANT;
            case "Index":
                return PrivilegeType.INDEX;
            case "References":
                return PrivilegeType.REFERENCES;
            case "Create View":
                return PrivilegeType.CREATE_VIEW;
            case "Show view":
                return PrivilegeType.SHOW_VIEW;
            case "Trigger":
                return PrivilegeType.TRIGGER;
            default:
                throw new UnsupportedOperationException(privilege);
        }
    }
    
    private Collection<PrivilegeType> loadAdministrativePrivileges(final ResultSet resultSet) throws SQLException {
        Collection<PrivilegeType> result = new LinkedList<>();
        addToPrivilegeTypesIfPresent(resultSet.getObject("Super_priv"), PrivilegeType.SUPER, result);
        addToPrivilegeTypesIfPresent(resultSet.getObject("Reload_priv"), PrivilegeType.RELOAD, result);
        addToPrivilegeTypesIfPresent(resultSet.getObject("Shutdown_priv"), PrivilegeType.SHUTDOWN, result);
        addToPrivilegeTypesIfPresent(resultSet.getObject("Process_priv"), PrivilegeType.PROCESS, result);
        addToPrivilegeTypesIfPresent(resultSet.getObject("File_priv"), PrivilegeType.FILE, result);
        addToPrivilegeTypesIfPresent(resultSet.getObject("Show_db_priv"), PrivilegeType.SHOW_DB, result);
        addToPrivilegeTypesIfPresent(resultSet.getObject("Repl_slave_priv"), PrivilegeType.REPL_SLAVE, result);
        addToPrivilegeTypesIfPresent(resultSet.getObject("Repl_client_priv"), PrivilegeType.REPL_CLIENT, result);
        addToPrivilegeTypesIfPresent(resultSet.getObject("Create_user_priv"), PrivilegeType.CREATE_USER, result);
        addToPrivilegeTypesIfPresent(resultSet.getObject("Create_tablespace_priv"), PrivilegeType.CREATE_TABLESPACE, result);
        return result;
    }
    
    private Collection<PrivilegeType> loadDatabaseGlobalPrivileges(final ResultSet resultSet) throws SQLException {
        Collection<PrivilegeType> result = new LinkedList<>();
        addToPrivilegeTypesIfPresent(resultSet.getObject("Select_priv"), PrivilegeType.SELECT, result);
        addToPrivilegeTypesIfPresent(resultSet.getObject("Insert_priv"), PrivilegeType.INSERT, result);
        addToPrivilegeTypesIfPresent(resultSet.getObject("Update_priv"), PrivilegeType.UPDATE, result);
        addToPrivilegeTypesIfPresent(resultSet.getObject("Delete_priv"), PrivilegeType.DELETE, result);
        addToPrivilegeTypesIfPresent(resultSet.getObject("Create_priv"), PrivilegeType.CREATE, result);
        addToPrivilegeTypesIfPresent(resultSet.getObject("Alter_priv"), PrivilegeType.ALTER, result);
        addToPrivilegeTypesIfPresent(resultSet.getObject("Drop_priv"), PrivilegeType.DROP, result);
        addToPrivilegeTypesIfPresent(resultSet.getObject("Grant_priv"), PrivilegeType.GRANT, result);
        addToPrivilegeTypesIfPresent(resultSet.getObject("Index_priv"), PrivilegeType.INDEX, result);
        addToPrivilegeTypesIfPresent(resultSet.getObject("References_priv"), PrivilegeType.REFERENCES, result);
        addToPrivilegeTypesIfPresent(resultSet.getObject("Create_tmp_table_priv"), PrivilegeType.CREATE_TMP, result);
        addToPrivilegeTypesIfPresent(resultSet.getObject("Lock_tables_priv"), PrivilegeType.LOCK_TABLES, result);
        addToPrivilegeTypesIfPresent(resultSet.getObject("Execute_priv"), PrivilegeType.EXECUTE, result);
        addToPrivilegeTypesIfPresent(resultSet.getObject("Create_view_priv"), PrivilegeType.CREATE_VIEW, result);
        addToPrivilegeTypesIfPresent(resultSet.getObject("Show_view_priv"), PrivilegeType.SHOW_VIEW, result);
        addToPrivilegeTypesIfPresent(resultSet.getObject("Create_routine_priv"), PrivilegeType.CREATE_PROC, result);
        addToPrivilegeTypesIfPresent(resultSet.getObject("Alter_routine_priv"), PrivilegeType.ALTER_PROC, result);
        addToPrivilegeTypesIfPresent(resultSet.getObject("Event_priv"), PrivilegeType.EVENT, result);
        addToPrivilegeTypesIfPresent(resultSet.getObject("Trigger_priv"), PrivilegeType.TRIGGER, result);
        return result;
    }
    
    private void addToPrivilegeTypesIfPresent(final Object hasPrivilege, final PrivilegeType privilegeType, final Collection<PrivilegeType> target) {
        if (hasPrivilege instanceof String && "Y".equals(hasPrivilege)) {
            target.add(privilegeType);
        }
        if (hasPrivilege instanceof Boolean && (boolean) hasPrivilege) {
            target.add(privilegeType);
        }
    }
    
    @Override
    public String getType() {
        return "MySQL";
    }
}
