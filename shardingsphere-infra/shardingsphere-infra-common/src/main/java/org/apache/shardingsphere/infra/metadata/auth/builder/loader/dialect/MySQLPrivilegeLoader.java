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
import org.apache.shardingsphere.infra.metadata.auth.model.privilege.database.TablePrivilege;
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
 * MySQL privilege loader.
 */
public final class MySQLPrivilegeLoader implements PrivilegeLoader {

    private static final String GLOBAL_PRIVILEGE_SQL = "SELECT * FROM mysql.user WHERE (user, host) in (%s)";

    private static final String SCHEMA_PRIVILEGE_SQL = "SELECT * FROM mysql.db WHERE (user, host) in (%s)";

    private static final String TABLE_PRIVILEGE_SQL = "SELECT Db, Table_name, Table_priv FROM mysql.tables_priv WHERE (user, host) in (%s)";

    @Override
    public Map<ShardingSphereUser, ShardingSpherePrivilege> load(final Collection<ShardingSphereUser> users, final DataSource dataSource) throws SQLException {
        Map<ShardingSphereUser, ShardingSpherePrivilege> result = new LinkedHashMap<>();
        users.forEach(user -> result.put(user, new ShardingSpherePrivilege()));
        fillGlobalPrivilege(result, dataSource, users);
        fillSchemaPrivilege(result, dataSource, users);
        fillTablePrivilege(result, dataSource, users);
        return result;
    }
    
    private void fillGlobalPrivilege(final Map<ShardingSphereUser, ShardingSpherePrivilege> privileges, final DataSource dataSource, final Collection<ShardingSphereUser> users) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            try (ResultSet resultSet = statement.executeQuery(getGlobalPrivilegeSQL(users))) {
                while (resultSet.next()) {
                    fillGlobalPrivilege(privileges, resultSet);
                }
            }
        }
    }
    
    private void fillGlobalPrivilege(final Map<ShardingSphereUser, ShardingSpherePrivilege> privileges, final ResultSet resultSet) throws SQLException {
        Optional<ShardingSphereUser> user = getShardingSphereUser(privileges, resultSet);
        if (user.isPresent()) {
            privileges.get(user.get()).getAdministrativePrivilege().getPrivileges().addAll(loadAdministrativePrivileges(resultSet));
            privileges.get(user.get()).getDatabasePrivilege().getGlobalPrivileges().addAll(loadDatabaseGlobalPrivileges(resultSet));
        }
    }
    
    private void fillSchemaPrivilege(final Map<ShardingSphereUser, ShardingSpherePrivilege> privileges, final DataSource dataSource, final Collection<ShardingSphereUser> users) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            try (ResultSet resultSet = statement.executeQuery(getSchemaPrivilegeSQL(users))) {
                while (resultSet.next()) {
                    fillSchemaPrivilege(privileges, resultSet);
                }
            }
        }
    }
    
    private void fillSchemaPrivilege(final Map<ShardingSphereUser, ShardingSpherePrivilege> privileges, final ResultSet resultSet) throws SQLException {
        Optional<ShardingSphereUser> user = getShardingSphereUser(privileges, resultSet);
        if (user.isPresent()) {
            String db = resultSet.getString("Db");
            SchemaPrivilege schemaPrivilege = new SchemaPrivilege(db);
            schemaPrivilege.getGlobalPrivileges().addAll(loadDatabaseGlobalPrivileges(resultSet));
            privileges.get(user.get()).getDatabasePrivilege().getSpecificPrivileges().put(db, schemaPrivilege);
        }
    }
    
    private void fillTablePrivilege(final Map<ShardingSphereUser, ShardingSpherePrivilege> privileges, final DataSource dataSource, final Collection<ShardingSphereUser> users) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            try (ResultSet resultSet = statement.executeQuery(getTablePrivilegeSQL(users))) {
                while (resultSet.next()) {
                    fillTablePrivilege(privileges, resultSet);
                }
            }
        }
    }
    
    private void fillTablePrivilege(final Map<ShardingSphereUser, ShardingSpherePrivilege> privileges, final ResultSet resultSet) throws SQLException {
        Optional<ShardingSphereUser> user = getShardingSphereUser(privileges, resultSet);
        if (user.isPresent()) {
            String db = resultSet.getString("Db");
            String tableName = resultSet.getString("Table_name");
            String[] tablePrivileges = (String[]) resultSet.getArray("Table_priv").getArray();
            TablePrivilege tablePrivilege = new TablePrivilege(tableName, getPrivileges(tablePrivileges));
            ShardingSpherePrivilege privilege = privileges.get(user.get());
            if (!privilege.getDatabasePrivilege().getSpecificPrivileges().containsKey(db)) {
                privilege.getDatabasePrivilege().getSpecificPrivileges().put(db, new SchemaPrivilege(db));
            }
            privilege.getDatabasePrivilege().getSpecificPrivileges().get(db).getSpecificPrivileges().put(tableName, tablePrivilege);
        }
    }
    
    private String getGlobalPrivilegeSQL(final Collection<ShardingSphereUser> users) {
        String userHostTuples = users.stream().map(each -> String.format("('%s', '%s')", each.getGrantee().getUsername(), each.getGrantee().getHostname())).collect(Collectors.joining(","));
        return String.format(GLOBAL_PRIVILEGE_SQL, userHostTuples);
    }
    
    private String getSchemaPrivilegeSQL(final Collection<ShardingSphereUser> users) {
        String userHostTuples = users.stream().map(each -> String.format("('%s', '%s')", each.getGrantee().getUsername(), each.getGrantee().getHostname()))
                .collect(Collectors.joining(","));
        return String.format(SCHEMA_PRIVILEGE_SQL, userHostTuples);
    }
    
    private String getTablePrivilegeSQL(final Collection<ShardingSphereUser> users) {
        String userHostTuples = users.stream().map(each -> String.format("('%s', '%s')", each.getGrantee().getUsername(), each.getGrantee().getHostname()))
                .collect(Collectors.joining(","));
        return String.format(TABLE_PRIVILEGE_SQL, userHostTuples);
    }
    
    private Optional<ShardingSphereUser> getShardingSphereUser(final Map<ShardingSphereUser, ShardingSpherePrivilege> privileges, final ResultSet resultSet) throws SQLException {
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
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("Super_priv"), PrivilegeType.SUPER, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("Reload_priv"), PrivilegeType.RELOAD, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("Shutdown_priv"), PrivilegeType.SHUTDOWN, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("Process_priv"), PrivilegeType.PROCESS, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("File_priv"), PrivilegeType.FILE, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("Show_db_priv"), PrivilegeType.SHOW_DB, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("Repl_slave_priv"), PrivilegeType.REPL_SLAVE, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("Repl_client_priv"), PrivilegeType.REPL_CLIENT, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("Create_user_priv"), PrivilegeType.CREATE_USER, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("Create_tablespace_priv"), PrivilegeType.CREATE_TABLESPACE, result);
        return result;
    }
    
    private Collection<PrivilegeType> loadDatabaseGlobalPrivileges(final ResultSet resultSet) throws SQLException {
        Collection<PrivilegeType> result = new LinkedList<>();
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("Select_priv"), PrivilegeType.SELECT, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("Insert_priv"), PrivilegeType.INSERT, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("Update_priv"), PrivilegeType.UPDATE, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("Delete_priv"), PrivilegeType.DELETE, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("Create_priv"), PrivilegeType.CREATE, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("Alter_priv"), PrivilegeType.ALTER, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("Drop_priv"), PrivilegeType.DROP, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("Grant_priv"), PrivilegeType.GRANT, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("Index_priv"), PrivilegeType.INDEX, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("References_priv"), PrivilegeType.REFERENCES, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("Create_tmp_table_priv"), PrivilegeType.CREATE_TMP, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("Lock_tables_priv"), PrivilegeType.LOCK_TABLES, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("Execute_priv"), PrivilegeType.EXECUTE, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("Create_view_priv"), PrivilegeType.CREATE_VIEW, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("Show_view_priv"), PrivilegeType.SHOW_VIEW, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("Create_routine_priv"), PrivilegeType.CREATE_PROC, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("Alter_routine_priv"), PrivilegeType.ALTER_PROC, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("Event_priv"), PrivilegeType.EVENT, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("Trigger_priv"), PrivilegeType.TRIGGER, result);
        return result;
    }
    
    private void addToPrivilegeTypesIfPresent(final boolean hasPrivilege, final PrivilegeType privilegeType, final Collection<PrivilegeType> target) {
        if (hasPrivilege) {
            target.add(privilegeType);
        }
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
