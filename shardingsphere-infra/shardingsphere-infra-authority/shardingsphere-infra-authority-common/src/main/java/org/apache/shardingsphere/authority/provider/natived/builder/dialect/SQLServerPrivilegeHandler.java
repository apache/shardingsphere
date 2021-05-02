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
import org.apache.shardingsphere.authority.provider.natived.model.privilege.database.TablePrivileges;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SQLServer privilege handler.
 */
public final class SQLServerPrivilegeHandler implements StoragePrivilegeHandler {
    
    private static final String QUESTION_MARK = "?";
    
    private static final String LINE_BREAK = "\n";
    
    private static final String GO_SQL = "GO";
    
    private static final String CREATE_LOGIN_USER_SQL = "CREATE LOGIN %s WITH PASSWORD = '%s';";
    
    private static final String CREATE_DATABASE_USER_SQL = "CREATE USER %s FOR LOGIN %s;";
    
    private static final String GRANT_ALL_SQL = "GRANT CONTROL ON DATABASE::%s TO %s";
    
    private static final String GLOBAL_PRIVILEGE_SQL =
            "SELECT pr.name AS GRANTEE, pe.state_desc AS STATE, pe.permission_name AS PRIVILEGE_TYPE"
                    + "FROM sys.server_principals AS pr JOIN sys.server_permissions AS pe"
                    + "ON pe.grantee_principal_id = pr.principal_id WHERE pr.name IN (%s) GROUP BY pr.name, pe.state_desc, pe.permission_name";
    
    private static final String SCHEMA_PRIVILEGE_SQL =
            "SELECT pr.name AS GRANTEE, pe.state_desc AS STATE, pe.permission_name AS PRIVILEGE_TYPE, o.name AS DB"
                    + "FROM sys.database_principals AS pr JOIN sys.database_permissions AS pe"
                    + "ON pe.grantee_principal_id = pr.principal_id JOIN sys.objects AS o"
                    + "ON pe.major_id = o.object_id WHERE pr.name IN (%s) GROUP BY pr.name, pe.state_desc, pe.permission_name, o.name";
    
    private static final String TABLE_PRIVILEGE_SQL =
            "SELECT GRANTOR, GRANTEE, TABLE_CATALOG, TABLE_SCHEMA, TABLE_NAME, PRIVILEGE_TYPE, IS_GRANTABLE from INFORMATION_SCHEMA.TABLE_PRIVILEGES WHERE GRANTEE IN (%s)";
    
    @Override
    public Collection<ShardingSphereUser> diff(final Collection<ShardingSphereUser> users, final DataSource dataSource) throws SQLException {
        Collection<Grantee> grantees = new LinkedList<>();
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            try (ResultSet resultSet = statement.executeQuery(getGlobalPrivilegesSQL(users))) {
                while (resultSet.next()) {
                    grantees.add(new Grantee(resultSet.getString("GRANTEE"), ""));
                }
            }
        }
        return users.stream().filter(each -> !grantees.contains(each.getGrantee())).collect(Collectors.toList());
    }
    
    @Override
    public void create(final Collection<ShardingSphereUser> users, final DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            for (ShardingSphereUser each : users) {
                statement.execute(getCreateUsersSQL(each));
            }
        }
    }
    
    private String getCreateUsersSQL(final ShardingSphereUser user) {
        StringBuilder result = new StringBuilder();
        result.append(String.format(CREATE_LOGIN_USER_SQL, user.getGrantee().getUsername(), user.getPassword())).append(LINE_BREAK);
        result.append(GO_SQL).append(LINE_BREAK);
        result.append(String.format(CREATE_DATABASE_USER_SQL, user.getGrantee().getUsername(), user.getGrantee().getUsername())).append(LINE_BREAK);
        result.append(GO_SQL);
        return result.toString();
    }
    
    @Override
    public void grantAll(final Collection<ShardingSphereUser> users, final DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            String databaseName = getDatabaseName(connection.getMetaData().getURL());
            for (ShardingSphereUser each : users) {
                statement.execute(getGrantAllSQL(databaseName, each));
            }
        }
    }
    
    private String getDatabaseName(final String url) {
        if (url.contains(QUESTION_MARK)) {
            return url.substring(url.indexOf("DatabaseName=") + 1, url.indexOf("?"));
        }
        return url.substring(url.indexOf("DatabaseName=") + 1);
    }
    
    private String getGrantAllSQL(final String databaseName, final ShardingSphereUser user) {
        return String.format(GRANT_ALL_SQL, databaseName, user.getGrantee().getUsername());
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
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            try (ResultSet resultSet = statement.executeQuery(getGlobalPrivilegesSQL(users))) {
                while (resultSet.next()) {
                    fillGlobalPrivileges(userPrivilegeMap, resultSet);
                }
            }
        }
    }
    
    private void fillGlobalPrivileges(final Map<ShardingSphereUser, NativePrivileges> userPrivilegeMap, final ResultSet resultSet) throws SQLException {
        Optional<ShardingSphereUser> user = findShardingSphereUser(userPrivilegeMap, resultSet);
        if (user.isPresent()) {
            userPrivilegeMap.get(user.get()).getAdministrativePrivileges().getPrivileges().addAll(loadPrivileges(resultSet));
        }
    }
    
    private void fillSchemaPrivileges(final Map<ShardingSphereUser, NativePrivileges> userPrivilegeMap, final DataSource dataSource, final Collection<ShardingSphereUser> users) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            try (ResultSet resultSet = statement.executeQuery(getSchemaPrivilegesSQL(users))) {
                while (resultSet.next()) {
                    fillSchemaPrivileges(userPrivilegeMap, resultSet);
                }
            }
        }
    }
    
    private void fillSchemaPrivileges(final Map<ShardingSphereUser, NativePrivileges> userPrivilegeMap, final ResultSet resultSet) throws SQLException {
        Optional<ShardingSphereUser> user = findShardingSphereUser(userPrivilegeMap, resultSet);
        if (user.isPresent()) {
            String db = resultSet.getString("DB");
            SchemaPrivileges schemaPrivileges = new SchemaPrivileges(db);
            schemaPrivileges.getGlobalPrivileges().addAll(loadPrivileges(resultSet));
            userPrivilegeMap.get(user.get()).getDatabasePrivileges().getSpecificPrivileges().put(db, schemaPrivileges);
        }
    }
    
    private void fillTablePrivileges(final Map<ShardingSphereUser, NativePrivileges> userPrivilegeMap, final DataSource dataSource, final Collection<ShardingSphereUser> users) throws SQLException {
        Map<ShardingSphereUser, Map<String, Map<String, List<PrivilegeType>>>> privilegeCache = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            try (ResultSet resultSet = statement.executeQuery(getTablePrivilegesSQL(users))) {
                while (resultSet.next()) {
                    collectPrivileges(privilegeCache, resultSet);
                }
            }
        }
        fillTablePrivileges(privilegeCache, userPrivilegeMap);
    }
    
    private void fillTablePrivileges(final Map<ShardingSphereUser, Map<String, Map<String, List<PrivilegeType>>>> privilegeCache, final Map<ShardingSphereUser, NativePrivileges> userPrivilegeMap) {
        for (Map.Entry<ShardingSphereUser, Map<String, Map<String, List<PrivilegeType>>>> entry : privilegeCache.entrySet()) {
            for (String db : entry.getValue().keySet()) {
                for (String tableName : entry.getValue().get(db).keySet()) {
                    TablePrivileges tablePrivileges = new TablePrivileges(tableName, entry.getValue().get(db).get(tableName));
                    NativePrivileges privileges = userPrivilegeMap.get(entry.getKey());
                    if (!privileges.getDatabasePrivileges().getSpecificPrivileges().containsKey(db)) {
                        privileges.getDatabasePrivileges().getSpecificPrivileges().put(db, new SchemaPrivileges(db));
                    }
                    privileges.getDatabasePrivileges().getSpecificPrivileges().get(db).getSpecificPrivileges().put(tableName, tablePrivileges);
                }
            }
        }
    }
    
    private void collectPrivileges(final Map<ShardingSphereUser, Map<String, Map<String, List<PrivilegeType>>>> privilegeCache, final ResultSet resultSet) throws SQLException {
        String db = resultSet.getString("TABLE_CATALOG");
        String tableName = resultSet.getString("TABLE_NAME");
        String privilegeType = resultSet.getString("PRIVILEGE_TYPE");
        boolean hasPrivilege = "YES".equalsIgnoreCase(resultSet.getString("IS_GRANTABLE"));
        String grantee = resultSet.getString("GRANTEE");
        if (hasPrivilege) {
            privilegeCache
                    .computeIfAbsent(new ShardingSphereUser(grantee, "", ""), k -> new HashMap<>())
                    .computeIfAbsent(db, k -> new HashMap<>())
                    .computeIfAbsent(tableName, k -> new ArrayList<>())
                    .add(getPrivilegeType(privilegeType));
        }
    }
    
    private Optional<ShardingSphereUser> findShardingSphereUser(final Map<ShardingSphereUser, NativePrivileges> privileges, final ResultSet resultSet) throws SQLException {
        Grantee grantee = new Grantee(resultSet.getString("GRANTEE"), "");
        return privileges.keySet().stream().filter(each -> each.getGrantee().equals(grantee)).findFirst();
    }
    
    private String getGlobalPrivilegesSQL(final Collection<ShardingSphereUser> users) {
        String userList = users.stream().map(each -> String.format("'%s'", each.getGrantee().getUsername())).collect(Collectors.joining(", "));
        return String.format(GLOBAL_PRIVILEGE_SQL, userList);
    }
    
    private String getSchemaPrivilegesSQL(final Collection<ShardingSphereUser> users) {
        String userList = users.stream().map(each -> String.format("'%s'", each.getGrantee().getUsername())).collect(Collectors.joining(", "));
        return String.format(SCHEMA_PRIVILEGE_SQL, userList);
    }
    
    private String getTablePrivilegesSQL(final Collection<ShardingSphereUser> users) {
        String userList = users.stream().map(each -> String.format("'%s'", each.getGrantee().getUsername())).collect(Collectors.joining(", "));
        return String.format(TABLE_PRIVILEGE_SQL, userList);
    }
    
    private PrivilegeType getPrivilegeType(final String privilege) {
        switch (privilege) {
            case "SELECT":
                return PrivilegeType.SELECT;
            case "INSERT":
                return PrivilegeType.INSERT;
            case "UPDATE":
                return PrivilegeType.UPDATE;
            case "DELETE":
                return PrivilegeType.DELETE;
            case "CREATE FUNCTION":
                return PrivilegeType.CREATE_FUNCTION;
            case "REFERENCES":
                return PrivilegeType.REFERENCES;
            case "CREATE TABLE":
                return PrivilegeType.CREATE_TABLE;
            case "CREATE DATABASE":
                return PrivilegeType.CREATE_DATABASE;
            case "EXECUTE":
                return PrivilegeType.EXECUTE;
            case "CREATE VIEW":
                return PrivilegeType.CREATE_VIEW;
            case "CREATE PROCEDURE":
                return PrivilegeType.CREATE_PROC;
            case "BACKUP DATABASE":
                return PrivilegeType.BACKUP_DATABASE;
            case "CREATE DEFAULT":
                return PrivilegeType.CREATE_DEFAULT;
            case "BACKUP LOG":
                return PrivilegeType.BACKUP_LOG;
            case "CREATE RULE":
                return PrivilegeType.CREATE_RULE;
            case "CONNECT":
                return PrivilegeType.CONNECT;
            case "ADMINISTER BULK OPERATIONS":
                return PrivilegeType.ADMINISTER_BULK_OPERATIONS;
            case "ALTER ANY AVAILABILITY GROUP":
                return PrivilegeType.ALTER_ANY_AVAILABILITY_GROUP;
            case "ALTER ANY CONNECTION":
                return PrivilegeType.ALTER_ANY_CONNECTION;
            case "ALTER ANY CREDENTIAL":
                return PrivilegeType.ALTER_ANY_CREDENTIAL;
            case "ALTER ANY DATABASE":
                return PrivilegeType.ALTER_ANY_DATABASE;
            case "ALTER ANY ENDPOINT":
                return PrivilegeType.ALTER_ANY_ENDPOINT;
            case "ALTER ANY EVENT SESSION":
                return PrivilegeType.ALTER_ANY_EVENT_SESSION;
            case "ALTER ANY EVENT NOTIFICATION":
                return PrivilegeType.ALTER_ANY_EVENT_NOTIFICATION;
            case "ALTER ANY LINKED SERVER":
                return PrivilegeType.ALTER_ANY_LINKED_SERVER;
            case "ALTER ANY LOGIN":
                return PrivilegeType.ALTER_ANY_LOGIN;
            case "ALTER ANY SERVER AUDIT":
                return PrivilegeType.ALTER_ANY_SERVER_AUDIT;
            case "ALTER ANY SERVER ROLE":
                return PrivilegeType.ALTER_ANY_SERVER_ROLE;
            case "ALTER RESOURCES":
                return PrivilegeType.ALTER_RESOURCES;
            case "ALTER SERVER STATE":
                return PrivilegeType.ALTER_SERVER_STATE;
            case "ALTER SETTINGS":
                return PrivilegeType.ALTER_SETTINGS;
            case "ALTER TRACE":
                return PrivilegeType.ALTER_TRACE;
            case "AUTHENTICATE SERVER":
                return PrivilegeType.AUTHENTICATE_SERVER;
            case "CONNECT ANY DATABASE":
                return PrivilegeType.CONNECT_ANY_DATABASE;
            case "CONNECT SQL":
                return PrivilegeType.CONNECT_SQL;
            case "CONTROL SERVER":
                return PrivilegeType.CONTROL_SERVER;
            case "CREATE ANY DATABASE":
                return PrivilegeType.CREATE_ANY_DATABASE;
            case "CREATE AVAILABILITY GROUP":
                return PrivilegeType.CREATE_AVAILABILITY_GROUP;
            case "CREATE DDL EVENT NOTIFICATION":
                return PrivilegeType.CREATE_DDL_EVENT_NOTIFICATION;
            case "CREATE ENDPOINT":
                return PrivilegeType.CREATE_ENDPOINT;
            case "CREATE SERVER ROLE":
                return PrivilegeType.CREATE_SERVER_ROLE;
            case "CREATE TRACE EVENT NOTIFICATION ":
                return PrivilegeType.CREATE_TRACE_EVENT_NOTIFICATION;
            case "EXTERNAL ACCESS ASSEMBLY":
                return PrivilegeType.EXTERNAL_ACCESS_ASSEMBLY;
            case "IMPERSONATE ANY LOGIN":
                return PrivilegeType.IMPERSONATE_ANY_LOGIN;
            case "SELECT ALL USER SECURABLES":
                return PrivilegeType.SELECT_ALL_USER_SECURABLES;
            case "SHUTDOWN":
                return PrivilegeType.SHUTDOWN;
            case "UNSAFE ASSEMBLY":
                return PrivilegeType.UNSAFE_ASSEMBLY;
            case "VIEW ANY DATABASE":
                return PrivilegeType.VIEW_ANY_DATABASE;
            case "VIEW ANY DEFINITION":
                return PrivilegeType.VIEW_ANY_DEFINITION;
            case "VIEW SERVER STATE ":
                return PrivilegeType.VIEW_SERVER_STATE;
            default:
                throw new UnsupportedOperationException(privilege);
        }
    }
    
    private Collection<PrivilegeType> loadPrivileges(final ResultSet resultSet) throws SQLException {
        Collection<PrivilegeType> result = new LinkedList<>();
        if ("GRANT".equals(resultSet.getString("STATE"))) {
            result.add(getPrivilegeType(resultSet.getString("PRIVILEGE_TYPE")));
        }
        return result;
    }
    
    @Override
    public String getType() {
        return "SQLServer";
    }
}
