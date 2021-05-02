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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Oracle privilege handler.
 */
public final class OraclePrivilegeHandler implements StoragePrivilegeHandler {

    private static final String CREATE_USER_SQL = "CREATE USER %s IDENTIFIED BY %s";

    private static final String GRANT_ALL_SQL = "GRANT ALL PRIVILEGES TO %s";
    
    private static final String SYS_PRIVILEGE_SQL = "SELECT * FROM DBA_SYS_PRIVS WHERE GRANTEE IN (%s)";
    
    private static final String TABLE_PRIVILEGE_SQL = "SELECT GRANTEE, TABLE_SCHEMA, TABLE_NAME, PRIVILEGE, GRANTABLE, INHERITED FROM ALL_TAB_PRIVS WHERE GRANTEE IN (%s)";
    
    @Override
    public Collection<ShardingSphereUser> diff(final Collection<ShardingSphereUser> users, final DataSource dataSource) throws SQLException {
        Collection<Grantee> grantees = new LinkedList<>();
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            try (ResultSet resultSet = statement.executeQuery(getSysPrivilegesSQL(users))) {
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
                statement.execute(String.format(CREATE_USER_SQL, each.getGrantee().getUsername(), each.getPassword()));
            }
        }
    }
    
    @Override
    public void grantAll(final Collection<ShardingSphereUser> users, final DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            for (ShardingSphereUser each : users) {
                statement.execute(String.format(GRANT_ALL_SQL, each.getGrantee().getUsername()));
            }
        }
    }
    
    @Override
    public Map<ShardingSphereUser, NativePrivileges> load(final Collection<ShardingSphereUser> users, final DataSource dataSource) throws SQLException {
        Map<ShardingSphereUser, NativePrivileges> result = new LinkedHashMap<>();
        users.forEach(user -> result.put(user, new NativePrivileges()));
        fillSysPrivileges(result, dataSource, users);
        fillTablePrivileges(result, dataSource, users);
        return result;
    }
    
    private void fillTablePrivileges(final Map<ShardingSphereUser, NativePrivileges> userPrivilegeMap, final DataSource dataSource,
                                     final Collection<ShardingSphereUser> users) throws SQLException {
        Map<ShardingSphereUser, Map<String, Map<String, List<PrivilegeType>>>> privilegeCache = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            try (ResultSet resultSet = statement.executeQuery(getTablePrivilegesSQL(users))) {
                while (resultSet.next()) {
                    collectTablePrivileges(privilegeCache, resultSet);
                }
            }
        }
        fillTablePrivileges(privilegeCache, userPrivilegeMap);
    }
    
    private void fillTablePrivileges(final Map<ShardingSphereUser, Map<String, Map<String, List<PrivilegeType>>>> privilegeCache, 
                                     final Map<ShardingSphereUser, NativePrivileges> userPrivilegeMap) {
        for (Entry<ShardingSphereUser, Map<String, Map<String, List<PrivilegeType>>>> entry : privilegeCache.entrySet()) {
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
    
    private void collectTablePrivileges(final Map<ShardingSphereUser, Map<String, Map<String, List<PrivilegeType>>>> privilegeCache, final ResultSet resultSet) throws SQLException {
        String db = resultSet.getString("TABLE_SCHEMA");
        String tableName = resultSet.getString("TABLE_NAME");
        String privilegeType = resultSet.getString("PRIVILEGE");
        boolean hasPrivilege = "YES".equalsIgnoreCase(resultSet.getString("GRANTABLE"));
        String grantee = resultSet.getString("GRANTEE");
        if (hasPrivilege) {
            privilegeCache
                    .computeIfAbsent(new ShardingSphereUser(grantee, "", ""), k -> new HashMap<>())
                    .computeIfAbsent(db, k -> new HashMap<>())
                    .computeIfAbsent(tableName, k -> new ArrayList<>())
                    .add(getPrivilegeType(privilegeType));
        }
    }
    
    private void fillSysPrivileges(final Map<ShardingSphereUser, NativePrivileges> userPrivilegeMap, final DataSource dataSource,
                                   final Collection<ShardingSphereUser> users) throws SQLException {
        Map<ShardingSphereUser, List<PrivilegeType>> privilegeCache = new HashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            try (ResultSet resultSet = statement.executeQuery(getSysPrivilegesSQL(users))) {
                while (resultSet.next()) {
                    collectSysPrivileges(privilegeCache, resultSet);
                }
            }
        }
        fillSysPrivileges(privilegeCache, userPrivilegeMap);
    }
    
    private void fillSysPrivileges(final Map<ShardingSphereUser, List<PrivilegeType>> privilegeCache, final Map<ShardingSphereUser, NativePrivileges> userPrivilegeMap) throws SQLException {
        for (Entry<ShardingSphereUser, List<PrivilegeType>> entry : privilegeCache.entrySet()) {
            userPrivilegeMap.get(entry.getKey()).getAdministrativePrivileges().getPrivileges().addAll(entry.getValue());
        }
    }
    
    private void collectSysPrivileges(final Map<ShardingSphereUser, List<PrivilegeType>> privilegeCache, final ResultSet resultSet) throws SQLException {
        String privilegeType = resultSet.getString("PRIVILEGE");
        String grantee = resultSet.getString("GRANTEE");
        privilegeCache
                .computeIfAbsent(new ShardingSphereUser(grantee, "", ""), k -> new ArrayList<>())
                .add(getPrivilegeType(privilegeType));
    }
    
    private Optional<ShardingSphereUser> findShardingSphereUser(final Map<ShardingSphereUser, NativePrivileges> userPrivilegeMap, final ResultSet resultSet) throws SQLException {
        Grantee grantee = new Grantee(resultSet.getString("rolname"), "");
        return userPrivilegeMap.keySet().stream().filter(each -> each.getGrantee().equals(grantee)).findFirst();
    }
    
    private String getSysPrivilegesSQL(final Collection<ShardingSphereUser> users) {
        String userList = users.stream().map(each -> String.format("'%s'", each.getGrantee().getUsername())).collect(Collectors.joining(", "));
        return String.format(SYS_PRIVILEGE_SQL, userList);
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
            case "REFERENCES":
                return PrivilegeType.REFERENCES;
            case "INDEX":
                return PrivilegeType.INDEX;
            case "EXECUTE":
                return PrivilegeType.EXECUTE;
            case "CREATE PROCEDURE":
                return PrivilegeType.CREATE_PROC;
            case "CREATE ROLE":
                return PrivilegeType.CREATE_ROLE;
            case "CREATE SEQUENCE":
                return PrivilegeType.CREATE_SEQUENCE;
            case "CREATE TABLESPACE":
                return PrivilegeType.CREATE_TABLESPACE;
            case "CREATE USER":
                return PrivilegeType.CREATE_USER;
            case "CREATE VIEW":
                return PrivilegeType.CREATE_VIEW;
            case "SYSDBA":
                return PrivilegeType.SUPER;
            // TODO other privilege
            default:
                throw new UnsupportedOperationException(privilege);
        }
    }
    
    @Override
    public String getType() {
        return "Oracle";
    }
}
