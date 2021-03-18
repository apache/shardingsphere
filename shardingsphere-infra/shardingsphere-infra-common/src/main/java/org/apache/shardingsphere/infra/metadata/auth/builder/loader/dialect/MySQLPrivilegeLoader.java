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
            connection.setAutoCommit(true);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM mysql.user WHERE user=? and host=?");
            statement.setString(1, user.getGrantee().getUsername());
            statement.setString(2, user.getGrantee().getHostname());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.first()) {
                boolean selectPrivilege = resultSet.getBoolean("Select_priv");
                if (selectPrivilege) {
                    privilege.getAdministrativePrivilege().getPrivileges().add(PrivilegeType.SELECT);
                    privilege.getDatabasePrivilege().getGlobalPrivileges().add(PrivilegeType.SELECT);
                }
                boolean insertPrivilege = resultSet.getBoolean("Insert_priv");
                if (insertPrivilege) {
                    privilege.getAdministrativePrivilege().getPrivileges().add(PrivilegeType.INSERT);
                    privilege.getDatabasePrivilege().getGlobalPrivileges().add(PrivilegeType.INSERT);
                }
                boolean updatePrivilege = resultSet.getBoolean("Update_priv");
                if (updatePrivilege) {
                    privilege.getAdministrativePrivilege().getPrivileges().add(PrivilegeType.UPDATE);
                    privilege.getDatabasePrivilege().getGlobalPrivileges().add(PrivilegeType.UPDATE);
                }
                boolean deletePrivilege = resultSet.getBoolean("Delete_priv");
                if (deletePrivilege) {
                    privilege.getAdministrativePrivilege().getPrivileges().add(PrivilegeType.DELETE);
                    privilege.getDatabasePrivilege().getGlobalPrivileges().add(PrivilegeType.DELETE);
                }
                boolean createPrivilege = resultSet.getBoolean("Create_priv");
                if (createPrivilege) {
                    privilege.getAdministrativePrivilege().getPrivileges().add(PrivilegeType.CREATE);
                    privilege.getDatabasePrivilege().getGlobalPrivileges().add(PrivilegeType.CREATE);
                }
                boolean dropPrivilege = resultSet.getBoolean("Drop_priv");
                if (dropPrivilege) {
                    privilege.getAdministrativePrivilege().getPrivileges().add(PrivilegeType.DROP);
                    privilege.getDatabasePrivilege().getGlobalPrivileges().add(PrivilegeType.DROP);
                }
                boolean reloadPrivilege = resultSet.getBoolean("Reload_priv");
                if (reloadPrivilege) {
                    privilege.getAdministrativePrivilege().getPrivileges().add(PrivilegeType.RELOAD);
                }
                boolean shutdownPrivilege = resultSet.getBoolean("Shutdown_priv");
                if (shutdownPrivilege) {
                    privilege.getAdministrativePrivilege().getPrivileges().add(PrivilegeType.SHUTDOWN);
                }
                boolean processPrivilege = resultSet.getBoolean("Process_priv");
                if (processPrivilege) {
                    privilege.getAdministrativePrivilege().getPrivileges().add(PrivilegeType.PROCESS);
                }
                boolean filePrivilege = resultSet.getBoolean("File_priv");
                if (filePrivilege) {
                    privilege.getAdministrativePrivilege().getPrivileges().add(PrivilegeType.FILE);
                }
                boolean grantPrivilege = resultSet.getBoolean("Grant_priv");
                if (grantPrivilege) {
                    privilege.getAdministrativePrivilege().getPrivileges().add(PrivilegeType.GRANT);
                    privilege.getDatabasePrivilege().getGlobalPrivileges().add(PrivilegeType.GRANT);
                }
                boolean referencesPrivilege = resultSet.getBoolean("References_priv");
                if (referencesPrivilege) {
                    privilege.getAdministrativePrivilege().getPrivileges().add(PrivilegeType.REFERENCES);
                    privilege.getDatabasePrivilege().getGlobalPrivileges().add(PrivilegeType.REFERENCES);
                }
                boolean indexPrivilege = resultSet.getBoolean("Index_priv");
                if (indexPrivilege) {
                    privilege.getAdministrativePrivilege().getPrivileges().add(PrivilegeType.INDEX);
                    privilege.getDatabasePrivilege().getGlobalPrivileges().add(PrivilegeType.INDEX);
                }
                boolean alterPrivilege = resultSet.getBoolean("Alter_priv");
                if (alterPrivilege) {
                    privilege.getAdministrativePrivilege().getPrivileges().add(PrivilegeType.ALTER);
                    privilege.getDatabasePrivilege().getGlobalPrivileges().add(PrivilegeType.ALTER);
                }
                boolean showDbPrivilege = resultSet.getBoolean("Show_db_priv");
                if (showDbPrivilege) {
                    privilege.getAdministrativePrivilege().getPrivileges().add(PrivilegeType.SHOW_DB);
                }
                boolean superPrivilege = resultSet.getBoolean("Super_priv");
                if (superPrivilege) {
                    privilege.getAdministrativePrivilege().getPrivileges().add(PrivilegeType.SUPER);
                }
                boolean createTmpTablePrivilege = resultSet.getBoolean("Create_tmp_table_priv");
                if (createTmpTablePrivilege) {
                    privilege.getAdministrativePrivilege().getPrivileges().add(PrivilegeType.CREATE_TMP);
                    privilege.getDatabasePrivilege().getGlobalPrivileges().add(PrivilegeType.CREATE_TMP);
                }
                boolean lockTablesPrivilege = resultSet.getBoolean("Lock_tables_priv");
                if (lockTablesPrivilege) {
                    privilege.getAdministrativePrivilege().getPrivileges().add(PrivilegeType.LOCK_TABLES);
                    privilege.getDatabasePrivilege().getGlobalPrivileges().add(PrivilegeType.LOCK_TABLES);
                }
                boolean executePrivilege = resultSet.getBoolean("Execute_priv");
                if (executePrivilege) {
                    privilege.getAdministrativePrivilege().getPrivileges().add(PrivilegeType.EXECUTE);
                    privilege.getDatabasePrivilege().getGlobalPrivileges().add(PrivilegeType.EXECUTE);
                }
                boolean replSlavePrivilege = resultSet.getBoolean("Repl_slave_priv");
                if (replSlavePrivilege) {
                    privilege.getAdministrativePrivilege().getPrivileges().add(PrivilegeType.REPL_SLAVE);
                }
                boolean replClientPrivilege = resultSet.getBoolean("Repl_client_priv");
                if (replClientPrivilege) {
                    privilege.getAdministrativePrivilege().getPrivileges().add(PrivilegeType.REPL_CLIENT);
                }
                boolean createViewPrivilege = resultSet.getBoolean("Create_view_priv");
                if (createViewPrivilege) {
                    privilege.getAdministrativePrivilege().getPrivileges().add(PrivilegeType.CREATE_VIEW);
                    privilege.getDatabasePrivilege().getGlobalPrivileges().add(PrivilegeType.CREATE_VIEW);
                }
                boolean showViewPrivilege = resultSet.getBoolean("Show_view_priv");
                if (showViewPrivilege) {
                    privilege.getAdministrativePrivilege().getPrivileges().add(PrivilegeType.SHOW_VIEW);
                    privilege.getDatabasePrivilege().getGlobalPrivileges().add(PrivilegeType.SHOW_VIEW);
                }
                boolean createRoutinePrivilege = resultSet.getBoolean("Create_routine_priv");
                if (createRoutinePrivilege) {
                    privilege.getAdministrativePrivilege().getPrivileges().add(PrivilegeType.CREATE_PROC);
                    privilege.getDatabasePrivilege().getGlobalPrivileges().add(PrivilegeType.CREATE_PROC);
                }
                boolean alterRoutinePrivilege = resultSet.getBoolean("Alter_routine_priv");
                if (alterRoutinePrivilege) {
                    privilege.getAdministrativePrivilege().getPrivileges().add(PrivilegeType.ALTER_PROC);
                    privilege.getDatabasePrivilege().getGlobalPrivileges().add(PrivilegeType.ALTER_PROC);
                }
                boolean createUserPrivilege = resultSet.getBoolean("Create_user_priv");
                if (createUserPrivilege) {
                    privilege.getAdministrativePrivilege().getPrivileges().add(PrivilegeType.CREATE_USER);
                }
                boolean eventPrivilege = resultSet.getBoolean("Event_priv");
                if (eventPrivilege) {
                    privilege.getAdministrativePrivilege().getPrivileges().add(PrivilegeType.EVENT);
                    privilege.getDatabasePrivilege().getGlobalPrivileges().add(PrivilegeType.EVENT);
                }
                boolean triggerPrivilege = resultSet.getBoolean("Trigger_priv");
                if (triggerPrivilege) {
                    privilege.getAdministrativePrivilege().getPrivileges().add(PrivilegeType.TRIGGER);
                    privilege.getDatabasePrivilege().getGlobalPrivileges().add(PrivilegeType.TRIGGER);
                }
                boolean createTablespacePrivilege = resultSet.getBoolean("Create_tablespace_priv");
                if (createTablespacePrivilege) {
                    privilege.getAdministrativePrivilege().getPrivileges().add(PrivilegeType.CREATE_TABLESPACE);
                }
            }
        }
        
    }
    
    private void fillSchemaPrivilege(final ShardingSpherePrivilege privilege, final DataSource dataSource, final ShardingSphereUser user) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM mysql.db WHERE user=? AND host=?");
            statement.setString(1, user.getGrantee().getUsername());
            statement.setString(2, user.getGrantee().getHostname());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.first()) {
                String schema = resultSet.getString("Db");
                SchemaPrivilege schemaPrivilege = new SchemaPrivilege(schema);
                boolean selectPrivilege = resultSet.getBoolean("Select_priv");
                if (selectPrivilege) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.SELECT);
                }
                boolean insertPrivilege = resultSet.getBoolean("Insert_priv");
                if (insertPrivilege) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.INSERT);
                }
                boolean updatePrivilege = resultSet.getBoolean("Update_priv");
                if (updatePrivilege) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.UPDATE);
                }
                boolean deletePrivilege = resultSet.getBoolean("Delete_priv");
                if (deletePrivilege) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.DELETE);
                }
                boolean createPrivilege = resultSet.getBoolean("Create_priv");
                if (createPrivilege) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.CREATE);
                }
                boolean dropPrivilege = resultSet.getBoolean("Drop_priv");
                if (dropPrivilege) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.DROP);
                }
                boolean grantPrivilege = resultSet.getBoolean("Grant_priv");
                if (grantPrivilege) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.GRANT);
                }
                boolean referencesPrivilege = resultSet.getBoolean("References_priv");
                if (referencesPrivilege) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.REFERENCES);
                }
                boolean indexPrivilege = resultSet.getBoolean("Index_priv");
                if (indexPrivilege) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.INDEX);
                }
                boolean alterPrivilege = resultSet.getBoolean("Alter_priv");
                if (alterPrivilege) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.ALTER);
                }
                boolean createTmpTablePrivilege = resultSet.getBoolean("Create_tmp_table_priv");
                if (createTmpTablePrivilege) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.CREATE_TMP);
                }
                boolean lockTablesPrivilege = resultSet.getBoolean("Lock_tables_priv");
                if (lockTablesPrivilege) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.LOCK_TABLES);
                }
                boolean executePrivilege = resultSet.getBoolean("Execute_priv");
                if (executePrivilege) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.EXECUTE);
                }
                boolean createViewPrivilege = resultSet.getBoolean("Create_view_priv");
                if (createViewPrivilege) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.CREATE_VIEW);
                }
                boolean showViewPrivilege = resultSet.getBoolean("Show_view_priv");
                if (showViewPrivilege) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.SHOW_VIEW);
                }
                boolean createRoutinePrivilege = resultSet.getBoolean("Create_routine_priv");
                if (createRoutinePrivilege) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.CREATE_PROC);
                }
                boolean alterRoutinePrivilege = resultSet.getBoolean("Alter_routine_priv");
                if (alterRoutinePrivilege) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.ALTER_PROC);
                }
                boolean eventPrivilege = resultSet.getBoolean("Event_priv");
                if (eventPrivilege) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.EVENT);
                }
                boolean triggerPrivilege = resultSet.getBoolean("Trigger_priv");
                if (triggerPrivilege) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.TRIGGER);
                }
                privilege.getDatabasePrivilege().getSpecificPrivileges().put(schema, schemaPrivilege);
            }
        }
    }
    
    private void fillTablePrivilege(final ShardingSpherePrivilege privilege, final DataSource dataSource, final ShardingSphereUser user) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
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
            case "Drop":
                return PrivilegeType.DROP;
            case "Grant":
                return PrivilegeType.GRANT;
            case "References":
                return PrivilegeType.REFERENCES;
            case "Index":
                return PrivilegeType.INDEX;
            case "Alter":
                return PrivilegeType.ALTER;
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
