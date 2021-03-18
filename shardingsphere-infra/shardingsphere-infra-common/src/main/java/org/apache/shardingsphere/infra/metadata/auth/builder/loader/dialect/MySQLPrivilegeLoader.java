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
import org.apache.shardingsphere.infra.metadata.auth.model.privilege.data.SchemaPrivilege;
import org.apache.shardingsphere.infra.metadata.auth.model.privilege.data.TablePrivilege;
import org.apache.shardingsphere.infra.metadata.auth.model.user.ShardingSphereUser;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

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
                boolean selectPriv = resultSet.getBoolean("Select_priv");
                if (selectPriv) {
                    privilege.getAdministrationPrivilege().getPrivileges().add(PrivilegeType.SELECT);
                    privilege.getDataPrivilege().getGlobalPrivileges().add(PrivilegeType.SELECT);
                }
                boolean insertPriv = resultSet.getBoolean("Insert_priv");
                if (insertPriv) {
                    privilege.getAdministrationPrivilege().getPrivileges().add(PrivilegeType.INSERT);
                    privilege.getDataPrivilege().getGlobalPrivileges().add(PrivilegeType.INSERT);
                }
                boolean updatePriv = resultSet.getBoolean("Update_priv");
                if (updatePriv) {
                    privilege.getAdministrationPrivilege().getPrivileges().add(PrivilegeType.UPDATE);
                    privilege.getDataPrivilege().getGlobalPrivileges().add(PrivilegeType.UPDATE);
                }
                boolean deletePriv = resultSet.getBoolean("Delete_priv");
                if (deletePriv) {
                    privilege.getAdministrationPrivilege().getPrivileges().add(PrivilegeType.DELETE);
                    privilege.getDataPrivilege().getGlobalPrivileges().add(PrivilegeType.DELETE);
                }
                boolean createPriv = resultSet.getBoolean("Create_priv");
                if (createPriv) {
                    privilege.getAdministrationPrivilege().getPrivileges().add(PrivilegeType.CREATE);
                    privilege.getDataPrivilege().getGlobalPrivileges().add(PrivilegeType.CREATE);
                }
                boolean dropPriv = resultSet.getBoolean("Drop_priv");
                if (dropPriv) {
                    privilege.getAdministrationPrivilege().getPrivileges().add(PrivilegeType.DROP);
                    privilege.getDataPrivilege().getGlobalPrivileges().add(PrivilegeType.DROP);
                }
                boolean reloadPriv = resultSet.getBoolean("Reload_priv");
                if (reloadPriv) {
                    privilege.getAdministrationPrivilege().getPrivileges().add(PrivilegeType.RELOAD);
                }
                boolean shutdownPriv = resultSet.getBoolean("Shutdown_priv");
                if (shutdownPriv) {
                    privilege.getAdministrationPrivilege().getPrivileges().add(PrivilegeType.SHUTDOWN);
                }
                boolean processPriv = resultSet.getBoolean("Process_priv");
                if (processPriv) {
                    privilege.getAdministrationPrivilege().getPrivileges().add(PrivilegeType.PROCESS);
                }
                boolean filePriv = resultSet.getBoolean("File_priv");
                if (filePriv) {
                    privilege.getAdministrationPrivilege().getPrivileges().add(PrivilegeType.FILE);
                }
                boolean grantPriv = resultSet.getBoolean("Grant_priv");
                if (grantPriv) {
                    privilege.getAdministrationPrivilege().getPrivileges().add(PrivilegeType.GRANT);
                    privilege.getDataPrivilege().getGlobalPrivileges().add(PrivilegeType.GRANT);
                }
                boolean referencesPriv = resultSet.getBoolean("References_priv");
                if (referencesPriv) {
                    privilege.getAdministrationPrivilege().getPrivileges().add(PrivilegeType.REFERENCES);
                    privilege.getDataPrivilege().getGlobalPrivileges().add(PrivilegeType.REFERENCES);
                }
                boolean indexPriv = resultSet.getBoolean("Index_priv");
                if (indexPriv) {
                    privilege.getAdministrationPrivilege().getPrivileges().add(PrivilegeType.INDEX);
                    privilege.getDataPrivilege().getGlobalPrivileges().add(PrivilegeType.INDEX);
                }
                boolean alterPriv = resultSet.getBoolean("Alter_priv");
                if (alterPriv) {
                    privilege.getAdministrationPrivilege().getPrivileges().add(PrivilegeType.ALTER);
                    privilege.getDataPrivilege().getGlobalPrivileges().add(PrivilegeType.ALTER);
                }
                boolean showDbPriv = resultSet.getBoolean("Show_db_priv");
                if (showDbPriv) {
                    privilege.getAdministrationPrivilege().getPrivileges().add(PrivilegeType.SHOW_DB);
                }
                boolean superPriv = resultSet.getBoolean("Super_priv");
                if (superPriv) {
                    privilege.getAdministrationPrivilege().getPrivileges().add(PrivilegeType.SUPER);
                }
                boolean createTmpTablePriv = resultSet.getBoolean("Create_tmp_table_priv");
                if (createTmpTablePriv) {
                    privilege.getAdministrationPrivilege().getPrivileges().add(PrivilegeType.CREATE_TMP);
                    privilege.getDataPrivilege().getGlobalPrivileges().add(PrivilegeType.CREATE_TMP);
                }
                boolean lockTablesPriv = resultSet.getBoolean("Lock_tables_priv");
                if (lockTablesPriv) {
                    privilege.getAdministrationPrivilege().getPrivileges().add(PrivilegeType.LOCK_TABLES);
                    privilege.getDataPrivilege().getGlobalPrivileges().add(PrivilegeType.LOCK_TABLES);
                }
                boolean executePriv = resultSet.getBoolean("Execute_priv");
                if (executePriv) {
                    privilege.getAdministrationPrivilege().getPrivileges().add(PrivilegeType.EXECUTE);
                    privilege.getDataPrivilege().getGlobalPrivileges().add(PrivilegeType.EXECUTE);
                }
                boolean replSlavePriv = resultSet.getBoolean("Repl_slave_priv");
                if (replSlavePriv) {
                    privilege.getAdministrationPrivilege().getPrivileges().add(PrivilegeType.REPL_SLAVE);
                }
                boolean replClientPriv = resultSet.getBoolean("Repl_client_priv");
                if (replClientPriv) {
                    privilege.getAdministrationPrivilege().getPrivileges().add(PrivilegeType.REPL_CLIENT);
                }
                boolean createViewPriv = resultSet.getBoolean("Create_view_priv");
                if (createViewPriv) {
                    privilege.getAdministrationPrivilege().getPrivileges().add(PrivilegeType.CREATE_VIEW);
                    privilege.getDataPrivilege().getGlobalPrivileges().add(PrivilegeType.CREATE_VIEW);
                }
                boolean showViewPriv = resultSet.getBoolean("Show_view_priv");
                if (showViewPriv) {
                    privilege.getAdministrationPrivilege().getPrivileges().add(PrivilegeType.SHOW_VIEW);
                    privilege.getDataPrivilege().getGlobalPrivileges().add(PrivilegeType.SHOW_VIEW);
                }
                boolean createRoutinePriv = resultSet.getBoolean("Create_routine_priv");
                if (createRoutinePriv) {
                    privilege.getAdministrationPrivilege().getPrivileges().add(PrivilegeType.CREATE_PROC);
                    privilege.getDataPrivilege().getGlobalPrivileges().add(PrivilegeType.CREATE_PROC);
                }
                boolean alterRoutinePriv = resultSet.getBoolean("Alter_routine_priv");
                if (alterRoutinePriv) {
                    privilege.getAdministrationPrivilege().getPrivileges().add(PrivilegeType.ALTER_PROC);
                    privilege.getDataPrivilege().getGlobalPrivileges().add(PrivilegeType.ALTER_PROC);
                }
                boolean createUserPriv = resultSet.getBoolean("Create_user_priv");
                if (createUserPriv) {
                    privilege.getAdministrationPrivilege().getPrivileges().add(PrivilegeType.CREATE_USER);
                }
                boolean eventPriv = resultSet.getBoolean("Event_priv");
                if (eventPriv) {
                    privilege.getAdministrationPrivilege().getPrivileges().add(PrivilegeType.EVENT);
                    privilege.getDataPrivilege().getGlobalPrivileges().add(PrivilegeType.EVENT);
                }
                boolean triggerPriv = resultSet.getBoolean("Trigger_priv");
                if (triggerPriv) {
                    privilege.getAdministrationPrivilege().getPrivileges().add(PrivilegeType.TRIGGER);
                    privilege.getDataPrivilege().getGlobalPrivileges().add(PrivilegeType.TRIGGER);
                }
                boolean createTablespacePriv = resultSet.getBoolean("Create_tablespace_priv");
                if (createTablespacePriv) {
                    privilege.getAdministrationPrivilege().getPrivileges().add(PrivilegeType.CREATE_TABLESPACE);
                }
            }
        }
        
    }
    
    private void fillSchemaPrivilege(final ShardingSpherePrivilege privilege, final DataSource dataSource, final ShardingSphereUser user) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            PreparedStatement statement = connection.prepareStatement("select * from mysql.db where user=? and host=?");
            statement.setString(1, user.getGrantee().getUsername());
            statement.setString(2, user.getGrantee().getHostname());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.first()) {
                String schema = resultSet.getString("Db");
                SchemaPrivilege schemaPrivilege = new SchemaPrivilege(schema);
                boolean selectPriv = resultSet.getBoolean("Select_priv");
                if (selectPriv) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.SELECT);
                }
                boolean insertPriv = resultSet.getBoolean("Insert_priv");
                if (insertPriv) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.INSERT);
                }
                boolean updatePriv = resultSet.getBoolean("Update_priv");
                if (updatePriv) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.UPDATE);
                }
                boolean deletePriv = resultSet.getBoolean("Delete_priv");
                if (deletePriv) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.DELETE);
                }
                boolean createPriv = resultSet.getBoolean("Create_priv");
                if (createPriv) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.CREATE);
                }
                boolean dropPriv = resultSet.getBoolean("Drop_priv");
                if (dropPriv) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.DROP);
                }
                boolean grantPriv = resultSet.getBoolean("Grant_priv");
                if (grantPriv) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.GRANT);
                }
                boolean referencesPriv = resultSet.getBoolean("References_priv");
                if (referencesPriv) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.REFERENCES);
                }
                boolean indexPriv = resultSet.getBoolean("Index_priv");
                if (indexPriv) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.INDEX);
                }
                boolean alterPriv = resultSet.getBoolean("Alter_priv");
                if (alterPriv) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.ALTER);
                }
                boolean createTmpTablePriv = resultSet.getBoolean("Create_tmp_table_priv");
                if (createTmpTablePriv) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.CREATE_TMP);
                }
                boolean lockTablesPriv = resultSet.getBoolean("Lock_tables_priv");
                if (lockTablesPriv) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.LOCK_TABLES);
                }
                boolean executePriv = resultSet.getBoolean("Execute_priv");
                if (executePriv) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.EXECUTE);
                }
                boolean createViewPriv = resultSet.getBoolean("Create_view_priv");
                if (createViewPriv) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.CREATE_VIEW);
                }
                boolean showViewPriv = resultSet.getBoolean("Show_view_priv");
                if (showViewPriv) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.SHOW_VIEW);
                }
                boolean createRoutinePriv = resultSet.getBoolean("Create_routine_priv");
                if (createRoutinePriv) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.CREATE_PROC);
                }
                boolean alterRoutinePriv = resultSet.getBoolean("Alter_routine_priv");
                if (alterRoutinePriv) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.ALTER_PROC);
                }
                boolean eventPriv = resultSet.getBoolean("Event_priv");
                if (eventPriv) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.EVENT);
                }
                boolean triggerPriv = resultSet.getBoolean("Trigger_priv");
                if (triggerPriv) {
                    schemaPrivilege.getGlobalPrivileges().add(PrivilegeType.TRIGGER);
                }
                privilege.getDataPrivilege().getSpecificPrivileges().put(schema, schemaPrivilege);
            }
        }
    }
    
    private void fillTablePrivilege(final ShardingSpherePrivilege privilege, final DataSource dataSource, final ShardingSphereUser user) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            PreparedStatement statement = connection.prepareStatement("select * from mysql.tables_priv where user=? and host=?");
            statement.setString(1, user.getGrantee().getUsername());
            statement.setString(2, user.getGrantee().getHostname());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String schema = resultSet.getString("Db");
                String tableName = resultSet.getString("Table_name");
                TablePrivilege tablePrivilege = new TablePrivilege(tableName);
                String[] privs = (String[]) resultSet.getArray("Table_priv").getArray();
                for (String each : privs) {
                    switch (each) {
                        case "Select":
                            tablePrivilege.getPrivileges().add(PrivilegeType.SELECT);
                            break;
                        case "Insert":
                            tablePrivilege.getPrivileges().add(PrivilegeType.INSERT);
                            break;
                        case "Update":
                            tablePrivilege.getPrivileges().add(PrivilegeType.UPDATE);
                            break;
                        case "Delete":
                            tablePrivilege.getPrivileges().add(PrivilegeType.DELETE);
                            break;
                        case "Create":
                            tablePrivilege.getPrivileges().add(PrivilegeType.CREATE);
                            break;
                        case "Drop":
                            tablePrivilege.getPrivileges().add(PrivilegeType.DROP);
                            break;
                        case "Grant":
                            tablePrivilege.getPrivileges().add(PrivilegeType.GRANT);
                            break;
                        case "References":
                            tablePrivilege.getPrivileges().add(PrivilegeType.REFERENCES);
                            break;
                        case "Index":
                            tablePrivilege.getPrivileges().add(PrivilegeType.INDEX);
                            break;
                        case "Alter":
                            tablePrivilege.getPrivileges().add(PrivilegeType.ALTER);
                            break;
                        case "Create View":
                            tablePrivilege.getPrivileges().add(PrivilegeType.CREATE_VIEW);
                            break;
                        case "Show view":
                            tablePrivilege.getPrivileges().add(PrivilegeType.SHOW_VIEW);
                            break;
                        case "Trigger":
                            tablePrivilege.getPrivileges().add(PrivilegeType.TRIGGER);
                            break;
                        default:
                            break;
                    }
                }
                if (privilege.getDataPrivilege().getSpecificPrivileges().containsKey(schema)) {
                    privilege.getDataPrivilege().getSpecificPrivileges().get(schema).getSpecificPrivileges().put(tableName, tablePrivilege);
                } else {
                    SchemaPrivilege schemaPrivilege = new SchemaPrivilege(schema);
                    schemaPrivilege.getSpecificPrivileges().put(tableName, tablePrivilege);
                    privilege.getDataPrivilege().getSpecificPrivileges().put(schema, schemaPrivilege);
                }
            }
        }
    }
    
    @Override
    public String getDatabaseType() {
        return "MySQL";
    }
}
