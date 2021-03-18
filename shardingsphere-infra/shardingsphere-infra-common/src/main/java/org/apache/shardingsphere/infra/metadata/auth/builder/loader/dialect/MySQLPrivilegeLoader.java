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
import org.apache.shardingsphere.infra.metadata.auth.model.user.ShardingSphereUser;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * MySQL privilege loader.
 */
public final class MySQLPrivilegeLoader implements PrivilegeLoader {
    
    @Override
    public Optional<ShardingSpherePrivilege> load(final ShardingSphereUser user, final DataSource dataSource) throws SQLException {
        ShardingSpherePrivilege result = new ShardingSpherePrivilege();
        fillGlobalPrivilege(result, dataSource, user);
        fillSchemaPrivilege(result, dataSource, user);
        fillTablePrivilege(result, dataSource, user);
        return Optional.of(result);
    }
    
    private void fillGlobalPrivilege(final ShardingSpherePrivilege privilege, final DataSource dataSource, final ShardingSphereUser user) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM mysql.user WHERE user=? AND host=?");
            preparedStatement.setString(1, user.getGrantee().getUsername());
            preparedStatement.setString(2, user.getGrantee().getHostname());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.first()) {
                    privilege.getAdministrativePrivilege().getPrivileges().addAll(loadAdministrativePrivileges(resultSet));
                    privilege.getDatabasePrivilege().getGlobalPrivileges().addAll(loadDatabaseGlobalPrivileges(resultSet));
                }
            }
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
    
    private void fillSchemaPrivilege(final ShardingSpherePrivilege privilege, final DataSource dataSource, final ShardingSphereUser user) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM mysql.db WHERE user=? AND host=?");
            preparedStatement.setString(1, user.getGrantee().getUsername());
            preparedStatement.setString(2, user.getGrantee().getHostname());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.first()) {
                    String db = resultSet.getString("Db");
                    SchemaPrivilege schemaPrivilege = new SchemaPrivilege(db);
                    schemaPrivilege.getGlobalPrivileges().addAll(loadDatabaseGlobalPrivileges(resultSet));
                    privilege.getDatabasePrivilege().getSpecificPrivileges().put(db, schemaPrivilege);
                }
            }
        }
    }
    
    private void fillTablePrivilege(final ShardingSpherePrivilege privilege, final DataSource dataSource, final ShardingSphereUser user) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT Db, Table_name, Table_priv FROM mysql.tables_priv WHERE user=? AND host=?");
            preparedStatement.setString(1, user.getGrantee().getUsername());
            preparedStatement.setString(2, user.getGrantee().getHostname());
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String db = resultSet.getString("Db");
                    String tableName = resultSet.getString("Table_name");
                    String[] tablePrivileges = (String[]) resultSet.getArray("Table_priv").getArray();
                    TablePrivilege tablePrivilege = new TablePrivilege(tableName, getPrivileges(tablePrivileges));
                    if (!privilege.getDatabasePrivilege().getSpecificPrivileges().containsKey(db)) {
                        privilege.getDatabasePrivilege().getSpecificPrivileges().put(db, new SchemaPrivilege(db));
                    }
                    privilege.getDatabasePrivilege().getSpecificPrivileges().get(db).getSpecificPrivileges().put(tableName, tablePrivilege);
                }
            }
        }
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
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
