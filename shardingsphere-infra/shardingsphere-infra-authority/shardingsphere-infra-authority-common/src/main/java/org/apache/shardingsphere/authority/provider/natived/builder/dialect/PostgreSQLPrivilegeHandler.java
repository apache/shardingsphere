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
 * PostgreSQL privilege handler.
 */
public final class PostgreSQLPrivilegeHandler implements StoragePrivilegeHandler {
    
    private static final String CREATE_USER_SQL = "CREATE USER %s WITH PASSWORD '%s'";
    
    private static final String GRANT_ALL_SQL = "ALTER USER %s WITH SUPERUSER";
    
    private static final String ROLES_SQL = "SELECT * FROM pg_roles WHERE rolname IN (%s)";
    
    private static final String TABLE_PRIVILEGE_SQL = "SELECT grantor, grantee, table_catalog, table_name, privilege_type, is_grantable FROM information_schema.table_privileges WHERE grantee IN (%s)";
    
    @Override
    public Collection<ShardingSphereUser> diff(final Collection<ShardingSphereUser> users, final DataSource dataSource) throws SQLException {
        Collection<Grantee> grantees = new LinkedList<>();
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            try (ResultSet resultSet = statement.executeQuery(getRolePrivilegesSQL(users))) {
                while (resultSet.next()) {
                    grantees.add(new Grantee(resultSet.getString("rolname"), ""));
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
        return String.format(CREATE_USER_SQL, user.getGrantee().getUsername(), user.getPassword());
    }
    
    @Override
    public void grantAll(final Collection<ShardingSphereUser> users, final DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            for (ShardingSphereUser each : users) {
                statement.execute(getGrantAllSQL(each));
            }
        }
    }
    
    private String getGrantAllSQL(final ShardingSphereUser user) {
        return String.format(GRANT_ALL_SQL, user.getGrantee().getUsername());
    }
    
    @Override
    public Map<ShardingSphereUser, NativePrivileges> load(final Collection<ShardingSphereUser> users, final DataSource dataSource) throws SQLException {
        Map<ShardingSphereUser, NativePrivileges> result = new LinkedHashMap<>();
        users.forEach(user -> result.put(user, new NativePrivileges()));
        fillTablePrivileges(result, dataSource, users);
        fillRolePrivileges(result, dataSource, users);
        return result;
    }
    
    private void fillTablePrivileges(final Map<ShardingSphereUser, NativePrivileges> userPrivilegeMap,
                                     final DataSource dataSource, final Collection<ShardingSphereUser> users) throws SQLException {
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
    
    private void collectPrivileges(final Map<ShardingSphereUser, Map<String, Map<String, List<PrivilegeType>>>> privilegeCache, final ResultSet resultSet) throws SQLException {
        String db = resultSet.getString("table_catalog");
        String tableName = resultSet.getString("table_name");
        String privilegeType = resultSet.getString("privilege_type");
        boolean hasPrivilege = "TRUE".equalsIgnoreCase(resultSet.getString("is_grantable"));
        String grantee = resultSet.getString("grantee");
        if (hasPrivilege) {
            privilegeCache
                    .computeIfAbsent(new ShardingSphereUser(grantee, "", ""), k -> new HashMap<>())
                    .computeIfAbsent(db, k -> new HashMap<>())
                    .computeIfAbsent(tableName, k -> new ArrayList<>())
                    .add(getPrivilegeType(privilegeType));
        }
    }
    
    private void fillRolePrivileges(final Map<ShardingSphereUser, NativePrivileges> userPrivilegeMap, 
                                    final DataSource dataSource, final Collection<ShardingSphereUser> users) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            try (ResultSet resultSet = statement.executeQuery(getRolePrivilegesSQL(users))) {
                while (resultSet.next()) {
                    fillRolePrivileges(userPrivilegeMap, resultSet);
                }
            }
        }
    }
    
    private void fillRolePrivileges(final Map<ShardingSphereUser, NativePrivileges> userPrivilegeMap, final ResultSet resultSet) throws SQLException {
        Optional<ShardingSphereUser> user = findShardingSphereUser(userPrivilegeMap, resultSet);
        if (user.isPresent()) {
            userPrivilegeMap.get(user.get()).getAdministrativePrivileges().getPrivileges().addAll(loadRolePrivileges(resultSet));
        }
    }
    
    private Optional<ShardingSphereUser> findShardingSphereUser(final Map<ShardingSphereUser, NativePrivileges> userPrivilegeMap, final ResultSet resultSet) throws SQLException {
        Grantee grantee = new Grantee(resultSet.getString("rolname"), "");
        return userPrivilegeMap.keySet().stream().filter(each -> each.getGrantee().equals(grantee)).findFirst();
    }
    
    private Collection<PrivilegeType> loadRolePrivileges(final ResultSet resultSet) throws SQLException {
        Collection<PrivilegeType> result = new LinkedList<>();
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("rolsuper"), PrivilegeType.SUPER, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("rolcreaterole"), PrivilegeType.CREATE_ROLE, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("rolcreatedb"), PrivilegeType.CREATE_DATABASE, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("rolreplication"), PrivilegeType.REPL_CLIENT, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("rolinherit"), PrivilegeType.INHERIT, result);
        addToPrivilegeTypesIfPresent(resultSet.getBoolean("rolcanlogin"), PrivilegeType.CAN_LOGIN, result);
        return result;
    }
    
    private String getTablePrivilegesSQL(final Collection<ShardingSphereUser> users) {
        String userList = users.stream().map(each -> String.format("'%s'", each.getGrantee().getUsername())).collect(Collectors.joining(", "));
        return String.format(TABLE_PRIVILEGE_SQL, userList);
    }
    
    private String getRolePrivilegesSQL(final Collection<ShardingSphereUser> users) {
        String userList = users.stream().map(each -> String.format("'%s'", each.getGrantee().getUsername())).collect(Collectors.joining(", "));
        return String.format(ROLES_SQL, userList);
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
            case "TRUNCATE":
                return PrivilegeType.TRUNCATE;
            case "REFERENCES":
                return PrivilegeType.REFERENCES;
            case "TRIGGER":
                return PrivilegeType.TRIGGER;
            case "CREATE":
                return PrivilegeType.CREATE;
            case "EXECUTE":
                return PrivilegeType.EXECUTE;
            case "USAGE":
                return PrivilegeType.USAGE;
            case "CONNECT":
                return PrivilegeType.CONNECT;
            case "TEMPORARY":
                return PrivilegeType.TEMPORARY;
            default:
                throw new UnsupportedOperationException(privilege);
        }
    }
    
    private void addToPrivilegeTypesIfPresent(final boolean hasPrivilege, final PrivilegeType privilegeType, final Collection<PrivilegeType> target) {
        if (hasPrivilege) {
            target.add(privilegeType);
        }
    }
    
    @Override
    public String getType() {
        return "PostgreSQL";
    }
}
